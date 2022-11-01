import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class InRangeForShootingTarget: Component, Pool.Poolable {
    var target: Entity? = null
    override fun reset() {
        target = null
    }

    companion object {
        val mapper = mapperFor<InRangeForShootingTarget>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): InRangeForShootingTarget {
            return mapper.get(entity)
        }
    }
}