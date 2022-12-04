package com.cms.engine.input

import com.cms.engine.input.event.KeyEvent
import com.cms.engine.input.event.MouseButtonEvent
import com.cms.engine.input.event.MouseMovedEvent
import com.cms.engine.input.event.MouseScrollEvent

interface InputHandler {

    fun mouseMoved(e: MouseMovedEvent)

    fun mouseButton(e: MouseButtonEvent)

    fun key(e: KeyEvent)

    fun scroll(e: MouseScrollEvent)

}
