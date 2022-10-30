package dark.ecs.components

import com.aliasifkhan.hackLights.HackLight
import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2

class Flashlight: Component, Pool.Poolable {
    lateinit var light: HackLight
    var offset = 0.5f
    val direction = Vector2.X.cpy()
    var on = true
    override fun reset() {
        offset = 1f
        direction.set(Vector2.X)
        on = true
    }

    companion object {
        val mapper = mapperFor<Flashlight>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): Flashlight {
            return mapper.get(entity)
        }
    }
}