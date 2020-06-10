package comsats.fyp.game.abbottabad.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import comsats.fyp.game.abbottabad.Adapters.friendsAdapter;
import comsats.fyp.game.abbottabad.Adapters.selectFriendForGameAdapter;
import comsats.fyp.game.abbottabad.Models.friendModel;
import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.RequestHandler;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class selectFriendForGame extends AppCompatActivity {

    RecyclerView friendsView;
    ArrayList<friendModel> friendModels;
    selectFriendForGameAdapter adapter;
    Realm realm;
    String TAG = "selectFriend";
    RealmResults<friendModel> prevModels;
    public SwipeRefreshLayout mSwipeRefreshLayout = null;
    ProgressDialog customDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend_for_game);

        friendsView = findViewById(R.id.friends);

        customDialog = new ProgressDialog(selectFriendForGame.this);
        customDialog.setMessage("Processing");
        customDialog.setCancelable(false);

        fetchUserFriendsData();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchUserFriendsData();
            }
        });

        friendsView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        realm = Realm.getDefaultInstance();

        friendModels = new ArrayList<>();

        adapter = new selectFriendForGameAdapter(friendModels, new selectFriendForGameAdapter.OnItemClicked() {
            @Override
            public void onItemClick(View view, final int itemPosition, String type) {
            }
        });

        prevModels = realm.where(friendModel.class)
                .equalTo("status", "friends")
                .findAllAsync();

        prevModels.addChangeListener(new RealmChangeListener<RealmResults<friendModel>>() {
            @Override
            public void onChange(RealmResults<friendModel> mod) {
                Log.i(TAG, "changeListener is called " + mod.size());
                processListData(mod);
                Log.i(TAG, "I am in the last now");
            }
        });

        friendsView.setAdapter(adapter);
    }

    public void fetchUserFriendsData() {
        customDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, URLs.USER_FRIENDS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, response + "");
                        if (customDialog != null && customDialog.isShowing()) {
                            customDialog.dismiss();
                        }
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

                Toast.makeText(selectFriendForGame.this, message, Toast.LENGTH_SHORT).show();
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

    private void processListData(RealmResults<friendModel> prevModels) {
        Log.i(TAG, prevModels.size() + "");
        friendModels.clear();
        adapter.notifyDataSetChanged();
        if (prevModels != null) {
            for (final friendModel m : prevModels) {
                if (m.getStatus().trim().equalsIgnoreCase("pending") || m.getStatus().trim().equalsIgnoreCase("friends")) {
                    friendModels.add(m);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.i(TAG, "friend is past : " + m.getID() + " - " + m.getStatus());
                }
            }
        }
    }

}
