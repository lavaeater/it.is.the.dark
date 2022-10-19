package dark.core

class GameSettings {

    val BlobDetectionRadius = 15f
    val GameWidth = 24f
    val GameHeight = (16f / 9f) * GameWidth
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