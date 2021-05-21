//
//  BDAlphaPlayerMetalView.h
//  BDAlphaPlayer
//
//  Created by ByteDance on 2020/7/5.
//

#import "BDAlphaPlayerMetalConfiguration.h"
#import "BDAlphaPlayerResourceModel.h"

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

typedef NS_ENUM(NSUInteger, BDAlphaPlayerPlayState) {
    BDAlphaPlayerPlayStateStop = 0,
    BDAlphaPlayerPlayStatePlay,
};

NS_ASSUME_NONNULL_BEGIN
@class BDAlphaPlayerAssetReaderOutput;
@protocol BDAlphaPlayerMetalViewDelegate;

@interface BDAlphaPlayerMetalView : UIView

- (instancetype)initWithDelegate:(nullable id<BDAlphaPlayerMetalViewDelegate>)delegate NS_DESIGNATED_INITIALIZER;

/**
 @brief Resource model for MP4.
*/
@property (nonatomic, strong, readonly) BDAlphaPlayerResourceModel *model;

/**
 @brief Current state for player.
*/
@property (nonatomic, assign, readonly) BDAlphaPlayerPlayState state;

/**
 @brief Core method for player.Only this method can start to play MP4.
 
 @prama configuration Params player needs.
*/
- (void)playWithMetalConfiguration:(BDAlphaPlayerMetalConfiguration *)configuration;

/**
 @brief Get total duration of currently displaying MP4.Duration is only available after [BDAlphaPlayerMetalView playWithMetalConfiguration:] method called.

 @seealso [BDAlphaPlayerMetalView playWithMetalConfiguration:]
 @return Total Duration of MP4.
*/
- (NSTimeInterval)totalDurationOfPlayingEffect;

/**
 @brief Stop displaying without calling didFinishPlayingWithError method.

 @seealso [BDAlphaPlayerMetalView stopWithFinishPlayingCallback:]
*/
- (void)stop;

/**
 @brief Stop displaying with calling didFinishPlayingWithError method.

 @seealso [BDAlphaPlayerMetalView stop:]
*/
- (void)stopWithFinishPlayingCallback;

@end


@protocol BDAlphaPlayerMetalViewDelegate <NSObject>

- (void)metalView:(BDAlphaPlayerMetalView *)metalView didFinishPlayingWithError:(NSError *)error;

@optional

/**
 @brief The method will be called for every frame during displaying duration.
 @prama duration The duration from start to current frame.
*/
- (void)frameCallBack:(NSTimeInterval)duration;

@end

NS_ASSUME_NONNULL_END
