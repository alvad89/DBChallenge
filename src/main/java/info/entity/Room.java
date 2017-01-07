package info.entity;

/**
 * Created by alvad89 on 07.01.17.
 */
public class Room {
    int id;
    String roomName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Room room = (Room) o;

        if (id != room.id) return false;
        return roomName != null ? roomName.equals(room.roomName) : room.roomName == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (roomName != null ? roomName.hashCode() : 0);
        return result;
    }
}
