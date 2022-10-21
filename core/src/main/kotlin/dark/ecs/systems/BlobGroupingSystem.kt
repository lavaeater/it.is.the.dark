package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.ashley.systems.IntervalSystem
import dark.ecs.components.Blob
import dark.core.GameSettings
import eater.ecs.ashley.components.Box2d
import ktx.ashley.allOf

class BlobGroupingSystem(private val gameSettings: GameSettings) :
    IntervalIteratingSystem(allOf(Blob::class).get(),0.0025f) {

    private val blobFam = allOf(Blob::class).get()
    private val allBlobs get() = engine.getEntitiesFor(blobFam)

    override fun processEntity(entity: Entity) {
        val blob = Blob.get(entity)
        val position = Box2d.get(entity).body.position
        if (blob.blobGroup == -1) {
            val closeBlobs =
                (allBlobs - entity).filter { Box2d.get(it).body.position.dst(position) < gameSettings.BlobDetectionRadius }
            if (closeBlobs.any()) {
                val groupIds = closeBlobs.map { Blob.get(it).blobGroup }
                if (groupIds.any { it != -1 }) {
                    BlobGrouper.addBlobsToGroup(groupIds.first { it != -1 }, entity, *closeBlobs.toTypedArray())
                } else {
                    BlobGrouper.addBlobsToNewGroup(entity, *closeBlobs.toTypedArray())
                }
            }
        } else {
            val closeBlobs =
                (allBlobs - entity).filter { Box2d.get(it).body.position.dst(position) < gameSettings.BlobDetectionRadius }
            if (closeBlobs.any()) {
                BlobGrouper.addBlobsToGroup(blob.blobGroup, entity, *closeBlobs.toTypedArray())
            }

            val groupBlobs = BlobGrouper.getBlobsForGroup(blob.blobGroup) - entity
            if(groupBlobs.isEmpty() || groupBlobs.all { Box2d.get(it).body.position.dst(position) > gameSettings.BlobForgettingRadius }) {
                BlobGrouper.removeBlobFromGroup(blob.blobGroup, entity)
            }
        }
    }
}