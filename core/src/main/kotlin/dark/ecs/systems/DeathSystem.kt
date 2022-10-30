package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import createBlob
import dark.ecs.components.Blob
import dark.ecs.components.LogComponent
import dark.ecs.components.PropsAndStuff
import eater.ecs.ashley.components.Remove
import eater.physics.addComponent
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.log.info

class DeathSystem: IteratingSystem(allOf(PropsAndStuff::class).exclude(Remove::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if(PropsAndStuff.has(entity) && !LogComponent.has(entity)) {
            val health = PropsAndStuff.get(entity).getHealth()
            if(health.current <= 0f) {
                entity.addComponent<Remove>()
                if(Blob.has(entity)) {
                    BlobGrouper.removeBlob(entity)
                    if (BlobGrouper.blobCount < 10) {
                        info { "Add a new, too few left" }
                        createBlob(BlobGrouper.blobPoints.random())
                    }
                }
            }
        }

    }
}