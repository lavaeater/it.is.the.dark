package dark.ai

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.ai.steer.SteeringBehavior
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool

class WanderStateComponent : Component, Pool.Poolable {
    var state: WanderState = WanderState.NotStarted
    override fun reset() {
        state = WanderState.NotStarted
    }
}