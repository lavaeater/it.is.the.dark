package dark.ecs.components


import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import eater.ai.AiAction

class SomeOneTriesToConnectRightNow : Component<SomeOneTriesToConnectRightNow> {
    override fun type() = SomeOneTriesToConnectRightNow

    companion object : ComponentType<SomeOneTriesToConnectRightNow>()
}

data class Ai(val actions: MutableList<AiAction>) : Component<Ai> {
    override fun type() = Ai

    companion object : ComponentType<Ai>() {

    }
}

data class BlobComponent(var energy: Float) : Component<BlobComponent> {
    override fun type() = BlobComponent

    companion object : ComponentType<BlobComponent>()
}

class BlobMonster {
    /**
     * The blobmonster is an entity that might be connected to other entities
     *
     * It has a will of it's own.
     *
     * It could be like a weird game-of-life type of creature.
     *
     * OK, so what do we do? We start with... three entities. They are connected to each other.
     *
     * There has to be a... central connecting entity. Something to keep track of this particular entity of
     * a blobmonster.
     *
     * It keeps a list of all the blobs of the monster - and to draw the blob-monster.
     *
     * All blobs must be connected to each other but may at most have three connections. When adding new ones,
     * this might mean destroying and recreating joints on the fly.
     *
     * Will this result in blobby behavior?
     *
     * There must be some kind of energy-counter... or something, that might suddenly force the entire blob to split
     * in two parts.
     *
     *
     * We could then simply just remove that number of blobs from the monster
     *
     * Event cooler would be if they were in fact completely separate entities that, if close enough, started to
     * join up using box2d joints. And I THINK those joints can be dynamic actually, so we are going to try that.
     *
     * If we are close and try to join up, we need to block the other entity from joining up back, to not create a mess
     *
     */
    var energy = 100f
    val blobReq = 30f
    var numberOfBlobs = 3

    fun checkBlobCount() {
        var blobEnergy = energy -(numberOfBlobs * blobReq)
        while(blobEnergy > blobReq) {
            addBlob()
            blobEnergy -= blobReq
        }
    }

    private fun addBlob() {
        /**
         * Create another blob entity and add it to this blob monster
         * Cool.
         */
        numberOfBlobs++
    }

    fun eat(e: Float) {
        energy += e
        checkBlobCount()
    }
}