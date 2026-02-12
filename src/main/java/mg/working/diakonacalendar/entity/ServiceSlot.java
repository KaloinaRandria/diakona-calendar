package mg.working.diakonacalendar.entity;

public enum ServiceSlot {
    SERVICE_1(1),
    SERVICE_2(2);

    private final int code;
    ServiceSlot(int code) { this.code = code; }
    public int getCode() { return code; }

    public static ServiceSlot fromCode(int code) {
        return switch (code) {
            case 1 -> SERVICE_1;
            case 2 -> SERVICE_2;
            default -> throw new IllegalArgumentException("Service invalide: " + code);
        };
    }

    public ServiceSlot opposite() {
        return this == SERVICE_1 ? SERVICE_2 : SERVICE_1;
    }
}
