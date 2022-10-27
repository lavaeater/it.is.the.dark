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
        return (3..10).random() / 10f
    }

    val allBlobs = mutableSetOf<Entity>()
    fun addNewBlob(blob: Entity) {
        allBlobs.add(blob)
    }
    fun removeBlob(blob:Entity) {
        allBlobs.remove(blob)
    }

    val blobCount get() = allBlobs.count()
    val canSplit: Boolean
        get() {
            return blobCount < 100
        }

    fun addBlobsToNewGroup(vararg blobs: Entity) : Int {
        val newGroupId = blobGroupIds
        blobGroups[newGroupId] = mutableSetOf()
        if(newGroupId % 2 == 0)
            groupColors[newGroupId] = Color(getRandomNumber(), 0f, getRandomNumber(), 1f)
        else
            groupColors[newGroupId] = Color(0f, getRandomNumber(), getRandomNumber(), 1f)

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

    fun removeBlobsFromGroup(group: Int, vararg blobs: Entity) {
        for(blob in blobs) {
            val b = Blob.get(blob) //Cannot have neighbours no more?
            if (blobGroups.containsKey(group) && b.blobGroup == group) {
                blobGroups[group]!!.remove(blob)
                b.blobGroup = -1
                if (blobGroups[group]!!.isEmpty())
                    removeBlobGroup(group)
            }
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

    fun addBlobsToGroup(group: Int, vararg blobs: Entity) : Int {
        if (!blobGroups.containsKey(group)) {
            blobGroups[group] = mutableSetOf()
        }
        for (e in blobs) {
            val blob = Blob.get(e)
            removeBlobsFromGroup(blob.blobGroup, e)
            if(blobGroups[group]!!.add(e)) {
                blob.blobGroup = group
            }
        }
        return group
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

    val blobGroups = mutableMapOf<Int, MutableSet<Entity>>()
    val groupColors = mutableMapOf<Int, Color>()

}