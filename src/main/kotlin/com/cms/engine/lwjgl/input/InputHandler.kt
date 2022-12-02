package com.cms.engine.lwjgl.input

import com.cms.engine.lwjgl.input.event.KeyEvent
import com.cms.engine.lwjgl.input.event.MouseButtonEvent
import com.cms.engine.lwjgl.input.event.MouseMovedEvent
import com.cms.engine.lwjgl.input.event.MouseScrollEvent

interface InputHandler {

    fun mouseMoved(e: MouseMovedEvent)

    fun mouseButton(e: MouseButtonEvent)

    fun key(e: KeyEvent)

    fun scroll(e: MouseScrollEvent)

}
