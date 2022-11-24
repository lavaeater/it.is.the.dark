package dark.ecs.systems.stackai

import com.badlogic.gdx.math.Vector2
import eater.ecs.ashley.components.GeneralMemory

sealed class MemoryEvent : GeneralMemory {
    class HitByLight(val lightSource: Vector2) : MemoryEvent()
}