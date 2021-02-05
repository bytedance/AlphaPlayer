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
package com.ss.ugc.android.alpha_player.utils

import com.ss.ugc.android.alpha_player.model.PointRect
import com.ss.ugc.android.alpha_player.model.Src

/**
 * 纹理坐标工具
 * 坐标顺序是倒N
 */
object TexCoordsUtil {

    /**
     * @param width 纹理的宽高
     * @param height
     */
    fun create(width: Int, height: Int, rect: PointRect, array: FloatArray): FloatArray {

        // x0
        array[0] = rect.x.toFloat() / width
        // y0
        array[1] = rect.y.toFloat() / height

        // x1
        array[2] = rect.x.toFloat() / width
        // y1
        array[3] = (rect.y.toFloat() + rect.h) / height

        // x2
        array[4] = (rect.x.toFloat() + rect.w) / width
        // y2
        array[5] = rect.y.toFloat() / height

        // x3
        array[6] = (rect.x.toFloat() + rect.w) / width
        // y3
        array[7] = (rect.y.toFloat() + rect.h) / height

        return array
    }




    /**
     * 顺时针90度
     */
    fun rotate90(array: FloatArray): FloatArray {
        // 0->2 1->0 3->1 2->3
        val tx = array[0]
        val ty = array[1]

        // 1->0
        array[0] = array[2]
        array[1] = array[3]

        // 3->1
        array[2] = array[6]
        array[3] = array[7]

        // 2->3
        array[6] = array[4]
        array[7] = array[5]

        // 0->2
        array[4] = tx
        array[5] = ty
        return array
    }


    fun genSrcCoordsArray(array: FloatArray, fw: Int, fh: Int, sw: Int, sh: Int, fitType: Src.FitType): FloatArray {
        return if (fitType == Src.FitType.CENTER_FULL) {
            if (fw <= sw && fh <= sh) {
                // 中心对齐，不拉伸
                val gw = (sw - fw) / 2
                val gh = (sh - fh) / 2
                TexCoordsUtil.create(sw, sh, PointRect(gw, gh, fw, fh), array)
            } else { // centerCrop
                val fScale = fw * 1.0f / fh
                val sScale = sw * 1.0f / sh
                val srcRect = if (fScale > sScale) {
                    val w = sw
                    val x = 0
                    val h = (sw / fScale).toInt()
                    val y = (sh - h) / 2

                    PointRect(x, y, w, h)
                } else {
                    val h = sh
                    val y = 0
                    val w = (sh * fScale).toInt()
                    val x = (sw - w) / 2
                    PointRect(x, y, w, h)
                }
                TexCoordsUtil.create(sw, sh, srcRect, array)
            }
        } else { // 默认 fitXY
            TexCoordsUtil.create(fw, fh, PointRect(0, 0, fw, fh), array)
        }
    }

    fun transColor(color: Int): FloatArray {
        val argb = FloatArray(4)
        argb[0] = (color.ushr(24) and 0x000000ff) / 255f
        argb[1] = (color.ushr(16) and 0x000000ff) / 255f
        argb[2] = (color.ushr(8) and 0x000000ff) / 255f
        argb[3] = (color and 0x000000ff) / 255f
        return argb
    }
}