package ru.space.monitoring;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class LocalDatabaseManager {

    // База в виде файла local_telemetry_db.mv.db в папке проекта
    private static final String URL = "jdbc:h2:./local_telemetry_db;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        System.out.println("Initializing local H2 database...");
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Создаем таблицу сеансов связи
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS mpss_sso_session_hist (
                    schedule_id INT,
                    schedule_created_at TIMESTAMP,
                    spacecraft_id INT,
                    corresponding_sso_id INT,
                    start_time TIMESTAMP,
                    end_time TIMESTAMP
                )
            """);

            // 2. Создаем таблицу сырой телеметрии ЗС КРЛ УКВ
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS telemetry_raw (
                    esu_dttm TIMESTAMP,
                    device_type VARCHAR(50),
                    device_name VARCHAR(50),
                    agent_id INT,
                    agent_name VARCHAR(50),
                    indicator_name VARCHAR(50),
                    indicator_float DOUBLE,
                    indicator_int INT
                )
            """);

            System.out.println("H2 tables successfully created/verified.");
        } catch (Exception e) {
            System.err.println("H2 initialization error:");
            e.printStackTrace();
        }
    }

    public static void importCsvData() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Очищаем старые данные, чтобы не дублировать при повторном запуске
            stmt.execute("TRUNCATE TABLE mpss_sso_session_hist");
            stmt.execute("TRUNCATE TABLE telemetry_raw");

            System.out.println("Loading communication sessions from CSV into H2...");
            stmt.execute("""
            INSERT INTO mpss_sso_session_hist 
            SELECT * FROM CSVREAD('telemetry_data/sessions_export.csv')
        """);

            System.out.println("Loading 1 million rows of telemetry from CSV into H2 (this will take a couple of seconds)...");
            stmt.execute("""
            INSERT INTO telemetry_raw
            SELECT * FROM CSVREAD('telemetry_data/telemetry_export.csv')
        """);

            System.out.println("Import completed successfully!");
        } catch (Exception e) {
            System.err.println("Error importing CSV into H2:");
            e.printStackTrace();
        }

    }

}
