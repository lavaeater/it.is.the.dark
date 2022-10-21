package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import dark.ecs.components.Blob
import dark.ecs.components.BlobMessage
import eater.ecs.ashley.components.Box2d
import ktx.math.div
import ktx.math.vec2

object BlobGrouper {
    fun getRandomNumber(): Float {
        return (0..10).random() / 10f
    }
    fun addBlobsToNewGroup(vararg blobs: Entity) : Int {
        val newGroupId = blobGroupIds
        blobGroups[newGroupId] = mutableSetOf()
        groupColors[newGroupId] = Color(getRandomNumber(), getRandomNumber(), getRandomNumber(), 1f)
        for (blob in blobs) {
            Blob.get(blob).blobGroup = newGroupId
            blobGroups[newGroupId]!!.add(blob)
        }
        return newGroupId
    }

    fun getGroupCenter(group: Int): Vector2 {
        val blobs = getBlobsForGroup(group)
        return if(blobs.isEmpty()) vec2() else blobs.map { Box2d.get(it).body.position }.reduce { acc, p -> acc.add(p) }.div(blobs.count())
    }

    fun numberOfBlobsInGroup(group: Int): Int {
        return if (blobGroups.containsKey(group))
            blobGroups[group]!!.count()
        else
            0
    }

    fun removeBlobFromGroup(group: Int, blob: Entity) {
        val b = Blob.get(blob) //Cannot have neighbours no more?
        if (blobGroups.containsKey(group) && b.blobGroup == group) {
            blobGroups[group]!!.remove(blob)
            b.blobGroup = -1
            if (blobGroups[group]!!.isEmpty())
                removeBlobGroup(group)
        }

    }

    fun removeBlobGroup(group: Int) {
        if (blobGroups.containsKey(group)) {
            for (e in blobGroups[group]!!) {
                Blob.get(e).blobGroup = -1
            }
            blobGroups.remove(group)
            groupColors.remove(group)
        }
    }

    fun getGroupColor(group: Int): Color {
        return if(groupColors.containsKey(group)) groupColors[group]!! else Color.GREEN
    }

    fun addBlobsToGroup(group: Int, vararg blobs: Entity) {
        if (blobGroups.containsKey(group)) {
            for (e in blobs) {
                val blob = Blob.get(e)
                val oldGroup = blob.blobGroup
                if(oldGroup != group && blobGroups.containsKey(oldGroup)) {
                    blobGroups[oldGroup]!!.remove(e)
                    if(blobGroups[oldGroup]!!.isEmpty()) {
                        blobGroups.remove(oldGroup)
                        groupColors.remove(oldGroup)
                    }
                }
                if(blobGroups[group]?.add(e) == true) {
                    blob.blobGroup = group
                } else {
                    blob.blobGroup = -1
                }
            }
        }
    }

    fun getBlobsForGroup(group: Int): List<Entity> {
        return if (blobGroups.containsKey(group)) blobGroups[group]!!.toList() else emptyList()
    }

    fun sendMessageToGroup(group: Int, message: BlobMessage) {
        if (blobGroups.containsKey(group)) {
            for (blob in (getBlobsForGroup(group) - message.sender)) {
                Blob.get(blob).messageQueue.addLast(message)
            }
        }
    }

    var blobGroupIds = 0
        private set
        get() {
            val id = field
            field++
            return id
        }
    val canSplit: Boolean
        get() {
            return blobGroups.values.sumOf { it.count() } < 200
        }
    val blobGroups = mutableMapOf<Int, MutableSet<Entity>>()
    val groupColors = mutableMapOf<Int, Color>()

}