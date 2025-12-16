package application.model;

import java.util.ArrayList;
import java.util.List;

public class Floor {
    private int level;
    private int roomCount; // Stores the total capacity set in AddProperty
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

    public int getLevel() {
        return level;
    }

    // ✅ FIX: Added Getter for HouseViewController
    public int getRoomCount() {
        return roomCount;
    }

    // ✅ FIX: Added Setter for your Database Controller/DAO
    public void setRoomCount(int roomCount) {
        this.roomCount = roomCount;
    }
}