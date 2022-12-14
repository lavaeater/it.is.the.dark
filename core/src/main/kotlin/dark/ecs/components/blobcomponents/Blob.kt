package dark.ecs.components.blobcomponents

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Queue
import eater.core.engine
import eater.core.world
import eater.ecs.ashley.components.Box2d
import ktx.ashley.mapperFor
import ktx.ashley.remove


class Blob : Component, Pool.Poolable {
    val ropes = mutableListOf<SlimeRope>()
    var blobGroup = -1
    var color = Color.GREEN
    var radius = 3f
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

    fun getOldestMessage(): BlobMessage? {
        return if (messageQueue.notEmpty()) messageQueue.removeFirst() else null
    }

    fun peekOldestMessage(): BlobMessage? {
        return messageQueue.firstOrNull()
    }

    override fun reset() {
        for (rope in ropes.filter { !it.destroyed }) {
            rope.destroy()
        }
        ropes.clear()
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