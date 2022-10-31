package dark.ecs.components.blobcomponents

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class FoundHuntingTarget: Component, Pool.Poolable {
    override fun reset() {

    }

    companion object {
        val mapper = mapperFor<FoundHuntingTarget>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): FoundHuntingTarget {
            return mapper.get(entity)
        }
    }
}