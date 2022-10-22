package dark.ai

sealed class WanderState {
    object NotStarted : WanderState()
    object Running : WanderState()
}