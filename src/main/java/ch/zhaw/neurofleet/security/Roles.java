package ch.zhaw.neurofleet.security;

public final class Roles {

    private Roles() {
        // Prevent instantiation
    }

    public static final String ADMIN = "admin";
    public static final String OWNER = "owner";
    public static final String FLEETMANAGER = "fleetmanager";
    public static final String DRIVER = "driver";
}
