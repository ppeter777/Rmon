import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbTest {
    public static void main(String[] args) {
        String url = "jdbc:ch://10.77.184.33:8123/esu_prod_raw";
        String user = "p.pesotsky";
        String password = "Fomalhaut1976!";

        System.out.println("Попытка подключения...");
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            if (rs.next()) {
                System.out.println("УСПЕХ! Ответ от ClickHouse: " + rs.getInt(1));
            }
        } catch (Exception e) {
            System.err.println("ОШИБКА ПОДКЛЮЧЕНИЯ:");
            e.printStackTrace();
        }
    }
}

