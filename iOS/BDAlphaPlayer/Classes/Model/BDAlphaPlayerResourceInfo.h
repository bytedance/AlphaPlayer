//
//  BDAlphaPlayerResourceInfo.h
//  BDAlphaPlayer
//
//  Created by ByteDance on 2020/12/18.
//

#import "BDAlphaPlayerDefine.h"

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BDAlphaPlayerResourceInfo : NSObject

#pragma mark AutoSet

@property (nonatomic, assign) BDAlphaPlayerContentMode contentMode;

/** Name for MP4 file. */
@property (nonatomic, copy) NSString *resourceName;

/** Directory for MP4 file. */
@property (nonatomic, copy) NSString *resourceFilePath;

/** URL for MP4 file. */
@property (nonatomic, strong) NSURL *resourceFileURL;

- (BOOL)resourceAvailable;

@end

NS_ASSUME_NONNULL_END
