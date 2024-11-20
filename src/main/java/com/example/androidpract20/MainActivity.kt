package com.example.androidpract20

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private var threadWorks: Boolean = false
    private var haveGyroscope: Boolean = true
    private var giroTextViewRef: TextView? = null
    private var sensor: Sensor? = null

    private var angleX = 0.0
    private var angleY = 0.0
    private var angleZ = 0.0

    private var lastTimestamp: Long = 0

    private lateinit var sensorManager: SensorManager
    private var updateThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        giroTextViewRef = findViewById(R.id.giroTextView)

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (sensor == null) {
            haveGyroscope = false
            Toast.makeText(this, "У вас нет гироскопа", Toast.LENGTH_LONG).show()
        }
    }

    fun onGiroThreadMageButtonClick(view: View) {
        if (!haveGyroscope) {
            Toast.makeText(this, "У вас нет гироскопа", Toast.LENGTH_LONG).show()
            return
        }

        val buttonRef: Button = view as Button

        if (!threadWorks) {
            buttonRef.text = getString(R.string.stop_giro_thread)

            // Регистрируем слушатель гироскопа
            sensorManager.registerListener(listenerGyroscope, sensor, SensorManager.SENSOR_DELAY_NORMAL)

            // Запускаем поток для обновления UI
            threadWorks = true
            updateThread = Thread {
                while (threadWorks) {
                    runOnUiThread {
                        giroTextViewRef?.text = getString(R.string.axis_angle_descriptiom) +
                                " x: %d°\n".format(Math.toDegrees(angleX).toInt()%360) +
                                getString(R.string.axis_angle_descriptiom) + " y: %d°\n".format(Math.toDegrees(angleY).toInt()%360) +
                                getString(R.string.axis_angle_descriptiom) + " z: %d°".format(Math.toDegrees(angleZ).toInt()%360)
                    }
                    Thread.sleep(500)
                }
            }
            updateThread?.start()
        } else {
            buttonRef.text = getString(R.string.start_giro_thread)
            sensorManager.unregisterListener(listenerGyroscope)
            threadWorks = false
            updateThread?.join()
        }
    }

    override fun onPause() {
        super.onPause()
        if (haveGyroscope) {
            sensorManager.unregisterListener(listenerGyroscope)
        }
        threadWorks = false
        updateThread?.interrupt()
    }

    private val listenerGyroscope: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                val deltaTime = (event.timestamp - lastTimestamp) / 1_000_000_000.0 // наносекунды в секунды
                lastTimestamp = event.timestamp

                if (deltaTime > 0) {
                    angleX += event.values[0] * deltaTime
                    angleY += event.values[1] * deltaTime
                    angleZ += event.values[2] * deltaTime
                }
            }
        }
    }
}