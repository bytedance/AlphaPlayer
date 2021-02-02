package com.ss.ugc.android.alpha_player.render

import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.view.Surface
import com.ss.ugc.android.alpha_player.model.DataInfo
import com.ss.ugc.android.alpha_player.model.MaskSrc
import com.ss.ugc.android.alpha_player.model.ScaleType
import com.ss.ugc.android.alpha_player.widget.GLTextureView

/**
 * created by dengzhuoyao on 2020/07/07
 *
 *  A generic renderer with opengles interface.
 */
interface IRender : GLTextureView.Renderer, GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    fun setSurfaceListener(surfaceListener: SurfaceListener)

    /**
     * Called when the AlphaVideoView received first frame from media player.
     */
    fun onFirstFrame()

    /**
     * Called when the AlphaVideoView received completion event from media player.
     */
    fun onCompletion()

    /**
     * set scaleType for VideoRenderer
     */
    fun setScaleType(scaleType: ScaleType)

    /**
     * Called when the AlphaVideoView touch onMeasure() callback or the media source be parsed mate data.
     */
    fun measureInternal(viewWidth: Float, viewHeight: Float, tmpVideoWidth: Float, tmpVideoHeight: Float)

    /**
     * set config params to renderer.
     */
    fun setConfigParam(dataInfo: DataInfo)

    fun addMaskSrcList(maskSrcList: ArrayList<MaskSrc>)

    interface SurfaceListener {

        /**
         * Called when Surface is prepared.
         */
        fun onSurfacePrepared(surface: Surface)

        /**
         * Called when Surface is Destroyed.
         */
        fun onSurfaceDestroyed()

        /**
         * Get current frame from player.
         */
        fun getCurrentFrame(): Int
    }
}