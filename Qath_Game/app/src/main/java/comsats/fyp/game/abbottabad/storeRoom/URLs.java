package comsats.fyp.game.abbottabad.storeRoom;

public class URLs {

    public static String DOMAIN = "192.168.1.8:3005";

    public static String ACCESS_ACCOUNT = "http://" + DOMAIN + "/users/login";
    public static String CREATE_ACCOUNT = "http://" + DOMAIN + "/users/register";
    public static String RESEND_EMAIL = "http://" + DOMAIN + "/users/resendEmail";
    public static String VERIFY_EMAIL = "http://" + DOMAIN + "/users/verifyEmail";
    public static String RESER_PASSWORD = "http://" + DOMAIN + "/users/resetPassword";

    public static String UPDATE_NAME = "http://" + DOMAIN + "/settings/updateName";
    public static String MAKE_PURCHASE = "http://" + DOMAIN + "/settings/makePurchase";
    public static String CHANGE_BALL = "http://" + DOMAIN + "/settings/changeBall";
    public static String UPDATE_PASSWORD = "http://" + DOMAIN + "/settings/updatePassword";
    public static String UPLOAD_PROFILE = "http://" + DOMAIN + "/uploads/profile";
    public static String USER_STATS = "http://" + DOMAIN + "/users/stats";
    public static String USER_FRIENDS = "http://" + DOMAIN + "/users/friends";
    public static String ACCEPT_FRIEND_REQUEST = "http://" + DOMAIN + "/friends/acceptRequest";
    public static String CANCEL_FRIEND_REQUEST = "http://" + DOMAIN + "/friends/cancelRequest";
    public static String REMOVE_FRIEND = "http://" + DOMAIN + "/friends/removeFriend";

    public static String UPLOADED_FILES(String name) {
        return "http://" + DOMAIN + "/uploadedFiles/" + name.trim();
    }


}
