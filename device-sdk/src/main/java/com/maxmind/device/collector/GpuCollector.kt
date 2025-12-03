package com.maxmind.device.collector

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import com.maxmind.device.model.GpuInfo

/**
 * Collects GPU information using OpenGL ES.
 *
 * Creates a temporary EGL context to query GPU capabilities without
 * requiring a visible surface or window.
 */
internal class GpuCollector {
    /**
     * Collects GPU information.
     *
     * @return [GpuInfo] containing GPU details, or null if unavailable
     */
    @Suppress("LongMethod", "ReturnCount", "CyclomaticComplexMethod")
    fun collect(): GpuInfo? {
        var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
        var context: EGLContext = EGL14.EGL_NO_CONTEXT
        var surface: EGLSurface = EGL14.EGL_NO_SURFACE

        return try {
            // Initialize EGL display
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (display == EGL14.EGL_NO_DISPLAY) {
                return null
            }

            val version = IntArray(2)
            if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
                return null
            }

            // Choose an EGL config
            val configAttribs =
                intArrayOf(
                    EGL14.EGL_RENDERABLE_TYPE,
                    EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_SURFACE_TYPE,
                    EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_RED_SIZE,
                    8,
                    EGL14.EGL_GREEN_SIZE,
                    8,
                    EGL14.EGL_BLUE_SIZE,
                    8,
                    EGL14.EGL_NONE,
                )

            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            if (!EGL14.eglChooseConfig(
                    display,
                    configAttribs,
                    0,
                    configs,
                    0,
                    1,
                    numConfigs,
                    0,
                )
            ) {
                return null
            }

            val config = configs[0] ?: return null

            // Create a PBuffer surface (offscreen)
            val surfaceAttribs =
                intArrayOf(
                    EGL14.EGL_WIDTH,
                    1,
                    EGL14.EGL_HEIGHT,
                    1,
                    EGL14.EGL_NONE,
                )

            surface = EGL14.eglCreatePbufferSurface(display, config, surfaceAttribs, 0)
            if (surface == EGL14.EGL_NO_SURFACE) {
                return null
            }

            // Create an OpenGL ES 2.0 context
            val contextAttribs =
                intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION,
                    2,
                    EGL14.EGL_NONE,
                )

            context =
                EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
            if (context == EGL14.EGL_NO_CONTEXT) {
                return null
            }

            // Make context current
            if (!EGL14.eglMakeCurrent(display, surface, surface, context)) {
                return null
            }

            // Query GPU information
            val renderer = GLES20.glGetString(GLES20.GL_RENDERER)
            val vendor = GLES20.glGetString(GLES20.GL_VENDOR)
            val glVersion = GLES20.glGetString(GLES20.GL_VERSION)
            val extensionsString = GLES20.glGetString(GLES20.GL_EXTENSIONS)

            val extensions = extensionsString?.split(" ")?.filter { it.isNotBlank() }

            GpuInfo(
                renderer = renderer,
                vendor = vendor,
                version = glVersion,
                extensions = extensions,
            )
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception,
        ) {
            // OpenGL ES may not be available on some devices or emulators
            null
        } finally {
            // Clean up EGL resources
            if (display != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(
                    display,
                    EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT,
                )
                if (context != EGL14.EGL_NO_CONTEXT) {
                    EGL14.eglDestroyContext(display, context)
                }
                if (surface != EGL14.EGL_NO_SURFACE) {
                    EGL14.eglDestroySurface(display, surface)
                }
                EGL14.eglTerminate(display)
            }
        }
    }
}
