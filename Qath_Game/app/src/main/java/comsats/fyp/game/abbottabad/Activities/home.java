package comsats.fyp.game.abbottabad.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import comsats.fyp.game.abbottabad.Fragments.friendsFragment;
import comsats.fyp.game.abbottabad.Fragments.homeFragment;
import comsats.fyp.game.abbottabad.Fragments.requestFragment;
import comsats.fyp.game.abbottabad.Fragments.settingsFragment;
import comsats.fyp.game.abbottabad.Fragments.storeFragment;
import comsats.fyp.game.abbottabad.GamePlay.online_with_friends;
import comsats.fyp.game.abbottabad.Models.friendModel;
import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.RequestHandler;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.constants;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;
import comsats.fyp.game.abbottabad.storeRoom.socketConnectionHandler;
import io.realm.Realm;

public class home extends AppCompatActivity {

    String TAG = "home345";
    Socket socket;
    Realm realm;
    public static home homeObj;
    BroadcastReceiver gameRequestReceived;
    BroadcastReceiver creditReceived;
    String currentLoadedFrag = "home";
    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        if (home.homeObj != null) {
            home.homeObj.finish();
        }
        home.homeObj = this;

        realm = Realm.getDefaultInstance();

        socket = socketConnectionHandler.getInstance(getApplicationContext()).getConnection();

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        loadFragment(new homeFragment());
        navigation.setSelectedItemId(R.id.home);
        fetchUserGameInfoData();
        fetchUserFriendsData();

