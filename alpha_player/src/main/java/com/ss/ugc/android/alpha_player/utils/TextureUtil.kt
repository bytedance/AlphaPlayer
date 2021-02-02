package com.ss.ugc.android.alpha_player.utils

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils

fun loadTexture(bitmap: Bitmap?): Int {
    if (bitmap == null || bitmap.isRecycled) {
        return 0
    }
    val textureIds = IntArray(1)

    // create texture object.
    GLES20.glGenTextures(1, textureIds, 0)
    if (textureIds[0] == 0) {
        return 0
    }
    // bind texture to OpenGL.
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])

    // set default texture filter params.
    GLES20.glTexParameteri(
        GLES20.GL_TEXTURE_2D,
        GLES20.GL_TEXTURE_MIN_FILTER,
        GLES20.GL_LINEAR_MIPMAP_LINEAR
    )
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

    // load bitmap to texture.
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

    // create Mipmap
    GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)

    // unbind texture
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    return textureIds[0]
}

/**
 * 释放纹理
 *
 * @param textureId 纹理id
 */
fun deleteTextures(textureId: Int) {
    if (textureId == 0) {
        return
    }
    val textureIds = intArrayOf(textureId)
    GLES20.glDeleteTextures(1, textureIds, 0)
}