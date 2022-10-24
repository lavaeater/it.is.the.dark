package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import dark.core.GameSettings
import dark.ecs.components.Blob
import dark.ecs.components.PropsAndStuff
import ktx.ashley.allOf
import ktx.log.info

class BlobHealthDiminishingSystem(private val gameSettings: GameSettings): IntervalIteratingSystem(
    allOf(
        Blob::class,
        PropsAndStuff::class
    ).get(), 1f) {
    override fun processEntity(entity: Entity) {
        val props = PropsAndStuff.get(entity)
        props.getHealth().current -= gameSettings.BlobHealthReductionPerSecond
        info { "Current Health: ${props.getHealth().current}" }
    }

}