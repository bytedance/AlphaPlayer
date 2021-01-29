package com.ss.ugc.android.alphavideoplayer.model

import com.google.gson.annotations.SerializedName
import com.ss.ugc.android.alpha_player.model.DataInfo

/**
 * created by dengzhuoyao on 2020/07/08
 */
class ConfigModel {
    @SerializedName("landscape")
    var landscapeItem: Item? = null

    @SerializedName("portrait")
    var portraitItem: Item? = null

    class Item {
        @SerializedName("path")
        var path: String? = null

        @SerializedName("align")
        var align = 0

        @SerializedName("v")
        var version = 0

        @SerializedName("f")
        var totalFrame = 0

        @SerializedName("w")
        var actualWidth = 0

        @SerializedName("h")
        var actualHeight = 0

        @SerializedName("videoW")
        var videoWidth = 0

        @SerializedName("videoH")
        var videoHeight = 0

        @SerializedName("aFrame")
        var alphaFrame: FloatArray? = null

        @SerializedName("rgbFrame")
        var rgbFrame: FloatArray? = null

        @SerializedName("masks")
        var masks: Map<String, Map<String, DataInfo.Element>>? = null
    }
}