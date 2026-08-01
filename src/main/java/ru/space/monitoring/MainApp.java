package ru.space.monitoring;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ru.space.monitoring.ClickHouseTelemetryRepository;
import ru.space.monitoring.MonitoringViewModel;
import ru.space.monitoring.MultiMetricChartPane;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        LocalDatabaseManager.initializeDatabase();
        LocalDatabaseManager.importCsvData(); // можно закомментировать после первого успешного запуска

// 2. Подключаем H2-репозиторий во ViewModel
        H2TelemetryRepository repository = new H2TelemetryRepository();
        MonitoringViewModel viewModel = new MonitoringViewModel(repository);
        // Инициализируем слои ВРУЧНУЮ (одна строчка вместо аннотаций @Autowired)

        // Создаем компоненты интерфейса
        BorderPane mainWindow = new BorderPane();

        // Левая панель (Элементы сети и метрики)
        TreeView<EarthStation> networkTree = createNetworkTree(viewModel);
        ListView<String> metricsList = createMetricsList(viewModel);
        SplitPane sidebarSplit = new SplitPane(new TabPane(new Tab("Сеть", networkTree)), metricsList);
        sidebarSplit.setDividerPositions(0.6);

        // Верхняя панель управления (Выбор даты вместо application.properties)
        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());
        datePicker.setOnAction(e -> {
            // При изменении даты уведомляем ViewModel
            viewModel.setSelectedDate(datePicker.getValue().toString());
        });
        // Устанавливаем начальную дату во ViewModel при старте
        viewModel.setSelectedDate(datePicker.getValue().toString());

        // Центральная панель (Наш мульти-метрический график)
        MultiMetricChartPane chartPane = new MultiMetricChartPane();
        // Связываем (Bind) график со списком данных во ViewModel
        chartPane.bindData(viewModel.getSessionDataList());

        // Сборка главного окна
        mainWindow.setTop(new ToolBar(new Label("Дата анализа:"), datePicker));
        mainWindow.setLeft(sidebarSplit);
        mainWindow.setCenter(chartPane);

        // Запуск
        primaryStage.setScene(new Scene(mainWindow, 1300, 850));
        primaryStage.setTitle("Утилита экспресс-анализа телеметрии ЦКС (ClickHouse MVP)");
        primaryStage.show();
    }

    private TreeView<EarthStation> createNetworkTree(MonitoringViewModel viewModel) {
        TreeItem<EarthStation> root = new TreeItem<>(new EarthStation(0, "Земные станции"));

        TreeItem<EarthStation> station1 = new TreeItem<>(new EarthStation(14, "ЗС Москва v3"));
        TreeItem<EarthStation> station2 = new TreeItem<>(new EarthStation(18, "ЗС Сочи v3"));
        TreeItem<EarthStation> station3 = new TreeItem<>(new EarthStation(19, "ЗС Анадырь v3"));
        TreeItem<EarthStation> station4 = new TreeItem<>(new EarthStation(20, "ЗС Новый Уренгой v3"));
        TreeItem<EarthStation> station5 = new TreeItem<>(new EarthStation(21, "ЗС Якутск v3"));
        TreeItem<EarthStation> station6 = new TreeItem<>(new EarthStation(22, "ЗС Красноярск v3"));
        TreeItem<EarthStation> station7 = new TreeItem<>(new EarthStation(23, "ЗС Кандалакша v3"));
        TreeItem<EarthStation> station8 = new TreeItem<>(new EarthStation(24, "ЗС Магадан v3"));

        root.getChildren().addAll(station1, station2, station3, station4, station5, station6, station7, station8);

        TreeView<EarthStation> tree = new TreeView<>(root);
        tree.setShowRoot(false);

        tree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.isLeaf()) {
                EarthStation selected = newVal.getValue();
                // Отправляем числовой ID в логику
                viewModel.setSelectedStationId(selected.getId());
            }
        });
        return tree;
    }

    private ListView<String> createMetricsList(MonitoringViewModel viewModel) {
        ListView<String> list = new ListView<>();
        list.getItems().addAll("Fwd_Power", "SWR", "SNR"); // Имена как в БД

        list.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.setSelectedMetric(newVal);
            }
        });
        return list;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
