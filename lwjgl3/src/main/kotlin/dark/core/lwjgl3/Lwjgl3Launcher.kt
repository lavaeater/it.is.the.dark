@file:JvmName("Lwjgl3Launcher")

package dark.core.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import dark.core.DarkGame

/** Launches the desktop (LWJGL3) application. */
fun main() {
    Lwjgl3Application(DarkGame(), Lwjgl3ApplicationConfiguration().apply {
        disableAudio(true)
        setTitle("ItIsTheDark")
        setWindowedMode(640, 480)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
    })
}
