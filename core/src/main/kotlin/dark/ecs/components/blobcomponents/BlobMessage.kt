package dark.ecs.components.blobcomponents

import com.badlogic.ashley.core.Entity

sealed class BlobMessage(val sender: Entity) {
    class FoundAFoodTarget(val target: Entity, val energy:Float, sender: Entity): BlobMessage(sender)
    class TakeSomeOfMyHealth(val healthToAdd: Float, sender: Entity): BlobMessage(sender)
}