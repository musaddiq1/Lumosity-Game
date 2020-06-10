package comsats.fyp.game.abbottabad.storeRoom;

import android.content.Context;
import android.content.SharedPreferences;


public class preferenceManager {

    private final String STORAGE = "comsats.fyp.game.abbottabad.storeRoom.preferenceManager.utils";
    private SharedPreferences preferences;
    private static preferenceManager instance;
    private SharedPreferences.Editor editor;

    public static preferenceManager getInstance(Context context) {
        if (instance == null)
            instance = new preferenceManager(context.getApplicationContext());
        return instance;
    }

    private preferenceManager(Context context) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void setActiveUser() {
        editor.putBoolean(constants.USER_LOGIN_STATUS, true);
        editor.apply();
    }

    public void removeActiveUser() {
        editor.putBoolean(constants.USER_LOGIN_STATUS, false);
        editor.putString(constants.USER_NAME, constants.UNDEFINED);
        editor.putString(constants.USER_EMAIL, constants.UNDEFINED);
        editor.putString(constants.USER_PROVIDER, constants.UNDEFINED);
        editor.putString(constants.USER_PROFILE, constants.UNDEFINED);
        editor.putString(constants.USER_PROVIDER_ID, constants.UNDEFINED);
        editor.putString(constants.USER_PASSWORD, constants.UNDEFINED);
        editor.putInt(constants.USER_ID, -99);
        editor.putInt(constants.USER_GAME_INFO_ID, -99);
        editor.putInt(constants.GAME_LOST, -99);
        editor.putInt(constants.GAME_WON, -99);
        editor.putInt(constants.USER_COINS, -99);
        editor.putString(constants.USER_CURRENT_SELECTED_BALL, constants.SILVER_BALL);
        editor.putBoolean(constants.GOLD_BALL_PURCHASE, false);
        editor.putBoolean(constants.DIAMOND_BALL_PURCHASE, false);
        editor.putInt(constants.BEST_GAME_WON, -99);
        editor.putString(constants.RANK, "none");
        editor.putBoolean(constants.GAME_SOUND, true);
        editor.apply();
    }

    public boolean isUserActive() {
        return preferences.getBoolean(constants.USER_LOGIN_STATUS, false);
    }

    public boolean hasUserPurchasedGoldBalls() {
        return preferences.getBoolean(constants.GOLD_BALL_PURCHASE, false);
    }

    public boolean hasUserPurhcasedDiamondBalls() {
        return preferences.getBoolean(constants.DIAMOND_BALL_PURCHASE, false);
    }

    public void setUserPurchasedGoldBalls(Boolean a) {
        editor.putBoolean(constants.GOLD_BALL_PURCHASE, a);
        editor.apply();
    }

    public void setUserPurchasedDiamondBalls(Boolean a) {
        editor.putBoolean(constants.DIAMOND_BALL_PURCHASE, a);
        editor.apply();
    }

    public void setGameSound(Boolean a) {
        editor.putBoolean(constants.GAME_SOUND, a);
        editor.apply();
    }

    public Boolean isGameSoundEnabled() {
        return preferences.getBoolean(constants.GAME_SOUND, true);
    }

    public void setRank(String a) {
        editor.putString(constants.RANK, a);
        editor.apply();
    }

    public String getRank() {
        return preferences.getString(constants.RANK, "none");
    }

    public void setBestGameWon(int a) {
        editor.putInt(constants.BEST_GAME_WON, a);
        editor.apply();
    }

    public int getBestGameWon() {
        return preferences.getInt(constants.BEST_GAME_WON, -99);
    }

    public void setUserProvider(String a) {
        editor.putString(constants.USER_PROVIDER, a);
        editor.apply();
    }

    public String getUserProvider() {
        return preferences.getString(constants.USER_PROVIDER, constants.UNDEFINED);
    }

    public void setUserCurrentBall(String a) {
        editor.putString(constants.USER_CURRENT_SELECTED_BALL, a);
        editor.apply();
    }

