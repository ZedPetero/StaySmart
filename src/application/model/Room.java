package application.model;

public class Room {
    private int id;             // Database Primary Key
    private int propertyId;     // Database Foreign Key
    private String roomNumber;
    private String status;
    private Double price;
    private String paymentStatus;
    private String imagePath;
    private String facilities;

    // Updated Constructor
    public Room(int id, int propertyId, String roomNumber, String status, Double price, String facilities, String paymentStatus, String imagePath) {
        this.id = id;
        this.propertyId = propertyId;
        this.roomNumber = roomNumber;
        this.status = status;
        this.price = price;
        this.facilities = facilities;
        this.paymentStatus = paymentStatus;
        this.imagePath = imagePath;
    }

    // NEW GETTERS REQUIRED BY THE CONTROLLER
    public int getId() { return id; }
    public int getPropertyId() { return propertyId; }

    // Existing Getters
    public String getRoomNumber() { return roomNumber; }
    public String getStatus() { return status; }
    public Double getPrice() { return price; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getImagePath() { return imagePath; }
    public String getFacilities() { return facilities; }
}