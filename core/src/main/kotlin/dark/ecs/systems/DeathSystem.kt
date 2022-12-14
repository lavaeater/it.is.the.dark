package dark.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import createBlob
import dark.core.DarkGame
import dark.core.GameSettings
import dark.ecs.components.blobcomponents.Blob
import eater.ecs.ashley.components.LogComponent
import eater.ecs.ashley.components.Player
import eater.ecs.ashley.components.PropsAndStuff
import dark.ecs.systems.blob.BlobGrouper
import eater.ecs.ashley.components.Remove
import eater.physics.addComponent
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.log.info

class DeathSystem(private val gameSettings: GameSettings, private val game: DarkGame): IteratingSystem(allOf(
    PropsAndStuff::class).exclude(Remove::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if(PropsAndStuff.has(entity) && !LogComponent.has(entity)) {
            val health = PropsAndStuff.get(entity).getHealth()
            if(health.current <= 0f) {
                entity.addComponent<Remove>()
                if(Blob.has(entity)) {
                    BlobGrouper.removeBlob(entity)
                    if (BlobGrouper.blobCount < gameSettings.MinBlobs) {
                        info { "Add a new, too few left" }
                        createBlob(BlobGrouper.blobPoints.random())
                    }
                }
                if(Player.has(entity)) {
                    game.gameOver()
                }
            }
        }

    }
}