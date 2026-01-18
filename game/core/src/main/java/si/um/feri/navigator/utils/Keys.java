package si.um.feri.navigator.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class Keys {
    public static String MAPBOX = "";
    private static Dotenv dotenv = Dotenv.load();

    public static String GEOAPIFY = dotenv.get("GEOAPIFY_KEY");
    public static String SERVER_URL= dotenv.get("SERVER_URL");
    public static final String OPENROUTESERVICE = "5b3ce3597851110001cf6248e144b426a65242b68905aa92335e0183";

}
