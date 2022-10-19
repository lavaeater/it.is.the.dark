package dark.map

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

    fun createRooms(numberOfRooms: Int) {
        var noOfRooms = numberOfRooms
        if(noOfRooms < 2)
            noOfRooms = 2
        val roomList = mutableListOf<Room>()
        var x = 0
        var y = 0
        var width = 12
        var height = 8
        /*
        Every room has at least ONE connection
         */
        noOfRooms--
        val startRoom = createRoom(x,y, width, height, noOfRooms)
    }

    fun createRoom(x: Int, y: Int, width: Int, height: Int, roomsLeft: Int) {

        if(roomsLeft > 0) {
            
        }
    }
}

sealed class CardinalDirection(val name: String) {
    object North: CardinalDirection("North")
    object East: CardinalDirection("East")
    object South: CardinalDirection("South")
    object West: CardinalDirection("West")
}

class Room(val x: Int, val y: Int, val width: Int, val height: Int) {
    val connections = mutableMapOf<CardinalDirection, Room>()
    val left get() = x
    val right get() = x + width
    val top get() = y + height
    val bottom get() = ys
}