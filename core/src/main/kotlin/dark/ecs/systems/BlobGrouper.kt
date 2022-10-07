package dark.ecs.systems

import com.badlogic.ashley.core.Entity

object BlobGrouper {
    val canSplit: Boolean
        get() {
            return blobGroups.sumOf { it.count() } < 100
        }
    val blobGroups = mutableListOf(mutableListOf<Entity>())
}