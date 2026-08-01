package ru.space.monitoring;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class H2TelemetryRepository {

    public List<SessionTelemetry> fetchSessionsData(int ssoId, String checkDateStr) {
        List<SessionTelemetry> sessions = new ArrayList<>();

        // Адаптированный под H2 ваш оригинальный аналитический запрос
        String sql = """
            SELECT 
                s.corresponding_sso_id, s.spacecraft_id, s.schedule_id,
                s.start_time, s.end_time,
                MAX(CASE WHEN t.indicator_name = 'fwd_power' THEN t.indicator_int END) AS session_power,
                AVG(CASE WHEN t.indicator_name = 'swr' THEN t.indicator_float END) AS session_avg_swr
            FROM mpss_sso_session_hist AS s
            LEFT JOIN telemetry_raw AS t 
                ON s.corresponding_sso_id = t.agent_id
               AND t.esu_dttm BETWEEN s.start_time AND s.end_time
            WHERE s.corresponding_sso_id = ? 
              AND CAST(s.start_time AS DATE) = CAST(? AS DATE)
            GROUP BY s.corresponding_sso_id, s.spacecraft_id, s.start_time, s.end_time, s.schedule_id
            ORDER BY s.start_time ASC
            """;

        try (Connection conn = LocalDatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ssoId);
            stmt.setString(2, checkDateStr); // Передаем дату (например "2026-07-28")

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(new SessionTelemetry(
                            String.valueOf(rs.getInt("corresponding_sso_id")),
                            rs.getString("spacecraft_id"),
                            rs.getString("schedule_id"),
                            rs.getTimestamp("start_time").toLocalDateTime(),
                            rs.getTimestamp("end_time").toLocalDateTime(),
                            rs.getDouble("session_power"),
                            rs.getDouble("session_avg_swr")
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка выполнения SQL-запроса в локальной H2:");
            e.printStackTrace();
        }
        return sessions;
    }
}
