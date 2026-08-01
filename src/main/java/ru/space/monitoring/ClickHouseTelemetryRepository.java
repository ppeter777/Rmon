package ru.space.monitoring;

import ru.space.monitoring.SessionTelemetry;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class ClickHouseTelemetryRepository {
    private static final String URL = "jdbc:ch://10.77.184.33:8123/esu_prod_raw";
    private static final String USER = "p.pesotsky";
    private static final String PASSWORD = "Fomalhaut1976!";

    public List<SessionTelemetry> fetchSessionsData(int ignoredSsoId, String ignoredDateStr) {
        List<SessionTelemetry> sessions = new ArrayList<>();

        // Прописываем всё руками, БЕЗ знаков "?"
        // Замените 101 на реальный ID, а дату на вашу целевую дату
        String sql = """
                SELECT
                  toStartOfSecond(toTimeZone(esu_dttm, 'Europe/Moscow')),
                  device_type,
                  device_name,
                  agent_name,
                  indicator_name,
                  indicator_float
                FROM esu_prod_raw.telemetry_metrics_view
                WHERE device_type = 'gs_positioner.turntable.currentPosition'
                  AND agent_name = 'ЗС Магадан v3'
                  and esu_dttm > '2026-07-29T10:00:00'
                  and indicator_name = 'azimuth'
                ORDER BY esu_dttm ASC
                LIMIT 10
        """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement(); // Используем обычный Statement вместо PreparedStatement
             ResultSet rs = stmt.executeQuery(sql)) { // Выполняем напрямую

            while (rs.next()) {
                // Переводим timestamp Кликхауса (секунды) в LocalDateTime для графиков
                java.time.Instant instant = java.time.Instant.ofEpochSecond(rs.getLong("t"));
                java.time.LocalDateTime localDateTime = java.time.LocalDateTime.ofInstant(
                        instant, java.time.ZoneId.of("Europe/Moscow")
                );

                sessions.add(new SessionTelemetry(
                        "STATION_101", // заглушка ssoId
                        "SAT_UNKNOWN",  // заглушка spacecraftId
                        "SCH_UNKNOWN",  // заглушка scheduleId
                        localDateTime,
                        localDateTime,  // end_time равен start_time для сырых точек
                        rs.getDouble("session_power"),
                        rs.getDouble("session_avg_swr")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessions;
    }
}

