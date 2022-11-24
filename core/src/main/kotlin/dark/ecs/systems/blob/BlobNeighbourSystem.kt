package dark.ecs.systems.blob

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.gdx.math.Vector2
import dark.core.GameSettings
import dark.ecs.components.blobcomponents.Blob
import eater.ecs.ashley.components.Remove
import eater.ecs.ashley.components.TransformComponent
import ktx.ashley.allOf

class BlobNeighbourSystem(private val gameSettings: GameSettings) :
    IntervalIteratingSystem(allOf(Blob::class, TransformComponent::class).exclude(Remove::class.java).get(), 0.02f) {

    private val blobFam = allOf(Blob::class, TransformComponent::class).exclude(Remove::class.java).get()
    lateinit var allBlobs: Map<Entity, Vector2>
    private var oldCount = -1
    private val newCount get() = BlobGrouper.blobCount
    override fun updateInterval() {
        /**
         * Optimize, we don't have to do this all the time, actually
         * only needed when the blobCount has gone up!
         */
        if (oldCount != newCount) {
            oldCount = newCount
            allBlobs = engine.getEntitiesFor(blobFam).associateWith { TransformComponent.get(it).position }
        }
        super.updateInterval()
    }

    override fun processEntity(entity: Entity) {
        val blob = Blob.get(entity)
        val position = TransformComponent.get(entity).position
        val distantNeighbours = getDistantNeighbours(blob, position)
        distantNeighbours.forEach { blob.neighbours.remove(it) }
        val closeBlobs = getCloseBlobs(position)
        closeBlobs.forEach { blob.neighbours[it.key] = it.value }
    }

    private fun getCloseBlobs(position: Vector2) =
        allBlobs.filter { it.value.dst2(position) < (gameSettings.BlobDetectionRadius * gameSettings.BlobDetectionRadius) }

    private fun getDistantNeighbours(
        blob: Blob,
        position: Vector2
    ) =
        blob.neighbours.filter { it.value.dst2(position) > gameSettings.BlobForgettingRadius * gameSettings.BlobForgettingRadius }.keys
}
