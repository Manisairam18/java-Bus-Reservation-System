import java.sql.*;
import java.util.Scanner;

public class BusReservationSystem {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/BusReservationSystem";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Welcome to the Bus Reservation System!");

            while (true) {
                System.out.println("1. View Seats");
                System.out.println("2. Reserve Seat");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        viewSeats(connection);
                        break;
                    case 2:
                        reserveSeat(connection, scanner);
                        break;
                    case 3:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewSeats(Connection connection) throws SQLException {
        String query = "SELECT seat_number, is_reserved FROM seats";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int seatNumber = rs.getInt("seat_number");
                boolean isReserved = rs.getBoolean("is_reserved");
                System.out.println("Seat " + seatNumber + ": " + (isReserved ? "Reserved" : "Available"));
            }
        }
    }

    private static void reserveSeat(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter your name: ");
        String name = scanner.next();
        System.out.print("Enter seat number to reserve: ");
        int seatNumber = scanner.nextInt();
        System.out.print("Enter travel date (YYYY-MM-DD): ");
        String travelDate = scanner.next();
        System.out.print("Enter travel time (HH:MM:SS): ");
        String travelTime = scanner.next();

        String checkSeatQuery = "SELECT is_reserved FROM seats WHERE seat_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSeatQuery)) {
            pstmt.setInt(1, seatNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next() && rs.getBoolean("is_reserved")) {
                System.out.println("Sorry, this seat is already reserved.");
                return;
            }
        }

        String reserveSeatQuery = "INSERT INTO reservations (seat_id, passenger_name, travel_date, travel_time) VALUES ((SELECT seat_id FROM seats WHERE seat_number = ?), ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(reserveSeatQuery)) {
            pstmt.setInt(1, seatNumber);
            pstmt.setString(2, name);
            pstmt.setDate(3, Date.valueOf(travelDate));
            pstmt.setTime(4, Time.valueOf(travelTime));
            pstmt.executeUpdate();

            String updateSeatQuery = "UPDATE seats SET is_reserved = TRUE WHERE seat_number = ?";
            try (PreparedStatement updatePstmt = connection.prepareStatement(updateSeatQuery)) {
                updatePstmt.setInt(1, seatNumber);
                updatePstmt.executeUpdate();
                System.out.println("Seat reserved successfully!");
            }
        }
    }
}
