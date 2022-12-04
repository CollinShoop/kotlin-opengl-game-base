package com.cms.engine.lwjgl.input

import com.cms.engine.input.InputHandler
import com.cms.engine.input.QueuedInputHandler
import com.cms.engine.input.event.KeyEvent
import com.cms.engine.input.event.MouseButtonEvent
import com.cms.engine.input.event.MouseMovedEvent
import com.cms.engine.input.event.MouseScrollEvent
import org.junit.Test
import kotlin.collections.ArrayList
import kotlin.test.assertEquals

class QueuedInputHandlerTest {

    /**
     * Tests that [QueuedInputHandler] is queueing and flushing events in order.
     *
     * TODO Test thread safety
     */
    @Test
    fun testFlush() {
        val handler = LoggingInputHandler()
        val queuedHandler = QueuedInputHandler(handler)

        // queue events
        queuedHandler.key(KeyEvent(1, 2, 3, 4))
        queuedHandler.mouseMoved(MouseMovedEvent(1.0, 2.0, 3f, 4f))
        queuedHandler.key(KeyEvent(2, 3, 4, 5))
        queuedHandler.scroll(MouseScrollEvent(10.0, 20.0))
        queuedHandler.mouseButton(MouseButtonEvent(10, 11, 12))
        queuedHandler.scroll(MouseScrollEvent(30.0, 40.0))

        // logging handler should not yet have received anything
        assertEquals(0, handler.eventLog.size)

        // flush will cause all events to be processed by the logging handler
        queuedHandler.flush()

        // add an extra event that wasn't part of flush
        // logging handler shouldn't receive it
        queuedHandler.scroll(MouseScrollEvent(999.0, 999.0))

        // logging handler should have received all events now
        val expectedEventLog = listOf(
            "key 1, 2, 3, 4",
            "mouseMoved 1.0, 2.0, 3.0, 4.0",
            "key 2, 3, 4, 5",
            "scroll 10.0, 20.0",
            "mouseButton 10, 11, 12",
            "scroll 30.0, 40.0"
        )
        assertEquals(expectedEventLog, handler.eventLog)
    }

    /**
     * Simple InputHandler which captures events as logs for easy validation
     */
    private class LoggingInputHandler : InputHandler {

        val eventLog = ArrayList<String>()

        private fun log(s: String) {
            println("LoggingInputHandler received event[${eventLog.size}]: '${s}'")
            eventLog.add(s)
        }

        override fun mouseMoved(e: MouseMovedEvent) {
            log("mouseMoved ${e.rx}, ${e.ry}, ${e.vx}, ${e.vy}")
        }

        override fun mouseButton(e: MouseButtonEvent) {
            log("mouseButton ${e.button}, ${e.action}, ${e.mods}")
        }

        override fun key(e: KeyEvent) {
            log("key ${e.key}, ${e.scancode}, ${e.action}, ${e.mods}")
        }

        override fun scroll(e: MouseScrollEvent) {
            log("scroll ${e.xOffset}, ${e.yOffset}")
        }

    }

}
