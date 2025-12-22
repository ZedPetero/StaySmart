package application.model;

import java.sql.Timestamp;

public class Application {
    private int id;
    private int roomId;
    private int tenantId;
    private int propertyId;
    private String type;
    private String message;
    private String paymentMethod;
    private String contactNumber;
    private String status;
    private Timestamp applyDate;

    // Helper fields for display
    private String tenantName;
    private String propertyName;
    private String roomNumber;
    private int landlordId;
    private String landlordName;

    public Application(int id, int roomId, int tenantId, int propertyId, String type, String message,
                       String paymentMethod, String contactNumber, String status, Timestamp applyDate) {
        this.id = id;
        this.roomId = roomId;
        this.tenantId = tenantId;
        this.propertyId = propertyId;
        this.type = type;
        this.message = message;
        this.paymentMethod = paymentMethod;
        this.contactNumber = contactNumber;
        this.status = status;
        this.applyDate = applyDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public int getPropertyId() {
        return propertyId;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getApplyDate() {
        return applyDate;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(int landlordId) {
        this.landlordId = landlordId;
    }

    public String getLandlordName() {
        return landlordName;
    }

    public void setLandlordName(String landlordName) {
        this.landlordName = landlordName;
    }
}
