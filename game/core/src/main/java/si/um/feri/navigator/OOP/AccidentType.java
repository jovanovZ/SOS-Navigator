package si.um.feri.navigator.OOP;

public enum AccidentType {
    PROMETNA("prometna"),
    NARAVNA_NESRECA("naravna nesreča"),
    ZDRAVSTVENI_PRIMER("zdravstveni primer"),
    KRIMINAL("kriminal");

    public final String dbValue;

    AccidentType(String dbValue) {
        this.dbValue = dbValue;
    }

    public static AccidentType fromDbValue(String v) {
        if (v == null) return PROMETNA;
        String s = v.trim().toLowerCase();
        for (AccidentType t : values()) {
            if (t.dbValue.equals(s)) return t;
        }
        return PROMETNA;
    }
}
