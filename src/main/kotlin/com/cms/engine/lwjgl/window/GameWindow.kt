package com.cms.engine.lwjgl.window

import com.cms.engine.lwjgl.text.GLText
import com.cms.engine.lwjgl.text.TextRenderer
import com.cms.engine.lwjgl.util.GLHelpers
import com.cms.engine.lwjgl.view.LetterBoxView
import com.cms.engine.input.InputHandler
import com.cms.engine.input.QueuedInputHandler
import com.cms.engine.input.event.KeyEvent
import com.cms.engine.input.event.MouseButtonEvent
import com.cms.engine.input.event.MouseMovedEvent
import com.cms.engine.input.event.MouseScrollEvent
import com.cms.engine.lwjgl.sprite.Sprite
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN
import org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP
import org.lwjgl.glfw.GLFW.GLFW_RELEASE
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI
import org.lwjgl.glfw.GLFWWindowSizeCallbackI
import org.lwjgl.opengl.ARBFramebufferObject.GL_COLOR_ATTACHMENT0
import org.lwjgl.opengl.ARBFramebufferObject.GL_DEPTH24_STENCIL8
import org.lwjgl.opengl.ARBFramebufferObject.GL_DEPTH_ATTACHMENT
import org.lwjgl.opengl.ARBFramebufferObject.GL_FRAMEBUFFER
import org.lwjgl.opengl.ARBFramebufferObject.GL_FRAMEBUFFER_COMPLETE
import org.lwjgl.opengl.ARBFramebufferObject.GL_MAX_SAMPLES
import org.lwjgl.opengl.ARBFramebufferObject.GL_READ_FRAMEBUFFER
import org.lwjgl.opengl.ARBFramebufferObject.GL_RENDERBUFFER
import org.lwjgl.opengl.ARBFramebufferObject.glBindFramebuffer
import org.lwjgl.opengl.ARBFramebufferObject.glBindRenderbuffer
import org.lwjgl.opengl.ARBFramebufferObject.glBlitFramebuffer
import org.lwjgl.opengl.ARBFramebufferObject.glCheckFramebufferStatus
import org.lwjgl.opengl.ARBFramebufferObject.glDeleteFramebuffers
import org.lwjgl.opengl.ARBFramebufferObject.glDeleteRenderbuffers
import org.lwjgl.opengl.ARBFramebufferObject.glFramebufferRenderbuffer
import org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers
import org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers
import org.lwjgl.opengl.ARBFramebufferObject.glRenderbufferStorageMultisample
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT
import org.lwjgl.opengl.GL11.GL_LINES
import org.lwjgl.opengl.GL11.GL_NEAREST
import org.lwjgl.opengl.GL11.GL_ONE
import org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_PROJECTION
import org.lwjgl.opengl.GL11.GL_RGBA8
import org.lwjgl.opengl.GL11.glBegin
import org.lwjgl.opengl.GL11.glBlendFunc
import org.lwjgl.opengl.GL11.glClear
import org.lwjgl.opengl.GL11.glClearColor
import org.lwjgl.opengl.GL11.glColor3d
import org.lwjgl.opengl.GL11.glColor4d
import org.lwjgl.opengl.GL11.glColor4f
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL11.glEnd
import org.lwjgl.opengl.GL11.glGetInteger
import org.lwjgl.opengl.GL11.glLoadIdentity
import org.lwjgl.opengl.GL11.glMatrixMode
import org.lwjgl.opengl.GL11.glOrtho
import org.lwjgl.opengl.GL11.glRectf
import org.lwjgl.opengl.GL11.glVertex2f
import org.lwjgl.opengl.GL11.glViewport
import org.lwjgl.opengl.GLUtil
import org.lwjgl.system.Callback
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.Color
import java.awt.geom.Rectangle2D

