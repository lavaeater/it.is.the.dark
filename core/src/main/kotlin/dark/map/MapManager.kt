package dark.map

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import eater.core.world
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.filter
import ktx.math.vec2

class RecursiveCounter(var counter: Int)

class MapManager {
    /**
     * The hassle free map handler. Fucking hell
     *
     * The map should be free, as in we see all of it all of the time.
     *
     * A room is width x height.
     *
     * It can have openings and openings are always in the middle of a direction
     *
     */
    val roomList = mutableListOf<Room>()
    val connectionList = mutableListOf<RoomConnection>()
    val bodyList = mutableListOf<Body>()

    fun getRandomPositionInBounds(): Vector2 {
        return vec2()
    }

    val world by lazy { world() }
    val tileSize = 1f

    fun createBounds() {
        for (room in roomList) {
            /**
             * Lets be tricky about it.
             */
            for (direction in CardinalDirection.directions) {
                if (room.usedDirections.contains(direction)) {
                    /*
                    We have an opening in this direction
                     */
                    when (direction) {
                        CardinalDirection.East -> {
                            bodyList.add(world.body {
                                type = BodyDef.BodyType.StaticBody
                                position.set(
                                    room.right * tileSize - tileSize / 4f,
                                    room.bottom * tileSize + (room.top - room.verticalMiddle - 1) / 2f * tileSize
                                )
                                box(tileSize / 2f, (room.top - room.verticalMiddle - 1) * tileSize - tileSize / 2f) {
                                    filter {
                                        categoryBits = Categories.walls
                                        maskBits = Categories.whatWallsCollideWith
                                    }
                                }
                            })
                            bodyList.add(world.body {
                                type = BodyDef.BodyType.StaticBody
                                position.set(
                                    room.right * tileSize - tileSize / 4f,
                                    room.top * tileSize - (room.top - room.verticalMiddle - 1) / 2f * tileSize
                                )
                                box(tileSize / 2f, (room.top - room.verticalMiddle - 1) * tileSize - tileSize / 2f) {
                                    filter {
                                        categoryBits = Categories.walls
                                        maskBits = Categories.whatWallsCollideWith
                                    }
                                }
                            })
                        }

                        CardinalDirection.North -> {
                            bodyList.add(world.body {
                                type = BodyDef.BodyType.StaticBody
                                position.set(
                                    (room.left + (room.right - room.horizontalMiddle - 1) / 2f) * tileSize,
                                    room.top * tileSize - tileSize / 2f
                                )
                                box(
                                    (room.right - room.horizontalMiddle - 1) * tileSize - tileSize / 2f,
                                    tileSize / 2f
                                ) {
                                    filter {
                                        categoryBits = Categories.walls
                                        maskBits = Categories.whatWallsCollideWith
                                    }
                                }
                            })
                            bodyList.add(world.body {
                                type = BodyDef.BodyType.StaticBody
                                position.set(
                                    (room.right - (room.right - room.horizontalMiddle - 1) / 2f) * tileSize,
                                    room.top * tileSize - tileSize / 2f
                                )
                                box(
                                    (room.right - room.horizontalMiddle - 1) * tileSize - tileSize / 2f,
                                    tileSize / 2f
                                ) {
                                    filter {
                                        categoryBits = Categories.walls
                                        maskBits = Categories.whatWallsCollideWith
                                    }
                                }
                            })
                        }

                        CardinalDirection.South -> {
                            bodyList.add(world.body {
                                type = BodyDef.BodyType.StaticBody
                                position.set(
                                    (room.left + (room.right - room.horizontalMiddle - 1) / 2f) * tileSize,
                                    room.bottom * tileSize + tileSize / 4f
                                )
                                box(
                                    (room.right - room.horizontalMiddle - 1) * tileSize - tileSize / 2f,
                                    tileSize / 2f
                                ) {
                                    filter {
                                        categoryBits = Categories.walls
                                        maskBits = Categories.whatWallsCollideWith
                                    }
                                }
                            })
                            bodyList.add(world.body {
                                type = BodyDef.BodyType.StaticBody
                                position.set(
                                    (room.right - (room.right - room.horizontalMiddle - 1) / 2f) * tileSize,
                                    room.bottom * tileSize + tileSize / 4f
                                )
                                box(
                                    (room.right - room.horizontalMiddle - 1) * tileSize - tileSize / 2f,
                                    tileSize / 2f
                                ) {
                                    filter {
                                        categoryBits = Categories.walls
                                        maskBits = Categories.whatWallsCollideWith
                                    }
                                }
                            })
                        }

                        CardinalDirection.West -> {
                            bodyList.add(world.body {
                                type = BodyDef.BodyType.StaticBody
                                position.set(
                                    room.left * tileSize + tileSize / 4f,
                                    room.bottom * tileSize + (room.top - room.verticalMiddle - 1) / 2f * tileSize
                                )
                                box(tileSize / 2f, (room.top - room.verticalMiddle - 1) * tileSize - tileSize / 2f) {
                                    filter {
                                        categoryBits = Categories.walls
                                        maskBits = Categories.whatWallsCollideWith
                                    }
                                }
                            })
                            bodyList.add(world.body {
                                type = BodyDef.BodyType.StaticBody
                                position.set(
                                    room.left * tileSize + tileSize / 4f,
                                    room.top * tileSize - (room.top - room.verticalMiddle - 1) / 2f * tileSize
                                )
                                box(tileSize / 2f, (room.top - room.verticalMiddle - 1) * tileSize - tileSize / 2f) {
                                    filter {
                                        categoryBits = Categories.walls
                                        maskBits = Categories.whatWallsCollideWith
                                    }
                                }
                            })
                        }
                    }
                } else {
                    /*
                    Easy case
                     */
                    when (direction) {
                        CardinalDirection.East -> {
                            bodyList.add(world.body {
                                type = BodyDef.BodyType.StaticBody
                                position.set(room.right * tileSize - tileSize / 4f, room.verticalMiddle * tileSize)
                                box(tileSize / 2f, room.height * tileSize - tileSize) {
                                    filter {
                                        categoryBits = Categories.walls
                                        maskBits = Categories.whatWallsCollideWith
                                    }
                                }
                            })
                        }

                        CardinalDirection.North -> {
                            bodyList.add(world.body {
                                type = BodyDef.BodyType.StaticBody
                                position.set(room.horizontalMiddle * tileSize, room.top * tileSize - tileSize / 2f)
                                box(room.width * tileSize, tileSize / 2f) {
                                    filter {
                                        categoryBits = Categories.walls
                                        maskBits = Categories.whatWallsCollideWith
                                    }
                                }
                            })
                        }

                        CardinalDirection.South -> {
                            bodyList.add(world.body {
                                type = BodyDef.BodyType.StaticBody
                                position.set(room.horizontalMiddle * tileSize, room.bottom * tileSize + tileSize / 4f)
                                box(room.width * tileSize, tileSize / 2f) {
                                    filter {
                                        categoryBits = Categories.walls
                                        maskBits = Categories.whatWallsCollideWith
                                    }
                                }
                            })
                        }

                        CardinalDirection.West -> {
                            bodyList.add(world.body {
                                type = BodyDef.BodyType.StaticBody
                                position.set(room.left * tileSize + tileSize / 4f, room.verticalMiddle * tileSize)
                                box(tileSize / 2f, room.height * tileSize - tileSize) {
                                    filter {
                                        categoryBits = Categories.walls
                                        maskBits = Categories.whatWallsCollideWith
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    fun createRooms(numberOfRooms: Int) {
        var noOfRooms = numberOfRooms
        if (noOfRooms < 2)
            noOfRooms = 2
        val x = 0
        val y = 0
        val width = 8
        val height = 8
        /*
        Every room has at least ONE connection
         */
        newCreateRoom(x, y, width, height, RecursiveCounter(noOfRooms))
        createBounds()
    }

    fun newCreateRoom(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        roomCounter: RecursiveCounter, incomingConnection: RoomConnection? = null
    ) {
        val room = Room(x, y, width, height)
        roomList.add(room)
        val firstRoom = incomingConnection == null
        val availableCardinalDirections = mutableListOf(
            CardinalDirection.North,
            CardinalDirection.East,
            CardinalDirection.South,
            CardinalDirection.West
        )
        if (!firstRoom) {
            incomingConnection!!.toRoom = room
            room.connections.add(incomingConnection)
            availableCardinalDirections.remove(incomingConnection.direction.end)
        }
        if (roomCounter.counter > 0) {
            // Add 1-4 connected Rooms
            val maxRoomsToAdd = if (roomCounter.counter > 2) 3 else roomCounter.counter
            val numberOfRoomsToAdd = if (firstRoom) 4 else (1..maxRoomsToAdd).random()
            roomCounter.counter = roomCounter.counter - numberOfRoomsToAdd
            for (i in 1..numberOfRoomsToAdd) {
                val direction =
                    if (!firstRoom && (1..3).random() == 1) CardinalDirection.opposites[incomingConnection!!.direction.end]!! else availableCardinalDirections.random() //
                availableCardinalDirections.remove(direction)
                val xy = when (direction) {
                    CardinalDirection.East -> Pair(room.right + 1, room.y)
                    CardinalDirection.North -> Pair(room.x, room.top + 1)
                    CardinalDirection.South -> Pair(room.x, room.bottom - 1)
                    CardinalDirection.West -> Pair(room.left - 1, room.y)
                }

                val newRoomRect = Rectangle(xy.first.toFloat(), xy.second.toFloat(), width.toFloat(), height.toFloat())
                if (roomList.filter { it.roomRect.overlaps(newRoomRect) }.isEmpty()) {
                    val connectionXY = when (direction) {
                        CardinalDirection.East -> Pair(room.right + 1, room.bottom + room.height / 2)
                        CardinalDirection.North -> Pair(room.left + room.width / 2, room.top + 1)
                        CardinalDirection.South -> Pair(room.left + room.width / 2, room.bottom - 1)
                        CardinalDirection.West -> Pair(room.left - 1, room.bottom + room.height / 2)
                    }


                    val connection =
                        RoomConnection(
                            connectionXY.first,
                            connectionXY.second,
                            room,
                            null,
                            ConnectionDirection.fromTo[direction]!!
                        )

                    room.connections.add(connection)
                    connectionList.add(connection)
                    newCreateRoom(xy.first, xy.second, width, height, roomCounter, connection)
                } else {
                    roomCounter.counter++
                }
            }
        }
    }
}

sealed class CardinalDirection(val name: String, val x: Int, val y: Int) {
    object North : CardinalDirection("North", 0, 1)
    object East : CardinalDirection("East", 1, 0)
    object South : CardinalDirection("South", 0, -1)
    object West : CardinalDirection("West", -1, 0)

    companion object {
        val opposites by lazy { mapOf(North to South, South to North, East to West, West to East) }
        val directions by lazy { listOf(North, East, South, West) }
    }
}

sealed class ConnectionDirection(val start: CardinalDirection, val end: CardinalDirection) {
    object NorthToSouth : ConnectionDirection(CardinalDirection.North, CardinalDirection.South)
    object SouthToNorth : ConnectionDirection(CardinalDirection.South, CardinalDirection.North)
    object EastToWest : ConnectionDirection(CardinalDirection.East, CardinalDirection.West)
    object WestToEast : ConnectionDirection(CardinalDirection.West, CardinalDirection.East)

    companion object {
        val fromTo by lazy {
            mapOf(
                CardinalDirection.North to NorthToSouth,
                CardinalDirection.South to SouthToNorth,
                CardinalDirection.West to WestToEast,
                CardinalDirection.East to EastToWest
            )
        }
    }
}

class RoomConnection(
    val x: Int,
    val y: Int,
    var fromRoom: Room,
    var toRoom: Room? = null,
    val direction: ConnectionDirection
)

class Room(val x: Int, val y: Int, val width: Int, val height: Int) {
    val connections = mutableListOf<RoomConnection>()
    val usedDirections
        get() = connections.filter { it.fromRoom == this }
            .map { it.direction.start } + connections.filter { it.toRoom == this }.map { it.direction.end }
    val left = x
    val right = x + width
    val top = y + height
    val bottom = y
    val verticalMiddle = y + height / 2
    val horizontalMiddle = x + width / 2
    val roomRect = Rectangle(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
}