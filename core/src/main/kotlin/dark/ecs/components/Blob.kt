package dark.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Queue
import dark.ecs.systems.log
import ktx.ashley.mapperFor
import ktx.log.info


class Blob: Component, Pool.Poolable {
    var blobGroup = -1
    var color = Color.GREEN
    private val messageQueue = Queue<BlobMessage>()
    val messageCount get() = messageQueue.size
    val neighbours = mutableMapOf<Entity, Vector2>()

    fun sendMessageToNeighbours(message: BlobMessage) {
        neighbours.filter { !has(it.key) }.forEach { neighbours.remove(it.key) }
        neighbours.forEach { get(it.key).receiveMessage(message) }
    }

    fun receiveMessage(message: BlobMessage) {
        messageQueue.addLast(message)
    }

    fun getOldestMessage():BlobMessage? {
        return if(messageQueue.notEmpty()) messageQueue.removeFirst() else null
    }

    fun peekOldestMessage():BlobMessage? {
        return messageQueue.firstOrNull()
    }

    override fun reset() {
        neighbours.clear()
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