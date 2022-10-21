package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import dark.ecs.components.Blob
import dark.core.GameSettings
import eater.ecs.ashley.components.Box2d
import ktx.ashley.allOf

class BlobGroupingSystem(private val gameSettings: GameSettings) :
    IntervalIteratingSystem(allOf(Blob::class).get(), 0.0025f) {

    val blobFam = allOf(Blob::class).get()
    val allBlobs get() = engine.getEntitiesFor(blobFam)
    override fun processEntity(entity: Entity) {
        val thisBlobComponent = Blob.get(entity)
        val position = Box2d.get(entity).body.position

        thisBlobComponent.neigbours.remove(entity) //basically a no-op
        //remove neighbours that are too far away
        val distantNeighbours =
            thisBlobComponent.neigbours.filter { Box2d.get(it).body.position.dst(position) > gameSettings.BlobDetectionRadius }
        thisBlobComponent.neigbours.removeAll(distantNeighbours.toSet())

        //Check all blobs except this one and neighbours to see if they are close
        val nearestBlobs =
            (allBlobs - entity - thisBlobComponent.neigbours).filter { Box2d.get(it).body.position.dst(position) < gameSettings.BlobDetectionRadius }
        // No neighbours, no new neighbours, this blob is lonely, indicate this
        if(thisBlobComponent.neigbours.isEmpty() && nearestBlobs.isEmpty() && thisBlobComponent.blobGroup != -1) {
            BlobGrouper.removeBlobFromGroup(thisBlobComponent.blobGroup, entity)
            return
        }

        for (nearestBlob in nearestBlobs) {
            val firstBlob = entity
            val secondBlob = nearestBlob

            val firstBlobC = thisBlobComponent
            val secondBlobC = Blob.get(secondBlob)

            firstBlobC.neigbours.add(secondBlob)
            secondBlobC.neigbours.add(firstBlob)

            if (firstBlobC.blobGroup == -1 && secondBlobC.blobGroup == -1) {
                /*
                No group exists for these blobs, add it!
                 */
                BlobGrouper.addBlobsToNewGroup(firstBlob, secondBlob)
            } else if (firstBlobC.blobGroup != -1 && secondBlobC.blobGroup != -1 && firstBlobC.blobGroup != secondBlobC.blobGroup) {
                /**
                 * first swallows second
                 */
                val blobsInSecond = BlobGrouper.getBlobsForGroup(secondBlobC.blobGroup)
                BlobGrouper.removeBlobGroup(secondBlobC.blobGroup)
                BlobGrouper.addBlobsToGroup(firstBlobC.blobGroup, *blobsInSecond.toTypedArray())
            } else if (firstBlobC.blobGroup == -1 || secondBlobC.blobGroup == -1) {
                val blobGroupThatExists =
                    if (firstBlobC.blobGroup != -1) firstBlobC.blobGroup else secondBlobC.blobGroup
                val blobThatShouldBeAdded = if (firstBlobC.blobGroup != -1) firstBlob else secondBlob
                BlobGrouper.addBlobsToGroup(blobGroupThatExists, blobThatShouldBeAdded)
            }
        }
    }
}