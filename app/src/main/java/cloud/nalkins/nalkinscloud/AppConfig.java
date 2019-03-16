package cloud.nalkins.nalkinscloud;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Main configuration file
 *
 * Created by Arie on 3/8/2017.
 *
 */
public class AppConfig {

    //static String ENVIRONMENT= System.getenv("environment");
    static String ENVIRONMENT= "dev"; // production, alpha, dev

    public final static String APP_NAME = "NalkinsCloud";

    // SSL Certificate configs
    static boolean TRUST_ALL_CERTIFICATES = false; // TRUST_ALL_CERTIFICATES set at readProperties function
    static String SERVER_SSL_CRT_FILE = ""; // SERVER_SSL_CRT_FILE set at readProperties function

    // Web Server URIs
    public static String API_SERVER_HOST = ""; // API_SERVER_HOST set at readProperties function
    private static String API_SERVER_PORT = ""; // API_SERVER_PORT set at readProperties function
    private static String API_SERVER_PROTOCOL = ""; // API_SERVER_PROTOCOL set at readProperties function
    private static String API_SERVER_URI = ""; // API_SERVER_URI set at readProperties function

    public static String URL_AUTHENTICATION = API_SERVER_URI + "/token/"; // Client Authentication
    static String URL_REVOKE_TOKEN = API_SERVER_URI + "/revoke_token/"; // Revoke clients Token
    public static String URL_REGISTER = API_SERVER_URI + "/register/";
    public static String URL_FORGOT_PASSWORD = API_SERVER_URI + "/forgot_password/";
    public static String URL_RESET_PASSWORD = API_SERVER_URI + "/reset_password/";
    public static String URL_GET_DEVICE_PASS = API_SERVER_URI + "/get_device_pass/";
    static String URL_REMOVE_DEVICE = API_SERVER_URI + "/remove_device/";
    public static String URL_ACTIVATION = API_SERVER_URI + "/device_activation/";
    static String URL_DEVICE_LIST = API_SERVER_URI + "/device_list/";
    static String URL_UPDATE_DEVICE_PASS = API_SERVER_URI + "/update_device_pass/";
    public static String URL_HEALTH_CHECK = API_SERVER_URI + "/health_check/";
    static String URL_SET_SCHEDULED_JOB = API_SERVER_URI + "/set_scheduled_job/";
    static String URL_GET_SCHEDULED_JOB = API_SERVER_URI + "/get_scheduled_job/";

    // OAuth Client ID
    public static String OAUTH_CLIENT_ID = ""; // OAUTH_CLIENT_ID set at readProperties function
    // Client secret
    public static String OAUTH_CLIENT_SECRET = ""; // OAUTH_CLIENT_SECRET set at readProperties function

    // Mosquitto Server configs
    public static String MQTT_SERVER_HOST = ""; // MQTT_SERVER_HOST set at readProperties function
    public static String MQTT_SERVER_PORT = ""; // MQTT_SERVER_PORT set at readProperties function
    static String MQTT_SERVER_PROTOCOL = ""; // MQTT_SERVER_PROTOCOL set at readProperties function
    public static boolean MQTT_ENCRYPTED = true; // MQTT_ENCRYPTED set at readProperties function
    static String MQTT_SERVER_URI = ""; // MQTT_SERVER_URI set at readProperties function
    static String MQTT_SERVER_BKS_FILE = ""; // MQTT_SERVER_BKS_FILE set at readProperties function
    static String MQTT_SERVER_BKS_PASSWORD = ""; // MQTT_SERVER_BKS_PASSWORD set at readProperties function
    static int MESSAGE_QOS_1 = 1;
    static boolean RETAINED_MESSAGE = true;
    static boolean NOT_RETAINED_MESSAGE = false;
    static int REQUESTS_TIMEOUT = 15000; // Indicate the maximum time to wait for mqtt client actions to complete

