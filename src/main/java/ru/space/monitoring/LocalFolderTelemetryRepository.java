package ru.space.monitoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LocalFolderTelemetryRepository {
    private static final String DATA_FOLDER_PATH = "telemetry_data";

    // В Superset даты обычно выгружаются в формате "yyyy-MM-dd HH:mm:ss"
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<SessionTelemetry> fetchSessionsData(int ssoId, String checkDateStr) {
        List<SessionTelemetry> sessions = new ArrayList<>();
        File folder = new File(DATA_FOLDER_PATH);

        if (!folder.exists()) {
            folder.mkdir();
            return sessions;
        }

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (files == null || files.length == 0) return sessions;

        for (File file : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isHeader = true;

                while ((line = br.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false; // Пропускаем строку заголовков
                        continue;
                    }

                    // Регулярное выражение для корректного разделения CSV с учетом возможных кавычек
                    String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (columns.length < 7) continue;

                    // Чистим кавычки и пробелы вокруг значений
                    int currentSsoId = Integer.parseInt(clean(columns[0]));
                    if (currentSsoId != ssoId) continue; // Фильтр по выбранной ЗС

                    String spacecraftId = clean(columns[1]);
                    String scheduleId = clean(columns[2]);

                    LocalDateTime startLocal = LocalDateTime.parse(clean(columns[3]), formatter);
                    LocalDateTime endLocal = LocalDateTime.parse(clean(columns[4]), formatter);

                    // Защита от пустых значений NULL в телеметрии
                    double sessionPower = parseDoubleSafe(columns[5]);
                    double sessionAvgSwr = parseDoubleSafe(columns[6]);

                    // Фильтр по дате календаря (если выбран конкретный день)
                    if (checkDateStr != null && !startLocal.toLocalDate().toString().equals(checkDateStr)) {
                        continue;
                    }

                    sessions.add(new SessionTelemetry(
                            String.valueOf(currentSsoId), spacecraftId, scheduleId,
                            startLocal, endLocal, sessionPower, sessionAvgSwr
                    ));
                }
            } catch (Exception e) {
                System.err.println("Ошибка в файле: " + file.getName() + " -> " + e.getMessage());
            }
        }
        return sessions;
    }

    private String clean(String value) {
        if (value == null) return "";
        return value.replace("\"", "").trim();
    }

    private double parseDoubleSafe(String value) {
        String cleaned = clean(value);
        if (cleaned.isEmpty() || cleaned.equalsIgnoreCase("null")) return 0.0;
        return Double.parseDouble(cleaned);
    }
}
