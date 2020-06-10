package comsats.fyp.game.abbottabad.Fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import comsats.fyp.game.abbottabad.Activities.home;
import comsats.fyp.game.abbottabad.Adapters.friendsAdapter;
import comsats.fyp.game.abbottabad.Models.friendModel;
import comsats.fyp.game.abbottabad.Models.onlineFriendModel;
import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.RequestHandler;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.constants;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;
import comsats.fyp.game.abbottabad.storeRoom.socketConnectionHandler;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class friendsFragment extends Fragment {

    RecyclerView friendsView;
    ArrayList<friendModel> friendModels;
    friendsAdapter adapter;
    Realm realm;
    String TAG = "friendsFragment";
    RealmResults<friendModel> prevModels;
    public SwipeRefreshLayout mSwipeRefreshLayout = null;
    ProgressDialog customDialog;

    BroadcastReceiver friendsOnline, CONNECTION_LOST;
    ArrayList<onlineFriendModel> onlineFriendModelArrayList;

    public friendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate is called");

        realm = Realm.getDefaultInstance();

        friendModels = new ArrayList<>();
        onlineFriendModelArrayList = new ArrayList<>();

        adapter = new friendsAdapter(friendModels, onlineFriendModelArrayList, new friendsAdapter.OnItemClicked() {
            @Override
            public void onItemClick(View view, final int itemPosition, String type) {
                if (type.trim().equalsIgnoreCase("removeFriend")) {
                    String name = "";
                    if (preferenceManager.getInstance(getActivity()).getUserID() == friendModels.get(itemPosition).getSender()) {
                        // show receiver data
                        name = friendModels.get(itemPosition).getReceiverName();
                    } else {
                        // show sender data
                        name = friendModels.get(itemPosition).getSenderName();
                    }


                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Are you sure you want to remove " + name + " from friends list?");
                    builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            makeApiCall(friendModels.get(itemPosition).getID(), URLs.REMOVE_FRIEND, "removeFriend");
                        }
                    });

                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
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

        friendsOnline = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle pack = intent.getExtras();
                try {
                    onlineFriendModelArrayList.clear();
                    adapter.notifyDataSetChanged();
                    JSONObject ob = new JSONObject(pack.getString("data").trim());
                    JSONArray jsonArray = ob.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject singleOb = jsonArray.getJSONObject(i);

                        onlineFriendModel model = new onlineFriendModel(
                                singleOb.getInt("friendID"),
                                singleOb.getString("last_seen"),
                                singleOb.getString("state")
                        );
                        if (!onlineFriendModelArrayList.contains(model)) {
                            onlineFriendModelArrayList.add(model);
                            adapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, e + "");
                }
            }
        };

        CONNECTION_LOST = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onlineFriendModelArrayList.clear();
                adapter.notifyDataSetChanged();
            }
        };

    }

    public void makeApiCall(final int ID, String URL_TYPE, final String type) {
        customDialog.show();
        if (getActivity() != null) {

            StringRequest request = new StringRequest(Request.Method.POST, URL_TYPE,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                if (getActivity() != null && customDialog != null && customDialog.isShowing()) {
                                    customDialog.dismiss();
                                }

                                JSONObject jsonObject = new JSONObject(response);
                                Boolean error = jsonObject.getBoolean("error");
                                if (!error) {

                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            friendModel record = realm.where(friendModel.class).equalTo("ID", ID).findFirst();
                                            if (type.trim().equalsIgnoreCase("removeFriend")) {
                                                if (record != null) {
                                                    record.deleteFromRealm();
                                                    Toast.makeText(getActivity(), "Removed from friend list", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    });

                                } else {
                                    showMessage(jsonObject.getString("message"));
                                }
                            } catch (Exception e) {
                                Log.i(TAG, "JSON exception\n" + e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    customDialog.dismiss();
                    String message = "";
                    if (volleyError instanceof NetworkError) {
                        message = "Cannot connect to Internet.\nPlease check your connection!";
                    } else if (volleyError instanceof ServerError) {
                        message = "The server could not be found.\nPlease try again after some time!!";
                    } else if (volleyError instanceof AuthFailureError) {
                        message = "Cannot connect to Internet.\nPlease check your connection!";
                    } else if (volleyError instanceof ParseError) {
                        message = "Parsing error! Please try again after some time!!";
                    } else if (volleyError instanceof NoConnectionError) {
                        message = "Cannot connect to Internet.\nPlease check your connection!";
                    } else if (volleyError instanceof TimeoutError) {
                        message = "Connection TimeOut.\nPlease check your internet connection.";
                    } else {
                        message = "Ooops\nSomething went wrong.\nPlease try again";
                    }
                    showMessage(message);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/json");
                    params.put("token", preferenceManager.getInstance(getActivity()).getKeyToken());
                    params.put("API_KEY", getResources().getString(R.string.server_side_api_key));
                    params.put("ID", ID + "");
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

    private void showMessage(String message) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(message);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView is called");
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        friendsView = view.findViewById(R.id.friends);

        customDialog = new ProgressDialog(getActivity());
        customDialog.setMessage("Processing");
        customDialog.setCancelable(false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchDataFromSeverBro();
            }
        });

        friendsView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (adapter != null) {
            friendsView.setAdapter(adapter);
        }
        return view;
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

    public void fetchDataFromSeverBro() {
        home.homeObj.fetchUserFriendsData();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("tetser12312", "Resume is called boss");

        if (socketConnectionHandler.hasUIExpired) {
            onlineFriendModelArrayList.clear();
            adapter.notifyDataSetChanged();
        }


        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver((friendsOnline), new IntentFilter(constants.BROADCAST_ONLINE_FRIENDS));
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver((CONNECTION_LOST), new IntentFilter(constants.BROADCAST_CONNECTION_LOST_WITH_SERVER));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(friendsOnline);
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(CONNECTION_LOST);
        }
    }
}
