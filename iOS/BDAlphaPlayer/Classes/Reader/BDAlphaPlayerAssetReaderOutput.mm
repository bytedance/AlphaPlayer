//
//  BDAlphaPlayerAssetReaderOutput.mm
//  BDAlphaPlayer
//
//  Created by ByteDance on 2020/4/23.
//

#import "BDAlphaPlayerAssetReaderOutput.h"

#include <queue>

#define KBufferCapacity 3

NSString * const BDAlphaPlayerAssetReaderOutputErrorDomain = @"BDAlphaPlayerAssetReaderOutputErrorDomain";

@interface BDAlphaPlayerAssetReaderOutput () {
    std::queue<CMSampleBufferRef> _sampleBufferQueue;
}

@property (nonatomic, assign) NSUInteger preferredFramesPerSecond;
@property (nonatomic, strong) AVAssetReaderTrackOutput *output;
@property (nonatomic, strong) AVAssetReader *reader;

@property (nonatomic, strong) dispatch_queue_t readingQueue;

@property (nonatomic, strong) NSObject *bufferQueueToken;

@end

@implementation BDAlphaPlayerAssetReaderOutput

- (void)dealloc
{
    [self drainSampleBufferQueue];
    if (AVAssetReaderStatusReading == self.reader.status) {
        [self.reader cancelReading];
    }
}

- (instancetype)initWithURL:(NSURL *)url error:(NSError * _Nullable __autoreleasing * _Nullable)outError
{
    if (self = [super init]) {
        [self setupWithURL:url error:outError];
    }
    return self;
}

- (void)drainSampleBufferQueue
{
    @synchronized (self.bufferQueueToken) {
        if (_sampleBufferQueue.size() > 0) {
            NSInteger size = _sampleBufferQueue.size();
            for (NSInteger index = 0; index < size; index++) {
                CMSampleBufferRef ret = _sampleBufferQueue.front();
                if (ret) {
                    CFRelease(ret);
                }
                _sampleBufferQueue.pop();
            }
        }
    }
}

- (void)fillBufferQueueIfNeed
{
    dispatch_async(self.readingQueue, ^{
        do {
            @synchronized (self.bufferQueueToken) {
                if (self->_sampleBufferQueue.size() >= KBufferCapacity) {
                    break;
                }
            }
            CMSampleBufferRef const next = [self.output copyNextSampleBuffer];
            if (next) {
                @synchronized (self.bufferQueueToken) {
                    self->_sampleBufferQueue.push(next);
                    if (self->_sampleBufferQueue.size() >= KBufferCapacity) {
                        break;
                    }
                }
            } else {
                break;
            }
        } while (true);
    });
}

#pragma mark -

- (void)setupWithURL:(NSURL *)url error:(NSError * _Nullable __autoreleasing * _Nullable)outError
{
    AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:url options:nil];
    if (!asset) {
        BOOL exist = [[NSFileManager defaultManager] fileExistsAtPath:url.path];
        if (!exist) {
            *outError = [NSError errorWithDomain:BDAlphaPlayerAssetReaderOutputErrorDomain code:BDAlphaPlayerAssetReaderOutputErrorFileNotExists userInfo:nil];
            return;
        }
        NSDictionary *fileAttr = [[NSFileManager defaultManager] attributesOfItemAtPath:url.path error:nil];
        NSString *errorDesscription = [NSString stringWithFormat:@"can not read file:%@, exist:%d, fileAttr:%@", url, exist, fileAttr ? [fileAttr description] : @"non attr"];
        *outError = [NSError errorWithDomain:BDAlphaPlayerAssetReaderOutputErrorDomain code:BDAlphaPlayerAssetReaderOutputErrorCannotReadFile userInfo:@{NSLocalizedDescriptionKey:errorDesscription}];
        return;
    }
    
    AVAssetReader *reader = [[AVAssetReader alloc] initWithAsset:asset error:outError];
    if (!reader) {
        return;
    }
    
    // 提取音轨
    NSArray *audioTracks = [asset tracksWithMediaType:AVMediaTypeAudio];
    AVAssetTrack *audioTrack = [audioTracks firstObject];
    if (audioTrack) {
        // 存在音轨，转成AVPlayerItem
        _audioItem = [AVPlayerItem playerItemWithAsset:audioTrack.asset];
    }
    
    NSArray *videoTracks = [asset tracksWithMediaType:AVMediaTypeVideo];
    AVAssetTrack *videoTrack = [videoTracks firstObject];
    if (!videoTrack) {
        *outError = [NSError errorWithDomain:BDAlphaPlayerAssetReaderOutputErrorDomain code:BDAlphaPlayerAssetReaderOutputErrorVideoTrackNotExists userInfo:nil];
        return;
    }
    
    _videoSize = videoTrack.naturalSize;
    if (_videoSize.width <= 0 || _videoSize.height <= 0) {
        *outError = [NSError errorWithDomain:BDAlphaPlayerAssetReaderOutputErrorDomain code:BDAlphaPlayerAssetReaderOutputErrorVideoTrackFrameException userInfo:nil];
        return;
    }

    NSDictionary *setting = @{
        (id)kCVPixelBufferPixelFormatTypeKey:@(kCVPixelFormatType_420YpCbCr8BiPlanarFullRange),
        (id)kCVPixelBufferMetalCompatibilityKey: @(YES),
    };
    AVAssetReaderTrackOutput *output = [[AVAssetReaderTrackOutput alloc] initWithTrack:videoTrack outputSettings:setting];
    output.alwaysCopiesSampleData = NO;
    [reader addOutput:output];
    [reader startReading];
    
    NSTimeInterval duration = CMTimeGetSeconds(videoTrack.minFrameDuration);
    CMTime videoTotalTime = [asset duration];
    if (videoTotalTime.timescale) {
        _videoDuration = videoTotalTime.value / (videoTotalTime.timescale * 1.0f);
    } else {
        _videoDuration = 0.0f;
    }
    _frameDuration = duration;
    _preferredFramesPerSecond = 1 / duration;
    _output = output;
    _reader = reader;
    dispatch_queue_attr_t attr = dispatch_queue_attr_make_with_qos_class(DISPATCH_QUEUE_SERIAL, QOS_CLASS_DEFAULT, 0);
    _readingQueue = dispatch_queue_create("com.bytedance.asset.reader", attr);
    _bufferQueueToken = [[NSObject alloc] init];
    
    [self fillBufferQueueIfNeed];
}

- (BOOL)hasNextSampleBuffer
{
    BOOL ret = NO;
    @synchronized (self.bufferQueueToken) {
        ret = _sampleBufferQueue.size() > 0 || AVAssetReaderStatusReading == self.reader.status;
    }
    return ret;
}

- (CMSampleBufferRef)copyNextSampleBuffer
{
    CMSampleBufferRef ret = NULL;
    @synchronized (self.bufferQueueToken) {
        if (!_sampleBufferQueue.empty()) {
            ret = _sampleBufferQueue.front();
            _sampleBufferQueue.pop();
        }
    }
    [self fillBufferQueueIfNeed];
    return ret;
}

@end
