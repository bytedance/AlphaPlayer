package com.ss.ugc.android.alpha_player.player

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.text.TextUtils
import android.view.Surface
import com.ss.ugc.android.alpha_player.model.VideoInfo
import java.lang.Exception

/**
 * created by dengzhuoyao on 2020/07/07
 */
class DefaultSystemPlayer : AbsPlayer() {

    lateinit var mediaPlayer : MediaPlayer
    val retriever: MediaMetadataRetriever = MediaMetadataRetriever()
    lateinit var dataPath : String


    override fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()

        mediaPlayer.setOnCompletionListener(MediaPlayer.OnCompletionListener { mediaPlayer ->
            completionListener?.onCompletion()
        })

        mediaPlayer.setOnPreparedListener(MediaPlayer.OnPreparedListener { mediaPlayer ->
            preparedListener?.onPrepared()
        })

        mediaPlayer.setOnErrorListener(MediaPlayer.OnErrorListener { mp, what, extra ->
            errorListener?.onError(what, extra, "")
            false
        })

        mediaPlayer.setOnInfoListener { mp, what, extra ->
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                firstFrameListener?.onFirstFrame()
            }
            false
        }
    }

    override fun setSurface(surface: Surface) {
        mediaPlayer.setSurface(surface)
    }

    override fun setDataSource(dataPath: String) {
        this.dataPath = dataPath
        mediaPlayer.setDataSource(dataPath)
    }

    override fun prepareAsync() {
        mediaPlayer.prepareAsync()
    }

    override fun start() {
        mediaPlayer.start()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun stop() {
        mediaPlayer.stop()
    }

    override fun reset() {
        mediaPlayer.reset()
        this.dataPath = ""
    }

    override fun release() {
        mediaPlayer.release()
        this.dataPath = ""
    }

    override fun setLooping(looping: Boolean) {
        mediaPlayer.isLooping = looping
    }

    override fun setScreenOnWhilePlaying(onWhilePlaying: Boolean) {
        mediaPlayer.setScreenOnWhilePlaying(onWhilePlaying)
    }

    override fun getVideoInfo(): VideoInfo {
        if (TextUtils.isEmpty(dataPath)) {
            throw Exception("dataPath is null, please set setDataSource firstly!")
        }

        retriever.setDataSource(dataPath)
        val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        if (TextUtils.isEmpty(widthStr) || TextUtils.isEmpty(heightStr)) {
            throw Exception("DefaultSystemPlayer get metadata failure!")
        }

        val videoWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
        val videoHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()

        return VideoInfo(videoWidth, videoHeight)
    }

    override fun getPlayerType(): String {
        return "DefaultSystemPlayer"
    }
}