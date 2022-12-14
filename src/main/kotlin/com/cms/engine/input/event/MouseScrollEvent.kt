package com.cms.engine.input.event

import com.cms.engine.input.InputHandler

/**
 * InputEvent for [InputHandler.scroll]
 */
data class MouseScrollEvent(val xOffset: Double, val yOffset: Double) : InputEvent {

    override fun trigger(handler: InputHandler) {
        handler.scroll(this)
    }

    override fun toString(): String {
        return "MouseScrollEvent(xOffset=$xOffset, yOffset=$yOffset)"
    }

}
