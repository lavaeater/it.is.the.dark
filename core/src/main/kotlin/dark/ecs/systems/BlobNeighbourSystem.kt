package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.math.Vector2
import dark.core.GameSettings
import dark.ecs.components.Blob
import dark.ecs.components.BlobMessage
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.Remove
import ktx.ashley.allOf

class BlobNeighbourSystem(private val gameSettings: GameSettings) :
    IntervalIteratingSystem(allOf(Blob::class, Box2d::class).exclude(Remove::class.java).get(), 0.05f) {

    private val blobFam = allOf(Blob::class, Box2d::class).exclude(Remove::class.java).get()
    lateinit var allBlobs: List<Pair<Vector2, Entity>>
    override fun updateInterval() {
        allBlobs = engine.getEntitiesFor(blobFam).map { Box2d.get(it).body.position to it }
        super.updateInterval()
    }

    override fun processEntity(entity: Entity) {
        val blob = Blob.get(entity)
        val position = Box2d.get(entity).body.position
        val distantNeighbours = blob.neighbours.filter { Box2d.get(it).body.position.dst(position) > gameSettings.BlobForgettingRadius }
        blob.neighbours.removeAll(distantNeighbours)

        val closeBlobs = allBlobs.filter { it.first.dst(position) < gameSettings.BlobDetectionRadius }.map { it.second }

        blob.neighbours.addAll(closeBlobs)
    }
}

fun Set<Entity>.sendMessageTo(message: BlobMessage) {
    for (e in this) {
        if(Blob.has(e)) {
            Blob.get(e).messageQueue.addLast(message)
        }
    }
}

fun List<Entity>.sendMessageTo(message: BlobMessage) {
    for (e in this) {
        if(Blob.has(e)) {
            Blob.get(e).messageQueue.addLast(message)
        }
    }
}