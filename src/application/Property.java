package application;

public class Property {
    private int id;
    private String name;
    private String location;
    private double price;
    private String type;
    private String floors;
    private String imagePath; // ✅ PATH ONLY

    public Property(int id, String name, String location, double price,
                    String type, String floors, String imagePath) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.price = price;
        this.type = type;
        this.floors = floors;
        this.imagePath = imagePath;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public double getPrice() { return price; }
    public String getType() { return type; }
    public String getFloors() { return floors; }
    public String getImagePath() { return imagePath; } // ✅
}
