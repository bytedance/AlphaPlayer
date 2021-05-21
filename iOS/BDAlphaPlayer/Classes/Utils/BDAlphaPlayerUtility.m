//
//  BDAlphaPlayerUtility.m
//  BDAlphaPlayer
//
//  Created by ByteDance on 2018/8/13.
//

#import "BDAlphaPlayerUtility.h"

#import "BDAlphaPlayerResourceModel.h"

NSString *const BDAlphaPlayerErrorDomain = @"BDAlphaPlayerErrorDomain";

@implementation BDAlphaPlayerUtility

+ (BDAlphaPlayerResourceModel *)createModelFromDictionary:(NSDictionary *)dictionary error:(NSError **)error
{
    BDAlphaPlayerResourceModel *model = [[BDAlphaPlayerResourceModel alloc] init];
    
    id portrait = dictionary[@"portrait"];
    if (![portrait isKindOfClass:[NSDictionary class]]) {
        *error = [self createErrorForDictionary:dictionary];
    } else {
        model.portraitResourceInfo = [self createResourceInfoFromDictionary:portrait error:error];
    }
    
    id landscape = dictionary[@"landscape"];
    if (![landscape isKindOfClass:[NSDictionary class]]) {
        *error = [self createErrorForDictionary:dictionary];
    } else {
        model.landscapeResourceInfo = [self createResourceInfoFromDictionary:landscape error:error];
    }
    
    return model;
}

+ (BDAlphaPlayerResourceInfo *)createResourceInfoFromDictionary:(NSDictionary *)dictionary error:(NSError **)error
{
    BDAlphaPlayerResourceInfo *info = [[BDAlphaPlayerResourceInfo alloc] init];
    
    id align = dictionary[@"align"];
    if ([align isKindOfClass:[NSString class]]) {
        info.contentMode = [align description].integerValue;
    } else if ([align isKindOfClass:[NSNumber class]]) {
        info.contentMode = [(NSNumber *)align integerValue];
    } else {
        *error = [self createErrorForDictionary:dictionary];
    }
    
    if (info.contentMode >= BDAlphaPlayerContentModeMax) {
        *error = [self createErrorForDictionary:dictionary];
    }
    
    id path = dictionary[@"path"];
    if ([path isKindOfClass:[NSString class]]) {
        info.resourceName = [path description];
    } else {
        *error = [self createErrorForDictionary:dictionary];
    }
    
    return info;
}

+ (NSError *)createErrorForDictionary:(NSDictionary *)dictionary
{
    return [NSError errorWithDomain:BDAlphaPlayerErrorDomain code:BDAlphaPlayerErrorConfigResolve userInfo:@{NSLocalizedDescriptionKey:[NSString stringWithFormat:@"config.json serialization fail at %@", dictionary]}];
}

+ (CGRect)frameFromVideoSize:(CGSize)size renderSuperViewFrame:(CGRect)renderSuperViewFrame  resourceModel:(BDAlphaPlayerResourceModel *)resourceModel
{
    CGFloat layerRatio = renderSuperViewFrame.size.width / renderSuperViewFrame.size.height;
    CGFloat imgRatio = size.width * 0.5 / size.height;
    CGRect rationRect = [BDAlphaPlayerUtility scaleLayerRatio:layerRatio imgRatio:imgRatio mode:resourceModel.currentOrientationResourceInfo.contentMode];
    CGRect renderRect  = [BDAlphaPlayerUtility transFrameFromRationRect:rationRect superViewFrame:renderSuperViewFrame];
    return renderRect;
}

