package dark.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.utils.Pool
import eater.core.world
import ktx.ashley.mapperFor
import ktx.math.vec2

class Map: Component, Pool.Poolable {
    lateinit var mapBounds: Rectangle
    val mapOrigin = vec2()
    var mapScale = 1f
    lateinit var mapTextureRegion: TextureRegion
    val mapBodies = mutableListOf<Body>()
    val validPoints = mutableListOf<Vector2>()

    override fun reset() {
        for(body in mapBodies) {
            world().destroyBody(body)
        }
        mapBodies.clear()
        validPoints.clear()
        mapOrigin.setZero()
        mapScale = 1f
        mapTextureRegion = TextureRegion()
        mapBounds = Rectangle()
    }

    companion object {
        val mapper = mapperFor<Map>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): Map {
            return mapper.get(entity)
        }
    }
}