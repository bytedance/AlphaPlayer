package com.ss.ugc.android.alphavideoplayer.player

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.Surface
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoListener
import com.ss.ugc.android.alpha_player.model.VideoInfo
import com.ss.ugc.android.alpha_player.player.AbsPlayer

/**
 * created by dengzhuoyao on 2020/07/08
 *
 * Implemented by ExoPlayer.
 */
class ExoPlayerImpl(private val context: Context) : AbsPlayer(context) {

    private lateinit var exoPlayer: SimpleExoPlayer
    private val dataSourceFactory: DefaultDataSourceFactory
    private var videoSource: MediaSource? = null

    private var currVideoWidth: Int = 0
    private var currVideoHeight: Int = 0
    private var isLooping: Boolean = false

    private val exoPlayerListener: Player.EventListener = object: Player.EventListener {
        override fun onPlayerError(error: ExoPlaybackException?) {
            errorListener?.onError(0, 0, "ExoPlayer on error: " + Log.getStackTraceString(error))
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    if (playWhenReady) {
                        preparedListener?.onPrepared()
                    }
                }
                Player.STATE_ENDED -> {
                    completionListener?.onCompletion()
                }
                else -> {}
            }
        }
    }

    private val exoVideoListener = object : VideoListener {
        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            currVideoWidth = width
            currVideoHeight = height
        }

        override fun onRenderedFirstFrame() {
            firstFrameListener?.onFirstFrame()
        }
    }

    init {
        dataSourceFactory = DefaultDataSourceFactory(context,
            Util.getUserAgent(context, "player"))
    }

    override fun initMediaPlayer() {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(context)
        exoPlayer.addListener(exoPlayerListener)
        exoPlayer.addVideoListener(exoVideoListener)
    }

    override fun setSurface(surface: Surface) {
        exoPlayer.setVideoSurface(surface)
    }

    override fun setDataSource(dataPath: String) {
        if (isLooping) {
            val extractorMediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(dataPath))
            videoSource = LoopingMediaSource(extractorMediaSource)
        } else {
            videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(dataPath))
        }
        reset()
    }

    override fun prepareAsync() {
        exoPlayer.prepare(videoSource)
        exoPlayer.playWhenReady = true
    }

    override fun start() {
        exoPlayer.playWhenReady = true
    }

    override fun pause() {
        exoPlayer.playWhenReady = false
    }

    override fun stop() {
        exoPlayer.stop()
    }

    override fun reset() {
        exoPlayer.stop(true)
    }

    override fun release() {
        exoPlayer.release()
    }

    override fun setLooping(looping: Boolean) {
        this.isLooping = looping
    }

    override fun setScreenOnWhilePlaying(onWhilePlaying: Boolean) {
    }

    override fun getVideoInfo(): VideoInfo {
        return VideoInfo(currVideoWidth, currVideoHeight)
    }

    override fun getPlayerType(): String {
        return "ExoPlayerImpl"
    }
}