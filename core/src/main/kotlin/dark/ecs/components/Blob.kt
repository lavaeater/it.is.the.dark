package dark.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import java.util.Vector


sealed class BlobMessage(val sender: Entity) {
    class FoundSomeFood(val at: Vector2, sender: Entity): BlobMessage(sender)
}


class Blob: Component, Pool.Poolable {
    var blobGroup = -1
    val neigbours = mutableListOf<Entity>()
    override fun reset() {
        blobGroup = -1
        neigbours.clear()
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

fun <T> Sequence<T>.selectRecursive(recursiveSelector: T.() -> Sequence<T>): Sequence<T> = flatMap {
    sequence {
        yield(it)
        yieldAll(it.recursiveSelector().selectRecursive(recursiveSelector))
    }
}