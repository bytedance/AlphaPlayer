package com.ss.ugc.android.alpha_player.widget

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Surface
import com.ss.ugc.android.alpha_player.controller.IPlayerControllerExt
import com.ss.ugc.android.alpha_player.model.ScaleType
import com.ss.ugc.android.alpha_player.render.IRender

/**
 * created by dengzhuoyao on 2020/07/07
 */
class AlphaVideoView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null)
    : GLSurfaceView(context, attr) {

    val GL_CONTEXT_VERSION = 2

    @Volatile
    var isSurfaceCreated: Boolean = false

    var mVideoWidth: Float = 0f
    var mVideoHeight: Float = 0f
    var mScaleType: ScaleType = ScaleType.ScaleAspectFill

    var mRenderer: IRender? = null
    var mPlayerController: IPlayerControllerExt? = null
    var mSurface: Surface? = null

    val mSurfaceListener = object: IRender.SurfaceListener {
        override fun onSurfacePrepared(surface: Surface) {
            mSurface?.release()
            mSurface = surface
            isSurfaceCreated = true
            mPlayerController?.setSurface(surface)
            mPlayerController?.resume()
        }

        override fun onSurfaceDestroyed() {
            mSurface?.release()
            mSurface = null
            isSurfaceCreated = false
        }
    }

    init {
        setEGLContextClientVersion(GL_CONTEXT_VERSION)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        addOnSurfacePreparedListener()
        setZOrderOnTop(true)
        preserveEGLContextOnPause = true
    }

    fun addOnSurfacePreparedListener() {
        mRenderer?.setSurfaceListener(mSurfaceListener)
    }

    fun setPlayerController(playerController: IPlayerControllerExt) {
        this.mPlayerController = playerController
    }

    fun setVideoRenderer(renderer: IRender) {
        this.mRenderer = renderer
        setRenderer(renderer)
        addOnSurfacePreparedListener()
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun setScaleType(scaleType: ScaleType) {
        this.mScaleType = scaleType
        mRenderer?.setScaleType(scaleType)
    }

    fun measureInternal(videoWidth: Float, videoHeight: Float) {
        if (videoWidth > 0 && videoHeight > 0) {
            mVideoWidth = videoWidth
            mVideoHeight = videoHeight
        }
        mRenderer?.let {
            val width = measuredWidth
            val height = measuredHeight
            queueEvent {
                it.measureInternal(width.toFloat(), height.toFloat(), mVideoWidth, mVideoHeight)
            }
        }
    }

    fun onFirstFrame() {
        mRenderer?.onFirstFrame()
    }

    fun onCompletion() {
        mRenderer?.onCompletion()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureInternal(mVideoWidth, mVideoHeight)
    }

    fun release() {
        mSurfaceListener.onSurfaceDestroyed()
    }
}