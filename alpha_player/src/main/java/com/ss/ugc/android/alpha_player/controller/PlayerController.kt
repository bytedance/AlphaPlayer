package com.ss.ugc.android.alpha_player.controller

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.os.*
import android.support.annotation.WorkerThread
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import com.ss.ugc.android.alpha_player.Constant
import com.ss.ugc.android.alpha_player.IMonitor
import com.ss.ugc.android.alpha_player.IPlayerAction
import com.ss.ugc.android.alpha_player.model.AlphaVideoViewType
import com.ss.ugc.android.alpha_player.model.Configuration
import com.ss.ugc.android.alpha_player.model.DataSource
import com.ss.ugc.android.alpha_player.model.MaskSrc
import com.ss.ugc.android.alpha_player.player.DefaultSystemPlayer
import com.ss.ugc.android.alpha_player.player.IMediaPlayer
import com.ss.ugc.android.alpha_player.player.PlayerState
import com.ss.ugc.android.alpha_player.render.VideoRenderer
import com.ss.ugc.android.alpha_player.utils.createTextBitmap
import com.ss.ugc.android.alpha_player.widget.AlphaVideoGLSurfaceView
import com.ss.ugc.android.alpha_player.widget.AlphaVideoGLTextureView
import com.ss.ugc.android.alpha_player.widget.IAlphaVideoView
import java.io.File
import kotlin.math.ceil

/**
 * created by dengzhuoyao on 2020/07/08
 */
