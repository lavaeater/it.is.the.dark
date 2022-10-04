package dark.ecs.systems

import com.github.quillraven.fleks.IntervalSystem
import dark.core.GameSettings
import eater.core.world

class Box2dUpdateSystem(private val gameSettings: GameSettings) : IntervalSystem() {
    var accumulator = 0f
    override fun onTick() {
        val ourTime = deltaTime.coerceAtMost(gameSettings.TimeStep * 2)
        accumulator += ourTime
        while (accumulator > gameSettings.TimeStep) {
            world().step(gameSettings.TimeStep, gameSettings.VelIters, gameSettings.PosIters)
            accumulator -= ourTime
        }
        /**
         * Perhaps add some kind of physics-add-body-queue-system to do adding of stuff
         * not inside a step
         */
    }

}