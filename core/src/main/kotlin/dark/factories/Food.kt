import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class Food: Component, Pool.Poolable {
    var foodEnergy = (10..20).random() * 10f
    override fun reset() {
        foodEnergy = (10..20).random() * 10f
    }

    companion object {
        val mapper = mapperFor<Food>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): Food {
            return mapper.get(entity)
        }
    }
}