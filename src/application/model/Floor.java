package application.model;

import java.util.ArrayList;
import java.util.List;

public class Floor {
    private int level;
    private List<Room> rooms = new ArrayList<>();

    public Floor(int level) {
        this.level = level;
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public int getLevel() { return level; }
}