data class GameWindow(
    val graphicWidth: Float = 1920f,
    val graphicHeight: Float = 1080f,
    val windowWidth: Int = 500,
    val windowHeight: Int = 500,
    val windowTitle: String
) {

    var debug: Boolean = false
    
    // Window
    private var windowId: Long = 0
    private var openglDebugCallback: Callback? = null

    // Mouse / Keyboard Inputs
    private val inputs = GameInputHandler()
    private val glQueuedInputsHandler = QueuedInputHandler(inputs)

    // Graphics Helpers
    private val boxView = LetterBoxView(this.graphicWidth, this.graphicHeight)
    private val textRenderer = TextRenderer(boxView)

    // Temprary assets to be removed
    private val textCursor = Sprite("red.png")
    private val textBg = Sprite("background.png")

    // OpenGL Antialiasing
    // See https://github.com/LWJGL/lwjgl3-demos/blob/main/src/org/lwjgl/demo/opengl/fbo/MultisampledFboDemo.java
    private var antialiasingEnabled = true
    private var colorRenderBuffer = 0
    private var depthRenderBuffer = 0
    private var fboId = 0
    private var antialiasSampling = 8
    private var resetFbo = false

    // debug framerate
    private var frameId = 0

    fun run() {
        try {
            init()
            loop()
        } finally {
            try {
                destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun init() {
        println("Initializing graphics...")
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE) // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE) // the window will be resizable
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1)

        // Create the window
        windowId = GLFW.glfwCreateWindow(windowWidth, windowHeight, windowTitle, MemoryUtil.NULL, MemoryUtil.NULL)
        if (windowId == MemoryUtil.NULL) throw RuntimeException("Failed to create the GLFW window")

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        GLFW.glfwSetCursorPosCallback(windowId) { _, xpos, ypos ->
            val virtualPos = boxView.projectScreenPointToVirtual(xpos.toFloat(), ypos.toFloat())
            debug("glfwSetCursorPosCallback: xRaw=${xpos}; yRaw=${ypos}; xGraphic=${virtualPos.x}; yGraphic=${virtualPos.y};")
            glQueuedInputsHandler.mouseMoved(MouseMovedEvent(xpos, ypos, virtualPos.x, virtualPos.y))
        }
        GLFW.glfwSetMouseButtonCallback(windowId) { _, button, action, mods ->
            debug("glfwSetMouseButtonCallback: button=${button}; action=${action}; mods=${mods};")
            glQueuedInputsHandler.mouseButton(MouseButtonEvent(button, action, mods))
        }
        GLFW.glfwSetKeyCallback(windowId) { window: Long, key: Int, scancode: Int, action: Int, mods: Int ->
            debug("glfwSetKeyCallback: key=${key}; scancode=${scancode}; action=${action}; mods=${mods};")
            glQueuedInputsHandler.key(KeyEvent(key, scancode, action, mods))
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) GLFW.glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
        }
        GLFW.glfwSetScrollCallback(windowId) { _, xoffset, yoffset ->
            glQueuedInputsHandler.scroll(MouseScrollEvent(xoffset, yoffset))
            debug("glfwSetScrollCallback: xoffset=${xoffset}; yoffset=${yoffset};")
        }
        GLFW.glfwSetWindowRefreshCallback(windowId) { window: Long -> render() }
        GLFW.glfwSetWindowSizeCallback(windowId) { window: Long, width: Int, height: Int ->
            debug("glfwSetWindowSizeCallback: width=${width}; height=${height};")
            windowSizeChanged(width, height)
        }
        GLFW.glfwSetFramebufferSizeCallback(windowId) { window, width, height ->
            debug("glfwSetFramebufferSizeCallback: width=${width}; height=${height};")
            framebufferSizeChanged(width, height)
        }
        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            GLFW.glfwGetWindowSize(windowId, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())

            // Center the window
            GLFW.glfwSetWindowPos(
                windowId,
                (vidmode!!.width() - pWidth[0]) / 2,
                (vidmode.height() - pHeight[0]) / 2
            )
        }

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(windowId)

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities()

        debugCapabilities()

        // Keep track of debug callback
        openglDebugCallback = GLUtil.setupDebugMessageCallback()

        // Enable v-sync
        GLFW.glfwSwapInterval(1)

        // Make the window visible
        GLFW.glfwShowWindow(windowId)

        glfwInvoke(
            windowId,
            { window: Long, width: Int, height: Int ->
                windowSizeChanged(
                    width,
                    height
                )
            }) { window: Long, width: Int, height: Int ->
            framebufferSizeChanged(
                width,
                height
            )
        }
    }

    class GameInputHandler : InputHandler {
        var mouseX = 0f
            private set
        var mouseY = 0f
            private set
        var mouseRealX = 0f
            private set
        var mouseRealY = 0f
            private set
        var fontSize = 12f

        override fun mouseMoved(e: MouseMovedEvent) {
            this.mouseX = e.vx
            this.mouseY = e.vy
            this.mouseRealX = e.rx.toFloat()
            this.mouseRealY = e.ry.toFloat()
        }

        override fun mouseButton(e: MouseButtonEvent) {
        }

        override fun key(e: KeyEvent) {
            if (e.action == GLFW_RELEASE) {
                return
            }

            if (e.key == GLFW_KEY_PAGE_UP) {
                fontSize++
            } else if (e.key == GLFW_KEY_PAGE_DOWN) {
                fontSize--;
            }
        }

        override fun scroll(e: MouseScrollEvent) {

        }

    }

    private fun debug(msg: String) {
        if (!debug) {
            return
        }
        println("DEBUG GameWindow ${System.currentTimeMillis()} ${msg}")
    }

    private fun windowSizeChanged(width: Int, height: Int) {
        resetFbo = true
        boxView.setBufferSize(width.toFloat(), height.toFloat())
    }

    private fun framebufferSizeChanged(width: Int, height: Int) {
        resetFbo = true
        boxView.setBufferSize(width.toFloat(), height.toFloat())
    }

    private fun debugCapabilities() {
        if(GL.getCapabilities().OpenGL20) {
            println("OpenGL 20 is supported")
        }
        if(GL.getCapabilities().OpenGL30) {
            println("OpenGL 30 is supported")
        }
        if(GL.getCapabilities().OpenGL33) {
            println("OpenGL 33 is supported")
        }
        if(GL.getCapabilities().OpenGL40) {
            println("OpenGL 40 is supported")
        }
        if(GL.getCapabilities().OpenGL46) {
            println("OpenGL 46 is supported")
        }
    }

    /**
     * Invokes the specified callbacks using the current window and framebuffer sizes of the specified GLFW window.
     *
     * @param window            the GLFW window
     * @param windowSizeCB      the window size callback, may be null
     * @param framebufferSizeCB the framebuffer size callback, may be null
     */
    fun glfwInvoke(
        window: Long,
        windowSizeCB: GLFWWindowSizeCallbackI?,
        framebufferSizeCB: GLFWFramebufferSizeCallbackI?
    ) {
        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            if (windowSizeCB != null) {
                GLFW.glfwGetWindowSize(window, w, h)
                windowSizeCB.invoke(window, w[0], h[0])
            }
            if (framebufferSizeCB != null) {
                GLFW.glfwGetFramebufferSize(window, w, h)
                framebufferSizeCB.invoke(window, w[0], h[0])
            }
        }
    }

    fun createFramebufferObject() {
        if (!antialiasingEnabled) {
            return
        }
        val width: Int = boxView.bufferWidth.toInt()
        val height: Int = boxView.bufferHeight.toInt()
        colorRenderBuffer = glGenRenderbuffers()
        depthRenderBuffer = glGenRenderbuffers()
        fboId = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, fboId)
        glBindRenderbuffer(GL_RENDERBUFFER, colorRenderBuffer)
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, antialiasSampling, GL_RGBA8, width, height)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorRenderBuffer)
        glBindRenderbuffer(GL_RENDERBUFFER, depthRenderBuffer)
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, antialiasSampling, GL_DEPTH24_STENCIL8, width, height)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRenderBuffer)
        val fboStatus: Int = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        if (fboStatus != GL_FRAMEBUFFER_COMPLETE) {
            throw AssertionError("Could not create FBO: $fboStatus")
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun resizeFramebufferTexture() {
        if (!antialiasingEnabled) {
            return
        }
        glDeleteRenderbuffers(depthRenderBuffer)
        glDeleteRenderbuffers(colorRenderBuffer)
        glDeleteFramebuffers(fboId)
        createFramebufferObject()
    }

    private fun loop() {
        // prepare drawing

        // load resources
        textCursor.load()
        textBg.load()

        glClearColor(0f, 0f, 0f, 0f)

        // max sampling debug
        antialiasSampling = glGetInteger(GL_MAX_SAMPLES);
        System.err.println("Using " + antialiasSampling + "x multisampling")

        /* Initially create the FBO with color texture and renderbuffer */
        createFramebufferObject()

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!GLFW.glfwWindowShouldClose(windowId)) {
            if (resetFbo) {
                resizeFramebufferTexture()
                resetFbo = false
            }

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            GLFW.glfwPollEvents()
            glQueuedInputsHandler.flush()

            render()
        }

        // cleanup
        textCursor.cleanup()
        textBg.cleanup()
    }

    private fun render() {
        // render to custom frame buffer
        if (antialiasingEnabled) {
            glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        }

        // clear the framebuffer
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // enable blending (transparency)
        glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)

        // unit top-down orthographic projection
        glMatrixMode(GL_PROJECTION)
        glViewport(0, 0, boxView.bufferWidth.toInt(), boxView.bufferHeight.toInt())
        glLoadIdentity()
        glOrtho(0.0, 1.0, 1.0, 0.0, -1.0, 1.0)

        // draw letter box background
        val marginX = boxView.getProjectionMarginX()
        val marginY = boxView.getProjectionMarginY()
        glColorBackground()
        glRectf(marginX, marginY, 1 - marginX, 1 - marginY)

        // draw background texture