        creditReceived = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle pack = intent.getExtras();
                try {
                    final JSONObject ob = new JSONObject(pack.getString("data").trim());
                    AlertDialog.Builder builder = new AlertDialog.Builder(home.this);
                    builder.setMessage(ob.getString("message"));
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } catch (Exception e) {
                    Log.i(TAG, "Exception " + e);
                }
            }
        };

        gameRequestReceived = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle pack = intent.getExtras();
                try {
                    socketConnectionHandler.hasAnyRequestArrived = false;
                    final JSONObject ob = new JSONObject(pack.getString("data").trim());
                    final int friendID = ob.getInt("friendID");
                    if (!ob.getBoolean("error")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(home.this);
                        builder.setMessage(ob.getString("message"));
                        builder.setCancelable(false);

                        builder.setPositiveButton("Start Game", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Bundle pack = new Bundle();
                                pack.putInt("friendID", friendID);
                                pack.putString("way", "broadcast");
                                Intent intent = new Intent(getApplicationContext(), online_with_friends.class);
                                intent.putExtras(pack);
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                // rejectDefinedRequest
                                try {
                                    int friendID = ob.getInt("friendID");
                                    String name = preferenceManager.getInstance(getApplicationContext()).getUserName();
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("friendID", friendID);
                                    jsonObject.put("name", name);
                                    socket.emit("rejectDefinedRequest", jsonObject, new Ack() {
                                        @Override
                                        public void call(final Object... objects) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    JSONObject ob = (JSONObject) objects[0];
                                                    try {
                                                        if (!ob.getBoolean("error")) {
                                                            dialog.dismiss();
                                                            Toast.makeText(home.this, ob.getString("message"), Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (Exception e) {
                                                        Log.i(TAG, e + "");
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.i(TAG, e + "");
                                }
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, e + "");
                }
            }
        };

    }

    public void fetchUserFriendsData() {
        StringRequest request = new StringRequest(Request.Method.POST, URLs.USER_FRIENDS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, response + "");

                        try {

                            JSONObject mainObj = new JSONObject(response);
                            Boolean error = mainObj.getBoolean("error");

                            if (!error) {

                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.deleteAll();
                                    }
                                });


                                JSONArray dataObject = mainObj.getJSONArray("data");

                                for (int i = 0; i < dataObject.length(); i++) {
                                    JSONObject singleRecord = dataObject.getJSONObject(i);
                                    final friendModel model = new friendModel(
                                            singleRecord.getInt("ID"),
                                            singleRecord.getString("status"),
                                            singleRecord.getInt("sender"),
                                            singleRecord.getInt("receiver"),
                                            singleRecord.getString("text"),
                                            singleRecord.getString("receiverName"),
                                            singleRecord.getString("receiverProfile")
                                    );

                                    model.setSenderName(singleRecord.getString("senderName"));
                                    model.setSenderProfile(singleRecord.getString("senderProfile"));

                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            friendModel record = realm.where(friendModel.class).equalTo("ID", model.getID()).findFirst();
                                            if (record == null) {
                                                realm.insertOrUpdate(model);
                                            } else {
                                                record.deleteFromRealm();
                                                realm.insertOrUpdate(model);
                                            }
                                        }
                                    });

                                }

                            }

                        } catch (Exception e) {
                            Log.i(TAG, "JSON exception - " + e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                String message = "";
                if (volleyError instanceof NetworkError) {
                    message = "Please check your internet connection";
                } else if (volleyError instanceof ServerError) {
                    message = "The server could not be found";
                } else if (volleyError instanceof AuthFailureError) {
                    message = "Please check your internet connection";
                } else if (volleyError instanceof ParseError) {
                    message = "Please try again after some time";
                } else if (volleyError instanceof NoConnectionError) {
                    message = "Please check your connection";
                } else if (volleyError instanceof TimeoutError) {
                    message = "Please check your internet connection";
                } else {
                    message = "Something went wrong.\nPlease try again";
                }

                Toast.makeText(home.this, message, Toast.LENGTH_SHORT).show();
                Log.i(TAG, volleyError + "");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", preferenceManager.getInstance(getApplicationContext()).getUserID() + "");
                params.put("API_KEY", getResources().getString(R.string.server_side_api_key));
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestHandler.getInstance(this).addToRequestQueue(request);
    }

    public void fetchUserGameInfoData() {
        StringRequest request = new StringRequest(Request.Method.POST, URLs.USER_STATS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, response + "");

                        try {

                            JSONObject mainObj = new JSONObject(response);
                            Boolean error = mainObj.getBoolean("error");

                            if (!error) {

                                JSONObject user = mainObj.getJSONObject("data");

                                if (user.getInt("userID") != -99) {
                                    preferenceManager.getInstance(getApplicationContext()).setUserGameInfoID(user.getInt("ID"));
                                    preferenceManager.getInstance(getApplicationContext()).setUserCoins(user.getInt("coins"));
                                    preferenceManager.getInstance(getApplicationContext()).setGameLostCount(user.getInt("gameLost"));
                                    preferenceManager.getInstance(getApplicationContext()).setGameWonCount(user.getInt("gameWon"));

                                    preferenceManager.getInstance(getApplicationContext()).setUserCurrentBall(user.getString("currentBalls"));

                                    preferenceManager.getInstance(getApplicationContext()).setUserPurchasedGoldBalls(user.getInt("goldPurhcased") != 0);
                                    preferenceManager.getInstance(getApplicationContext()).setUserPurchasedDiamondBalls(user.getInt("diamondPurhcased") != 0);

                                    preferenceManager.getInstance(getApplicationContext()).setBestGameWon(user.getInt("best_game_won"));
                                    preferenceManager.getInstance(getApplicationContext()).setRank(user.getString("rank"));
                                }

                            }

                        } catch (Exception e) {
                            Log.i(TAG, "JSON exception - " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                String message = "";
                if (volleyError instanceof NetworkError) {
                    message = "Please check your internet connection";
                } else if (volleyError instanceof ServerError) {
                    message = "The server could not be found";
                } else if (volleyError instanceof AuthFailureError) {
                    message = "Please check your internet connection";
                } else if (volleyError instanceof ParseError) {
                    message = "Please try again after some time";
                } else if (volleyError instanceof NoConnectionError) {
                    message = "Please check your connection";
                } else if (volleyError instanceof TimeoutError) {
                    message = "Please check your internet connection";
                } else {
                    message = "Something went wrong.\nPlease try again";
                }

                Toast.makeText(home.this, message, Toast.LENGTH_SHORT).show();
                Log.i(TAG, volleyError + "");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", preferenceManager.getInstance(getApplicationContext()).getUserID() + "");
                params.put("API_KEY", getResources().getString(R.string.server_side_api_key));
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestHandler.getInstance(this).addToRequestQueue(request);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.friends:
                    currentLoadedFrag = "friends";
                    fragment = new friendsFragment();
                    break;
                case R.id.home:
                    currentLoadedFrag = "home";
                    fragment = new homeFragment();
                    break;
                case R.id.settings:
                    currentLoadedFrag = "settings";
                    fragment = new settingsFragment();
                    break;
                case R.id.requests:
                    currentLoadedFrag = "requests";
                    fragment = new requestFragment();
                    break;
                case R.id.store:
                    currentLoadedFrag = "store";
                    fragment = new storeFragment();
                    break;
            }
            return loadFragment(fragment);
        }
    };

    @Override
    public void onBackPressed() {
        if (currentLoadedFrag.trim().equalsIgnoreCase("home")) {
            super.onBackPressed();
        } else {
            currentLoadedFrag = "home";
            loadFragment(new homeFragment());
            navigation.setSelectedItemId(R.id.home);
        }
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (socketConnectionHandler.isCredited) {
            socketConnectionHandler.isCredited = false;
            try {
                final JSONObject ob = socketConnectionHandler.arrivedCreditMaterial;
                AlertDialog.Builder builder = new AlertDialog.Builder(home.this);
                builder.setMessage(ob.getString("message"));
                AlertDialog dialog = builder.create();
                dialog.show();
            } catch (Exception e) {
                Log.i(TAG, "Exception " + e);
            }
        }

        if (socketConnectionHandler.hasAnyRequestArrived) {
            socketConnectionHandler.hasAnyRequestArrived = false;
            try {
                final JSONObject ob = socketConnectionHandler.arrivedRequestMaterial;
                final int friendID = ob.getInt("friendID");
                AlertDialog.Builder builder = new AlertDialog.Builder(home.this);
                builder.setMessage(ob.getString("message"));
                builder.setCancelable(false);

                builder.setPositiveButton("Start Game", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Bundle pack = new Bundle();
                        pack.putInt("friendID", friendID);
                        pack.putString("way", "broadcast");
                        Intent intent = new Intent(getApplicationContext(), online_with_friends.class);
                        intent.putExtras(pack);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        // rejectDefinedRequest
                        try {

                            String name = preferenceManager.getInstance(getApplicationContext()).getUserName();
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("friendID", friendID);
                            jsonObject.put("name", name);
                            socket.emit("rejectDefinedRequest", jsonObject, new Ack() {
                                @Override
                                public void call(final Object... objects) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            JSONObject ob = (JSONObject) objects[0];
                                            try {
                                                if (!ob.getBoolean("error")) {
                                                    dialog.dismiss();
                                                    Toast.makeText(home.this, ob.getString("message"), Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (Exception e) {
                                                Log.i(TAG, e + "");
                                            }
                                        }
                                    });
                                }
                            });
                        } catch (Exception e) {
                            Log.i(TAG, e + "");
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } catch (Exception e) {
                Log.i(TAG, "Exception " + e);
            }
        }
        Log.i("tetser12312", "Resume is called boss");
        if (getApplicationContext() != null) {
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((gameRequestReceived), new IntentFilter(constants.BROADCAST_GAME_REQUEST_RECEIVED));
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((creditReceived), new IntentFilter(constants.CREDIT_RECEIVED_BROADCAST));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getApplicationContext() != null) {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(gameRequestReceived);
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(creditReceived);
        }
    }
}
