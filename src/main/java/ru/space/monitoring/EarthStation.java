package ru.space.monitoring;

public class EarthStation {
    private final int id;          // Соответствует corresponding_sso_id и agent_id
    private final String name;     // Уникальное имя из колонки agent_name (например, "ЗС КРЛ УКВ №1")

    public EarthStation(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    // Текст, который увидит инженер эксплуатации в левой панели JavaFX
    @Override
    public String toString() {
        return name + " (ID: " + id + ")";
    }
}
