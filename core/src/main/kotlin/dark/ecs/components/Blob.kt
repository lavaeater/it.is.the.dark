package dark.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import java.util.Vector


sealed class BlobMessage(val sender: Entity, val blobGroup: Int) {
    class FoundSomeFood(val at: Vector2, sender: Entity, blobGroup: Int): BlobMessage(sender, blobGroup)
}


class Blob: Component, Pool.Poolable {
    var blobGroup = -1
    var color = Color.GREEN
    override fun reset() {
        blobGroup = -1
    }

    companion object {
        val mapper = mapperFor<Blob>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): Blob {
            return mapper.get(entity)
        }
    }
}