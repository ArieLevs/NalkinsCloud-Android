package cloud.nalkins.nalkinscloud;

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

    private final static String DOMAIN_NAME = "api-alpha.nalkins.cloud";
//    private final static String DOMAIN_NAME = "10.0.2.2:8000";

    // SSL Certificate configs
    static String SERVER_SSL_CRT_FILE = "nalkins.cloud.pem";
    static String SERVER_SSL_CRT_HOST_NAME = DOMAIN_NAME;

    // Web Server URIs
    private final static String SERVER_URI_PATH = "https://" + DOMAIN_NAME;
//    private final static String SERVER_URI_PATH = "http://" + DOMAIN_NAME;
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
    static String OAUTH_CLIENT_ID = "";
    // Client secret
    static String OAUTH_CLIENT_SECRET = "";

    // Mosquitto Server configs
    private static String BROKER_DOMAIN_NAME = "mosquitto.nalkins.cloud";
    private static String MQTT_SERVER_SSL_PORT = "8883";
    static String MQTT_SERVER_URI = "ssl://" + BROKER_DOMAIN_NAME + ":" + MQTT_SERVER_SSL_PORT;
    static String MQTT_SERVER_BKS_FILE = "mosquitto.nalkins.cloud.crt.bks";
    static String MQTT_SERVER_BKS_PASSWORD = "eOa4CVGx8dVHfeS4wSQaO";
    static int MESSAGE_QOS_1 = 1;
    static boolean RETAINED_MESSAGE = true;
    static boolean NOT_RETAINED_MESSAGE = false;
    static int REQUESTS_TIMEOUT = 15000; // Indicate the maximum time to wait for mqtt client actions to complete

    // Device Access Point configs
    static String DEVICE_MQTT_SERVER = BROKER_DOMAIN_NAME;
    static String DEVICE_MQTT_PORT = MQTT_SERVER_SSL_PORT;
    static String DEVICE_GET_ID = "http://10.0.0.1:80/returnid";
    static String DEVICE_SETUP = "http://10.0.0.1:80/autoconfig";
    static String DEVICE_AP_SSID = "ESP8266";
    static String DEVICE_AP_PASS = "nalkinscloud";

    static String NALKINS_CLOUD_ANDROID_README_URL = "https://github.com/ArieLevs/NalkinsCloud-Android/blob/master/README.md";
    static String NALKINS_CLOUD_ANDROID_LICENSE_URL = "https://github.com/ArieLevs/NalkinsCloud-Android/blob/master/LICENSE";

}
