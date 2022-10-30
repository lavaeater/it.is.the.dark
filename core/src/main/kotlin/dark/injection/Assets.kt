package dark.injection

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import eater.injection.InjectionContext.Companion.inject
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile

fun assets(): Assets {
    return inject()
}

class Assets : DisposableRegistry by DisposableContainer() {
    val buddy: Map<AnimDef, Animation<TextureRegion>> by lazy {
        val texture = Texture("player/buddy.png".toInternalFile()).alsoRegister()
        AnimDef.animDefs.associateWith { ad ->
            Animation(0.1f, *Array(ad.numberOfFrames) { x ->
                TextureRegion(texture, x * 32, ad.rowIndex * 32, 32, 32)
            })
        }
    }

    private val lightTexture = Texture("lights/light-textures.png".toInternalFile())
    val lights = Array(6) {
        val bla = it
        val x = 128 * (bla % 3)
        val y = 128 * (bla / 3)
        TextureRegion(lightTexture, x, y, 128, 128)
    }

    private val mapOne = Texture("maps/level-1/simplified/Level_0/_composite.png".toInternalFile())
    private val mapOneIntLayer = "maps/level-1/simplified/Level_0/IntGrid.csv".toInternalFile().readString()

    private val mapTwo = Texture("maps/new_level/simplified/Level_0/_composite.png".toInternalFile())
    private val mapTwoIntLayer = "maps/new_level/simplified/Level_0/IntGrid.csv".toInternalFile().readString()

    val maps = mapOf("one" to Pair(mapOne, mapOneIntLayer), "two" to Pair(mapTwo, mapTwoIntLayer))
    override fun dispose() {
        registeredDisposables.disposeSafely()
    }
}