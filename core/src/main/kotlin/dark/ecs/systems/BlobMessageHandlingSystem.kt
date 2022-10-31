package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import dark.ecs.components.*
import dark.ecs.components.Target
import eater.ecs.ashley.components.Remove
import ktx.ashley.allOf
import ktx.ashley.exclude


class BlobMessageHandlingSystem(private val numberOfMessagesPerFrame: Int = 10) : IteratingSystem(allOf(Blob::class).exclude(Remove::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val blob = Blob.get(entity)
        for(i in 0..numberOfMessagesPerFrame) {
            val message = blob.getOldestMessage()
            if (message != null) {
                when (message) {
                    is BlobMessage.FoundAFoodTarget -> {
                        if (Target.HuntingTarget.has(entity)) {
                            val tc = Target.HuntingTarget.get(entity)
                            if (tc.target != null && Food.has(tc.target!!)) {
                                val currentTarget = Food.get(tc.target!!)
                                if (currentTarget.foodEnergy < message.energy) {
                                    tc.target = message.target
                                }
                            }
                        }
                    }

                    is BlobMessage.TakeSomeOfMyHealth -> {
                        PropsAndStuff.get(entity).getHealth().current += message.healthToAdd
                    }
                }
            }
        }
    }
}