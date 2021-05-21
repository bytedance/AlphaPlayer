//
//  BDAlphaPlayerMetalRenderer.h
//  BDAlphaPlayer
//
//  Created by ByteDance on 2020/4/23.
//

#import "BDAlphaPlayerAssetReaderOutput.h"
#import "BDAlphaPlayerDefine.h"
#import "BDAlphaPlayerResourceModel.h"

#import <Foundation/Foundation.h>
#import <MetalKit/MetalKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^BDAlphaPlayerMetalRendererCompletion)(void);

@interface BDAlphaPlayerMetalRenderer : NSObject <MTKViewDelegate>

- (instancetype)initWithMetalKitView:(MTKView *)mtkView;

@property (nonatomic, copy) BDAlphaPlayerFramePlayDurationCallBack framePlayDurationCallBack;

- (void)renderOutput:(BDAlphaPlayerAssetReaderOutput *)output resourceModel:(BDAlphaPlayerResourceModel *)resourceModel completion:(BDAlphaPlayerMetalRendererCompletion)completion;

- (void)drainSampleBufferQueue;

@end

NS_ASSUME_NONNULL_END
