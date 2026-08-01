package ru.space.monitoring;

// Модель одного отработанного сеанса связи с ключевыми метриками
public record SessionTelemetry(
        String ssoId,
        String spacecraftId,
        String scheduleId,
        java.time.LocalDateTime startLocal,
        java.time.LocalDateTime endLocal,
        double sessionPower,
        double sessionAvgSwr
) {}
