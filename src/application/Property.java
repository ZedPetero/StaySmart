package application;

public class Property {
    private int id;
    private String name;
    private String location;
    private double price;
    private String type;        // <--- Added this
    private String floors;      // <--- Added this (useful for your floor logic later)
    private byte[] imageData;

    // Updated Constructor
    public Property(int id, String name, String location, double price, String type, String floors, byte[] imageData) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.price = price;
        this.type = type;       // <--- Initialize it
        this.floors = floors;   // <--- Initialize it
        this.imageData = imageData;
    }

    // --- Getters ---

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public double getPrice() { return price; }

    // 🟢 The missing method causing your error:
    public String getType() { return type; }

    public String getFloors() { return floors; }
    public byte[] getImageData() { return imageData; }
}