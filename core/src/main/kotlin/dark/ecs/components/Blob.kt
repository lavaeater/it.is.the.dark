package dark.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Queue
import ktx.ashley.mapperFor


sealed class BlobMessage(val sender: Entity) {
    class FoundAFoodTarget(val target: Entity, sender: Entity): BlobMessage(sender)
}


class Blob: Component, Pool.Poolable {
    var blobGroup = -1
    var color = Color.GREEN
    val messageQueue = Queue<BlobMessage>()
    override fun reset() {
        messageQueue.clear()
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