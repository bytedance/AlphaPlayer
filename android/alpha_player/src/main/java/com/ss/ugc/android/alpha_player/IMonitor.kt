package com.ss.ugc.android.alpha_player

/**
 * created by dengzhuoyao on 2020/07/08
 *
 * Interface definition for a callback to be invoked when a alpha video is played or
 * an error occurred during playback
 */
interface IMonitor {
    /**
     * Called when a alpha video is played or an error occurred during playback
     *
     * @param result        the result of alpha video playback
     * @param playType      the type of alpha player impl
     * @param what          the type of error that has occurred:
     * @param extra         an extra code, specific to the error. Typically
     * implementation dependent.
     * @param errorInfo     detail error information
     */
    fun monitor(result: Boolean, playType: String, what: Int, extra: Int, errorInfo: String)
}