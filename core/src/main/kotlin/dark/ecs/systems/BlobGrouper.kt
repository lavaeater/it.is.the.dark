package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import dark.ecs.components.Blob
import dark.ecs.components.BlobMessage

object BlobGrouper {
    fun addBlobsToNewGroup(vararg blobs: Entity) {
        val newGroupId = blobGroupIds
        blobGroups[newGroupId] = mutableSetOf()
        for (blob in blobs) {
            Blob.get(blob).blobGroup = newGroupId
            blobGroups[newGroupId]!!.add(blob)
        }
    }

    fun removeBlobFromGroup(group:Int, blob: Entity) {
        val b = Blob.get(blob) //Cannot have neighbours no more?
        if(blobGroups.containsKey(group) && b.blobGroup == group) {
            blobGroups[group]!!.remove(blob)
            b.blobGroup = -1
            if(blobGroups[group]!!.isEmpty())
                blobGroups.remove(group)
        }

    }

    fun removeBlobGroup(group: Int) {
        if(blobGroups.containsKey(group)) {
            for (e in blobGroups[group]!!) {
                Blob.get(e).blobGroup = -1
            }
            blobGroups.remove(group)
        }
    }

    fun addBlobsToGroup(group: Int, vararg blobs: Entity) {
        if(blobGroups.containsKey(group)) {
            for(e in blobs) {
                Blob.get(e).blobGroup = group
                blobGroups[group]!!.add(e)
            }
        }
    }

    fun getBlobsForGroup(group: Int): List<Entity> {
        return if(blobGroups.containsKey(group)) blobGroups[group]!!.toList() else emptyList()
    }

    fun sendMessageToGroup(group: Int, message: BlobMessage) {
        if(blobGroups.containsKey(group)) {
            for(blob in (getBlobsForGroup(group) - message.sender)) {
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
            return blobGroups.values.sumOf { it.count() } < 100
        }
    val blobGroups = mutableMapOf<Int, MutableSet<Entity>>()

}