    public String getUserCurrentBall() {
        return preferences.getString(constants.USER_CURRENT_SELECTED_BALL, constants.SILVER_BALL);
    }


    public void setAccountStatus(String a) {
        editor.putString(constants.ACCOUNT_STATUS, a);
        editor.apply();
    }

    public String getAccountStatus() {
        return preferences.getString(constants.ACCOUNT_STATUS, constants.UNDEFINED);
    }

    public void setUserProviderID(String a) {
        editor.putString(constants.USER_PROVIDER_ID, a);
        editor.apply();
    }

    public String getUserProviderID() {
        return preferences.getString(constants.USER_PROVIDER_ID, constants.UNDEFINED);
    }

    public void setUserPassword(String a) {
        editor.putString(constants.USER_PASSWORD, a);
        editor.apply();
    }

    public String getUserPassword() {
        return preferences.getString(constants.USER_PASSWORD, constants.UNDEFINED);
    }

    public void setUserProfileProvider(String a) {
        editor.putString(constants.PROFILE_PROVIDER, a);
        editor.apply();
    }

    public String getUserProfileProvider() {
        return preferences.getString(constants.PROFILE_PROVIDER, constants.UNDEFINED);
    }

    public void setUserProfile(String a) {
        editor.putString(constants.USER_PROFILE, a);
        editor.apply();
    }

    public String getUserProfile() {
        return preferences.getString(constants.USER_PROFILE, constants.UNDEFINED);
    }

    public void setUserID(int id) {
        editor.putInt(constants.USER_ID, id);
        editor.apply();
    }

    public int getUserID() {
        return preferences.getInt(constants.USER_ID, -99);
    }

    public void setUserGameInfoID(int id) {
        editor.putInt(constants.USER_GAME_INFO_ID, id);
        editor.apply();
    }

    public int getUserGameInfoID() {
        return preferences.getInt(constants.USER_GAME_INFO_ID, -99);
    }

    public void setUserCoins(int id) {
        editor.putInt(constants.USER_COINS, id);
        editor.apply();
    }

    public int getUserCoins() {
        return preferences.getInt(constants.USER_COINS, -99);
    }

    public void setGameWonCount(int id) {
        editor.putInt(constants.GAME_WON, id);
        editor.apply();
    }

    public int getGameWonCount() {
        return preferences.getInt(constants.GAME_WON, -99);
    }


    public void setGameLostCount(int id) {
        editor.putInt(constants.GAME_LOST, id);
        editor.apply();
    }

    public int getGameLostCount() {
        return preferences.getInt(constants.GAME_LOST, -99);
    }


    public String getUserName() {
        return preferences.getString(constants.USER_NAME, constants.UNDEFINED);
    }


    public void setUserName(String name) {
        editor.putString(constants.USER_NAME, name);
        editor.apply();
    }

    public String getUserEmail() {
        return preferences.getString(constants.USER_EMAIL, constants.UNDEFINED);
    }

    public void setUserEmail(String m) {
        editor.putString(constants.USER_EMAIL, m);
        editor.apply();
    }

    public void setUSERDeviceToken(String text) {
        editor.putString(constants.FIREBASE_TOKEN, text);
        editor.apply();
    }

    public String getUSERDeivceToken() {
        return preferences.getString(constants.FIREBASE_TOKEN, constants.UNDEFINED);
    }

    public void set_notification_status(Boolean z) {
        editor.putBoolean(constants.NOTIFICATION_DEVICE, z);
        editor.apply();
    }

    public Boolean isNotificationsEnabled() {
        return preferences.getBoolean(constants.NOTIFICATION_DEVICE, true);
    }

    public void setKeyToken(String text) {
        editor.putString(constants.KEY_TOKEN, text);
        editor.apply();
    }

    public String getKeyToken() {
        return preferences.getString(constants.KEY_TOKEN, constants.UNDEFINED);
    }

}
