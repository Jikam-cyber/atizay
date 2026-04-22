
import java.sql.*;

public class CheckMediaUrls {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/atizay";
        String user = "root";
        String password = "@msterlyCA002";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("--- SALON MEDIA ---");
            String querySalon = "SELECT * FROM media_salon WHERE id_salon = 1 LIMIT 1";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(querySalon)) {
                while (rs.next()) {
                    System.out.println("Salon Media URL: " + rs.getString("url_media"));
                }
            }
            System.out.println("--- PRESTATION MEDIA ---");
            String queryPrestation = "SELECT * FROM media_prestation WHERE id_prestation = 1 LIMIT 1";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(queryPrestation)) {
                while (rs.next()) {
                    System.out.println("Prestation Media URL: " + rs.getString("url_media"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
