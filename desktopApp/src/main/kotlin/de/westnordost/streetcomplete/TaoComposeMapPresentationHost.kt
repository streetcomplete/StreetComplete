package de.westnordost.streetcomplete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.nucleusframework.window.tao.TaoGpuRenderContext
import dev.nucleusframework.window.tao.TaoMetalRenderContext
import dev.nucleusframework.window.tao.TaoOpenGlRenderContext
import dev.nucleusframework.window.tao.rememberTaoGpuRenderContext
import org.maplibre.compose.desktop.ComposeMapPresentationHost
import org.maplibre.compose.desktop.MetalComposeGpuContext
import org.maplibre.compose.desktop.OpenGlComposeGpuContext
import org.maplibre.compose.mlnffi.NativeHandle
import org.maplibre.compose.util.ExperimentalMaplibreComposeApi

/** The [ComposeMapPresentationHost] for the current Nucleus Tao window, or null until it has a GPU
 *  context.
 *
 *  Tao replaces the render context when it rebuilds a window's graphics stack, so the host is
 *  recreated along with it.
 *
 *  Copied from maplibre-compose's demo-app/desktop-nucleus, keep in sync when updating
 *  maplibre-compose. See https://github.com/maplibre/maplibre-compose/issues/1802 */
@Composable
fun rememberTaoComposeMapPresentationHost(): ComposeMapPresentationHost? {
    val renderContext = rememberTaoGpuRenderContext() ?: return null
    return remember(renderContext) { taoComposeMapPresentationHost(renderContext) }
}

@OptIn(ExperimentalMaplibreComposeApi::class)
private fun taoComposeMapPresentationHost(
    renderContext: TaoGpuRenderContext
): ComposeMapPresentationHost {
    val runOnGpuThread: (Runnable) -> Unit = { action ->
        renderContext.runOnGpuThread { action.run() }
    }
    return when (renderContext) {
        is TaoMetalRenderContext ->
            ComposeMapPresentationHost.macosMetal(
                description = "the Nucleus Tao Metal host",
                gpuContext = renderContext.metalGpuContext(),
                runOnGpuThread = runOnGpuThread,
            )
        is TaoOpenGlRenderContext if isWindows ->
            ComposeMapPresentationHost.windowsAngle(
                description = OPEN_GL_DESCRIPTION,
                gpuContext = renderContext.openGlGpuContext(),
                runOnGpuThread = runOnGpuThread,
            )
        is TaoOpenGlRenderContext ->
            ComposeMapPresentationHost.linuxOpenGl(
                description = OPEN_GL_DESCRIPTION,
                gpuContext = renderContext.openGlGpuContext(),
                runOnGpuThread = runOnGpuThread,
            )
        // Tao has a private intermediate type, public callers only get the two above
        else -> error("Unsupported Tao GPU context: ${renderContext::class.simpleName}")
    }
}

@OptIn(ExperimentalMaplibreComposeApi::class)
private fun TaoMetalRenderContext.metalGpuContext(): () -> MetalComposeGpuContext = {
    MetalComposeGpuContext(skiaContext, NativeHandle(metalDevicePtr))
}

@OptIn(ExperimentalMaplibreComposeApi::class)
private fun TaoOpenGlRenderContext.openGlGpuContext(): () -> OpenGlComposeGpuContext = {
    OpenGlComposeGpuContext(
        skiaContext = skiaContext,
        // Tao restores the previous context and invalidates Skia's GL cache afterwards
        withContextCurrent = { action ->
            checkNotNull(withContextCurrent { action.run() }) {
                "$OPEN_GL_DESCRIPTION could not make the GL context current"
            }
        },
    )
}

private const val OPEN_GL_DESCRIPTION = "the Nucleus Tao OpenGL host"

private val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
