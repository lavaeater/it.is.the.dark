package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import dark.ecs.components.Blob
import dark.ecs.components.BlobMessage
import ktx.ashley.allOf


class BlobMessageHandlingSystem : IteratingSystem(allOf(Blob::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val blob = Blob.get(entity)
        if (blob.messageQueue.any()) {
            /**
             * All blobs are separate... kinda?
             *
             * So, a Blob finds something, experiences something, it sends a message
             * to all other blobs.
             *
             * Depending on their internal state (health etc), they shall make different decisions
             * depending on this information
             *
             * The information might be outdated... of course...
             *
             * Let's go with groups, still.
             *
             */
            when (val message = blob.messageQueue.removeFirst()) {
                is BlobMessage.FoundAFoodTarget -> {

                }
                is BlobMessage.TakeSomeOfMyHealth -> TODO()
            }


        }
    }
}