package dark.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Queue
import ktx.ashley.mapperFor
import ktx.log.info

abstract class StackAiAction(val name: String) {
    abstract fun abortFunction(entity: Entity)
    abstract fun act(entity: Entity, deltaTime: Float): Boolean
    abstract fun abort(entity: Entity)
    abstract fun pause()
}

abstract class StackedAiAction<T:Any>(name: String, private var state:T):StackAiAction(name) {
    abstract fun actFunction(entity: Entity, state: T, deltaTime: Float): Boolean

    abstract fun pauseFunction(): T

    fun updateState(newState: T) {
        state = newState
    }

    override fun pause() {
        state = pauseFunction()
    }

    override fun abort(entity: Entity) {
        abortFunction(entity)
    }

    override fun act(entity: Entity, deltaTime: Float): Boolean {
        return actFunction(entity, state, deltaTime)
    }
}

class StackAiComponent : Component, Pool.Poolable {
    fun addNewActionToTheTop(action: StackAiAction) {
        if(actionStack.any()) {
            actionStack.first().pause()
        }
        actionStack.addFirst(action)
    }

    val actionStack = Queue<StackAiAction>()
    override fun reset() {
        actionStack.clear()
    }
}