package application.model;

import java.util.ArrayList;
import java.util.List;

public class Floor {
    private int id;
    private int level;
    private int roomCount;
    private List<Room> rooms;

    // Constructor 1: Used when creating a simple floor
    public Floor(int level) {
        this.level = level;
        this.rooms = new ArrayList<>();
    }

    // Constructor 2: Used when fetching from Database (Fixes your error)
    public Floor(int id, int level, int roomCount) {
        this.id = id;
        this.level = level;
        this.roomCount = roomCount;
        this.rooms = new ArrayList<>();
    }

    // Method to add a room (Fixes "cannot find symbol" error)
    public void addRoom(Room room) {
        if (this.rooms == null) {
            this.rooms = new ArrayList<>();
        }
        this.rooms.add(room);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getRoomCount() { return roomCount; }
    public void setRoomCount(int roomCount) { this.roomCount = roomCount; }

    public List<Room> getRooms() { return rooms; }
    public void setRooms(List<Room> rooms) { this.rooms = rooms; }
}