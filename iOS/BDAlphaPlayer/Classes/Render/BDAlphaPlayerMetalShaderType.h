//
//  BDAlphaPlayerMetalShaderType.h
//  BDAlphaPlayer
//
//  Created by ByteDance on 2020/4/12.
//

#ifndef BDAlphaPlayerMetalShaderType_h
#define BDAlphaPlayerMetalShaderType_h

#include <simd/simd.h>

typedef struct
{
    vector_float4 position;
    vector_float2 textureCoordinate;
} BDAlphaPlayerVertex;


typedef struct {
    matrix_float3x3 matrix;
    vector_float3 offset;
} BDAlphaPlayerConvertMatrix;

typedef enum BDAlphaPlayerVertexInputIndex
{
    BDAlphaPlayerVertexInputIndexVertices     = 0,
} BDAlphaPlayerVertexInputIndex;


typedef enum BDAlphaPlayerFragmentBufferIndex
{
    BDAlphaPlayerFragmentInputIndexMatrix     = 0,
} BDAlphaPlayerFragmentBufferIndex;


typedef enum BDAlphaPlayerFragmentTextureIndex
{
    BDAlphaPlayerFragmentTextureIndexTextureY     = 0,
    BDAlphaPlayerFragmentTextureIndexTextureUV     = 1,
} BDAlphaPlayerFragmentTextureIndex;


#endif /* BDAlphaPlayerMetalShaderType_h */
