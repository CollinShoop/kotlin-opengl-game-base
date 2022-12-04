package com.cms.engine.lwjgl.text

import com.cms.engine.lwjgl.util.GLHelpers
import com.cms.engine.lwjgl.view.LetterBoxView
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color
import java.awt.geom.Rectangle2D
import java.nio.ByteBuffer

data class GLText(val text: String, val view: LetterBoxView, val textVertexBuffer: ByteBuffer, val numQuads: Int, val bufferWidth: Float, val bufferHeight: Float) {

    private var debug = false
    private var color = Color.black
    private var clampX = Clamp.LEFT
    private var clampY = Clamp.TOP

    companion object {
        // Scalar for font size adjustment, eg make increases and decreases in font size less dramatic.
        // The lower this scale, the smaller the text will appear at the same font size
        const val FONT_SIZE_GRANULARITY = 0.25f
    }

    fun color(c: Color): GLText {
        this.color = c
        return this
    }

    fun debug(): GLText {
        this.debug = true
        return this
    }

    fun clamp(x: Clamp?, y: Clamp?): GLText {
        if (x != null) {
            clampX = x
        }
        if (y != null) {
            clampY = y
        }
        return this
    }

    fun renderFixedWidth(targetWidth: Float, x: Float, y: Float): Rectangle2D.Float {
//        val fontSize = width / (text.length * FONT_VIRTUAL_SCALE_X * FONT_SIZE_GRANULARITY)
        val fontSize = (1f/ FONT_SIZE_GRANULARITY) * (targetWidth / this.bufferWidth)
        return render(fontSize, x, y)
    }

    fun render(fontSize: Float, x: Float, y: Float): Rectangle2D.Float {
        val fontScale = FONT_SIZE_GRANULARITY * fontSize
        val w = bufferWidth * fontScale
        val h = bufferHeight * fontScale
        val bounds = Rectangle2D.Float(x + clampX.xShift * w, y + clampY.yShift * h, w, h)

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY)
            GL11.glPushMatrix()
                // Offset box margins
                GL11.glTranslatef(view.getProjectionMarginX(), view.getProjectionMarginY(), 0f)
                // Zoom to virtual scale
                GL11.glScalef(view.virtualScaleX, view.virtualScaleY, 1f)
                // Place text upper-left corner in virtual space
                GL11.glTranslatef(bounds.x, bounds.y, 0f)
                // Scale font
                GL11.glScalef(fontScale, fontScale, 1f)

                // point to the vertex buffer with quad data
                GL11.glVertexPointer(2, GL11.GL_FLOAT, 16, textVertexBuffer)

                // draw quads from vertex buffer with color
                glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
                GL11.glDrawArrays(GL11.GL_QUADS, 0, numQuads * 4)
            GL11.glPopMatrix()
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY)

        if (debug) {
            // draw a bounding box around the text
            GL11.glBegin(GL11.GL_LINES)
            GLHelpers.rectV(bounds, view)
            GL11.glEnd()
        }

        return bounds
    }

    /**
     * Describes how text is placed with respect to the render location.
     */
    enum class Clamp(val xShift: Float, val yShift: Float) {
        /**
         * Render location is on the left side of the text (Default)
         */
        LEFT(0f, 0f),

        /**
         * Render location is at the center of the text
         */
        CENTER(-0.5f, -0.5f),

        /**
         * Render location is on the right side of the text
         */
        RIGHT(-1f, 0f),

        /**
         * Render locaation is at the top of the text (Default)
         */
        TOP(0f, 0f),

        /**
         * Render location is at the bottom of the text
         */
        BOTTOM(0f, -1f)
    }

}