package ru.space.monitoring;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class MonitoringViewModel {
    private final H2TelemetryRepository repository;
    private final ObservableList<SessionTelemetry> sessionDataList = FXCollections.observableArrayList();

    private Integer selectedStationId = null; // Теперь храним числовой ID
    private String selectedDate = null;
    private String selectedMetric = null;

    public MonitoringViewModel(H2TelemetryRepository repository) {
        this.repository = repository;
    }

    public ObservableList<SessionTelemetry> getSessionDataList() {
        return sessionDataList;
    }

    // Метод принимает int из дерева сети
    public void setSelectedStationId(int stationId) {
        this.selectedStationId = stationId;
        triggerDataReload();
    }

    public void setSelectedDate(String dateStr) {
        this.selectedDate = dateStr;
        triggerDataReload();
    }

    public void setSelectedMetric(String metricName) {
        this.selectedMetric = metricName;
        triggerDataReload();
    }

    private void triggerDataReload() {
        if (selectedStationId == null || selectedDate == null) return;

        new Thread(() -> {
            // Передаем числовой ID и дату с началом суток в репозиторий
            List<SessionTelemetry> results = repository.fetchSessionsData(selectedStationId, selectedDate + " 00:00:00");

            Platform.runLater(() -> {
                sessionDataList.setAll(results);
            });
        }).start();
    }
}


