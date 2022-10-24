package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import dark.core.GameSettings
import dark.ecs.components.Blob
import dark.ecs.components.BlobMessage
import eater.ecs.ashley.components.Box2d
import ktx.ashley.allOf

class BlobNeighbourSystem(private val gameSettings: GameSettings) :
    IntervalIteratingSystem(allOf(Blob::class, Box2d::class).get(), 0.015f) {

    private val blobFam = allOf(Blob::class, Box2d::class).get()
    private val allBlobs get() = engine.getEntitiesFor(blobFam)

    val toRemoveList = mutableListOf<Int>()
    override fun updateInterval() {
        super.updateInterval()
    }

    override fun processEntity(entity: Entity) {
        val blob = Blob.get(entity)
        val position = Box2d.get(entity).body.position
        val distantNeighbours = blob.neighbours.filter { Box2d.get(it).body.position.dst(position) > gameSettings.BlobForgettingRadius }
        blob.neighbours.removeAll(distantNeighbours.toSet())

        val closeBlobs = allBlobs.filter { Box2d.get(it).body.position.dst(position) < gameSettings.BlobDetectionRadius }

        blob.neighbours.addAll(closeBlobs.toSet())
    }
}

fun Set<Entity>.sendMessageTo(message: BlobMessage) {
    for (e in this) {
        if(Blob.has(e)) {
            Blob.get(e).messageQueue.addLast(message)
        }
    }
}