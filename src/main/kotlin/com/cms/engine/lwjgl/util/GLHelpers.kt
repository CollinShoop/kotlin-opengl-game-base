package com.cms.engine.lwjgl.util

import com.cms.engine.lwjgl.view.LetterBoxView
import org.lwjgl.opengl.GL11
import java.awt.geom.Rectangle2D

class GLHelpers {

    companion object {

        /**
         * Draw a rectangle as a series of lines.
         *
         * Must be called from within GL_LINES.
         */
        fun rect(rect: Rectangle2D.Float) {
            GL11.glVertex2f(rect.x, rect.y)

            GL11.glVertex2f(rect.x + rect.width, rect.y)
            GL11.glVertex2f(rect.x + rect.width, rect.y)

            GL11.glVertex2f(rect.x + rect.width, rect.y + rect.height)
            GL11.glVertex2f(rect.x + rect.width, rect.y + rect.height)

            GL11.glVertex2f(rect.x, rect.y + rect.height)
            GL11.glVertex2f(rect.x, rect.y + rect.height)

            GL11.glVertex2f(rect.x, rect.y)
        }

        /**
         * Draw a virtual rectangle as a series of lines.
         *
         * Must be called from within GL_LINES.
         */
        fun rectV(rect: Rectangle2D.Float, view: LetterBoxView) {
            rect(view.projectVirtualRect(rect))
        }
    }
}