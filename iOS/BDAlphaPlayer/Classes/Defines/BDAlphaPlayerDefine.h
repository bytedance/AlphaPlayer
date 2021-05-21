//
//  BDAlphaPlayerDefine.h
//  BDAlphaPlayer
//
//  Created by ByteDance on 2020/7/8.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, BDAlphaPlayerOrientation) {
    BDAlphaPlayerOrientationPortrait = 0,
    BDAlphaPlayerOrientationLandscape = 1,
};

typedef NS_ENUM(NSInteger, BDAlphaPlayerErrorCode) {
    BDAlphaPlayerErrorCodeFile = -1,
    BDAlphaPlayerErrorCodePlay = -2,
    BDAlphaPlayerErrorCodeDisplay = -3,
    BDAlphaPlayerErrorConfigResolve = -4,
    BDAlphaPlayerErrorConfigAvailable = -5,
};

typedef NS_ENUM(NSUInteger, BDAlphaPlayerContentMode) {
    BDAlphaPlayerContentModeScaleToFill = 0,
    BDAlphaPlayerContentModeScaleAspectFit = 1,
    BDAlphaPlayerContentModeScaleAspectFill = 2,
    BDAlphaPlayerContentModeFillTop = 3,
    BDAlphaPlayerContentModeFillBottom = 4,
    BDAlphaPlayerContentModeFillLeft = 5,
    BDAlphaPlayerContentModeFillRight = 6,
    BDAlphaPlayerContentModeFitTop = 7,
    BDAlphaPlayerContentModeFitBottom = 8,
    BDAlphaPlayerContentModeFitLeft = 9,
    BDAlphaPlayerContentModeFitRight = 10,
    BDAlphaPlayerContentModeMax,//This content mode is invalid.Only for the range.
};

typedef void(^BDAlphaPlayerRenderOutputCompletion)(void);

typedef void (^BDAlphaPlayerFramePlayDurationCallBack)(NSTimeInterval duration);
