import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class Light: Component, Pool.Poolable {
    override fun reset() {

    }

    companion object {
        val mapper = mapperFor<Light>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): Light {
            return mapper.get(entity)
        }
    }
}