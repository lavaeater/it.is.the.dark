package dark.core

class GameSettings {

    val BlobMaxAcceleration = 100f
    val BlobMaxSpeed = 10f
    val LightDamage = 10f
    val Debug = true
    val BlobHealthReductionPerSecond = 5f
    val BlobDetectionRadius = 15f
    val BlobForgettingRadius = 20f
    val MaxBlobs = 5

    val HumanLightDetectionRadius = 50f

    val GameWidth = 128f
    val AspectRatio = 16f / 9f
    val GameHeight = AspectRatio * GameWidth
    val PixelsPerMeter = 32f
    val MetersPerPixel = 1f / PixelsPerMeter
    val outerShellHz = 1f
    val outerShellDamp = 0.5f
    val spokeHz = 1f
    val spokeDamp = 0.3f
    val segmentLength = 5f
    val TimeStep = 1 / 60f
    val VelIters = 16
    val PosIters = 6

}