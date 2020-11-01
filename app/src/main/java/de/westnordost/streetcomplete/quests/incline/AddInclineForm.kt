package de.westnordost.streetcomplete.quests.incline;

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import kotlinx.android.synthetic.main.quest_incline.*
import kotlin.math.atan2
import kotlin.math.sqrt

class AddInclineForm : AbstractQuestFormAnswerFragment<String>() {
    override val contentLayoutResId = R.layout.quest_incline

    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var sensorEventListener: SensorEventListener? = null

    private var deviceMeasurementActive = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleMeasurementButton.setOnClickListener {
            deviceMeasurementActive = !deviceMeasurementActive
            deviceMeasurementLayout.visibility = if (deviceMeasurementActive) View.VISIBLE else View.GONE
            manualInputLayout.visibility = if (deviceMeasurementActive) View.GONE else View.VISIBLE
            // TODO sst: translate
            toggleMeasurementButton.text = if (deviceMeasurementActive) "Enter manually" else "Measure with device"

            checkIsFormComplete()
        }

        inclineView.addListener(object: InclineView.Listener {
            override fun onLockChanged(locked: Boolean) {
                checkIsFormComplete()
            }
        })

        manualInputField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // NOP
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // TODO sst: format value here?
            }

            override fun afterTextChanged(s: Editable?) {
                checkIsFormComplete()
            }
        })

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorEventListener = object : SensorEventListener {
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

                if (!inclineView.locked) {
                    inclineView.changeIncline(pitch)
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
        sensorManager?.registerListener(sensorEventListener, sensor, 100_000)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(sensorEventListener)
    }

    override fun isFormComplete(): Boolean {
        // TODO sst: ask user about unrealistic values...
        return if (deviceMeasurementActive) {
            inclineView.locked
        } else {
            manualInputField.text.isNotEmpty()
        }
    }

    override fun onClickOk() {
        applyAnswer("test") // TODO sst: use real value
    }
}
