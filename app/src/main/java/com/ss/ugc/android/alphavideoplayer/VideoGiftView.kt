package com.ss.ugc.android.alphavideoplayer

import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.ss.ugc.android.alpha_player.Constant
import com.ss.ugc.android.alpha_player.IMonitor
import com.ss.ugc.android.alpha_player.IPlayerAction
import com.ss.ugc.android.alpha_player.controller.IPlayerController
import com.ss.ugc.android.alpha_player.controller.PlayerController
import com.ss.ugc.android.alpha_player.model.AlphaVideoViewType
import com.ss.ugc.android.alpha_player.model.Configuration
import com.ss.ugc.android.alpha_player.model.DataSource
import com.ss.ugc.android.alpha_player.model.MaskSrc
import com.ss.ugc.android.alphavideoplayer.player.ExoPlayerImpl
import com.ss.ugc.android.alphavideoplayer.utils.JsonUtil
import java.io.File

/**
 * created by dengzhuoyao on 2020/07/08
 */
class VideoGiftView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val TAG = "VideoGiftView"
    }

    private val mVideoContainer: RelativeLayout
    private var mPlayerController: IPlayerController? = null

    init {
        LayoutInflater.from(context).inflate(getResourceLayout(), this)
        mVideoContainer = findViewById(R.id.video_view)
    }

    private fun getResourceLayout(): Int {
        return R.layout.view_video_gift
    }

    fun initPlayerController(
        context: Context,
        owner: LifecycleOwner,
        playerAction: IPlayerAction,
        monitor: IMonitor
    ) {
        val configuration = Configuration(context, owner)
        //  GLTextureView supports custom display layer, but GLSurfaceView has better performance, and the GLSurfaceView is default.
        configuration.alphaVideoViewType = AlphaVideoViewType.GL_SURFACE_VIEW
        //  You can implement your IMediaPlayer, here we use ExoPlayerImpl that implemented by ExoPlayer, and
        //  we support DefaultSystemPlayer as default player.
        mPlayerController = PlayerController.get(configuration, ExoPlayerImpl(context))
        mPlayerController?.let {
            it.setPlayerAction(playerAction)
            it.setMonitor(monitor)
        }
    }

    fun startVideoGift(filePath: String) {
        if (TextUtils.isEmpty(filePath)) {
            return
        }
        val configModel = JsonUtil.parseConfigModel(filePath)
        val dataSource = DataSource()
        configModel.portraitItem?.apply {
            dataSource.setPortraitDataInfo {
                path = filePath + File.separator + this@apply.path
                setScaleType(this@apply.align)
                version = this@apply.version
                totalFrame = this@apply.totalFrame
                videoWidth = this@apply.videoWidth
                videoHeight = this@apply.videoHeight
                actualWidth = this@apply.actualWidth
                actualHeight = this@apply.actualHeight
                setAlphaArea(this@apply.alphaFrame)
                setRgbArea(this@apply.rgbFrame)
                masks = this@apply.masks
            }
        }
        configModel.landscapeItem?.apply {
            dataSource.setLandscapeDataInfo {
                path = filePath + File.separator + this@apply.path
                setScaleType(this@apply.align)
                version = this@apply.version
                totalFrame = this@apply.totalFrame
                videoWidth = this@apply.videoWidth
                videoHeight = this@apply.videoHeight
                actualWidth = this@apply.actualWidth
                actualHeight = this@apply.actualHeight
                setAlphaArea(this@apply.alphaFrame)
                setRgbArea(this@apply.rgbFrame)
                masks = this@apply.masks
            }
        }
        loadMask(dataSource)
    }

    private fun loadMask(dataSource: DataSource) {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_round)
        val maskSrc: MaskSrc = MaskSrc().apply {
            name = "matte"
            type = Constant.TYPE_MASK_IMG
            this.bitmap = bitmap
        }
        val maskSrc2: MaskSrc = MaskSrc().apply {
            name = "avatar1"
            type = Constant.TYPE_MASK_IMG
            this.bitmap = bitmap
        }
        mPlayerController?.setMask(maskSrc)
        mPlayerController?.setMask(maskSrc2)
        startDataSource(dataSource)
    }

    private fun startDataSource(dataSource: DataSource) {
        if (!dataSource.isValid()) {
            Log.e(TAG, "startDataSource: dataSource is invalid.")
        }
        attachView()
        mPlayerController?.start(dataSource)
    }

    fun attachView() {
        mPlayerController?.attachAlphaView(mVideoContainer)
        Toast.makeText(context, "attach alphaVideoView", Toast.LENGTH_SHORT).show()
    }

    fun detachView() {
        mPlayerController?.detachAlphaView(mVideoContainer)
        Toast.makeText(context, "detach alphaVideoView", Toast.LENGTH_SHORT).show()
    }

    fun releasePlayerController() {
        mPlayerController?.let {
            it.detachAlphaView(mVideoContainer)
            it.release()
        }
    }
}