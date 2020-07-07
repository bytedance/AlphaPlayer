package com.ss.ugc.android.alpha_player.player

import android.content.Context

/**
 * created by dengzhuoyao on 2020/07/07
 */
abstract class AbsPlayer(context: Context? = null) : IMediaPlayer {

    lateinit var completionListener: IMediaPlayer.OnCompletionListener
    lateinit var preparedListener: IMediaPlayer.OnPreparedListener
    lateinit var errorListener: IMediaPlayer.OnErrorListener
    lateinit var firstFrameListener: IMediaPlayer.OnFirstFrameListener

    override fun setOnCompletionListener(completionListener: IMediaPlayer.OnCompletionListener) {
        this.completionListener = completionListener
    }

    override fun setOnPreparedListener(preparedListener: IMediaPlayer.OnPreparedListener) {
        this.preparedListener = preparedListener
    }

    override fun setOnErrorListener(errorListener: IMediaPlayer.OnErrorListener) {
        this.errorListener = errorListener
    }

    override fun setOnFirstFrameListener(firstFrameListener: IMediaPlayer.OnFirstFrameListener) {
        this.firstFrameListener = firstFrameListener
    }
}