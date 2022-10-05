package dark.injection

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.ExtendViewport
import dark.core.DarkGame
import dark.core.GameSettings
import dark.ecs.systems.BodyControlSystem
import dark.screens.GameScreen
import eater.ecs.ashley.systems.*
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
            bindSingleton(getEngine(gameSettings))
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

    private fun getEngine(gameSettings: GameSettings): Engine {
        return Engine().apply {
            addSystem(CameraFollowSystem(inject(), 0.75f))
            addSystem(Box2dUpdateSystem(gameSettings.TimeStep, gameSettings.VelIters, gameSettings.PosIters))
            addSystem(Box2dDebugRenderSystem(inject(), inject()))
            addSystem(BodyControlSystem())
            addSystem(UpdateActionsSystem())
            addSystem(AshleyAiSystem())
        }
    }
}

