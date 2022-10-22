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
}

abstract class StackedAiAction<T:Any>(name: String, private val state:T):StackAiAction(name) {
    abstract fun actFunction(entity: Entity, state: T, deltaTime: Float): Boolean

    override fun abort(entity: Entity) {
        info { "Aborted $name" }
        abortFunction(entity)
    }

    override fun act(entity: Entity, deltaTime: Float): Boolean {
        return actFunction(entity, state, deltaTime)
    }
}

class StackAiComponent : Component, Pool.Poolable {
    val actionStack = Queue<StackAiAction>()
    override fun reset() {
        actionStack.clear()
    }

    companion object {
        val mapper = mapperFor<StackAiComponent>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }

        fun get(entity: Entity): StackAiComponent {
            return mapper.get(entity)
        }
    }
}