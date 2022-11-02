import com.aliasifkhan.hackLights.HackLight
import com.aliasifkhan.hackLights.HackLightEngine
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import dark.ai.BlobActions
import dark.ai.HumanActions
import dark.ai.getWanderSteering
import dark.core.GameSettings
import dark.ecs.components.*
import dark.ecs.components.Map
import dark.ecs.components.blobcomponents.Blob
import dark.ecs.components.blobcomponents.BlobsCanEatThis
import dark.ecs.systems.BlobGrouper
import dark.injection.Assets
import dark.injection.assets
import eater.ai.ashley.AiComponent
import eater.ai.steering.box2d.Box2dSteerable
import eater.core.engine
import eater.core.world
import eater.ecs.ashley.components.*
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.box2d.filter
import ktx.log.info
import ktx.math.vec2

fun createLights(points: List<Vector2>) {
    for (point in points)
        createLight(point)

}

fun createLight(lightPos: Vector2) {
    engine().entity {
        with<Light> {
            hackLight = HackLight(inject<Assets>().lights[0], 1f, 2f, 1f, 1f).apply {
                setOriginBasedPosition(lightPos.x, lightPos.y)
            }
            inject<HackLightEngine>().addLight(hackLight)
        }
        with<Box2d> {
            body = world().body {
                type = BodyDef.BodyType.DynamicBody
                userData = this@entity.entity
                position.set(lightPos)
                circle(10f) {
                    isSensor = true
                    filter {
                        categoryBits = Categories.lights
                        maskBits = Categories.whatLightsCollideWith
                    }
                }
            }
        }
        with<TransformComponent>()
    }
}

fun createFood(points: List<Vector2>) {
    engine().entity {
        with<Food>()
        with<Box2d> {
            body = world().body {
                userData = this@entity.entity
                type = BodyDef.BodyType.StaticBody
                position.set(points.random())
                circle(1.0f) {
                    filter {
                        categoryBits = Categories.food
                        maskBits = Categories.whatFoodCollidesWith
                    }
                }
            }
        }
        with<TransformComponent>()
        with<BlobsCanEatThis>()
    }
}

fun createHumans(points: MutableList<Vector2>) {
    for (i in 0..20) {
        createRegularHuman(points.random(), follow = false)
    }
}

fun createHuman() {
    createRegularHuman(inject<Map>().points[PointType.HumanStart]!!.random())
}

fun createRegularHuman(at: Vector2, health: Float = 100f, follow: Boolean = false) {
    engine().entity {
        with<Human>()
        with<PropsAndStuff> {
            props.add(Prop.FloatProp.Health(health))
        }
        if (follow)
            with<CameraFollow>()
        val b2Body = world().body {
            type = BodyDef.BodyType.DynamicBody
            userData = this@entity.entity
            position.set(at)
            circle(1.0f) {
                filter {
                    categoryBits = Categories.human
                    maskBits = Categories.whatHumansCollideWith
                }
            }
        }
        with<Box2d> {
            body = b2Body
        }
        with<TransformComponent>()
        with<Box2dSteerable> {
            isIndependentFacing = false
            body = b2Body
            maxLinearSpeed = 10f
            maxLinearAcceleration = 100f
            maxAngularAcceleration = 100f
            maxAngularSpeed = 10f
            boundingRadius = 5f
            steeringBehavior = null
        }
        with<AiComponent> {
            actions.addAll(HumanActions.actions)
        }
        with<BlobsCanEatThis>()
    }
}

fun createPlayer(at: Vector2, health: Float = 100f, follow: Boolean = false) {
    engine().entity {
        with<Human>()
        with<PropsAndStuff> {
            props.add(Prop.FloatProp.Health(health))
        }
        if (follow)
            with<CameraFollow>()
        with<BodyControl> {
            maxForce = 1000f
        }
        with<KeyboardAndMouseInput>()
        with<Light> {
            hackLight = HackLight(inject<Assets>().lights[5], 1f, 2f, 1f, 1f).apply {
                setOriginBasedPosition(at.x, at.y)
            }
            inject<HackLightEngine>().addLight(hackLight)
        }
        with<Flashlight> {
            light = HackLight(inject<Assets>().lights[1], 1f, 1f, 1f, 1f).apply {
                setOriginCenter()
                setOrigin(originX, originY / 5f)
            }
            inject<HackLightEngine>().addLight(light)
        }
        with<Box2d> {
            body = world().body {
                type = BodyDef.BodyType.DynamicBody
                userData = this@entity.entity
                position.set(at)
                circle(1.0f) {
                    filter {
                        categoryBits = Categories.human
                        maskBits = Categories.whatHumansCollideWith
                    }
                }
            }
        }
        with<TransformComponent>()
        with<BlobsCanEatThis>()
        with<Player>()
    }
}

