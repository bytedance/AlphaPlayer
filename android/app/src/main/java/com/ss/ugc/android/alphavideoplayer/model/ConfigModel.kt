package com.ss.ugc.android.alphavideoplayer.model

import com.google.gson.annotations.SerializedName

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
        var alignMode: Int = 0
    }
}