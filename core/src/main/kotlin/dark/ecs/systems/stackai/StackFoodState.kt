package dark.ecs.systems.stackai

sealed class StackFoodState {
    object NeedsSteering: StackFoodState()
    object InTransit: StackFoodState()
    object Eating: StackFoodState()
    object Done: StackFoodState()
    object Paused: StackFoodState()
}