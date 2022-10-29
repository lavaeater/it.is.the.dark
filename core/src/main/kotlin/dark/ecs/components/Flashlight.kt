package dark.ecs.components

import com.aliasifkhan.hackLights.HackLight
import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2

class Flashlight: Component, Pool.Poolable {
    lateinit var light: HackLight
    var offset = 1f
    val direction = vec2()
    override fun reset() {
        offset = 1f
        direction.setZero()
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