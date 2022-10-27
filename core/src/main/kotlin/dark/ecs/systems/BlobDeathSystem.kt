package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import dark.ecs.components.Blob
import dark.ecs.components.LogComponent
import dark.ecs.components.PropsAndStuff
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.Remove
import eater.physics.addComponent
import ktx.ashley.allOf

class BlobDeathSystem: IteratingSystem(allOf(Blob::class, PropsAndStuff::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if(Box2d.has(entity) && PropsAndStuff.has(entity) && !LogComponent.has(entity)) {
            val health = PropsAndStuff.get(entity).getHealth()
            if(health.current <= 0f) {
                entity.addComponent<Remove>()
            }
        }

    }
}