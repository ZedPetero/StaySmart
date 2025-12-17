// Debug script to test room numbering logic
import java.util.*;

public class debug_room_numbering {
    public static void main(String[] args) {
        System.out.println("=== ROOM NUMBERING DEBUG ===");
        
        // Test scenarios based on user reports
        testCase1();
        testCase2();
    }
    
    // Case 1: Room 301 exists but shows 301 again
    private static void testCase1() {
        System.out.println("\n--- CASE 1: Room 301 exists ---");
        int floorLevel = 3;
        int maxRoomNum = 301; // What the query might return
        int nextRoomNumber = (maxRoomNum == 0) ? (floorLevel * 100) + 1 : maxRoomNum + 1;
        
        System.out.println("Floor level: " + floorLevel);
        System.out.println("Max room from DB: " + maxRoomNum);
        System.out.println("Calculated next: " + nextRoomNumber);
        
        // What if the query returns NULL or 0?
        int maxRoomNumNull = 0;
        int nextRoomNumberNull = (maxRoomNumNull == 0) ? (floorLevel * 100) + 1 : maxRoomNumNull + 1;
        System.out.println("If query returns NULL/0: " + nextRoomNumberNull);
    }
    
    // Case 2: Room 104 exists but shows 109
    private static void testCase2() {
        System.out.println("\n--- CASE 2: Room 104 exists ---");
        int floorLevel = 1;
        int maxRoomNum = 104; // What the query should return
        int nextRoomNumber = (maxRoomNum == 0) ? (floorLevel * 100) + 1 : maxRoomNum + 1;
        
        System.out.println("Floor level: " + floorLevel);
        System.out.println("Max room from DB: " + maxRoomNum);
        System.out.println("Calculated next: " + nextRoomNumber);
        
        // What if there's a logic error?
        // Maybe the query is filtering wrong or there's a calculation issue
        System.out.println("Possible issues:");
        System.out.println("- SQL query might be filtering wrong floor");
        System.out.println("- CAST might be failing");
        System.out.println("- There might be duplicate room numbers");
    }
}