class PlayerController(
    val context: Context,
    owner: LifecycleOwner,
    val alphaVideoViewType: AlphaVideoViewType,
    mediaPlayer: IMediaPlayer
): IPlayerControllerExt, LifecycleObserver, Handler.Callback {

    companion object {
        const val INIT_MEDIA_PLAYER: Int = 1
        const val SET_DATA_SOURCE: Int =  2
        const val START: Int = 3
        const val PAUSE: Int = 4
        const val RESUME: Int = 5
        const val STOP: Int = 6
        const val DESTROY: Int = 7
        const val SURFACE_PREPARED: Int = 8
        const val RESET: Int = 9
        const val SET_MASK: Int = 10

        fun get(configuration: Configuration, mediaPlayer: IMediaPlayer? = null): PlayerController {
            return PlayerController(
                configuration.context, configuration.lifecycleOwner,
                configuration.alphaVideoViewType,
                mediaPlayer ?: DefaultSystemPlayer()
            )
        }
    }

    private var suspendDataSource: DataSource? = null
    private var messageId: Long = 0L
    var playing : Boolean = false
    var playerState = PlayerState.NOT_PREPARED
    var mMonitor: IMonitor? = null
    var mPlayerAction: IPlayerAction? = null
    var mediaPlayer: IMediaPlayer?
    lateinit var alphaVideoView: IAlphaVideoView

    var workHandler: Handler? = null
    val mainHandler: Handler = Handler(Looper.getMainLooper())
    var playThread: HandlerThread? = null
    private var totalFrame = 0
    private var actualWidth = 0
    private var actualHeight = 0
    private var version = 0
    private val masks = ArrayList<MaskSrc>()

    private val mPreparedListener = object: IMediaPlayer.OnPreparedListener {
        override fun onPrepared() {
            sendMessage(getMessage(START, null))
        }
    }

    private val mErrorListener = object : IMediaPlayer.OnErrorListener {
        override fun onError(what: Int, extra: Int, desc: String) {
            monitor(false, what, extra, "mediaPlayer error, info: $desc")
            emitEndSignal()
        }
    }

    init {
        this.mediaPlayer = mediaPlayer
        init(owner)
        initAlphaView()
        initMediaPlayer()
    }

    private fun init(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(this)
        playThread = HandlerThread("alpha-play-thread", Process.THREAD_PRIORITY_BACKGROUND)
        playThread!!.start()
        workHandler = Handler(playThread!!.looper, this)
    }

    private fun initAlphaView() {
        alphaVideoView = when(alphaVideoViewType) {
            AlphaVideoViewType.GL_SURFACE_VIEW -> AlphaVideoGLSurfaceView(context, null)
            AlphaVideoViewType.GL_TEXTURE_VIEW -> AlphaVideoGLTextureView(context, null)
        }
        alphaVideoView.let {
            val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            it.setLayoutParams(layoutParams)
            it.setPlayerController(this)
            it.setVideoRenderer(VideoRenderer(it))
        }
    }

    private fun initMediaPlayer() {
        sendMessage(getMessage(INIT_MEDIA_PLAYER, null))
    }

    override fun setPlayerAction(playerAction: IPlayerAction) {
        this.mPlayerAction = playerAction
    }

    override fun setMonitor(monitor: IMonitor) {
        this.mMonitor = monitor
    }

    override fun setVisibility(visibility: Int) {
        alphaVideoView.setVisibility(visibility)
        if (visibility == View.VISIBLE) {
            alphaVideoView.bringToFront()
        }
    }

    override fun attachAlphaView(parentView: ViewGroup) {
        alphaVideoView.addParentView(parentView)
    }

    override fun detachAlphaView(parentView: ViewGroup) {
        alphaVideoView.removeParentView(parentView)
    }

    override fun setMask(maskSrc: MaskSrc?) {
        maskSrc?.apply {
            sendMessage(getMessage(SET_MASK, this))
        }
    }

    override fun getCurrentFrame(): Int {
        if (mediaPlayer == null || totalFrame == 0) {
            return -1
        }
        val duration = getDuration()
        if (duration <= 0) {
            return -1
        }
        val progress = (mediaPlayer?.getCurrentPosition() ?: 0).toFloat() / duration.toFloat()
        if (progress > 1) {
            return -1
        }
        return ceil((progress * totalFrame).toDouble()).toInt()
    }

    private fun getDuration(): Int {
        return if (mediaPlayer == null) {
            -1
        } else {
            try {
                mediaPlayer?.getVideoInfo()?.duration ?: -1
            } catch (e: Exception) {
                -1
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        resume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        stop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        release()
    }

    private fun sendMessage(msg: Message) {
        playThread?.let {
            if (it.isAlive && !it.isInterrupted) {
                when (workHandler) {
                    null -> workHandler = Handler(it.looper, this)
                }
                workHandler!!.sendMessageDelayed(msg, 0)
            }
        }
    }

    private fun getMessage(what: Int, obj: Any?): Message {
        val message = Message.obtain()
        message.what = what
        message.obj = obj
        return message
    }

    override fun surfacePrepared(surface: Surface) {
        sendMessage(getMessage(SURFACE_PREPARED, surface))
    }

    override fun start(dataSource: DataSource) {
        messageId = dataSource.messageId
        if (dataSource.isValid()) {
            setVisibility(View.VISIBLE)
            sendMessage(getMessage(SET_DATA_SOURCE, dataSource))
        } else {
            emitEndSignal()
            monitor(false, errorInfo = "dataSource is invalid!")
        }
    }

    override fun pause() {
        sendMessage(getMessage(PAUSE, null))
    }

    override fun resume() {
        sendMessage(getMessage(RESUME, null))
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying() == true
    }

    override fun stop() {
        sendMessage(getMessage(STOP, null))
    }

    override fun reset() {
        sendMessage(getMessage(RESET, null))
    }

    override fun release() {
        sendMessage(getMessage(DESTROY, null))
    }

    override fun getView(): View {
        return alphaVideoView.getView()
    }

    override fun getPlayerType(): String {
        return mediaPlayer?.getPlayerType() ?: ""
    }

    @WorkerThread
    private fun initPlayer() {
        try {
            mediaPlayer?.initMediaPlayer()
        } catch (e: Exception) {
            mediaPlayer = DefaultSystemPlayer()
            mediaPlayer?.initMediaPlayer()
            // TODO: add log
        }
        mediaPlayer?.setScreenOnWhilePlaying(true)
        mediaPlayer?.setLooping(false)

        mediaPlayer?.setOnFirstFrameListener(object : IMediaPlayer.OnFirstFrameListener {
            override fun onFirstFrame() {
                alphaVideoView.onFirstFrame()
            }
        })
        mediaPlayer?.setOnCompletionListener(object : IMediaPlayer.OnCompletionListener {
            override fun onCompletion() {
                alphaVideoView.onCompletion()
                masks.clear()
                playerState = PlayerState.PAUSED
                monitor(true, errorInfo = "")
                emitEndSignal()
            }
        })
    }

    @WorkerThread
    private fun setDataSource(dataSource: DataSource) {
        try {
            setVideoFromFile(dataSource)
        } catch (e: Exception) {
            e.printStackTrace()
            monitor(
                false,
                errorInfo = "alphaVideoView set dataSource failure: " + Log.getStackTraceString(
                    e
                )
            )
            emitEndSignal()
        }
    }

    @WorkerThread
    private fun setVideoFromFile(dataSource: DataSource) {
        mediaPlayer?.reset()
        playerState = PlayerState.NOT_PREPARED
        val dataInfo = dataSource.getDataInfo(context.resources.configuration.orientation)
        val dataPath = dataInfo.path
        if (TextUtils.isEmpty(dataPath) || !File(dataPath).exists()) {
            monitor(false, errorInfo = "dataPath is empty or File is not exists. path = $dataPath")
            emitEndSignal()
            return
        }
        alphaVideoView.setConfigParam(dataInfo)
        alphaVideoView.addMaskSrcList(masks)
        mediaPlayer?.setLooping(dataInfo.looping)
        mediaPlayer?.setDataSource(dataPath)
        totalFrame = dataInfo.totalFrame
        actualWidth = dataInfo.actualWidth
        actualHeight = dataInfo.actualHeight
        version = dataInfo.version
        if (alphaVideoView.isSurfaceCreated()) {
            prepareAsync()
        } else {
            suspendDataSource = dataSource
        }
    }

    @WorkerThread
    private fun handleSuspendedEvent() {
        suspendDataSource?.let {
            setVideoFromFile(it)
        }
        suspendDataSource = null
    }


    @WorkerThread
    private fun prepareAsync() {
        mediaPlayer?.let {
            if (playerState == PlayerState.NOT_PREPARED || playerState == PlayerState.STOPPED) {
                it.setOnPreparedListener(mPreparedListener)
                it.setOnErrorListener(mErrorListener)
                it.prepareAsync()
            }
        }
    }

    @WorkerThread
    private fun startPlay() {
        when (playerState) {
            PlayerState.PREPARED -> {
                mediaPlayer?.start()
                playing = true
                playerState = PlayerState.STARTED
                mainHandler.post {
                    mPlayerAction?.startAction()
                }
            }
            PlayerState.PAUSED -> {
                mediaPlayer?.start()
                playerState = PlayerState.STARTED
            }
            PlayerState.NOT_PREPARED, PlayerState.STOPPED -> {
                try {
                    prepareAsync()
                } catch (e: Exception) {
                    e.printStackTrace()
                    monitor(false, errorInfo = "prepare and start MediaPlayer failure!")
                    emitEndSignal()
                }
            }
        }
    }

    @WorkerThread
    private fun parseVideoSize() {
        if (version <= 0) {
            val videoInfo = mediaPlayer?.getVideoInfo()
            actualWidth = (videoInfo?.videoWidth ?: 0) / 2
            actualHeight = videoInfo?.videoHeight ?: 0
        }
        alphaVideoView.measureInternal(actualWidth.toFloat(), actualHeight.toFloat())
        val scaleType = alphaVideoView.getScaleType()
        mainHandler.post {
            mPlayerAction?.onVideoSizeChanged(actualWidth, actualHeight, scaleType)
        }
    }

    override fun handleMessage(msg: Message?): Boolean {
        msg?.let {
            when(msg.what) {
                INIT_MEDIA_PLAYER -> {
                    initPlayer()
                }
                SURFACE_PREPARED -> {
                    val surface = msg.obj as Surface
                    mediaPlayer?.setSurface(surface)
                    handleSuspendedEvent()
                }
                SET_DATA_SOURCE -> {
                    val dataSource = msg.obj as DataSource
                    setDataSource(dataSource)
                }
                START -> {
                    try {
                        parseVideoSize()
                        playerState = PlayerState.PREPARED
                        startPlay()
                    } catch (e: Exception) {
                        monitor(
                            false, errorInfo = "start video failure: " + Log.getStackTraceString(
                                e
                            )
                        )
                        emitEndSignal()
                    }
                }
                PAUSE -> {
                    when (playerState) {
                        PlayerState.STARTED -> {
                            mediaPlayer?.pause()
                            playerState = PlayerState.PAUSED
                        }
                        else -> {
                        }
                    }
                }
                RESUME -> {
                    if (playing) {
                        startPlay()
                    } else {
                    }
                }
                STOP -> {
                    when (playerState) {
                        PlayerState.STARTED, PlayerState.PAUSED -> {
                            mediaPlayer?.pause()
                            playerState = PlayerState.PAUSED
                        }
                        else -> {
                        }
                    }
                }
                DESTROY -> {
                    alphaVideoView.onPause()
                    if (playerState == PlayerState.STARTED) {
                        mediaPlayer?.pause()
                        playerState = PlayerState.PAUSED
                    }
                    if (playerState == PlayerState.PAUSED) {
                        mediaPlayer?.stop()
                        playerState = PlayerState.STOPPED
                    }
                    mediaPlayer?.release()
                    alphaVideoView.release()
                    playerState = PlayerState.RELEASE

                    playThread?.let {
                        it.quit()
                        it.interrupt()
                    }
                }
                RESET -> {
                    mediaPlayer?.reset()
                    playerState = PlayerState.NOT_PREPARED
                    playing = false
                }
                SET_MASK -> {
                    if (msg.obj !is MaskSrc) {
                        return@let
                    }

                    val src = msg.obj as MaskSrc
                    if (src.type == Constant.TYPE_MASK_TEXT) {
                        try {
                            src.bitmap = createTextBitmap(src)
                        } catch (e: OutOfMemoryError) {
                            e.printStackTrace()
                        }
                    }

                    src.bitmap?.apply {
                        src.width = width
                        src.height = height
                        masks.add(src)
                    }
                }
                else -> {}
            }
        }
        return true
    }

    private fun emitEndSignal() {
        playing = false
        messageId = 0L
        mainHandler.post {
            mPlayerAction?.endAction()
        }
    }

    private fun monitor(state: Boolean, what: Int = 0, extra: Int = 0, errorInfo: String) {
        mMonitor?.monitor(state, getPlayerType(), what, extra, "$errorInfo, messageId: $messageId")
    }
}