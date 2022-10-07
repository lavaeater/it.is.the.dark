package dark.ecs.systems

import Blob
import com.badlogic.ashley.systems.IntervalSystem
import eater.ecs.ashley.components.Box2d
import ktx.ashley.allOf

class BlobGroupingSystem : IntervalSystem(0.1f) {
    val blobFamily = allOf(Blob::class, Box2d::class).get()
    val coherenceDistance = 10f
    override fun updateInterval() {
        BlobGrouper.blobGroups.clear()
        BlobGrouper.blobGroups.add(mutableListOf())
        for ((index, blob) in engine.getEntitiesFor(blobFamily).withIndex()) {
            if(index == 0) {
                BlobGrouper.blobGroups.first().add(blob)
            } else {
                var needsList = true
                for(blobList in BlobGrouper.blobGroups) {
                    val currentBlobPosition = Box2d.get(blob).body.position
                    if(needsList && blobList.any { Box2d.get(it).body.position.dst(currentBlobPosition) < coherenceDistance }) {
                        blobList.add(blob)
                        needsList = false
                    }
                }
                if(needsList) {
                    BlobGrouper.blobGroups.add(mutableListOf(blob))
                }
            }
        }
    }
}