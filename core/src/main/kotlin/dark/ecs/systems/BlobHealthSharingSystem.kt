package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import dark.ecs.components.Blob
import dark.ecs.components.BlobMessage
import dark.ecs.components.PropsAndStuff
import ktx.ashley.allOf

class BlobHealthSharingSystem: IteratingSystem(allOf(Blob::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val blob = Blob.get(entity)
        if(blob.blobGroup != -1) {
            val health = PropsAndStuff.get(entity).getHealth()
            if(health.current > health.max) {
                val healthToShare = health.current - health.max
                health.current -= healthToShare

                // Share some health with the rest of the group, will ya!
                BlobGrouper.sendMessageToGroup(
                    blob.blobGroup,
                    BlobMessage.TakeSomeOfMyHealth(
                        healthToShare / BlobGrouper.numberOfBlobsInGroup(blob.blobGroup),
                        entity
                    )
                )
            }
        }
    }

}