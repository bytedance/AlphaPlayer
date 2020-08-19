package com.ss.ugc.android.alpha_player.player

import android.view.Surface
import com.ss.ugc.android.alpha_player.model.VideoInfo
import java.io.IOException

/**
 * created by dengzhuoyao on 2020/07/07
 */
interface IMediaPlayer {

    /**
     * Register a callback to be invoked when the end of a media source has
     * been reached the end of the file.
     */
    fun setOnCompletionListener(completionListener: OnCompletionListener)

    /**
     * Register a callback to be invoked when the media source is ready for
     * playback.
     */
    fun setOnPreparedListener(preparedListener: OnPreparedListener)

    /**
     * Register a callback to be invoked when an error has happened during
     * an asynchronous operation.
     */
    fun setOnErrorListener(errorListener: OnErrorListener)

    /**
     * Register a callback to be invoked when the first frame has been decoded.
     */
    fun setOnFirstFrameListener(firstFrameListener: OnFirstFrameListener)

    /**
     * Maybe will init mediaPlayer on sub thread.
     */
    @Throws(Exception::class)
    fun initMediaPlayer()

    /**
     * Sets the Surface to be used as the sink for the video portion of the media.
     */
    fun setSurface(surface: Surface)

    /**
     * Sets the data source to use.
     *
     * @param path the path of the file you want to play.
     */
    @Throws(IOException::class)
    fun setDataSource(dataPath: String)

    fun prepareAsync()

    fun start()

    fun pause()

    fun stop()

    fun reset()

    fun release()

    fun setLooping(looping: Boolean)

    fun setScreenOnWhilePlaying(onWhilePlaying: Boolean)

    @Throws(Exception::class)
    fun getVideoInfo(): VideoInfo

    fun getPlayerType(): String

    /**
     * Interface definition for a callback to be invoked when playback of a
     * media source has completed.
     */
    interface OnCompletionListener {
        /**
         * Called when the end of a media source is reached during playback.
         */
        fun onCompletion()
    }

    /**
     * Interface definition for a callback to be invoked when the media source
     * is ready for playback.
     */
    interface OnPreparedListener {
        /**
         * Called when the media file is ready for playback.
         */
        fun onPrepared()
    }

    /**
     * Interface definition of a callback to be invoked when there has been an error
     * during an asynchronous operation.
     */
    interface OnErrorListener {
        /**
         * Called to indicate an error
         *
         * @param what      the type of error that has occurred.
         * @param extra     an extra code, specific to the error. Typically
         * implementation dependent.
         * @param desc      the description of error.
         */
        fun onError(what: Int, extra: Int, desc: String)
    }

    /**
     * Interface definition of a callback to be invoked when the first frame has
     * been decoded.
     */
    interface OnFirstFrameListener {
        /**
         * Called when the media player decodec the first frame. Typically
         * implementation dependent.
         */
        fun onFirstFrame()
    }
}
