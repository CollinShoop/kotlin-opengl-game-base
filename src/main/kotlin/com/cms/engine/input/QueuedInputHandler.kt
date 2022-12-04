package com.cms.engine.input

import com.cms.engine.input.event.InputEvent
import com.cms.engine.input.event.KeyEvent
import com.cms.engine.input.event.MouseButtonEvent
import com.cms.engine.input.event.MouseMovedEvent
import com.cms.engine.input.event.MouseScrollEvent
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * An [InputHandler] where all events are queued until [flush] is called. Flush will
 * cause all events to be received by the parent [InputHandler] in the same order they were received.
 *
 * This class is thread safe.
 */
class QueuedInputHandler(private val handler: InputHandler) : InputHandler {

    private val eventQueue: Queue<InputEvent> = ConcurrentLinkedQueue()

    /**
     * Flush queued events to the parent [InputHandler] in the same order they were received.
     */
    fun flush() {
        val keyEventsCount = eventQueue.size
        for (i in 1 .. keyEventsCount) {
            val e = eventQueue.poll() ?: break
            e.trigger(handler)
        }
    }

    /**
     * Queues [MouseMovedEvent]
     */
    override fun mouseMoved(e: MouseMovedEvent) {
        eventQueue.add(e)
    }

    /**
     * Queues [MouseButtonEvent]
     */
    override fun mouseButton(e: MouseButtonEvent) {
        eventQueue.add(e)
    }

    /**
     * Queues [KeyEvent]
     */
    override fun key(e: KeyEvent) {
        eventQueue.add(e)
    }

    /**
     * Queues [MouseScrollEvent]
     */
    override fun scroll(e: MouseScrollEvent) {
        eventQueue.add(e)
    }

}
