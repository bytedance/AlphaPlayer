/*
 * Tencent is pleased to support the open source community by making vap available.
 *
 * Copyright (C) 2020 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ss.ugc.android.alpha_player.mask

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.ss.ugc.android.alpha_player.model.PointRect
import com.ss.ugc.android.alpha_player.utils.GlFloatArray
import com.ss.ugc.android.alpha_player.utils.TexCoordsUtil
import com.ss.ugc.android.alpha_player.utils.TextUtil
import com.ss.ugc.android.alpha_player.utils.VertexUtil

/**
 * Description:单独控制，渲染蒙版
 * Created by 杜小菜 on 2021/2/8 - 17:04 .
 * E-mail: duqian2010@gmail.com
 */
class MaskRender() {
    companion object {
        private const val TAG = "dq.MaskRender"
    }

    var maskShader: MaskShader? = null
    var vertexArray = GlFloatArray()
    private var maskArray = GlFloatArray()

    /**
     * shader 与 texture初始化
     */
    fun initMaskShader(edgeBlur: Boolean) {
        // shader 初始化
        maskShader = MaskShader(edgeBlur)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST) // 关闭深度测试
    }

    fun renderFrame(videoTextureId: Int) {
        if (videoTextureId <= 0) return
        val shader = this.maskShader ?: return

        shader.useProgram()

        val maskPositionRect = PointRect(
            0,
            0,
            1080,
            1920
        )
        // 顶点坐标
        vertexArray.setArray(
            VertexUtil.create(
                1080,
                1920,
                maskPositionRect,
                vertexArray.array
            )
        )
        vertexArray.setVertexAttribPointer(shader.aPositionLocation)

        val maskTexId = TextUtil.genTextTextureId("杜小菜6666666666666666666666666")

        if (maskTexId > 0) {
            maskArray.setArray(
                TexCoordsUtil.create(
                    1080,
                    1920,
                    maskPositionRect,
                    vertexArray.array
                )
            )
            maskArray.setVertexAttribPointer(shader.aTextureMaskCoordinatesLocation)
            // 绑定alpha纹理
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, maskTexId)
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glUniform1i(shader.uTextureMaskUnitLocation, 0)

            GLES20.glEnable(GLES20.GL_BLEND)
            // 基于源象素alpha通道值的半透明混合函数
            GLES20.glBlendFuncSeparate(
                GLES20.GL_ONE,
                GLES20.GL_SRC_ALPHA,
                GLES20.GL_ZERO,
                GLES20.GL_SRC_ALPHA
            )
            // draw
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

            GLES20.glDisable(GLES20.GL_BLEND)
        }
    }
}