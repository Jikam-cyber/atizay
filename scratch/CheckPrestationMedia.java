
import java.sql.*;

public class CheckPrestationMedia {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/atizay";
        String user = "root";
        String password = "@msterlyCA002";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT * FROM media_prestation WHERE id_prestation = 1";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("Aucune média trouvé pour la prestation 1");
                }
                while (rs.next()) {
                    System.out.println("ID: " + rs.getLong("id_media") + ", URL: " + rs.getString("url_media"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
