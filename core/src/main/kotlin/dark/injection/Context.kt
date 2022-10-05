package dark.injection

import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import dark.core.DarkGame
import dark.core.GameSettings
import dark.ecs.components.DarkMonster
import eater.ecs.fleks.systems.Box2dDebugRenderSystem
import eater.ecs.fleks.systems.Box2dUpdateSystem
import dark.screens.GameScreen
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

    fun initialize(game: DarkGame) {
        buildContext {
            val gameSettings = GameSettings()
            bindSingleton(gameSettings)
            bindSingleton(game)
            bindSingleton(PolygonSpriteBatch())
            bindSingleton(OrthographicCamera())
            bindSingleton(
                ExtendViewport(
                    gameSettings.GameWidth,
                    gameSettings.GameHeight,
                    inject<OrthographicCamera>() as Camera
                )
            )
            bindSingleton(createWorld())
            bindSingleton(getFleksWorld(gameSettings))
            bindSingleton(Assets())
            bindSingleton(GameScreen(
                inject(),
                inject(),
                inject(),
                inject(),
                inject()
            ))
        }
    }

    private fun getFleksWorld(gameSettings: GameSettings): World {
        return world {

            components {
                onRemove(DarkMonster, DarkMonster.onRemove)
            }

            systems {
                add(Box2dUpdateSystem(gameSettings.TimeStep, gameSettings.VelIters, gameSettings.PosIters))
                add(Box2dDebugRenderSystem(inject(), inject()))
            }
        }
    }
}

