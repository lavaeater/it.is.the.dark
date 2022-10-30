package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import dark.ecs.components.Light
import eater.ecs.ashley.components.Remove
import eater.ecs.ashley.components.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.exclude

class LightPositionUpdateSystem: IteratingSystem(
    allOf(
        Light::class,
        TransformComponent::class
    ).exclude(Remove::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val light = Light.get(entity)
        val transform = TransformComponent.get(entity)
        light.hackLight.setOriginBasedPosition(transform.position.x, transform.position.y)
    }
}