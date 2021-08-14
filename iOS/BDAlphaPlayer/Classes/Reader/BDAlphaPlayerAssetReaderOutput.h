//
//  BDAlphaPlayerAssetReaderOutput.h
//  BDAlphaPlayer
//
//  Created by ByteDance on 2020/4/23.
//

#import <AVFoundation/AVFoundation.h>
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

FOUNDATION_EXPORT NSString * const BDAlphaPlayerAssetReaderOutputErrorDomain;

typedef NS_ENUM(NSUInteger, BDAlphaPlayerAssetReaderOutputErrorCode) {
    BDAlphaPlayerAssetReaderOutputErrorFileNotExists,
    BDAlphaPlayerAssetReaderOutputErrorCannotReadFile,
    BDAlphaPlayerAssetReaderOutputErrorVideoTrackNotExists,
    BDAlphaPlayerAssetReaderOutputErrorVideoTrackFrameException
};

@interface BDAlphaPlayerAssetReaderOutput : NSObject

- (instancetype _Nullable)initWithURL:(NSURL *)url error:(NSError * _Nullable * _Nullable)outError;

@property (nonatomic, assign, readonly) NSUInteger preferredFramesPerSecond;

@property (nonatomic, assign) NSTimeInterval frameDuration;

@property (nonatomic, assign) NSTimeInterval videoDuration;

@property (nonatomic, assign, readonly) CGSize videoSize;

@property (nonatomic, strong, readonly) AVPlayerItem *audioItem;

- (BOOL)hasNextSampleBuffer;

- (CMSampleBufferRef const _Nullable)copyNextSampleBuffer;

- (void)drainSampleBufferQueue;

@end

NS_ASSUME_NONNULL_END
