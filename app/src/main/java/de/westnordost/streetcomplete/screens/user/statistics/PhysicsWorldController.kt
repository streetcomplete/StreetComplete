package de.westnordost.streetcomplete.screens.user.statistics

import android.os.Handler
import android.os.HandlerThread
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.World
import kotlin.math.max

/** Contains the physics simulation world and the physics simulation loop */
class PhysicsWorldController(gravity: Vec2) {

    private val world: World = World(gravity)
    private val thread: HandlerThread = HandlerThread("Physics thread")
    private val handler: Handler

    private val loopRunnable = Runnable { loop() }

    private var isRunning = false

    var gravity: Vec2
        get() = world.gravity
        set(value) {
            // wake up everyone if the gravity changed
            world.gravity = value
            var bodyIt = world.bodyList
            while (bodyIt != null) {
                bodyIt.isAwake = true
                bodyIt = bodyIt.next
            }
        }

    interface Listener {
        fun onWorldStep()
    }
    var listener: Listener? = null

    init {
        thread.start()
        handler = Handler(thread.looper)
    }

    fun resume() {
        if (!isRunning) {
            isRunning = true
            handler.postDelayed(loopRunnable, DELAY.toLong())
        }
    }

    fun pause() {
        if (isRunning) {
            isRunning = false
            handler.removeCallbacks(loopRunnable)
        }
    }

    fun destroy() {
        handler.removeCallbacksAndMessages(null)
        thread.quit()
    }

    private fun loop() {
        val startTime = nowAsEpochMilliseconds()
        world.step(DELAY / 1000f, 6, 2)
        val executionTime = nowAsEpochMilliseconds() - startTime
        listener?.onWorldStep()
        if (isRunning) {
            handler.postDelayed(this::loop, max(0, DELAY - executionTime))
        }
    }

    suspend fun createBody(def: BodyDef, shape: Shape, density: Float): Body {
        // creating bodies cannot be done while the World is locked (= while world.step(...) is
        // executed), so we must post this on the same thread and then await it to be executed
        return withContext(handler.asCoroutineDispatcher()) {
            val body = world.createBody(def)
            body.createFixture(shape, density)
            body
        }
    }

    companion object {
        private const val DELAY = 16 // 60 fps
    }
}