//        textBg.render(boxView.projectVirtualRect(Rectangle2D.Float(0f, 0f, graphicWidth, graphicHeight)))

        // highlight the grid cell with the mouse hovered
        val gridSize = (graphicWidth / 32).toInt()
        val highlightGridX = (inputs.mouseX / gridSize).toInt() * gridSize
        val highlightGridY = (inputs.mouseY / gridSize).toInt() * gridSize
        // draw grid highlight only if it's within virtual view
        if (highlightGridX >= 0 && highlightGridX < graphicWidth && highlightGridY >= 0 && highlightGridY < graphicHeight) {
            glColorBackgroundHighlight()
            glRectV(highlightGridX, highlightGridY, gridSize, gridSize)
        }

        // background grid
        glColorGridLines()
        glBegin(GL_LINES)
            for (x in gridSize..graphicWidth.toInt()-gridSize step gridSize) {
                glVertexV(x, 0)
                glVertexV(x, graphicHeight)
            }
            for (y in gridSize..graphicHeight.toInt()-gridSize step gridSize) {
                glVertexV(0, y)
                glVertexV(graphicWidth, y)
            }
        glEnd()

        // center and mouse lines
        glBegin(GL_LINES)
            glColorLines()
            glVertexV(0f, 0f)
            glVertexV(graphicWidth / 2, graphicHeight / 2)

            // draw line to mouse in view space
            glVertexS(0f, 0f)
            glVertexS(inputs.mouseRealX, inputs.mouseRealY)

            // draw a line from the origin to mouse location in virtual space
            glVertexV(0f, 0f)
            glVertexV(inputs.mouseX, inputs.mouseY)

            // draw a box around the mouse where the cursor should go
            val cursorBox = boxView.projectVirtualRect(
                Rectangle2D.Float(
                    inputs.mouseX - textCursor.w,
                    inputs.mouseY - textCursor.h,
                    textCursor.w * 2,
                    textCursor.h * 2
                )
            )
            GLHelpers.rect(cursorBox)
        glEnd()

        // draw cursor texture
        textCursor.render(cursorBox)

        // debug frame id
        val boundDynamicSize = textRenderer.mono("Dynamic: ${inputs.fontSize}\nSecond Line\nThird Line\nFourth Line")
            .color(Color.red)
            .debug()
            .render(inputs.fontSize, inputs.mouseX, inputs.mouseY)
        textRenderer.mono("Fixed width(700): Right-Center anchored")
            .color(Color.red)
            .clamp(GLText.Clamp.RIGHT, GLText.Clamp.CENTER)
            .debug()
            .renderFixedWidth(700f, boundDynamicSize.x, boundDynamicSize.y + boundDynamicSize.height/2)

        val boundsRight = textRenderer.mono("Dynamic: Clamp Right Bottom ${inputs.fontSize}")
            .color(Color.red)
            .clamp(GLText.Clamp.RIGHT, GLText.Clamp.BOTTOM)
            .debug()
            .render(inputs.fontSize, inputs.mouseX, inputs.mouseY)

        textRenderer.mono("Fixed width(500): Center-bottom anchored")
            .color(Color.red)
            .clamp(GLText.Clamp.CENTER, GLText.Clamp.BOTTOM)
            .debug()
            .renderFixedWidth(500f, boundsRight.x + boundsRight.width / 2, boundsRight.y)
