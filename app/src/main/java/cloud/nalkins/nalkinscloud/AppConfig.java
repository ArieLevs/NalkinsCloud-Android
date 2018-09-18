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
class AppConfig {

    //static String ENVIRONMENT= System.getenv("environment");
    static String ENVIRONMENT= "alpha";

    //Web Server IP / Domain name
    final static String APP_NAME = "NalkinsCloud";

    private static String DOMAIN_NAME = ""; // DOMAIN_NAME set at readProperties function

    // SSL Certificate configs
    static String SERVER_SSL_CRT_FILE = ""; // SERVER_SSL_CRT_FILE set at readProperties function
    static String SERVER_SSL_CRT_HOST_NAME = DOMAIN_NAME; // SERVER_SSL_CRT_HOST_NAME set at readProperties function

    // Web Server URIs
    private static String SERVER_URI_PATH = ""; // SERVER_URI_PATH set at readProperties function

    static String URL_AUTHENTICATION = SERVER_URI_PATH + "/token/"; // Client Authentication
    static String URL_REVOKE_TOKEN = SERVER_URI_PATH + "/revoke_token/"; // Revoke clients Token
    static String URL_REGISTER = SERVER_URI_PATH + "/register/";
    static String URL_FORGOT_PASSWORD = SERVER_URI_PATH + "/forgot_password/";
    static String URL_RESET_PASSWORD = SERVER_URI_PATH + "/reset_password/";
    static String URL_GET_DEVICE_PASS = SERVER_URI_PATH + "/get_device_pass/";
    static String URL_REMOVE_DEVICE = SERVER_URI_PATH + "/remove_device/";
    static String URL_ACTIVATION = SERVER_URI_PATH + "/device_activation/";
    static String URL_DEVICE_LIST = SERVER_URI_PATH + "/device_list/";
    static String URL_UPDATE_DEVICE_PASS = SERVER_URI_PATH + "/update_device_pass/";
    static String URL_HEALTH_CHECK = SERVER_URI_PATH + "/health_check/";
    static String URL_SET_SCHEDULED_JOB = SERVER_URI_PATH + "/set_scheduled_job/";
    static String URL_GET_SCHEDULED_JOB = SERVER_URI_PATH + "/get_scheduled_job/";

    // OAuth Client ID
    static String OAUTH_CLIENT_ID = ""; // OAUTH_CLIENT_ID set at readProperties function
    // Client secret
    static String OAUTH_CLIENT_SECRET = ""; // OAUTH_CLIENT_SECRET set at readProperties function

    // Mosquitto Server configs
    private static String BROKER_DOMAIN_NAME = ""; // BROKER_DOMAIN_NAME set at readProperties function
    private static String MQTT_SERVER_SSL_PORT = ""; // MQTT_SERVER_SSL_PORT set at readProperties function
    static String MQTT_SERVER_URI = ""; // MQTT_SERVER_URI set at readProperties function
    static String MQTT_SERVER_BKS_FILE = ""; // MQTT_SERVER_BKS_FILE set at readProperties function
    static String MQTT_SERVER_BKS_PASSWORD = ""; // MQTT_SERVER_BKS_PASSWORD set at readProperties function
    static int MESSAGE_QOS_1 = 1;
    static boolean RETAINED_MESSAGE = true;
    static boolean NOT_RETAINED_MESSAGE = false;
    static int REQUESTS_TIMEOUT = 15000; // Indicate the maximum time to wait for mqtt client actions to complete

    // Device Access Point configs
    static String DEVICE_MQTT_SERVER = BROKER_DOMAIN_NAME; // DEVICE_MQTT_SERVER set at readProperties function
    static String DEVICE_MQTT_PORT = MQTT_SERVER_SSL_PORT; // DEVICE_MQTT_PORT set at readProperties function
    static String DEVICE_GET_ID = "http://10.0.0.1:80/returnid";
    static String DEVICE_SETUP = "http://10.0.0.1:80/autoconfig";
    static String DEVICE_AP_SSID = "ESP8266";
    static String DEVICE_AP_PASS = "nalkinscloud";

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

        DOMAIN_NAME = getProperty("domain_name");

        SERVER_SSL_CRT_FILE = getProperty("server_ssl_crt_file_name");
        SERVER_SSL_CRT_HOST_NAME = DOMAIN_NAME;

        SERVER_URI_PATH = getProperty("server_uri_path") + DOMAIN_NAME;

        URL_AUTHENTICATION = SERVER_URI_PATH + "/token/";
        URL_REVOKE_TOKEN = SERVER_URI_PATH + "/revoke_token/";
        URL_REGISTER = SERVER_URI_PATH + "/register/";
        URL_FORGOT_PASSWORD = SERVER_URI_PATH + "/forgot_password/";
        URL_RESET_PASSWORD = SERVER_URI_PATH + "/reset_password/";
        URL_GET_DEVICE_PASS = SERVER_URI_PATH + "/get_device_pass/";
        URL_REMOVE_DEVICE = SERVER_URI_PATH + "/remove_device/";
        URL_ACTIVATION = SERVER_URI_PATH + "/device_activation/";
        URL_DEVICE_LIST = SERVER_URI_PATH + "/device_list/";
        URL_UPDATE_DEVICE_PASS = SERVER_URI_PATH + "/update_device_pass/";
        URL_HEALTH_CHECK = SERVER_URI_PATH + "/health_check/";
        URL_SET_SCHEDULED_JOB = SERVER_URI_PATH + "/set_scheduled_job/";
        URL_GET_SCHEDULED_JOB = SERVER_URI_PATH + "/get_scheduled_job/";

        OAUTH_CLIENT_ID = getProperty("oauth_client_id");
        OAUTH_CLIENT_SECRET = getProperty("oauth_client_secret");

        BROKER_DOMAIN_NAME = getProperty("broker_domain_name");
        MQTT_SERVER_SSL_PORT = getProperty("mqtt_server_ssl_port");
        MQTT_SERVER_URI = "ssl://" + BROKER_DOMAIN_NAME + ":" + MQTT_SERVER_SSL_PORT;
        MQTT_SERVER_BKS_FILE = getProperty("mqtt_server_bks_file_name");
        MQTT_SERVER_BKS_PASSWORD = getProperty("mqtt_server_bks_password");

        DEVICE_MQTT_SERVER = BROKER_DOMAIN_NAME;
        DEVICE_MQTT_PORT = MQTT_SERVER_SSL_PORT;
    }
}
