package dark.injection

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import dark.core.GameSettings
import eater.injection.InjectionContext
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
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
            bindSingleton(Assets())
        }
    }

    private fun getFleksWorld(): World {
        return world {

        }
    }
}

sealed class AnimDef(val name: String, val rowIndex: Int, val numberOfFrames: Int) {
    object Walk: AnimDef("walk", 3, 4)
    object Interact: AnimDef("interact", 1, 3)

    companion object {
        val animDefs = listOf(Walk, Interact)
    }
}

class Assets: DisposableRegistry by DisposableContainer() {
    val buddy: Map<AnimDef, Animation<TextureRegion>> by lazy {
        val texture = Texture("player/buddy.png".toInternalFile()).alsoRegister()
        AnimDef.animDefs.associateWith { ad ->
            Animation(0.1f, *Array(ad.numberOfFrames) { x ->
                TextureRegion(texture, x * 32, ad.rowIndex * 32, 32, 32)
            })
        }
    }
    override fun dispose() {
        registeredDisposables.disposeSafely()
    }
}