package cloud.nalkins.nalkinscloud;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.StringTokenizer;

/**
 * Created by Arie on 3/8/2017.
 *
 * Class maintains session data across the app using the SharedPreferences
 */
public class SharedPreferences {
    // LogCat tag
    private static String TAG = SharedPreferences.class.getSimpleName();

    // Shared Preferences
    private android.content.SharedPreferences pref;

    private Editor _editor;
    private Context _context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "CloudBitToken";
    private static final String KEY_ACCESS_TOKEN = "AccessToken";
    private static final String KEY_REFRESH_TOKEN = "RefreshToken";
    private static final String KEY_USERNAME = "Username";
    private static final String KEY_MAIN_ACTIVITY = "MainActivity";

    public SharedPreferences(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        _editor = pref.edit();
    }

    /**
     * Store current token to shared preferences
     *
     * @param Token current users token
     */
    public void setToken(String Token) {
        _editor.putString(KEY_ACCESS_TOKEN, Token);
        // commit changes
        _editor.commit();
        Log.d(TAG, "Access Token: " + Token + ", stored in shared preferences.");
    }

    /**
     * Return current token from shared preferences
     * @return String users current token
     * In case KEY_ACCESS_TOKEN = null, the String "NULL" will return
     */
    public String getToken(){
        String key_access_token = pref.getString(KEY_ACCESS_TOKEN, "NULL");
        Log.d(TAG, "Access Token returned " + key_access_token + " from shared preferences.");
        return key_access_token;
    }

    /**
     * Store current refresh token to shared preferences
     *
     * @param refreshToken current users refresh token
     */
    public void setRefreshToken(String refreshToken) {
        _editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        // commit changes
        _editor.commit();
        Log.d(TAG, "Refresh Token: " + refreshToken + ", stored in shared preferences.");
    }

    /**
     * Return current refresh token from shared preferences
     * @return String users current refresh token
     * In case KEY_REFRESH_TOKEN = null, the String "NULL" will return
     */
    public String getRefreshToken(){
        String key_refresh_token = pref.getString(KEY_REFRESH_TOKEN, "NULL");
        Log.d(TAG, "Refresh Token returned " + key_refresh_token + " from shared preferences.");
        return key_refresh_token;
    }

    /**
     * Remove current token from shared preferences
     */
    public void removeToken(){
        Log.d(TAG, "Running 'removeToken'");
        _editor.remove(KEY_ACCESS_TOKEN);
        _editor.commit();
    }

    /**
     * Store current token to shared preferences
     *
     * @param username current username
     */
    public void setUsername(String username) {
        _editor.putString(KEY_USERNAME, username);
        // commit changes
        _editor.commit();
        Log.d(TAG, "Username: " + username + ", stored in shared preferences.");
    }

    /**
     * Return current username from shared preferences
     *
     * @return String users current username
     * In case KEY_USERNAME = null, the String "NULL" will return
     */
    String getUsername(){
        String key_username = pref.getString(KEY_USERNAME, "NULL");
        Log.d(TAG, "getUsername returned: " + key_username + ", from shared preferences.");
        return key_username;
    }

    void setIsMainActivityRunning(Boolean value) {
        _editor.putBoolean(KEY_MAIN_ACTIVITY, value);
        // commit changes
        _editor.commit();
    }

    Boolean getIsMainActivityRunning() {
        return pref.getBoolean(KEY_MAIN_ACTIVITY, false);
    }


    /**
     * Remove current username from shared preferences
     */
    public void removeUsername(){
        Log.d(TAG, "Running 'removeUsername'");
        _editor.remove(KEY_USERNAME);
        _editor.commit();
    }

    // ####### DISTILLERY PART START ################
    // Store automation job parameters
    private static final String AUTOMATION = "automation";
    private static final String AUTOMATION_ERROR = "automation_error";
    private static final String CUSTOM_TEMP_CONFIGURED = "custom_temp_configured";
    private static final String CUSTOM_TEMP_VALUES = "custom_temp_values";

    private static final String MAIN_HEATER_TEMP = "main_heater_temp";
    private static final String DISPOSAL_START_TEMP = "disposal_start_temp";
    private static final String DISPOSAL_END_TEMP = "disposal_end_temp";
    private static final String SECONDARY_START_TEMP = "secondary_start_temp";
    private static final String COOLER_START_TEMP = "cooler_start_temp";


    void setAutomation(Boolean value) {
        _editor.putBoolean(AUTOMATION, value);
        // commit changes
        _editor.commit();
        Log.d(TAG, "setAutomation stored: " + value + ", in shared preferences.");
    }

    void setAutomationError(Boolean value) {
        _editor.putBoolean(AUTOMATION_ERROR, value);
        // commit changes
        _editor.commit();
        Log.d(TAG, "setAutomationError stored: " + value + ", in shared preferences.");
    }

    Boolean getAutomationError(){
        Boolean automation_error = pref.getBoolean(AUTOMATION_ERROR, false);
        Log.d(TAG, "getAutomationError returned: " + automation_error + ", from shared preferences.");
        return automation_error;
    }

    public Boolean getAutomation(){
        Boolean automation = pref.getBoolean(AUTOMATION, false);
        Log.d(TAG, "getAutomation returned: " + automation + ", from shared preferences.");
        return automation;
    }

    public void setIsCustomTempConfigured(Boolean value) {
        _editor.putBoolean(CUSTOM_TEMP_CONFIGURED, value);
        _editor.commit();
        Log.d(TAG, "setIsCustomTempConfigured stored: " + value + ", in shared preferences.");
    }

    boolean getIsCustomTempConfigured() {
        Boolean customTemp = pref.getBoolean(CUSTOM_TEMP_CONFIGURED, false);
        Log.d(TAG, "getIsCustomTempConfigured returned: " + customTemp + ", from shared preferences.");
        return customTemp;
    }

    public void setCustomTempValues(int[] values) {
        StringBuilder str = new StringBuilder();
        for( int value : values) {
                str.append(value).append(",");
        }
        _editor.putString(CUSTOM_TEMP_VALUES, str.toString());
        _editor.commit();
        Log.d(TAG, "setIsCustomTempConfigured stored: " + str + ", in shared preferences.");
    }

    public String getCustomTempValues() {
        String custom_temp_values = pref.getString(CUSTOM_TEMP_VALUES, "94,60,80,80");
        Log.d(TAG, "getCustomTempValues returned: " + custom_temp_values + ", from shared preferences.");
        return custom_temp_values;
    }
    // ####### DISTILLERY PART END ################


    // ####### MAGNET PART START ################
    private static final String IS_RELEASED_TRIGGERED = "is_released_triggered";

    void setIsReleasedTriggered(String value) {
        _editor.putString(IS_RELEASED_TRIGGERED, value);
        // commit changes
        _editor.commit();
        Log.d(TAG, "is_released_triggered stored: " + value + ", in shared preferences.");
    }

    String getIsReleasedTriggered() {
        String is_released_triggered = pref.getString(IS_RELEASED_TRIGGERED, "NULL");
        Log.d(TAG, "is_released_triggered returned: " + is_released_triggered + ", from shared preferences.");
        return is_released_triggered;
    }
    // ####### MAGNET PART END ################
}
