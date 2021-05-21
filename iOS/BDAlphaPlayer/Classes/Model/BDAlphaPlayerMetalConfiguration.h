//
//  BDAlphaPlayerMetalConfiguration.h
//  BDAlphaPlayer
//
//  Created by ByteDance on 2020/11/8.
//

#import "BDAlphaPlayerDefine.h"

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BDAlphaPlayerMetalConfiguration : NSObject

/**
 @brief String of resource directory that contains json and MP4.
*/
@property (nonatomic, copy) NSString *directory;

/**
 @brief The orientation you wanna play for.
*/
@property (nonatomic, assign) BDAlphaPlayerOrientation orientation;

/**
 @brief Frame of super view.The final frame of rendering will be decided by contentMode and renderSuperViewFrame.
*/
@property (nonatomic, assign) CGRect renderSuperViewFrame;

+ (instancetype)defaultConfiguration;

@end

NS_ASSUME_NONNULL_END
