package dark.ecs.components

sealed class TargetState {
    object NeedsTarget: TargetState()
    object HasTarget: TargetState()
    object IsDoneWithTarget: TargetState()

}