package comsats.fyp.game.abbottabad.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import comsats.fyp.game.abbottabad.Activities.AboutGameRules;
import comsats.fyp.game.abbottabad.GamePlay.offline_player_vs_bot;
import comsats.fyp.game.abbottabad.GamePlay.offline_two_friends;
import comsats.fyp.game.abbottabad.GamePlay.online_random;
import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.RequestHandler;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.constants;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;

public class homeFragment extends Fragment {

    String TAG = "homeFragment";
    private GoogleSignInClient googleSignInClient;

    ImageView userProfile;
    TextView userName, gameWon, userCoins, lostGames, connectionTime, rank, best_game_won;
    SwipeRefreshLayout mSwipeRefreshLayout;
    BroadcastReceiver creditReceived, pingsCounter;

    public homeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_home, container, false);

        lostGames = view.findViewById(R.id.lostGames);
        userCoins = view.findViewById(R.id.userCoins);
        gameWon = view.findViewById(R.id.gameWon);
        userName = view.findViewById(R.id.userName);
        userProfile = view.findViewById(R.id.userProfile);
        connectionTime = view.findViewById(R.id.connectionTime);

        rank = view.findViewById(R.id.rank);
        best_game_won = view.findViewById(R.id.best_game_won);

        userName.setText(preferenceManager.getInstance(getActivity()).getUserName());
        userCoins.setText(preferenceManager.getInstance(getActivity()).getUserCoins() + "");
        gameWon.setText(preferenceManager.getInstance(getActivity()).getGameWonCount() + "");
        lostGames.setText(preferenceManager.getInstance(getActivity()).getGameLostCount() + "");

        if (!preferenceManager.getInstance(getActivity()).getRank().trim().equalsIgnoreCase("none")) {
            rank.setText("Rank : " + preferenceManager.getInstance(getActivity()).getRank().trim());
        }

        if (preferenceManager.getInstance(getActivity()).getBestGameWon() != -99) {
            String text = preferenceManager.getInstance(getActivity()).getBestGameWon() == 1 ? "Best Game won in " + preferenceManager.getInstance(getActivity()).getBestGameWon() + " move." : "Best Game won in " + preferenceManager.getInstance(getActivity()).getBestGameWon() + " moves";
            best_game_won.setText(text);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchUserGameInfoData();
            }
        });

        creditReceived = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                userCoins.setText(preferenceManager.getInstance(getActivity()).getUserCoins() + "");
            }
        };

        pingsCounter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle pack = intent.getExtras();
                try {
                    final JSONObject ob = new JSONObject(pack.getString("data").trim());
                    int min = ob.getInt("pings") / 60;
                    connectionTime.setText("Connectivity " + min + " min / 1 hour");
                } catch (Exception e) {
                    Log.i(TAG, "Exception " + e);
                }
            }
        };


        if (preferenceManager.getInstance(getActivity()).getUserProfileProvider().trim().equals("Qath")) {
            // use qath api to get image
            if (!preferenceManager.getInstance(getActivity()).getUserProfile().trim().equals(constants.UNDEFINED)) {
                if (!preferenceManager.getInstance(getActivity()).getUserProfile().trim().equals("none")) {
                    Picasso.get().
                            load(URLs.UPLOADED_FILES(preferenceManager.getInstance(getActivity()).getUserProfile().trim()))
                            .placeholder(R.drawable.user)
                            .error(R.drawable.user)
                            .into(userProfile);
                }
            }
        } else {
            if (!preferenceManager.getInstance(getActivity()).getUserProfile().trim().equals(constants.UNDEFINED)) {
                if (!preferenceManager.getInstance(getActivity()).getUserProfile().trim().equals("none")) {
                    Picasso.get().
                            load(preferenceManager.getInstance(getActivity()).getUserProfile().trim())
                            .placeholder(R.drawable.user)
                            .error(R.drawable.user)
                            .into(userProfile);
                }
            }
        }


        view.findViewById(R.id.playOnlineRandom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), online_random.class);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.playOfflineWithBot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), offline_player_vs_bot.class));
            }
        });

        view.findViewById(R.id.playOfflineWithFriend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), offline_two_friends.class));
            }
        });

        return view;
    }

    public void open_about(View view) {
        startActivity(new Intent(getActivity(), AboutGameRules.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            userCoins.setText(preferenceManager.getInstance(getActivity()).getUserCoins() + "");
            gameWon.setText(preferenceManager.getInstance(getActivity()).getGameWonCount() + "");
            lostGames.setText(preferenceManager.getInstance(getActivity()).getGameLostCount() + "");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver((creditReceived), new IntentFilter(constants.CREDIT_RECEIVED_BROADCAST));
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver((pingsCounter), new IntentFilter(constants.PINGS_COUNTER));
            connectionTime.setText("");

            if (preferenceManager.getInstance(getActivity()).getGameWonCount() > 20 && preferenceManager.getInstance(getActivity()).getGameWonCount() <= 60) {
                preferenceManager.getInstance(getActivity()).setRank("Platinum");
            } else if (preferenceManager.getInstance(getActivity()).getGameWonCount() > 60) {
                preferenceManager.getInstance(getActivity()).setRank("Crown");
            }

            if (!preferenceManager.getInstance(getActivity()).getRank().trim().equalsIgnoreCase("none")) {
                rank.setText("Rank : " + preferenceManager.getInstance(getActivity()).getRank().trim());
            }

            if (preferenceManager.getInstance(getActivity()).getBestGameWon() != -99) {
                String text = preferenceManager.getInstance(getActivity()).getBestGameWon() == 1 ? "Best Game won in " + preferenceManager.getInstance(getActivity()).getBestGameWon() + " move." : "Best Game won in " + preferenceManager.getInstance(getActivity()).getBestGameWon() + " moves";
                best_game_won.setText(text);
            } else {
                best_game_won.setText("");
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(creditReceived);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(pingsCounter);
    }

    public void fetchUserGameInfoData() {
        if (getActivity() != null) {

            userCoins.setText(preferenceManager.getInstance(getActivity()).getUserCoins() + "");
            gameWon.setText(preferenceManager.getInstance(getActivity()).getGameWonCount() + "");
            lostGames.setText(preferenceManager.getInstance(getActivity()).getGameLostCount() + "");


            StringRequest request = new StringRequest(Request.Method.POST, URLs.USER_STATS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i(TAG, response + "");
                            mSwipeRefreshLayout.setRefreshing(false);
                            try {

                                JSONObject mainObj = new JSONObject(response);
                                Boolean error = mainObj.getBoolean("error");

                                if (!error) {

                                    JSONObject user = mainObj.getJSONObject("data");

                                    if (user.getInt("userID") != -99) {
                                        preferenceManager.getInstance(getActivity()).setUserGameInfoID(user.getInt("ID"));
                                        preferenceManager.getInstance(getActivity()).setUserCoins(user.getInt("coins"));
                                        preferenceManager.getInstance(getActivity()).setGameLostCount(user.getInt("gameLost"));
                                        preferenceManager.getInstance(getActivity()).setGameWonCount(user.getInt("gameWon"));

                                        preferenceManager.getInstance(getActivity()).setUserCurrentBall(user.getString("currentBalls"));

                                        preferenceManager.getInstance(getActivity()).setUserPurchasedGoldBalls(user.getInt("goldPurhcased") != 0);
                                        preferenceManager.getInstance(getActivity()).setUserPurchasedDiamondBalls(user.getInt("diamondPurhcased") != 0);

                                        preferenceManager.getInstance(getActivity()).setBestGameWon(user.getInt("best_game_won"));
                                        preferenceManager.getInstance(getActivity()).setRank(user.getString("rank"));

                                        userCoins.setText(preferenceManager.getInstance(getActivity()).getUserCoins() + "");
                                        gameWon.setText(preferenceManager.getInstance(getActivity()).getGameWonCount() + "");
                                        lostGames.setText(preferenceManager.getInstance(getActivity()).getGameLostCount() + "");

                                        if (!preferenceManager.getInstance(getActivity()).getRank().trim().equalsIgnoreCase("none")) {
                                            rank.setText("Rank : " + preferenceManager.getInstance(getActivity()).getRank().trim());
                                        }

                                        if (preferenceManager.getInstance(getActivity()).getBestGameWon() != -99) {
                                            String text = preferenceManager.getInstance(getActivity()).getBestGameWon() == 1 ? "Best Game won in " + preferenceManager.getInstance(getActivity()).getBestGameWon() + " move." : "Best Game won in " + preferenceManager.getInstance(getActivity()).getBestGameWon() + " moves";
                                            best_game_won.setText(text);
                                        }
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
                    Log.i(TAG, volleyError + "");
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("userID", preferenceManager.getInstance(getActivity()).getUserID() + "");
                    params.put("API_KEY", getResources().getString(R.string.server_side_api_key));
                    return params;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestHandler.getInstance(getActivity()).addToRequestQueue(request);
        }
    }

}
