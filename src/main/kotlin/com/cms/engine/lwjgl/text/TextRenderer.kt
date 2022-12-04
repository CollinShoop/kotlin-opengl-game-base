package com.cms.engine.lwjgl.text

import com.cms.engine.lwjgl.view.LetterBoxView
import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBEasyFont

class TextRenderer(val view: LetterBoxView) {

    fun mono(text: String): GLText {
        // render a buffer to store vertex data, ~300 bytes per letter according to stb_easy_font_print
        val textVertexBuffer = BufferUtils.createByteBuffer(text.length * 300)
        // fills the buffer with quad vertex data to draw the string
        val numQuads = STBEasyFont.stb_easy_font_print(
            0f,
            0f,
            text,
            null,
            textVertexBuffer
        )
        val width = STBEasyFont.stb_easy_font_width(text).toFloat()
        val height = STBEasyFont.stb_easy_font_height(text).toFloat()
        return GLText(text, view, textVertexBuffer, numQuads, width, height)
    }

}