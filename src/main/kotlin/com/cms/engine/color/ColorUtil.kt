package com.cms.engine.color

import java.awt.Color

class ColorUtil {

    companion object {
        fun brighten(color: Color): Color {
            return brighten(color, 0.15)
        }
        fun brighten(color: Color, fraction: Double): Color {
            val red = Math.round(Math.min(255.0, color.red + 255 * fraction)).toInt()
            val green = Math.round(Math.min(255.0, color.green + 255 * fraction)).toInt()
            val blue = Math.round(Math.min(255.0, color.blue + 255 * fraction)).toInt()
            val alpha = color.alpha
            return Color(red, green, blue, alpha)
        }
        fun darken(color: Color): Color {
            return darken(color, 0.15)
        }
        fun darken(color: Color, fraction: Double): Color {
            val red = Math.round(Math.min(255.0, color.red - 255 * fraction)).toInt()
            val green = Math.round(Math.min(255.0, color.green - 255 * fraction)).toInt()
            val blue = Math.round(Math.min(255.0, color.blue - 255 * fraction)).toInt()
            val alpha = color.alpha
            return Color(red, green, blue, alpha)
        }




    }

}