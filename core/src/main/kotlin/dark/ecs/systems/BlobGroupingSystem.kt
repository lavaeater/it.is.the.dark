package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import dark.ecs.components.Blob
import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.ashley.systems.IteratingSystem
import dark.core.GameSettings
import eater.ecs.ashley.components.Box2d
import ktx.ashley.allOf

class BlobGroupingSystem(private val gameSettings: GameSettings) : IntervalIteratingSystem(allOf(Blob::class).get(), 0.1f) {
    override fun processEntity(entity: Entity) {
        val blob = Blob.get(entity)
        val position = Box2d.get(entity).body.position
        blob.neigbours.remove(entity)
        val solitary = blob.neigbours.map { Box2d.get(it).body.position.dst(position) }.all { it > gameSettings.BlobDetectionRadius }
        if(solitary) {
            BlobGrouper.removeBlobFromGroup(blob.blobGroup, entity)
        }
    }
}