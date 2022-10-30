package dark.injection

import com.aliasifkhan.hackLights.HackLightEngine
import dark.ecs.components.Food
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.utils.viewport.ExtendViewport
import createFood
import dark.core.DarkGame
import dark.core.GameSettings
import dark.ecs.components.Blob
import dark.ecs.systems.*
import dark.screens.GameScreen
import eater.ecs.ashley.systems.*
import eater.injection.InjectionContext
import eater.physics.*
import ktx.ashley.allOf
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import space.earlygrey.shapedrawer.ShapeDrawer

object Context : InjectionContext() {
    private val shapeDrawerRegion: TextureRegion by lazy {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.drawPixel(0, 0)
        val texture = Texture(pixmap) //remember to dispose of later
        pixmap.disposeSafely()
        TextureRegion(texture, 0, 0, 1, 1)
    }

    fun initialize(game: DarkGame) {
        buildContext {
            val gameSettings = GameSettings()
            bindSingleton(gameSettings)
            bindSingleton(game)
            bindSingleton(PolygonSpriteBatch())
            bindSingleton(OrthographicCamera())
            bindSingleton(
                ExtendViewport(
                    gameSettings.GameWidth,
                    gameSettings.GameHeight,
                    inject<OrthographicCamera>() as Camera
                )
            )
            bindSingleton(createWorld().apply {
                setContactListener(CollisionManager())
            })
            bindSingleton(ShapeDrawer(inject<PolygonSpriteBatch>() as Batch, shapeDrawerRegion))
            bindSingleton(getEngine(gameSettings))
            bindSingleton(Assets())
            bindSingleton(HackLightEngine(1f,1f,1f,0.25f))
            bindSingleton(
                GameScreen(
                    inject(),
                    inject(),
                    inject(),
                    inject(),
                    inject(),
                    inject()
                )
            )
        }
    }

    private fun getEngine(gameSettings: GameSettings): Engine {
        return PooledEngine().apply {
            addSystem(RemoveEntitySystem())
            addSystem(DeathSystem())
            addSystem(CameraAndMapSystem(inject(), 0.75f, inject()))
            addSystem(Box2dUpdateSystem(gameSettings.TimeStep, gameSettings.VelIters, gameSettings.PosIters))
            addSystem(BodyControlSystem())
            addSystem(KeyboardInputSystem(inject()))
            addSystem(FlashlightDirectionSystem())
            addSystem(FlashlightRayTracingSystem(inject()))
            addSystem(LightPositionUpdateSystem())
            addSystem(SteerSystem())
            addSystem(AiTimePieceSystem())
            addSystem(UpdateActionsSystem())
            addSystem(AshleyAiSystem())
            addSystem(EnsureEntitySystem(EnsureEntityDef(allOf(Food::class).get(), 50) { createFood() }))
            addSystem(BlobHealthSharingSystem())
            addSystem(BlobHealthDiminishingSystem(inject()))
            addSystem(RenderSystem(inject(), inject(), inject(), inject()))
//            addSystem(Box2dDebugRenderSystem(inject(), inject()))
            addSystem(BlobMessageHandlingSystem())
            addSystem(UpdateMemorySystem())
            addSystem(BlobNeighbourSystem(inject()))
            addSystem(LogSystem())
        }
    }
}

sealed class ContactType {
    object Unknown : ContactType()
    class BlobAndBlobSensors(val firstBlob: Entity, val secondBlob: Entity) : ContactType()

    companion object {
        fun getContactType(contact: Contact): ContactType {
            return if (contact.bothHaveComponent<Blob>() && contact.bothAreSensors())
                BlobAndBlobSensors(contact.fixtureA.getEntity(), contact.fixtureB.getEntity())
            else {
                Unknown
            }
        }
    }
}

class CollisionManager : ContactListener {
    override fun beginContact(contact: Contact) {
        when (val contactType = ContactType.getContactType(contact)) {
            is ContactType.BlobAndBlobSensors -> {
                /**
                 * Check if either one belongs to a blob group.
                 * If they do, add the other to it - but what to do if they both are in a blob group?
                 * All shall be in the same blob group in the end
                 *
                 * They also become neighbours automatically
                 */
//                val firstBlob = contactType.firstBlob
//                val secondBlob = contactType.secondBlob
//
//                val firstBlobC = Blob.get(firstBlob)
//                val secondBlobC = Blob.get(secondBlob)
//
//                firstBlobC.neigbours.add(secondBlob)
//                secondBlobC.neigbours.add(firstBlob)
//
//                if(firstBlobC.blobGroup == -1 && secondBlobC.blobGroup == -1) {
//                    /*
//                    No group exists for these blobs, add it!
//                     */
//                    BlobGrouper.addBlobsToNewGroup(firstBlob, secondBlob)
//                } else if(firstBlobC.blobGroup != -1 && secondBlobC.blobGroup != -1 && firstBlobC.blobGroup != secondBlobC.blobGroup) {
//                    /**
//                     * first swallows second
//                     */
//                    val blobsInSecond = BlobGrouper.getBlobsForGroup(secondBlobC.blobGroup)
//                    BlobGrouper.removeBlobGroup(secondBlobC.blobGroup)
//                    BlobGrouper.addBlobsToGroup(firstBlobC.blobGroup, *blobsInSecond.toTypedArray())
//                } else if(firstBlobC.blobGroup == -1 || secondBlobC.blobGroup == -1) {
//                    val blobGroupThatExists = if(firstBlobC.blobGroup != -1) firstBlobC.blobGroup else secondBlobC.blobGroup
//                    val blobThatShouldBeAdded = if(firstBlobC.blobGroup != -1) firstBlob else secondBlob
//                    BlobGrouper.addBlobsToGroup(blobGroupThatExists, blobThatShouldBeAdded)
//                }
            }

            ContactType.Unknown -> {}
        }
    }

    override fun endContact(contact: Contact) {
        when (val contactType = ContactType.getContactType(contact)) {
            is ContactType.BlobAndBlobSensors -> {
                /**
                 * So, if we are no longer in contact with... A blob, what do we do?
                 * We should only leave the group if we are actually no longer near ANYONE
                 * in the blob swarm. So simply put, if we have NO neigbours, i.e our
                 * blob proximity list is empty, we should leave the group
                 */
//                val firstBlob = contactType.firstBlob
//                val secondBlob = contactType.secondBlob
//
//                val firstBlobC = Blob.get(firstBlob)
//                val secondBlobC = Blob.get(secondBlob)
//
//                firstBlobC.neigbours.remove(secondBlob)
//                secondBlobC.neigbours.remove(firstBlob)
//
//                if(firstBlobC.neigbours.isEmpty()) {
//                    if(firstBlobC.blobGroup != -1)
//                        BlobGrouper.removeBlobFromGroup(firstBlobC.blobGroup, firstBlob)
//                }
//                if(secondBlobC.neigbours.isEmpty()) {
//                    if(secondBlobC.blobGroup != -1)
//                        BlobGrouper.removeBlobFromGroup(secondBlobC.blobGroup, secondBlob)
//                }
            }

            ContactType.Unknown -> {}
        }
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {
    }

    override fun postSolve(contact: Contact, impulse: ContactImpulse) {
    }
}

