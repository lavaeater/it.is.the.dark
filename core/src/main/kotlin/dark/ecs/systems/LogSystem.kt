package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import dark.ecs.components.LogComponent
import dark.ecs.components.PropsAndStuff
import eater.ai.ashley.AiComponent
import ktx.ashley.allOf
import ktx.log.info

fun Entity.log(message: String) {
    if(LogComponent.has(this)) {
        info { message }
    }
}

class LogSystem: IntervalIteratingSystem(allOf(LogComponent::class).get(), 5f) {
    override fun processEntity(entity: Entity) {
        LogComponent.get(entity).logFunction(entity)
    }
}