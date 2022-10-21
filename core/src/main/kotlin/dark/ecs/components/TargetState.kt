package dark.ecs.components

sealed class TargetState {
    object NeedsTarget: TargetState()
    object NeedsSteering: TargetState()
    object IsSteering: TargetState()
    object ArrivedAtTarget: TargetState()
    object IsDoneWithTarget: TargetState()

}