+ (CGRect)scaleLayerRatio:(float)layerRatio imgRatio:(float)imgRatio mode:(BDAlphaPlayerContentMode)renderContentMode
{
    CGRect preRect = CGRectMake(-1, -1, 2, 2);
    CGRect testRect = preRect;
    CGAffineTransform scaleTransform = CGAffineTransformIdentity;
    CGAffineTransform translateTransform = CGAffineTransformIdentity;
    switch (renderContentMode) {
        case BDAlphaPlayerContentModeScaleAspectFit:
        {
            scaleTransform = CGAffineTransformScale(CGAffineTransformIdentity, layerRatio > imgRatio ? imgRatio / layerRatio : 1, layerRatio < imgRatio ? layerRatio / imgRatio : 1);
            testRect = CGRectApplyAffineTransform(preRect, scaleTransform);
            break;
        }
        case BDAlphaPlayerContentModeScaleAspectFill:
        {
            scaleTransform = CGAffineTransformMakeScale(layerRatio < imgRatio ? imgRatio / layerRatio : 1, layerRatio > imgRatio ? layerRatio / imgRatio : 1);
            testRect = CGRectApplyAffineTransform(preRect, scaleTransform);
            break;
        }
        case BDAlphaPlayerContentModeFillTop: {
            scaleTransform = CGAffineTransformMakeScale(layerRatio < imgRatio ? imgRatio / layerRatio : 1, layerRatio > imgRatio ? layerRatio / imgRatio : 1);
            testRect = CGRectApplyAffineTransform(preRect, scaleTransform);
            translateTransform = CGAffineTransformTranslate(CGAffineTransformIdentity, 0, CGRectGetHeight(testRect) / 2 - 1);
            break;
        }
        case BDAlphaPlayerContentModeFillBottom: {
            scaleTransform = CGAffineTransformMakeScale(layerRatio < imgRatio ? imgRatio / layerRatio : 1, layerRatio > imgRatio ? layerRatio / imgRatio : 1);
            testRect = CGRectApplyAffineTransform(preRect, scaleTransform);
            translateTransform = CGAffineTransformTranslate(CGAffineTransformIdentity, 0, -(CGRectGetHeight(testRect) / 2 - 1));
            break;
        }
        case BDAlphaPlayerContentModeFillLeft: {
            scaleTransform = CGAffineTransformMakeScale(layerRatio < imgRatio ? imgRatio / layerRatio : 1, layerRatio > imgRatio ? layerRatio / imgRatio : 1);
            testRect = CGRectApplyAffineTransform(preRect, scaleTransform);
            translateTransform = CGAffineTransformTranslate(CGAffineTransformIdentity, CGRectGetWidth(testRect) / 2 - 1, 0);
            break;
        }
        case BDAlphaPlayerContentModeFillRight:
        {
            scaleTransform = CGAffineTransformMakeScale(layerRatio < imgRatio ? imgRatio / layerRatio : 1, layerRatio > imgRatio ? layerRatio / imgRatio : 1);
            testRect = CGRectApplyAffineTransform(preRect, scaleTransform);
            translateTransform = CGAffineTransformTranslate(CGAffineTransformIdentity, -(CGRectGetWidth(testRect) / 2 - 1), 0);
            break;
        }
        case BDAlphaPlayerContentModeFitTop:
        {
            scaleTransform = CGAffineTransformScale(CGAffineTransformIdentity, 1, layerRatio / imgRatio);
            testRect = CGRectApplyAffineTransform(preRect, scaleTransform);
            translateTransform = CGAffineTransformTranslate(CGAffineTransformIdentity, 0, CGRectGetHeight(testRect) / 2 - 1);
            break;
        }
        case BDAlphaPlayerContentModeFitBottom:
        {
            scaleTransform = CGAffineTransformScale(CGAffineTransformIdentity, 1, layerRatio / imgRatio);
            testRect = CGRectApplyAffineTransform(preRect, scaleTransform);
            translateTransform = CGAffineTransformTranslate(CGAffineTransformIdentity, 0, -(CGRectGetHeight(testRect) / 2 - 1));
            break;
        }
        case BDAlphaPlayerContentModeFitLeft:
        {
            scaleTransform = CGAffineTransformScale(CGAffineTransformIdentity, imgRatio / layerRatio, 1);
            testRect = CGRectApplyAffineTransform(preRect, scaleTransform);
            translateTransform = CGAffineTransformTranslate(CGAffineTransformIdentity, -(1 - CGRectGetWidth(testRect) / 2), 0);
            break;
        }
        case BDAlphaPlayerContentModeFitRight:
        {
            scaleTransform = CGAffineTransformScale(CGAffineTransformIdentity, imgRatio / layerRatio, 1);
            testRect = CGRectApplyAffineTransform(preRect, scaleTransform);
            translateTransform = CGAffineTransformTranslate(CGAffineTransformIdentity, 1 - CGRectGetWidth(testRect) / 2, 0);
            break;
        }
        default:
            break;
    }
    testRect = CGRectApplyAffineTransform(testRect, translateTransform);
    CGRect resultRect = CGRectApplyAffineTransform(testRect, CGAffineTransformMakeScale(1, -1));
    return resultRect;
}

+ (CGRect)transFrameFromRationRect:(CGRect)openglNewRect superViewFrame:(CGRect)superViewFrame
{
    CGRect tt =  CGRectMake(0, 0, 0, 0);
    CGFloat width = superViewFrame.size.width;
    CGFloat height = superViewFrame.size.height;
    tt.origin.x = (openglNewRect.origin.x + 1) * width / 2;
    tt.origin.y = height/2 - ceil((openglNewRect.origin.y  + openglNewRect.size.height) * height / 2);
    tt.size.width = openglNewRect.size.width / 2 * width;
    tt.size.height = openglNewRect.size.height / 2 * height;
    return tt;
}

@end
