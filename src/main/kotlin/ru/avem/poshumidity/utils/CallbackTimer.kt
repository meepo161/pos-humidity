package ru.avem.poshumidity.utils

import javafx.util.Duration
import tornadofx.seconds
import java.util.*
import kotlin.concurrent.thread

class CallbackTimer(
    delay: Duration = 0.seconds,
    tickPeriod: Duration,
    tickTimes: Int = 1,
    onStartJob: (tcb: ICallbackTimer) -> Unit = {},
    tickJob: (tcb: ICallbackTimer) -> Unit = {},
    onFinishJob: (tcb: ICallbackTimer) -> Unit = {},
    private var timerName: String = "default_timer"
) : ICallbackTimer {

    val timer = Timer(timerName)
    var currentTick = -1
    var isRunning = true

    private val timerTask = object : TimerTask() {
        override fun run() {
            currentTick++
            tickJob(this@CallbackTimer)

            if (currentTick == tickTimes) {
                isRunning = false
                timer.cancel()
                onFinishJob(this@CallbackTimer)
            }
        }
    }

    init {
        thread(isDaemon = true) {
            try {
                onStartJob(this)
                timer.schedule(timerTask, delay.toMillis().toLong(), tickPeriod.toMillis().toLong())
            } catch (ignored: Exception) {

            }
        }
    }

    override fun getCurrentTicks() = currentTick

    override fun getName() = timerName

    override fun stop() {
        try {
            timer.cancel()
            isRunning = false
        } catch (ignored: Exception) {

        }
    }
}

interface ICallbackTimer {
    fun getCurrentTicks(): Int

    fun getName(): String
    fun stop()
}
