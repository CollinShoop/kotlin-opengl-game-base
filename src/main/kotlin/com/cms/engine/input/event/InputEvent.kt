package com.cms.engine.input.event

import com.cms.engine.input.InputHandler

/**
 * Common type for all queued events. Once de-queued, trigger the event onto another event handler to effectively pop
 */
interface InputEvent {
    fun trigger(handler: InputHandler)
}
