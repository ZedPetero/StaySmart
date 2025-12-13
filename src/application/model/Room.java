package application.model;

public class Room {
    private String roomNumber;
    private String status; // "Occupied" or "Available"
    private double price;
    private String imagePath; // URL or file path to room image
    private String tenantName; // Null if empty

    public Room(String roomNumber, String status, double price, String imagePath) {
        this.roomNumber = roomNumber;
        this.status = status;
        this.price = price;
        this.imagePath = imagePath;
    }

    // Getters
    public String getRoomNumber() { return roomNumber; }
    public String getStatus() { return status; }
    public double getPrice() { return price; }
    public String getImagePath() { return imagePath; }
}