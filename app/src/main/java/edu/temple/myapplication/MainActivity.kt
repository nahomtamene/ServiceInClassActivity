package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import edu.temple.myapplication.R.id.textView

class MainActivity : AppCompatActivity() {

    lateinit var textView: TextView
    var timerBinder : TimerService.TimerBinder? = null
    var isConnected = false

    val timeHanlder = Handler(Looper.getMainLooper()){
        textView.text = it.what.toString()
        true
    }

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerBinder = service as TimerService.TimerBinder
            timerBinder!!.setHandler(timeHanlder)


            isConnected = true

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isConnected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        bindService(
            Intent(this, TimerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE

        )

        findViewById<Button>(R.id.startButton).setOnClickListener {

        }
        
        findViewById<Button>(R.id.stopButton).setOnClickListener {

        }
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()

    }


}