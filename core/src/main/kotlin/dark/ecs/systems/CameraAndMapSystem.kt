package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.ExtendViewport
import dark.core.GameSettings
import eater.ecs.ashley.systems.CameraFollowSystem
import ktx.ashley.allOf
import dark.ecs.components.Map
import eater.ecs.ashley.components.TransformComponent
import eater.injection.InjectionContext.Companion.inject
import ktx.math.vec3

class CameraAndMapSystem(camera: OrthographicCamera, alpha: Float, private val extendViewport: ExtendViewport, private val useMapBounds: Boolean = false) :
    CameraFollowSystem(camera, alpha) {
    private val mapFamily = allOf(Map::class).get()
    private val mapEntity get() = engine.getEntitiesFor(mapFamily).firstOrNull()
    private val gameSettings by lazy { inject<GameSettings>() }

    override fun processEntity(entity: Entity, deltaTime: Float) {


        if (useMapBounds && mapEntity != null) {
            val map = Map.get(mapEntity!!)
            camera.position.set(map.mapBounds.getCenter(cameraPosition), 0f)
            extendViewport.minWorldWidth =
                if (map.mapBounds.width > map.mapBounds.height) map.mapBounds.width * 1.25f else extendViewport.minWorldHeight / gameSettings.AspectRatio
            extendViewport.minWorldHeight =
                if (map.mapBounds.height > map.mapBounds.width) map.mapBounds.height * 1.25f else extendViewport.minWorldWidth / gameSettings.AspectRatio
            extendViewport.update(Gdx.graphics.width, Gdx.graphics.height)
        } else {
            val position = TransformComponent.get(entity).position
            cameraPosition.set(position)
        }

        camera.position.lerp(
            vec3(cameraPosition, 0f), alpha
        )

        camera.update(true)
    }

}