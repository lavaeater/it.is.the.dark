package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import dark.ecs.components.Blob
import dark.ecs.components.BlobMessage
import dark.ecs.components.PropsAndStuff
import ktx.ashley.allOf

class BlobHealthSharingSystem : IntervalIteratingSystem(allOf(Blob::class).get(), 3f) {
    override fun processEntity(entity: Entity) {
        val blob = Blob.get(entity)
        if (blob.neighbours.any()) {
            val health = PropsAndStuff.get(entity).getHealth()
            if (health.current > health.max * 1.5f) {
                val healthToShare = health.current - health.max
                health.current -= healthToShare
                blob.sendMessageToNeighbours(
                    BlobMessage.TakeSomeOfMyHealth(
                        healthToShare / blob.neighbours.count(),
                        entity
                    )
                )
            }
        }
    }
}