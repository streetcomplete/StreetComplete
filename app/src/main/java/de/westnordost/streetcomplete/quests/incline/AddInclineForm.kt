package de.westnordost.streetcomplete.quests.incline;

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import kotlinx.android.synthetic.main.fragment_quest_answer.*
import kotlinx.android.synthetic.main.quest_smoothness.*
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.tan

class AddInclineForm : AbstractQuestFormAnswerFragment<String>() {
    override val contentLayoutResId = R.layout.quest_smoothness

    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var sensorEventListener : SensorEventListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorEventListener = object : SensorEventListener {
            private var lastRenderTime = System.currentTimeMillis()

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.i("Sensor", "" + accuracy)
            }

            override fun onSensorChanged(event: SensorEvent?) {
                val g = convertFloatsToDoubles(event!!.values.clone())
                val norm = sqrt(g!![0] * g[0] + g[1] * g[1] + g[2] * g[2] + g[3] * g[3])
                g[0] /= norm
                g[1] /= norm
                g[2] /= norm
                g[3] /= norm

                // Set values to commonly known quaternion letter representatives
                val x = g[0]
                val y = g[1]
                val z = g[2]
                val w = g[3]

                // Calculate Pitch in degrees (-180 to 180)
                val sinP = 2.0 * (w * x + y * z)
                val cosP = 1.0 - 2.0 * (x * x + y * y)
                val pitch = atan2(sinP, cosP) * (180 / Math.PI)

                if (System.currentTimeMillis() - lastRenderTime > 100) {
                    titleLabel.text = "incline = ${(tan(pitch  * Math.PI / 180) * 100).roundToInt()} %"
                    inclineView.changeIncline(pitch.toFloat())
                    lastRenderTime = System.currentTimeMillis()
                }
            }

            private fun convertFloatsToDoubles(input: FloatArray?): DoubleArray? {
                if (input == null) return null
                val output = DoubleArray(input.size)
                for (i in input.indices) output[i] = input[i].toDouble()
                return output
            }
        }

        listener?.onHighlightSidewalkSide(questId, questGroup, Listener.SidewalkSide.LEFT)

        checkIsFormComplete()
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(sensorEventListener)
    }

    override fun isFormComplete(): Boolean {
        return true // TODO sst: sensor must be fixed or something...
    }

    override fun onClickOk() {
        applyAnswer("test") // TODO sst: use real value
    }
}
