package com.brendigo.systivra.data

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20

/**
 * Creates a temporary OpenGL ES2 pbuffer and reads only strings supplied by
 * that driver. Never claims access to global GPU utilization or temperatures.
 * Called once and cached by DeviceMetrics, off the main UI thread.
 */
internal object GpuProbe {
    fun read(): List<Reading> {
        val source = "OpenGL ES2 driver (temporary EGL context)"
        val results = runCatching { readDriverStrings() }.getOrNull()
        return listOf(
            Reading("GPU renderer", results?.first, source),
            Reading("GPU vendor", results?.second, source),
            Reading("OpenGL ES version", results?.third, source),
            Reading("GPU utilization", null, "Not accessible through the standard Android API"),
            Reading("GPU temperature", null, "Vendor specific / restricted")
        )
    }

    private fun readDriverStrings(): Triple<String?, String?, String?>? {
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display == EGL14.EGL_NO_DISPLAY) return null
        var context: EGLContext = EGL14.EGL_NO_CONTEXT
        var surface: EGLSurface = EGL14.EGL_NO_SURFACE
        try {
            val versions = IntArray(2)
            if (!EGL14.eglInitialize(display, versions, 0, versions, 1)) return null
            val attrs = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val count = IntArray(1)
            if (!EGL14.eglChooseConfig(display, attrs, 0, configs, 0, 1, count, 0) ||
                count[0] == 0 || configs[0] == null) return null

            val cfg = configs[0] ?: return null
            context = EGL14.eglCreateContext(
                display, cfg, EGL14.EGL_NO_CONTEXT,
                intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE), 0
            )
            if (context == EGL14.EGL_NO_CONTEXT) return null

            surface = EGL14.eglCreatePbufferSurface(
                display, cfg,
                intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE), 0
            )
            if (surface == EGL14.EGL_NO_SURFACE) return null
            if (!EGL14.eglMakeCurrent(display, surface, surface, context)) return null

            return Triple(
                GLES20.glGetString(GLES20.GL_RENDERER)?.takeIf(String::isNotBlank),
                GLES20.glGetString(GLES20.GL_VENDOR)?.takeIf(String::isNotBlank),
                GLES20.glGetString(GLES20.GL_VERSION)?.takeIf(String::isNotBlank)
            )
        } finally {
            EGL14.eglMakeCurrent(
                display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT
            )
            if (surface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(display, surface)
            if (context != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(display, context)
            EGL14.eglTerminate(display)
        }
    }
}
