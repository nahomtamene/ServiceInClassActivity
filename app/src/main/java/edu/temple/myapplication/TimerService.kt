package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.content.Context
import android.content.SharedPreferences


@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private var isRunning = false

    private var timerHandler : Handler? = null

    lateinit var t: TimerThread

    private var paused = false

    private lateinit var prefs: SharedPreferences
    private var currentTime = 0

    inner class TimerBinder : Binder() {

        // Check if Timer is already running
        val isRunning: Boolean
            get() = this@TimerService.isRunning

        // Check if Timer is paused
        val paused: Boolean
            get() = this@TimerService.paused

        // Start a new timer
        fun start(startValue: Int){

            if (!paused) {
                if (!isRunning) {
                    if (::t.isInitialized) t.interrupt()
                    this@TimerService.start(startValue)
                }
            } else {
                pause()
            }
        }

        fun getSavedTime(): Int {
            return this@TimerService.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
                .getInt("saved_time", 0)
        }



        // Receive updates from Service
        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        // Stop a currently running timer
        fun stop() {
            if (::t.isInitialized || isRunning) {
                t.interrupt()
            }
        }

        // Pause a running timer
        fun pause() {
            this@TimerService.pause()
        }

    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
        currentTime = prefs.getInt("saved_time", 0)
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }

    fun start(startValue: Int) {
        t = TimerThread(startValue)
        t.start()
    }

    fun pause() {
        if (::t.isInitialized) {
            paused = !paused
            isRunning = !paused

            if (paused) {
                getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
                    .edit().putInt("saved_time", currentTime).apply()
            }
        }
    }



    inner class TimerThread(private val startValue: Int) : Thread() {

        override fun run() {
            isRunning = true
            try {
                for (i in startValue downTo 1) {
                    currentTime = i
                    Log.d("Countdown", i.toString())
                    timerHandler?.sendEmptyMessage(i)

                    while (paused) sleep(200) // avoid tight loop
                    sleep(1000)
                }

                // Finished: Clear saved value
                prefs.edit().remove("saved_time").apply()
                isRunning = false

            } catch (e: InterruptedException) {
                Log.d("Timer interrupted", e.toString())
                isRunning = false
                paused = false
            }
        }
    }


    override fun onUnbind(intent: Intent?): Boolean {
        if (::t.isInitialized) {
            t.interrupt()
        }

        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("TimerService status", "Destroyed")
    }


}