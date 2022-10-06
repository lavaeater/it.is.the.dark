package dark.ecs.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import eater.ecs.ashley.components.Box2d
import ktx.graphics.use
import space.earlygrey.shapedrawer.ShapeDrawer

class BlobRenderSystem(private val batch: PolygonSpriteBatch, private val shapeDrawer: ShapeDrawer, private val camera: OrthographicCamera) :EntitySystem() {
    override fun update(deltaTime: Float) {
        batch.projectionMatrix = camera.combined
        batch.use {
            for(blobList in BlobGrouper.blobGroups) {
                for ((index, blob) in blobList.withIndex()) {
                    var nextIndex = index + 1
                    if(nextIndex > blobList.lastIndex)
                        nextIndex = 0

                    shapeDrawer.line(Box2d.get(blob).body.position, Box2d.get(blobList[nextIndex]).body.position)
                }
            }
        }
    }
}