    // Device Access Point configs
    public static String DEVICE_GET_ID = "http://10.0.0.1:80/return_id";
    public static String DEVICE_SETUP = "http://10.0.0.1:80/autoconfig";
    public static String DEVICE_AP_SSID = "ESP8266";
    public static String DEVICE_AP_PASS = "nalkinscloud";

    static String NALKINS_CLOUD_ANDROID_README_URL = "https://github.com/ArieLevs/NalkinsCloud-Android/blob/master/README.md";
    static String NALKINS_CLOUD_ANDROID_LICENSE_URL = "https://github.com/ArieLevs/NalkinsCloud-Android/blob/master/LICENSE";

    private static Properties properties;

    private static String getProperty(String key) throws NullPointerException {
        return properties.getProperty(key);
    }

    public static void readProperties(Context context) throws IOException {

        String propertiesFileName;

        switch (ENVIRONMENT) {
            case "alpha": {
                propertiesFileName = "alpha.properties";
            }
            break;
            case "dev": {
                propertiesFileName = "dev.properties";
            }
            break;
            case "production": {
                propertiesFileName = "production.properties";
            }
            break;
            default: {
                propertiesFileName = "";
            }
            break;
        }
        properties = new Properties();

        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(propertiesFileName);
        properties.load(inputStream);

        TRUST_ALL_CERTIFICATES = Boolean.parseBoolean(getProperty("trust_all_certificates"));

        // API configurations read
        SERVER_SSL_CRT_FILE = getProperty("server_ssl_crt_file_name");

        API_SERVER_HOST = getProperty("api_server_host");
        API_SERVER_PORT = getProperty("api_server_port");
        API_SERVER_PROTOCOL = getProperty("api_server_protocol");
        API_SERVER_URI = API_SERVER_PROTOCOL + "://" + API_SERVER_HOST + ":" + API_SERVER_PORT;

        URL_AUTHENTICATION = API_SERVER_URI + "/token/";
        URL_REVOKE_TOKEN = API_SERVER_URI + "/revoke_token/";
        URL_REGISTER = API_SERVER_URI + "/register/";
        URL_FORGOT_PASSWORD = API_SERVER_URI + "/forgot_password/";
        URL_RESET_PASSWORD = API_SERVER_URI + "/reset_password/";
        URL_GET_DEVICE_PASS = API_SERVER_URI + "/get_device_pass/";
        URL_REMOVE_DEVICE = API_SERVER_URI + "/remove_device/";
        URL_ACTIVATION = API_SERVER_URI + "/device_activation/";
        URL_DEVICE_LIST = API_SERVER_URI + "/device_list/";
        URL_UPDATE_DEVICE_PASS = API_SERVER_URI + "/update_device_pass/";
        URL_HEALTH_CHECK = API_SERVER_URI + "/health_check/";
        URL_SET_SCHEDULED_JOB = API_SERVER_URI + "/set_scheduled_job/";
        URL_GET_SCHEDULED_JOB = API_SERVER_URI + "/get_scheduled_job/";

        OAUTH_CLIENT_ID = getProperty("oauth_client_id");
        OAUTH_CLIENT_SECRET = getProperty("oauth_client_secret");

        // MQTT broker configs read
        MQTT_SERVER_HOST = getProperty("mqtt_server_host");
        MQTT_SERVER_PORT = getProperty("mqtt_server_port");
        MQTT_SERVER_PROTOCOL = getProperty("mqtt_server_protocol");
        MQTT_SERVER_URI = MQTT_SERVER_PROTOCOL + "://" + MQTT_SERVER_HOST + ":" + MQTT_SERVER_PORT;
        MQTT_SERVER_BKS_FILE = getProperty("mqtt_server_bks_file_name");
        MQTT_SERVER_BKS_PASSWORD = getProperty("mqtt_server_bks_password");

        // In case ssl used set encryption flag to true
        MQTT_ENCRYPTED = MQTT_SERVER_PROTOCOL.equals("ssl");
    }
}
