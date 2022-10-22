package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import dark.ecs.components.StackAi
import ktx.ashley.allOf

class StackAiSystem: IteratingSystem(allOf(StackAi::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val stack = StackAi.get(entity)
        if(stack.actionStack.any()) {
            val removeIfDone = stack.actionStack.count() > 1
            val currentAction = stack.actionStack.first()
            if(currentAction.act(entity, deltaTime) && removeIfDone) {
                stack.actionStack.removeValue(currentAction, true)
            }
        }
    }

}