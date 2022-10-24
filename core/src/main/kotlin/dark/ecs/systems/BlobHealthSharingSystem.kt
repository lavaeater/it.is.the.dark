package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import dark.ecs.components.Blob
import dark.ecs.components.BlobMessage
import dark.ecs.components.PropsAndStuff
import ktx.ashley.allOf

class BlobHealthSharingSystem : IteratingSystem(allOf(Blob::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val blob = Blob.get(entity)
        if (blob.neighbours.any()) {
            val health = PropsAndStuff.get(entity).getHealth()
            if (health.current > health.max) {
                val healthToShare = health.current - health.max
                health.current -= healthToShare
                blob.neighbours.sendMessageTo(
                    BlobMessage.TakeSomeOfMyHealth(
                        healthToShare / blob.neighbours.count(),
                        entity
                    )
                )
            }
        }
    }
}