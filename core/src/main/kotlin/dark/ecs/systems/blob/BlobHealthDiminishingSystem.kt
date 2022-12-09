package dark.ecs.systems.blob

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import dark.core.GameSettings
import dark.ecs.components.blobcomponents.Blob
import eater.ecs.ashley.components.PropsAndStuff
import ktx.ashley.allOf

class BlobHealthDiminishingSystem(private val gameSettings: GameSettings): IntervalIteratingSystem(
    allOf(
        Blob::class,
        PropsAndStuff::class
    ).get(), 1f) {
    override fun processEntity(entity: Entity) {
        val props = PropsAndStuff.get(entity)
        props.getHealth().current -= gameSettings.BlobHealthReductionPerSecond
    }

}