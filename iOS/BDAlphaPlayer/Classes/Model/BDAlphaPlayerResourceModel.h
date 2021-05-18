//
//  BDAlphaPlayerResourceModel.h
//  BDAlphaPlayer
//
//  Created by ByteDance on 2018/8/13.
//

#import "BDAlphaPlayerResourceInfo.h"
#import "BDAlphaPlayerDefine.h"
#import "BDAlphaPlayerUtility.h"

#import <Foundation/Foundation.h>

@interface BDAlphaPlayerResourceModel : NSObject

#pragma mark AutoSet

@property (nonatomic, strong) BDAlphaPlayerResourceInfo *portraitResourceInfo;

@property (nonatomic, strong) BDAlphaPlayerResourceInfo *landscapeResourceInfo;

#pragma mark ManualSet

/** String of resource directory that contains json and MP4. */
@property (nonatomic, copy) NSString *directory;

/** Orientation of currently displaying MP4. */
@property (nonatomic, readonly, assign) BDAlphaPlayerOrientation currentOrientation;

/** The resource model for current orientation. */
@property (nonatomic, strong) BDAlphaPlayerResourceInfo *currentOrientationResourceInfo;

/**
 @brief Initialize resource data from local path.
 @param directory Directory of resource file.
 @param orientation Orientation of MP4.
 @return resourceModel Resource data.
*/
+ (instancetype)resourceModelFromDirectory:(NSString *)directory orientation:(BDAlphaPlayerOrientation)orientation error:(NSError **)error;

@end
