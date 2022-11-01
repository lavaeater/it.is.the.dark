package dark.ecs.components.blobcomponents

sealed class ShootAndEatState {
    object HasNotYetShot: ShootAndEatState()
    object HasShot: ShootAndEatState()
    object IsEating: ShootAndEatState()
    object TotallyDone: ShootAndEatState()
}