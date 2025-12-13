package application.model;

public class Room {
    private String roomNumber;
    private String status;
    private double price;
    private String imagePath;
    private String facilities;
    private String paymentStatus; // <--- NEW FIELD

    // Updated Constructor with 6 arguments
    public Room(String roomNumber, String status, double price, String imagePath, String facilities, String paymentStatus) {
        this.roomNumber = roomNumber;
        this.status = status;
        this.price = price;
        this.imagePath = imagePath;
        this.facilities = facilities;
        this.paymentStatus = paymentStatus;
    }

    public String getRoomNumber() { return roomNumber; }
    public String getStatus() { return status; }
    public double getPrice() { return price; }
    public String getImagePath() { return imagePath; }
    public String getFacilities() { return facilities; }
    public String getPaymentStatus() { return paymentStatus; } // <--- NEW GETTER

    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}