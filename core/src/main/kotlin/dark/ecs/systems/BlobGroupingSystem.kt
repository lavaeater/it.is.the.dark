package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import dark.core.GameSettings
import dark.ecs.components.Blob
import eater.ecs.ashley.components.Box2d
import ktx.ashley.allOf

class BlobGroupingSystem(private val gameSettings: GameSettings) :
    IntervalIteratingSystem(allOf(Blob::class).get(), 0.015f) {

    private val blobFam = allOf(Blob::class).get()
    private val allBlobs get() = engine.getEntitiesFor(blobFam)

    val toRemoveList = mutableListOf<Int>()
    override fun updateInterval() {
        for ((key, list) in BlobGrouper.blobGroups) {
            if (list.isEmpty())
                toRemoveList.add(key)
        }
        for (key in toRemoveList)
            BlobGrouper.removeBlobGroup(key)
        toRemoveList.clear()
        super.updateInterval()
    }

    override fun processEntity(entity: Entity) {
        val blob = Blob.get(entity)
        blob.switchGroupCoolDown -= interval
        if (blob.blobGroup == -1 || blob.switchGroupCoolDown <= 0f) {
            blob.switchGroupCoolDown = 2.5f
            val position = Box2d.get(entity).body.position
            if (blob.blobGroup == -1) {
                val closeBlobs =
                    (allBlobs - entity).filter { Box2d.get(it).body.position.dst(position) < gameSettings.BlobDetectionRadius }
                if (closeBlobs.any()) {
                    val groups = closeBlobs.groupBy { Blob.get(it).blobGroup }
                    val combinedGroup = if (groups.keys.any { it != -1 })
                        BlobGrouper.addBlobsToGroup(groups.keys.filter { it != -1 }.random(), entity)
                    else
                        BlobGrouper.addBlobsToNewGroup(entity)

                    for ((key, blobs) in groups) {
                        if (key == -1) {
                            BlobGrouper.addBlobsToGroup(combinedGroup, *blobs.toTypedArray())
                        } else if(key != combinedGroup) {
                            BlobGrouper.addBlobsToGroup(
                                combinedGroup,
                                *BlobGrouper.getBlobsForGroup(key).toTypedArray()
                            )
                        }
                    }
                }
            } else {
                if (position.dst(BlobGrouper.getGroupCenter(blob.blobGroup)) > gameSettings.BlobForgettingRadius * 2f) {
                    BlobGrouper.removeBlobsFromGroup(blob.blobGroup, entity)
                }

//            val closeBlobs =
//                (allBlobs - entity).filter { Box2d.get(it).body.position.dst(position) < gameSettings.BlobDetectionRadius }
//            if (closeBlobs.any()) {
//                for ((key, blobs) in closeBlobs.groupBy { Blob.get(it).blobGroup }) {
//                    if (key == -1) {
//                        BlobGrouper.addBlobsToGroup(blob.blobGroup, *blobs.toTypedArray())
//                    } else if (key != blob.blobGroup) {
//                        BlobGrouper.addBlobsToGroup(blob.blobGroup, *BlobGrouper.getBlobsForGroup(key).toTypedArray())
//                    }
//                }
//            }

//            if(blob.blobGroup % 2 == 0) {

//            } else {
//                if((BlobGrouper.getBlobsForGroup(blob.blobGroup) - entity).count {
//                        Box2d.get(it).body.position.dst(position) < gameSettings.BlobForgettingRadius / 5f
//                    } < BlobGrouper.numberOfBlobsInGroup(blob.blobGroup) / 2) {
//                    BlobGrouper.removeBlobFromGroup(blob.blobGroup, entity)
//                }
//            }
            }
        }
    }
}