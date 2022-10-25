package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import dark.ecs.components.LogComponent
import dark.ecs.components.PropsAndStuff
import eater.ai.ashley.AiComponent
import ktx.ashley.allOf
import ktx.log.info

class LogSystem: IntervalIteratingSystem(allOf(LogComponent::class, AiComponent::class).get(), 5f) {
    override fun processEntity(entity: Entity) {
        val aiComponent = AiComponent.get(entity)
        info { aiComponent.actions.joinToString { "${it.score} - ${it.name}\n" } }
        info { "Health: ${PropsAndStuff.get(entity).getHealth().current}" }
    }
}