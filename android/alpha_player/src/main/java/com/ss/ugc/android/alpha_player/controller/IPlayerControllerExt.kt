package com.ss.ugc.android.alpha_player.controller

import android.view.Surface

/**
 * created by dengzhuoyao on 2020/07/07
 */
interface IPlayerControllerExt : IPlayerController {

    fun surfacePrepared(surface: Surface)
}