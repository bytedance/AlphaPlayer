package com.ss.ugc.android.alpha_player.widget

import android.view.View
import android.view.ViewGroup
import com.ss.ugc.android.alpha_player.controller.IPlayerControllerExt
import com.ss.ugc.android.alpha_player.model.ScaleType
import com.ss.ugc.android.alpha_player.render.IRender

interface IAlphaVideoView {

    fun setLayoutParams(params: ViewGroup.LayoutParams?)

    fun setVisibility(visibility: Int)

    fun bringToFront()

    fun addParentView(parentView: ViewGroup)

    fun removeParentView(parentView: ViewGroup)

    fun getView(): View

    fun getMeasuredWidth(): Int

    fun getMeasuredHeight(): Int

    fun requestRender()

    fun setPlayerController(playerController: IPlayerControllerExt)

    fun setVideoRenderer(renderer: IRender)

    fun setScaleType(scaleType: ScaleType)

    fun getScaleType(): ScaleType

    fun isSurfaceCreated(): Boolean

    fun measureInternal(videoWidth: Float, videoHeight: Float)

    fun onFirstFrame()

    fun onCompletion()

    fun onPause()

    fun release()
}