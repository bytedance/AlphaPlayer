package com.ss.ugc.android.alpha_player.utils

import com.ss.ugc.android.alpha_player.model.DataInfo

fun convertScreen2World(src: DataInfo.Area?) {
    src?.apply {
        // translate
        offset(-0.5f, -0.5f)
        // scale
        scale(2f)
        // rotate Y
        flipY()
    }
}

fun writeData(data: FloatArray?, left: Float, top: Float, right: Float, bottom: Float) {
    if (data == null || data.size < 8) {
        return
    }
    // left bottom
    data[0] = left
    data[1] = bottom
    // right bottom
    data[2] = right
    data[3] = bottom
    // left top
    data[4] = left
    data[5] = top
    // right top
    data[6] = right
    data[7] = top
}