fun createBlob(
    at: Vector2,
    health: Float = 100f,
    radius: Float = 3f,
    follow: Boolean = false
) {
    BlobGrouper.addNewBlob(engine().entity {
        with<Blob> {
            this.radius = radius
        }
        with<PropsAndStuff> {
            props.add(Prop.FloatProp.Health(health))
        }
        with<AiComponent> {
            actions.addAll(BlobActions.allActions)
        }
        if (follow) {
            with<CameraFollow>()
            with<LogComponent> {
                logFunction = { entity ->
                    val aiComponent = AiComponent.get(entity)
                    info { "We ${if (BlobGrouper.canSplit) "can" else "cannot"} split" }
                    info { "Health: ${PropsAndStuff.get(entity).getHealth().current}" }
                    info { "Top action: ${aiComponent.topAction(entity)?.name}" }
                    info { aiComponent.actions.joinToString { "${it.score} - ${it.name}\n" } }
                    info { "Messages: ${Blob.get(entity).messageCount}" }
                    info { "Blob count: ${BlobGrouper.blobCount}" }
                    info { "Top Message: ${Blob.get(entity).peekOldestMessage()}" }
                    info { "Neighbours: ${Blob.get(entity).neighbours.size}" }
                }
            }
        }
        with<Memory>()
        val b2Body = world().body {
            type = BodyDef.BodyType.DynamicBody
            userData = this@entity.entity
            position.set(at)
            circle(3.0f) {
                filter {
                    categoryBits = Categories.blob
                    maskBits = Categories.whatBlobsCollideWith
                }
            }
            circle(10.0f) {
                isSensor = true
                filter {
                    categoryBits = Categories.blob
                    maskBits = Categories.blob
                }
            }
        }
        with<Box2d> {
            body = b2Body
        }
        with<TransformComponent>()
        with<Box2dSteerable> {
            isIndependentFacing = false
            body = b2Body
            maxLinearSpeed = inject<GameSettings>().BlobMaxSpeed
            maxLinearAcceleration = inject<GameSettings>().BlobMaxAcceleration
            maxAngularAcceleration = 100f
            maxAngularSpeed = 10f
            boundingRadius = 5f
            steeringBehavior = getWanderSteering(this@entity.entity, this)
        }
        with<AgentProperties>() {
            fieldOfView = 270f
            viewDistance = 50f
        }
    })
}

fun createMap(key: String): Map {
    var scaleFactor = 1f
    if (key == "two")
        scaleFactor = 2f
    val gridSize = 8f * scaleFactor
    val mapOffset = vec2(-50f, -50f)
    val mapAssets = assets().maps[key]!!
    val textureRegion = TextureRegion(mapAssets.first)
    val topTextureRegion = TextureRegion(mapAssets.third)
    lateinit var map: Map
    engine().entity {
        map = with {
            mapTextureRegion = textureRegion
            mapTopLayerRegion = topTextureRegion
            mapScale = scaleFactor
            mapOrigin.set(mapOffset)
            mapBounds = Rectangle(
                mapOffset.x + gridSize,
                mapOffset.y + gridSize,
                textureRegion.regionWidth.toFloat() - 2 * gridSize,
                textureRegion.regionHeight.toFloat() - 2 * gridSize
            )
        }
        createBounds(mapAssets.second, gridSize, mapOffset, map)
    }
    return map
}

fun createBounds(intLayer: String, tileSize: Float, mapOffset: Vector2, map: Map) {
    /*
    To make it super easy, we just create a square per int-tile in the layer.
     */
    intLayer.lines().reversed().forEachIndexed { y, l ->
        l.split(',').forEachIndexed { x, c ->
            if (PointType.allTypes.containsKey(c)) {
                val pointType = PointType.allTypes[c]!!
                if (!map.points.containsKey(pointType)) {
                    map.points[pointType] = mutableListOf()
                }
                map.points[pointType]!!.add(
                    vec2(
                        x * tileSize + mapOffset.x + tileSize / 2f,
                        y * tileSize + mapOffset.y - tileSize / 2f
                    )
                )
            }
        }
    }

    for (bound in map.points[PointType.Impassable]!!) {
        map.mapBodies.add(world().body {
            type = BodyDef.BodyType.StaticBody
            position.set(
                bound.x,
                bound.y
            )
            box(tileSize, tileSize) {
                filter {
                    categoryBits = Categories.walls
                    maskBits = Categories.whatWallsCollideWith
                }
            }
        })
    }
}

