package dark.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class Light: Component, Pool.Poolable {
    var color = Color(1f, 1f, 0f, 0.5f)
    var radius = 15f
    override fun reset() {
        radius = 15f
        color = Color(1f, 1f, 0f, 0.5f)
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