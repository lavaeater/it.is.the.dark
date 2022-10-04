package dark.ai

import com.badlogic.ashley.core.Entity
import eater.ai.ashley.AiAction
import eater.ai.AshleyConsideration

class DoINeedEnergyAshleyConsideration:AshleyConsideration("Do I Need Energy?", { entity -> })

object BlobActions {
    val goTowardsFood = object: AiAction("Towards The Food") {

        init {
            ashleyConsiderations.add()
        }
        override fun abort(entity: Entity) {
            TODO("Not yet implemented")
        }

        override fun act(entity: Entity, deltaTime: Float) {
        }

    }
    val allActions = listOf<AiAction>()
}