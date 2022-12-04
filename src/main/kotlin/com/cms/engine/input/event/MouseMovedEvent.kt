package com.cms.engine.input.event

import com.cms.engine.input.InputHandler

/**
 * InputEvent for [InputHandler.mouseMoved]
 *
 * @param rx real x coordinate in pixels from the left
 * @param ry real y coordinate in pixels from the top
 * @param vx virtual x coordinate in game space from the left
 * @param vy virtual y coordinate in game space from the top
 */
data class MouseMovedEvent(val rx: Double, val ry: Double, val vx: Float, val vy: Float) : InputEvent {

    /**
     * Cursor movement on the screen.
     */
    override fun trigger(handler: InputHandler) {
        handler.mouseMoved(this)
    }

    override fun toString(): String {
        return "MouseMovedEvent(rx=$rx, ry=$ry, vx=$vx, vy=$vy)"
    }

}
