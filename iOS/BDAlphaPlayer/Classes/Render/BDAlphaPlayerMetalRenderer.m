//
//  BDAlphaPlayerMetalRenderer.m
//  BDAlphaPlayer
//
//  Created by ByteDance on 2020/4/23.
//

#import "BDAlphaPlayerMetalRenderer.h"

#import "BDAlphaPlayerMetalShaderType.h"

#import <CoreVideo/CVMetalTexture.h>
#import <CoreVideo/CVMetalTextureCache.h>
#import <CoreVideo/CoreVideo.h>

@interface BDAlphaPlayerMetalRenderer ()

@property (nonatomic, weak) MTKView *mtkView;

@property (nonatomic, strong) BDAlphaPlayerAssetReaderOutput *output;

@property (nonatomic, strong) BDAlphaPlayerResourceModel *resourceModel;
@property (nonatomic, assign) CVMetalTextureCacheRef textureCache;

@property (nonatomic, assign) vector_double2 viewportSize;
@property (nonatomic, strong) id<MTLRenderPipelineState> pipelineState;
@property (nonatomic, strong) id<MTLCommandQueue> commandQueue;
@property (nonatomic, strong) id<MTLTexture> texture;
@property (nonatomic, strong) id<MTLBuffer> vertices;
@property (nonatomic, strong) id<MTLBuffer> convertMatrix;
@property (nonatomic, assign) NSUInteger numVertices;
@property (nonatomic, copy) BDAlphaPlayerMetalRendererCompletion completion;

@property (nonatomic, assign) NSTimeInterval framePlayDuration;

@end

@implementation BDAlphaPlayerMetalRenderer

- (void)dealloc
{
    CVMetalTextureCacheRef cache = self.textureCache;
    if (cache) {
        CFRelease(cache);
    }
}

- (instancetype)initWithMetalKitView:(MTKView *)mtkView
{
    if (self = [super init]) {
        _mtkView = mtkView;
        _framePlayDuration = 0.0f;
        [self setup];
    }
    return self;
}

- (void)renderOutput:(BDAlphaPlayerAssetReaderOutput *)output resourceModel:(BDAlphaPlayerResourceModel *)resourceModel completion:(BDAlphaPlayerMetalRendererCompletion)completion
{
    self.framePlayDuration = 0.0f;
    self.resourceModel = resourceModel;
    [self setupMatrix];
    self.output = output;
    self.completion = completion;
    self.mtkView.preferredFramesPerSecond = self.output.preferredFramesPerSecond;
    self.mtkView.paused = NO;
}

- (void)drainSampleBufferQueue
{
    [self.output drainSampleBufferQueue];
}

- (void)setup
{
    self.mtkView.paused = YES;
    self.mtkView.delegate = self;
    CVMetalTextureCacheCreate(NULL, NULL, self.mtkView.device, NULL, &_textureCache);
    [self setupVertex];
    [self setupPipeline];
}

- (void)setupMatrix
{
    // Coding Matrices https://www.mir.com/DMG/ycbcr.html
    matrix_float3x3 kColorConversion601FullRangeMatrix = (matrix_float3x3){
        (simd_float3){1.0, 1.0, 1.0},
        (simd_float3){0.0, -0.344136, 1.772},
        (simd_float3){1.402, -0.714136, 0.0},
    };
    
    vector_float3 kColorConversion601FullRangeOffset = (vector_float3){ 0, -0.5, -0.5};
    
    BDAlphaPlayerConvertMatrix matrix;
    matrix.matrix = kColorConversion601FullRangeMatrix;
    matrix.offset = kColorConversion601FullRangeOffset;
    
    self.convertMatrix = [self.mtkView.device newBufferWithBytes:&matrix
                                                          length:sizeof(BDAlphaPlayerConvertMatrix)
                                                         options:MTLResourceStorageModeShared];
}

- (void)setupVertex
{
    static const BDAlphaPlayerVertex quadVertices[] =
    {   // 顶点坐标，分别是x、y、z、w；    纹理坐标，x、y；
        { {  1.0, -1.0, 0.0, 1.0 },  { 1.f, 1.f } },
        { { -1.0, -1.0, 0.0, 1.0 },  { 0.f, 1.f } },
        { { -1.0,  1.0, 0.0, 1.0 },  { 0.f, 0.f } },
        
        { {  1.0, -1.0, 0.0, 1.0 },  { 1.f, 1.f } },
        { { -1.0,  1.0, 0.0, 1.0 },  { 0.f, 0.f } },
        { {  1.0,  1.0, 0.0, 1.0 },  { 1.f, 0.f } },
    };
    self.vertices = [self.mtkView.device newBufferWithBytes:quadVertices
                                                     length:sizeof(quadVertices)
                                                    options:MTLResourceStorageModeShared];
    self.numVertices = sizeof(quadVertices) / sizeof(BDAlphaPlayerVertex);
}

