package com.example.androidpract20

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity(), SensorEventListener {
    var threadWorks:Boolean=false
    var giroTextViewRef:TextView?=null

    private var angleX = 0.0
    private var angleY = 0.0
    private var angleZ = 0.0

    private var lastTimestamp: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        giroTextViewRef=findViewById<TextView>(R.id.giroTextView) as TextView?
    }

    fun onGiroThreadMageButtonClick(view: View){
        val buttonRef:Button=view as Button

        if(!threadWorks){
            buttonRef.setText(getString(R.string.start_giro_thread))

            Thread.sleep(500)
            var action:Runnable=Runnable{
                while (threadWorks){
                    angleX+=4
                    giroTextViewRef?.post{
                        giroTextViewRef?.setText(getString(R.string.axis_angle_descriptiom)+" x:$angleX\n"+getString(R.string.axis_angle_descriptiom)+" y:$angleY\n"+getString(R.string.axis_angle_descriptiom)+" z:$angleZ")
                    }
                    Thread.sleep(495)
                }
            }
            Thread(action).start()
        }
        else{
            buttonRef.setText(getString(R.string.stop_giro_thread))
        }

        threadWorks=!threadWorks
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val deltaTime = (event.timestamp - lastTimestamp) / 1_000_000_000.0 // наносекунды в секунды
            lastTimestamp = event.timestamp

            val deltaX = event.values[0] * deltaTime // угловая скорость по X
            val deltaY = event.values[1] * deltaTime // угловая скорость по Y
            val deltaZ = event.values[2] * deltaTime // угловая скорость по Z

            // Обновляем углы
            angleX += deltaX
            angleY += deltaY
            angleZ += deltaZ
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        TODO("Not yet implemented")
    }
}