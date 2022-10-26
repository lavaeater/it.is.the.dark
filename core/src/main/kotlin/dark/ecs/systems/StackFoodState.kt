package dark.ecs.systems

sealed class StackFoodState {
    object NeedsSteering: StackFoodState()
    object InTransit: StackFoodState()
    object Eating: StackFoodState()
    object Done: StackFoodState()
    object Paused: StackFoodState()
}