-(void)setupPipeline
{
    NSString *filePath = [[NSBundle bundleForClass:[self class]] pathForResource:@"BDAlphaPlayer.bundle/default" ofType:@"metallib"];
    NSError *error = nil;
    id<MTLLibrary> defaultLibrary = [self.mtkView.device newLibraryWithFile:filePath error:&error];
    id<MTLFunction> vertexFunction = [defaultLibrary newFunctionWithName:@"vertexShader"];
    id<MTLFunction> fragmentFunction = [defaultLibrary newFunctionWithName:@"samplingShader"];
    
    MTLRenderPipelineDescriptor *pipelineStateDescriptor = [[MTLRenderPipelineDescriptor alloc] init];
    pipelineStateDescriptor.vertexFunction = vertexFunction;
    pipelineStateDescriptor.fragmentFunction = fragmentFunction;
    pipelineStateDescriptor.colorAttachments[0].pixelFormat = self.mtkView.colorPixelFormat;
    pipelineStateDescriptor.colorAttachments[0].blendingEnabled = true;
    pipelineStateDescriptor.colorAttachments[0].rgbBlendOperation = MTLBlendOperationAdd;
    pipelineStateDescriptor.colorAttachments[0].sourceRGBBlendFactor = MTLBlendFactorOne;
    pipelineStateDescriptor.colorAttachments[0].destinationRGBBlendFactor = MTLBlendFactorOne;
    pipelineStateDescriptor.colorAttachments[0].alphaBlendOperation = MTLBlendOperationMin;
    pipelineStateDescriptor.colorAttachments[0].sourceAlphaBlendFactor = MTLBlendFactorOne;
    pipelineStateDescriptor.colorAttachments[0].destinationAlphaBlendFactor = MTLBlendFactorOne;
    self.pipelineState = [self.mtkView.device newRenderPipelineStateWithDescriptor:pipelineStateDescriptor error:NULL];
    self.commandQueue = [self.mtkView.device newCommandQueue];
}

#pragma mark - MTKViewDelegate

- (void)mtkView:(MTKView *)view drawableSizeWillChange:(CGSize)size
{
    self.viewportSize = (vector_double2){size.width, size.height};
}

- (void)drawInMTKView:(MTKView *)view
{
    CMSampleBufferRef const sampleBuffer = [self.output copyNextSampleBuffer];
    id<MTLCommandBuffer> commandBuffer = [self.commandQueue commandBuffer];
    MTLRenderPassDescriptor *renderPassDescriptor = view.currentRenderPassDescriptor;
    if (self.framePlayDuration == 0.0f) {
        if (self.framePlayDurationCallBack) {
            self.framePlayDurationCallBack(self.framePlayDuration);
        }
    }
    self.framePlayDuration += [self.output frameDuration];
    self.framePlayDuration = MIN(self.output.videoDuration, self.framePlayDuration);
    if (renderPassDescriptor && sampleBuffer) {
        id<MTLRenderCommandEncoder> renderEncoder = [commandBuffer renderCommandEncoderWithDescriptor:renderPassDescriptor];
        [renderEncoder setViewport:(MTLViewport){0.0, 0.0, self.viewportSize.x, self.viewportSize.y, -1.0, 1.0 }];
        [renderEncoder setRenderPipelineState:self.pipelineState];
        
        [renderEncoder setVertexBuffer:self.vertices
                                offset:0
                               atIndex:BDAlphaPlayerVertexInputIndexVertices];
        CVPixelBufferRef pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
        id<MTLTexture> textureY = nil;
        id<MTLTexture> textureUV = nil;
        // textureY
        {
            size_t width = CVPixelBufferGetWidthOfPlane(pixelBuffer, 0);
            size_t height = CVPixelBufferGetHeightOfPlane(pixelBuffer, 0);
            MTLPixelFormat pixelFormat = MTLPixelFormatR8Unorm;
            
            CVMetalTextureRef texture = NULL;
            CVReturn status = CVMetalTextureCacheCreateTextureFromImage(NULL, self.textureCache, pixelBuffer, NULL, pixelFormat, width, height, 0, &texture);
            if (status == kCVReturnSuccess) {
                textureY = CVMetalTextureGetTexture(texture);
                CFRelease(texture);
            }
        }
        // textureUV
        {
            size_t width = CVPixelBufferGetWidthOfPlane(pixelBuffer, 1);
            size_t height = CVPixelBufferGetHeightOfPlane(pixelBuffer, 1);
            MTLPixelFormat pixelFormat = MTLPixelFormatRG8Unorm;
            
            CVMetalTextureRef texture = NULL;
            CVReturn status = CVMetalTextureCacheCreateTextureFromImage(NULL, self.textureCache, pixelBuffer, NULL, pixelFormat, width, height, 1, &texture);
            if (status == kCVReturnSuccess) {
                textureUV = CVMetalTextureGetTexture(texture);
                CFRelease(texture);
            }
        }
        
        if (textureY != nil && textureUV != nil) {
            [renderEncoder setFragmentTexture:textureY
                                      atIndex:BDAlphaPlayerFragmentTextureIndexTextureY];
            [renderEncoder setFragmentTexture:textureUV
                                      atIndex:BDAlphaPlayerFragmentTextureIndexTextureUV];
        }
        [renderEncoder setFragmentBuffer:self.convertMatrix
                                  offset:0
                                 atIndex:BDAlphaPlayerFragmentInputIndexMatrix];
        
        [renderEncoder drawPrimitives:MTLPrimitiveTypeTriangle
                          vertexStart:0
                          vertexCount:self.numVertices];
        
        [renderEncoder endEncoding];
        
        [commandBuffer presentDrawable:view.currentDrawable];
        
        [commandBuffer commit];
        if (self.framePlayDurationCallBack) {
            self.framePlayDurationCallBack(self.framePlayDuration);
        }
    } else {
        if (![self.output hasNextSampleBuffer]) {
            view.paused = YES;
            
            if (self.completion) {
                self.completion();
                self.completion = nil;
            }
        }
    }
    if (sampleBuffer) {
        CFRelease(sampleBuffer);
    }
}

@end
