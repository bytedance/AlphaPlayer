//
//  shaders.metal
//  BDAlphaPlayer
//
//  Created by ByteDance on 2018/6/21.
//  Copyright © 2018年 ByteDance. All rights reserved.
//

#include <metal_stdlib>
#include "BDAlphaPlayerMetalShaderType.h"

using namespace metal;

typedef struct {
    float4 clipSpacePosition [[position]];
    float2 textureCoordinate;
} RasterizerData;

vertex RasterizerData vertexShader(uint vertexID [[ vertex_id ]],
             constant BDAlphaPlayerVertex *vertexArray [[ buffer(BDAlphaPlayerVertexInputIndexVertices) ]]) {
    RasterizerData out;
    out.clipSpacePosition = vertexArray[vertexID].position;
    out.textureCoordinate = vertexArray[vertexID].textureCoordinate;
    return out;
}

fragment float4 samplingShader(RasterizerData input [[stage_in]],
               texture2d<float> textureY [[ texture(BDAlphaPlayerFragmentTextureIndexTextureY) ]],
               texture2d<float> textureUV [[ texture(BDAlphaPlayerFragmentTextureIndexTextureUV) ]],
               constant BDAlphaPlayerConvertMatrix *convertMatrix [[ buffer(BDAlphaPlayerFragmentInputIndexMatrix) ]])
{
    constexpr sampler textureSampler (mag_filter::linear, min_filter::linear);
    
    float tcx = input.textureCoordinate.x / 2 + 0.5;
    
    float3 yuv = float3(textureY.sample(textureSampler, float2(tcx, input.textureCoordinate.y)).r,
                          textureUV.sample(textureSampler, float2(tcx, input.textureCoordinate.y)).rg);
    
    float3 rgb = convertMatrix->matrix * (yuv + convertMatrix->offset);
    
    float3 alpha = float3(textureY.sample(textureSampler, float2(input.textureCoordinate.x / 2, input.textureCoordinate.y)).r,
    textureUV.sample(textureSampler, float2(input.textureCoordinate.x / 2, input.textureCoordinate.y)).rg);
        
    float3 alphargb = convertMatrix->matrix * (alpha + convertMatrix->offset);
    return float4(rgb, alphargb.r);
}


