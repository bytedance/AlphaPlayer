//
//  BDAlphaPlayerResourceModel.m
//  BDAlphaPlayer
//
//  Created by ByteDance on 2018/8/13.
//

#import "BDAlphaPlayerResourceModel.h"

#import "BDAlphaPlayerUtility.h"

@interface BDAlphaPlayerResourceModel ()

@property (nonatomic, readwrite, assign) BDAlphaPlayerOrientation currentOrientation;

@end

@implementation BDAlphaPlayerResourceModel

+ (instancetype)resourceModelFromDirectory:(NSString *)directory orientation:(BDAlphaPlayerOrientation)orientation error:(NSError **)error
{
    BDAlphaPlayerResourceModel *resourceModel = nil;
    // json to dic
    NSString *fileName = [NSString stringWithFormat:@"%@/config.json", directory];
    if ([[NSFileManager defaultManager] fileExistsAtPath:fileName]) {
        NSData *data = [NSData dataWithContentsOfFile:fileName];
        if (data.length) {
            NSError *jsonError = nil;
            NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:0 error:&jsonError];
            if (!jsonError) {
                resourceModel = [BDAlphaPlayerUtility createModelFromDictionary:dict error:error];
            } else {
                *error = jsonError;
            }
        } else {
            *error = [NSError errorWithDomain:BDAlphaPlayerErrorDomain code:BDAlphaPlayerErrorCodeFile userInfo:@{NSLocalizedDescriptionKey:[NSString stringWithFormat:@"config.json data nil %@", directory]}];
        }
    } else {
        *error = [NSError errorWithDomain:BDAlphaPlayerErrorDomain code:BDAlphaPlayerErrorCodeFile userInfo:@{NSLocalizedDescriptionKey:[NSString stringWithFormat:@"config.json does not exist at %@", directory]}];
    }
    if (resourceModel) {
        resourceModel.directory = directory;
        resourceModel.currentOrientation = orientation;
        [resourceModel pr_replenish];
        if (BDAlphaPlayerOrientationPortrait == resourceModel.currentOrientation) {
            resourceModel.currentOrientationResourceInfo = resourceModel.portraitResourceInfo;
        } else {
            resourceModel.currentOrientationResourceInfo = resourceModel.landscapeResourceInfo;
        }
        BOOL isAvailable = [resourceModel.currentOrientationResourceInfo resourceAvailable];
        if (!isAvailable) {
            *error = [NSError errorWithDomain:BDAlphaPlayerErrorDomain code:BDAlphaPlayerErrorConfigAvailable userInfo:@{NSLocalizedDescriptionKey:[NSString stringWithFormat:@"config.json data not available %@", directory]}];
            resourceModel = nil;
        }
    }
    return resourceModel;
}

- (void)pr_replenish
{
    if (self.portraitResourceInfo.resourceName.length) {
        self.portraitResourceInfo.resourceFilePath = [self.directory stringByAppendingPathComponent:self.portraitResourceInfo.resourceName];
        self.portraitResourceInfo.resourceFileURL = self.portraitResourceInfo.resourceFilePath ? [NSURL fileURLWithPath:self.portraitResourceInfo.resourceFilePath] : nil;
    }
    
    if (self.landscapeResourceInfo.resourceName.length) {
        self.landscapeResourceInfo.resourceFilePath = [self.directory stringByAppendingPathComponent:self.landscapeResourceInfo.resourceName];
        self.landscapeResourceInfo.resourceFileURL = self.landscapeResourceInfo.resourceFilePath ? [NSURL fileURLWithPath:self.landscapeResourceInfo.resourceFilePath] : nil;
    }
    
}

@end
