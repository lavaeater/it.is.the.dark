package dark.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Queue
import ktx.ashley.mapperFor


class Blob: Component, Pool.Poolable {
    var blobGroup = -1
    var color = Color.GREEN
    val messageQueue = Queue<BlobMessage>()
    var switchGroupCoolDown = 2.5f
    override fun reset() {
        messageQueue.clear()
        blobGroup = -1
        switchGroupCoolDown = 2.5f
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