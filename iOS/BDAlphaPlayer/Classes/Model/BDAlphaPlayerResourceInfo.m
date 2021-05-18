//
//  BDAlphaPlayerResourceInfo.m
//  BDAlphaPlayer
//
//  Created by ByteDance on 2020/12/18.
//

#import "BDAlphaPlayerResourceInfo.h"

@implementation BDAlphaPlayerResourceInfo

- (BOOL)resourceAvailable
{
    if (![[NSFileManager defaultManager] fileExistsAtPath:self.resourceFilePath]) {
        return NO;
    }
    if (nil == self.resourceFileURL) {
        return NO;
    }
    return YES;
}

@end
