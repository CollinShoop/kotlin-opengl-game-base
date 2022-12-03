package com.cms.engine

import com.cms.engine.lwjgl.graphics.GameWindow

class Demo {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val window = GameWindow(
                windowTitle = "Demo Game")
            window.debug = true
            window.run()
        }
    }
}