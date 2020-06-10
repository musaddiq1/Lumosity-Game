package comsats.fyp.game.abbottabad.storeRoom;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.login;

public class socketConnectionHandler {

    private static socketConnectionHandler mInstance;
    private static Context mCtx;
    private static Socket socket = null;
    private static final String TAG = "socketConnection123";
    static int beat = 0;
    Timer timer;
    public static Boolean hasUIExpired = false;
    public static Boolean hasAnyRequestArrived = false;
    public static Boolean isCredited = false;
    public static JSONObject arrivedRequestMaterial = null;
    public static JSONObject arrivedCreditMaterial = null;
    public static Boolean isConnectionLost = false;

    private socketConnectionHandler(Context context) {
        mCtx = context;
    }

    public static synchronized socketConnectionHandler getInstance(Context context) {
        if (mInstance == null) {
            Log.i(TAG, "Creating object");
            mCtx = context;
            mInstance = new socketConnectionHandler(context);
            try {
                String SERVER_URL = "http://" + URLs.DOMAIN;
                mInstance.socket = IO.socket(SERVER_URL);
                mInstance.socket.connect();

                String token = preferenceManager.getInstance(mCtx).getKeyToken();

                final JSONObject userJoiningReqObject = new JSONObject();
                userJoiningReqObject.put("API_KEY", mCtx.getResources().getString(R.string.server_side_api_key));
                userJoiningReqObject.put("token", token);

                Log.i(TAG, "PAYLOAD PREPARED");

                if (token.trim().equals(constants.UNDEFINED)) {

                    preferenceManager.getInstance(mCtx).removeActiveUser();
                    Intent intent = new Intent(mCtx, login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mCtx.startActivity(intent);
                } else {

                    mInstance.socket.emit("join", userJoiningReqObject, new Ack() {
                        @Override
                        public void call(Object... args) {
                            try {
                                if (!((JSONObject) args[0]).getBoolean("error")) {
                                    //good user
                                } else {
                                    preferenceManager.getInstance(mCtx).removeActiveUser();
                                    Intent intent = new Intent(mCtx, login.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mCtx.startActivity(intent);
                                    Log.i(TAG, "USER is not good person");
                                }
                            } catch (Exception e) {
                                Log.i(TAG, "Exception : " + e);
                            }
                        }
                    });
                }

                mInstance.socket.on("requestReceivedForDefinedGame", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            hasAnyRequestArrived = true;
                            JSONObject o = (JSONObject) args[0];
                            arrivedRequestMaterial = o;
                            Intent intent = new Intent(constants.BROADCAST_GAME_REQUEST_RECEIVED);
                            intent.putExtra("data", String.valueOf(o));
                            LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
                        } catch (Exception e) {
                            Log.i(TAG, e + "");
                        }
                    }
                });

                mInstance.socket.on("creditGenerated", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            preferenceManager.getInstance(mCtx).setUserCoins(preferenceManager.getInstance(mCtx).getUserCoins() + 500);
                            isCredited = true;
                            JSONObject o = (JSONObject) args[0];
                            arrivedCreditMaterial = o;
                            Intent intent = new Intent(constants.CREDIT_RECEIVED_BROADCAST);
                            intent.putExtra("data", String.valueOf(o));
                            LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
                        } catch (Exception e) {
                            Log.i(TAG, e + "");
                        }
                    }
                });

                mInstance.socket.on("pingsCounter", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            JSONObject o = (JSONObject) args[0];
                            Intent intent = new Intent(constants.PINGS_COUNTER);
                            intent.putExtra("data", String.valueOf(o));
                            LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
                        } catch (Exception e) {
                            Log.i(TAG, e + "");
                        }
                    }
                });

                mInstance.socket.on("ping", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {

                            getOnlineFriendsData();
                            mInstance._timer();
                            isConnectionLost = false;
                            isCredited = false;
                            hasUIExpired = false;

                            mInstance.socket.emit("pong", userJoiningReqObject, new Ack() {
                                @Override
                                public void call(Object... args) {
                                    try {


                                        if (!((JSONObject) args[0]).getBoolean("error")) {
                                            //good user
                                        } else {
                                            preferenceManager.getInstance(mCtx).removeActiveUser();
                                            Intent intent = new Intent(mCtx, login.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            mCtx.startActivity(intent);
                                            Log.i(TAG, "USER is not good person");
                                        }
                                    } catch (Exception e) {
                                        Log.i(TAG, "Exception : " + e);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            Log.i(TAG, "Exception " + e);
                        }
                    }


                });

            } catch (Exception e) {
                Log.i(TAG, "Exception " + e);
            }// try catch ends here
        }
        return mInstance;
    }


    public void _timer() {
        int milisInAMinute = 15 * 1000;

        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                isConnectionLost = true;
                hasUIExpired = true;
                Intent intent = new Intent(constants.BROADCAST_CONNECTION_LOST_WITH_SERVER);
                LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
            }
        }, milisInAMinute);

    }

    public static Socket getConnection() {
        return mInstance.socket;
    }

    private static void getOnlineFriendsData() {
        mInstance.socket.emit("onlineFriends", new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject o = (JSONObject) args[0];
                    Intent intent = new Intent(constants.BROADCAST_ONLINE_FRIENDS);
                    intent.putExtra("data", String.valueOf(o));
                    LocalBroadcastManager.getInstance(mCtx).sendBroadcast(intent);
                } catch (Exception e) {
                    Log.i(TAG, e + "");
                }
            }
        });
    }
}