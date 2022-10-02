package dark.injection

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import dark.core.GameSettings
import eater.injection.InjectionContext
import ktx.assets.disposeSafely
import ktx.box2d.createWorld

object Context : InjectionContext() {
    private val shapeDrawerRegion: TextureRegion by lazy {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.drawPixel(0, 0)
        val texture = Texture(pixmap) //remember to dispose of later
        pixmap.disposeSafely()
        TextureRegion(texture, 0, 0, 1, 1)
    }

    fun initialize() {
        buildContext {
            bindSingleton(GameSettings())
            bindSingleton(getFleksWorld())
            bindSingleton(createWorld())
        }
    }

    private fun getFleksWorld(): World {
        return world {
            
        }
    }
}