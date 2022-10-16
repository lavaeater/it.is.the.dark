import com.badlogic.gdx.ai.steer.behaviors.Wander
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import dark.ai.BlobActions
import dark.core.GameSettings
import dark.ecs.components.Blob
import dark.ecs.components.BodyControl
import dark.ecs.components.Prop
import dark.ecs.components.PropsAndStuff
import dark.injection.assets
import eater.ai.ashley.AiComponent
import eater.ai.steering.box2d.Box2dSteering
import eater.core.engine
import eater.core.world
import eater.ecs.ashley.components.Box2d
import eater.ecs.ashley.components.CameraFollow
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.box2d.filter
import ktx.math.vec2

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
    val mapFamily = allOf(Map::class).get()
    val mapEntity = engine().getEntitiesFor(mapFamily).firstOrNull()

    if (mapEntity != null) {
        val map = Map.get(mapEntity)
        engine().entity {
            with<Food>()
            with<Box2d> {
                body = world().body {
                    type = BodyDef.BodyType.StaticBody
                    position.set(RandomRanges.getRandomPositionInBounds(map.mapBounds))
                    circle(1.0f) {
                        filter {
                            categoryBits = Categories.food
                            maskBits = Categories.whatFoodCollidesWith
                        }
                    }
                }
            }
        }
    }
}

fun createBlob(at: Vector2, health: Float = 100f, settings: GameSettings = inject(), follow: Boolean = false) {
    engine().entity {
        with<Blob>()
        with<PropsAndStuff> {
            props.add(Prop.FloatProp.Health(health))
        }
//        with<BodyControl> {
//            maxForce = 50f
//        }
//        with<AiComponent> {
//            actions.addAll(BlobActions.allActions)
//        }
        if (follow)
            with<CameraFollow>()
        val b2Body = world().body {
            type = BodyDef.BodyType.DynamicBody
            userData = this@entity.entity
            position.set(at)
            circle(1.0f) {
                filter {
                    categoryBits = Categories.blob
                    maskBits = Categories.whatBlobsCollideWith
                }
            }
        }
        with<Box2d> {
            body = b2Body
        }
        with<Box2dSteering> {
            isIndependentFacing = false
            body = b2Body
            maxLinearSpeed = 10f
            maxLinearAcceleration = 100f
            maxAngularAcceleration = 100f
            maxAngularSpeed = 10f
            boundingRadius = 5f
            steeringBehavior = Wander(this).apply {
                wanderRate = .1f
                wanderOffset = 5f
                wanderRadius = 25f
                wanderOrientation = 0.5f
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

fun createMap() {
    val mapOffset = vec2(-50f, -50f)
    val textureRegion = TextureRegion(assets().mapOne)
    engine().entity {
        with<Map> {
            mapTextureRegion = textureRegion
            mapScale = 1.0f
            mapOrigin.set(mapOffset)
            mapBounds = Rectangle(
                mapOffset.x + 8f,
                mapOffset.y + 8f,
                textureRegion.regionWidth.toFloat() - 16f,
                textureRegion.regionHeight.toFloat() - 16f
            )
        }
    }
    createBounds(assets().mapOneIntLayer, 8f, mapOffset)
}

fun createBounds(intLayer: String, tileSize: Float, mapOffset: Vector2) {
    /*
    To make it super easy, we just create a square per int-tile in the layer.
     */
    intLayer.lines().reversed().forEachIndexed { y, l ->
        l.split(',').forEachIndexed { x, c ->
            if (c == "1") {
                world().body {
                    type = BodyDef.BodyType.StaticBody
                    position.set(x * tileSize + mapOffset.x + tileSize / 2f, y * tileSize + mapOffset.y - tileSize / 2f)
                    box(tileSize, tileSize)
                }
            }
        }
    }
}