package application.model;

public class Room {
    private String roomNumber;
    private String status;      // e.g., "Occupied", "Available"
    private Double price;
    private String type;        // e.g., "Apartment"
    private String paymentStatus; // e.g., "Paid", "Pending"
    private String imagePath;
    private String facilities;  // e.g., "Bed, Aircon, WiFi"

    // Constructor used in HouseViewController
    public Room(String roomNumber, String status, Double price, String facilities, String paymentStatus, String imagePath) {
        this.roomNumber = roomNumber;
        this.status = status;
        this.price = price;
        this.facilities = facilities;
        this.paymentStatus = paymentStatus;
        this.imagePath = imagePath;
    }

    // Getters and Setters
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }
}