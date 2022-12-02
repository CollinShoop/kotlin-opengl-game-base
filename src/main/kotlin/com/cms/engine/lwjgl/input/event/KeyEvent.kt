package com.cms.engine.lwjgl.input.event

import com.cms.engine.lwjgl.input.InputHandler

/**
 * InputEvent for [InputHandler.key]
 */
data class KeyEvent(val key: Int, val scancode: Int, val action: Int, val mods: Int) : InputEvent {

    override fun trigger(handler: InputHandler) {
        handler.key(this)
    }

    override fun toString(): String {
        return "KeyEvent(key=$key, scancode=$scancode, action=$action, mods=$mods)"
    }
}
