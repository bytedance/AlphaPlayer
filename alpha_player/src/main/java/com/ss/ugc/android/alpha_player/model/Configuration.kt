package com.ss.ugc.android.alpha_player.model

import android.arch.lifecycle.LifecycleOwner
import android.content.Context

/**
 * created by dengzhuoyao on 2020/07/07
 */
class Configuration(var context: Context,
                    var lifecycleOwner: LifecycleOwner) {
    var alphaVideoViewType: AlphaVideoViewType = AlphaVideoViewType.GL_SURFACE_VIEW
}

enum class AlphaVideoViewType {
    GL_TEXTURE_VIEW, GL_SURFACE_VIEW
}