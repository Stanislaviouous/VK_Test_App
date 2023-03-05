package com.example.audiorec.stopwatch

import android.os.Looper
import java.time.Duration
import java.util.logging.Handler

class StopWatch(listener: OnTimerTickListener) {

    interface OnTimerTickListener{
        fun onTimerTick(duration: String)
    }


    private var handler = android.os.Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var duration = 0L
    private var delay = 100L

    init{
        runnable = Runnable {
            duration += delay
            handler.postDelayed(runnable, delay)
            listener.onTimerTick(format())
        }
    }

    fun start(){
        handler.postDelayed(runnable, delay)
    }

    fun pause(){
        handler.removeCallbacks(runnable)
    }

    fun stop(){
        handler.removeCallbacks(runnable)
        duration = 0L
    }

    fun format() : String{
        val mil = (duration % 1000) / 10
        val sec = (duration / 1000) % 60
        val min = ((duration / 1000) / 60) % 60
        val hou = ((duration / 1000) / 60) / 60
        var form = "";
        if (hou > 0) {
            form = "%02d:%02d:%02d:%02d".format(hou, min, sec, mil)
        }
        else {
            form = "%02d:%02d:%02d".format(min, sec, mil)
        }

        return form
    }

    override fun toString(): String {
        return duration.toString()
    }



}