//
//        val bounds2 = textRenderer.mono("Hello Fixed Width ${inputs.fontSize*20}")
//            .color(Color.red.darker())
//            .debug()
//            .renderFixedWidth(inputs.fontSize*20, boundDynamicSize.x, boundDynamicSize.y + boundDynamicSize.height)
//
//        textRenderer.mono("Hello Fixed Width 2 ${inputs.fontSize*15}")
//            .color(Color.red.darker().darker())
//            .clamp(null, GLText.Clamp.CENTER)
//            .debug()
//            .renderFixedWidth(inputs.fontSize*15, bounds2.x + bounds2.width, bounds2.y)

        // final render
        if (antialiasingEnabled) {
            glClearColor()
            // write to default frame buffer
            glBindFramebuffer(GL_FRAMEBUFFER, 0)
            // setup blit to read from custom frame buffer
            glBindFramebuffer(GL_READ_FRAMEBUFFER, fboId)

            val width: Int = boxView.bufferWidth.toInt()
            val height: Int = boxView.bufferHeight.toInt()
            glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST)
        }

        // show the latest drawing
        GLFW.glfwSwapBuffers(windowId)
    }

    /*
        COLORS
     */
    private fun glColorBackground() {
        glColor3d(0.0, 1.0, 0.0)
    }
    private fun glColorBackgroundHighlight() {
        glColor3d(0.3, 0.5, 0.3)
    }
    private fun glColorLines() {
        glColor4d(1.0, 1.0, 1.0, 0.5)
    }
    private fun glColorGridLines() {
        glColor4d(0.0, 0.0, 0.0, 0.5)
    }
    private fun glTextColor() {
        glColor4d(0.0, 0.0, 1.0, 1.0)
    }
    private fun glClearColor() {
        glColor4f(0f, 0f, 0f, 0f)
    }


    private fun glRectV(x: Number, y: Number, width: Number, height: Number) {
        val mapped = boxView.projectVirtualPoint(x.toFloat(), y.toFloat())
        glRectf(
            mapped.x,
            mapped.y,
            mapped.x + width.toFloat() * boxView.virtualScaleX,
            mapped.y + height.toFloat() * boxView.virtualScaleY
        )
    }

    private fun glVertexV(x: Number, y: Number) {
        val mapped = boxView.projectVirtualPoint(x.toFloat(), y.toFloat())
        glVertex2f(mapped.x, mapped.y)
    }

    private fun glVertexS(x: Float, y: Float) {
        val mapped = boxView.projectScreenPoint(x, y)
        glVertex2f(mapped.x, mapped.y)
    }

    private fun destroy() {
        GL.setCapabilities(null)
        if (openglDebugCallback != null) {
            openglDebugCallback!!.free()
        }
        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(windowId)
        GLFW.glfwDestroyWindow(windowId)

        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null)!!.free()
    }
}