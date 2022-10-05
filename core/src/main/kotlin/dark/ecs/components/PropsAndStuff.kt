package dark.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import kotlin.reflect.KClass

class PropsAndStuff: Component, Pool.Poolable {
    val props = mutableListOf<Prop>()

    fun getHealth(): Prop.Health {
        return props.first { it is Prop.Health } as Prop.Health
    }

    override fun reset() {
        props.clear()
    }

    companion object {
        val mapper = mapperFor<PropsAndStuff>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): PropsAndStuff {
            return mapper.get(entity)
        }
    }
}