package ru.space.monitoring;
import javafx.collections.ObservableList;
import javafx.scene.chart.CategoryAxis; // Либо NumberAxis / Tool Axis
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

public class MultiMetricChartPane extends VBox {
    private final LineChart<Number, Number> powerChart;
    private final LineChart<Number, Number> swrChart;

    public MultiMetricChartPane() {
        // Настройка графиков
        NumberAxis xAxisPower = new NumberAxis();
        NumberAxis yAxisPower = new NumberAxis();
        yAxisPower.setLabel("Макс. Мощность (session_power)");
        powerChart = new LineChart<>(xAxisPower, yAxisPower);
        powerChart.setTitle("Мониторинг мощности сеансов");

        NumberAxis xAxisSwr = new NumberAxis();
        NumberAxis yAxisSwr = new NumberAxis();
        yAxisSwr.setLabel("Средний КСВ (session_avg_swr)");
        swrChart = new LineChart<>(xAxisSwr, yAxisSwr);
        swrChart.setTitle("Коэффициент стоячей волны");

        // Добавляем оба графика в вертикальный контейнер
        this.getChildren().addAll(powerChart, swrChart);
        this.setSpacing(10);
    }

    // Подписка на данные из ViewModel
    public void bindData(ObservableList<SessionTelemetry> sessions) {
        sessions.addListener((javafx.collections.ListChangeListener<SessionTelemetry>) c -> {
            powerChart.getData().clear();
            swrChart.getData().clear();

            XYChart.Series<Number, Number> powerSeries = new XYChart.Series<>();
            powerSeries.setName("Мощность сеанса");

            XYChart.Series<Number, Number> swrSeries = new XYChart.Series<>();
            swrSeries.setName("Средний КСВ");

            int index = 1;
            for (SessionTelemetry session : sessions) {
                // В качестве оси X для MVP можно взять просто порядковый номер сеанса за день
                // Или перевести startLocal в эпохальные секунды: session.startLocal().toEpochSecond(...)
                long xValue = index++;

                powerSeries.getData().add(new XYChart.Data<>(xValue, session.sessionPower()));
                swrSeries.getData().add(new XYChart.Data<>(xValue, session.sessionAvgSwr()));
            }

            powerChart.getData().add(powerSeries);
            swrChart.getData().add(swrSeries);
        });
    }
}

