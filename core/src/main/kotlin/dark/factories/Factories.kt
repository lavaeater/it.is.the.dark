import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.utils.Pool
import dark.ai.BlobActions
import dark.core.GameSettings
import dark.ecs.components.BlobMonster
import dark.ecs.components.BodyControl
import dark.ecs.components.Prop
import dark.ecs.components.PropsAndStuff
import eater.ai.ashley.AiComponent
import eater.core.engine
import eater.core.world
import eater.ecs.ashley.components.AgentProperties
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.CameraFollow
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.entity
import ktx.ashley.mapperFor
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.circle
import ktx.math.vec2

object RandomRanges {
    val positionRange = -10..10
    fun getRandomPosition(): Vector2 {
        return vec2(positionRange.random()*2.5f, positionRange.random() * 2.5f)
    }
}

fun createLight() {
    engine().entity {
        with<Light>()
        with<Box2d> {
            body = world().body {
                type = BodyDef.BodyType.DynamicBody
                position.set(RandomRanges.getRandomPosition())
                circle(1.0f) {

                }
            }
        }
    }
}

fun createFood() {
    engine().entity {
        with<Food>()
        with<Box2d> {
            body = world().body {
                type = BodyDef.BodyType.DynamicBody
                position.set(RandomRanges.getRandomPosition())
                circle(1.0f) {

                }
            }
        }
    }
}

class Blob: Component, Pool.Poolable {
    override fun reset() {

    }

    companion object {
        val mapper = mapperFor<Blob>()
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
        fun get(entity: Entity): Blob {
            return mapper.get(entity)
        }
    }
}


fun createBlob(at:Vector2, settings: GameSettings = inject()) {
    engine().entity {
        with<Blob>()
        with<PropsAndStuff> {
            props.add(Prop.Health())
        }
        with<BodyControl>()
        with<AiComponent> {
            actions.addAll(BlobActions.allActions)
        }
        with<CameraFollow>()
        with<Box2d> {
            body = world().body {
                type = BodyDef.BodyType.DynamicBody
                position.set(at)
                circle(1.0f) {

                }
            }
        }
    }
}

fun createDarkEntity(at: Vector2, radius: Float): Body {
    return world().body {
        type = BodyDef.BodyType.DynamicBody
        position.set(at)
        fixedRotation = false
        circle(radius) {
            density = .1f
        }
    }
}