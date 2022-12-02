package com.cms.engine.lwjgl.input.event

import com.cms.engine.lwjgl.input.InputHandler

/**
 * InputEvent for [InputHandler.mouseButton]
 */
data class MouseButtonEvent(val button: Int, val action: Int, val mods: Int) : InputEvent {

    override fun trigger(handler: InputHandler) {
        handler.mouseButton(this)
    }

    override fun toString(): String {
        return "MouseButtonEvent(button=$button, action=$action, mods=$mods)"
    }

}
