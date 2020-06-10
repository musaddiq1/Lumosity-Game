package comsats.fyp.game.abbottabad.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.RequestHandler;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.constants;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;

public class storeFragment extends Fragment {

    Button silverBalls_use, goldBalls_use, goldBalls_buy, diamondBalls_use, diamondBalls_buy;

    String TAG = "storeFragment";

    public storeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store, container, false);

        silverBalls_use = view.findViewById(R.id.silverBalls_use);
        goldBalls_use = view.findViewById(R.id.goldBalls_use);
        goldBalls_buy = view.findViewById(R.id.goldBalls_buy);
        diamondBalls_use = view.findViewById(R.id.diamondBalls_use);
        diamondBalls_buy = view.findViewById(R.id.diamondBalls_buy);

        if (preferenceManager.getInstance(getActivity()).getUserCurrentBall().trim().equalsIgnoreCase(constants.SILVER_BALL)) {
            silverBalls_use.setText("Using");
        } else if (preferenceManager.getInstance(getActivity()).getUserCurrentBall().trim().equalsIgnoreCase(constants.GOLD_BALL)) {
            goldBalls_use.setText("Using");
        } else if (preferenceManager.getInstance(getActivity()).getUserCurrentBall().trim().equalsIgnoreCase(constants.DIAMOND_BALL)) {
            diamondBalls_use.setText("Using");
        }

        if (preferenceManager.getInstance(getActivity()).hasUserPurchasedGoldBalls()) {
            goldBalls_buy.setVisibility(View.GONE);
        } else {
            goldBalls_use.setVisibility(View.GONE);
        }

        if (preferenceManager.getInstance(getActivity()).hasUserPurhcasedDiamondBalls()) {
            diamondBalls_buy.setVisibility(View.GONE);
        } else {
            diamondBalls_use.setVisibility(View.GONE);
        }

        silverBalls_use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferenceManager.getInstance(getActivity()).getUserCurrentBall().trim().equalsIgnoreCase(constants.SILVER_BALL)) {
                    Toast.makeText(getActivity(), "Already using silver balls", Toast.LENGTH_SHORT).show();
                } else {
                    changeBall(constants.SILVER_BALL);

                }
            }
        });

        goldBalls_use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferenceManager.getInstance(getActivity()).getUserCurrentBall().trim().equalsIgnoreCase(constants.GOLD_BALL)) {
                    Toast.makeText(getActivity(), "Already using gold balls", Toast.LENGTH_SHORT).show();
                } else {
                    changeBall(constants.GOLD_BALL);

                }
            }
        });


        diamondBalls_use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferenceManager.getInstance(getActivity()).getUserCurrentBall().trim().equalsIgnoreCase(constants.DIAMOND_BALL)) {
                    Toast.makeText(getActivity(), "Already using diamond balls", Toast.LENGTH_SHORT).show();
                } else {
                    changeBall(constants.DIAMOND_BALL);

                }
            }
        });

        goldBalls_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseBall(constants.GOLD_BALL);
            }
        });

        diamondBalls_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseBall(constants.DIAMOND_BALL);
            }
        });

        return view;
    }

    public void purchaseBall(final String type) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setMessage("Making purchase");
        dialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, URLs.MAKE_PURCHASE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, response + "");
                        if (getActivity() != null && dialog != null && dialog.isShowing()) {
                            dialog.cancel();
                        }
                        try {
                            JSONObject mainObj = new JSONObject(response);
                            Boolean error = mainObj.getBoolean("error");
                            if (!error) {

                                if (type.trim().equalsIgnoreCase(constants.DIAMOND_BALL)) {
                                    preferenceManager.getInstance(getActivity()).setUserCoins(preferenceManager.getInstance(getActivity()).getUserCoins() - 80000);
                                    diamondBalls_buy.setVisibility(View.GONE);
                                    diamondBalls_use.setVisibility(View.VISIBLE);
                                    diamondBalls_use.setText("use");
                                    preferenceManager.getInstance(getActivity()).setUserPurchasedDiamondBalls(true);
                                } else if (type.trim().equalsIgnoreCase(constants.GOLD_BALL)) {
                                    preferenceManager.getInstance(getActivity()).setUserCoins(preferenceManager.getInstance(getActivity()).getUserCoins() - 30000);
                                    goldBalls_buy.setVisibility(View.GONE);
                                    goldBalls_use.setVisibility(View.VISIBLE);
                                    goldBalls_use.setText("use");
                                    preferenceManager.getInstance(getActivity()).setUserPurchasedGoldBalls(true);
                                }

                                Toast.makeText(getActivity(), mainObj.getString("message"), Toast.LENGTH_SHORT).show();
                            } else {
                                showMessage(mainObj.getString("message"));
                            }

                        } catch (Exception e) {
                            Log.i(TAG, "JSON exception - " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (getActivity() != null && dialog != null && dialog.isShowing()) {
                    dialog.cancel();
                }
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

                showMessage(message);
                Log.i(TAG, volleyError + "");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("type", type);
                params.put("API_KEY", getResources().getString(R.string.server_side_api_key));
                params.put("token", preferenceManager.getInstance(getActivity()).getKeyToken());
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestHandler.getInstance(getActivity()).addToRequestQueue(request);
    }

    public void changeBall(final String type) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setMessage("Changing Ball");
        dialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, URLs.CHANGE_BALL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, response + "");
                        if (getActivity() != null && dialog != null && dialog.isShowing()) {
                            dialog.cancel();
                        }
                        try {
                            JSONObject mainObj = new JSONObject(response);
                            Boolean error = mainObj.getBoolean("error");
                            if (!error) {

                                if (type.trim().equalsIgnoreCase(constants.SILVER_BALL)) {
                                    diamondBalls_use.setText("use");
                                    goldBalls_use.setText("use");
                                    silverBalls_use.setText("Using");
                                    preferenceManager.getInstance(getActivity()).setUserCurrentBall(constants.SILVER_BALL);
                                } else if (type.trim().equalsIgnoreCase(constants.GOLD_BALL)) {
                                    diamondBalls_use.setText("use");
                                    goldBalls_use.setText("Using");
                                    silverBalls_use.setText("use");
                                    preferenceManager.getInstance(getActivity()).setUserCurrentBall(constants.GOLD_BALL);
                                } else {
                                    diamondBalls_use.setText("Using");
                                    goldBalls_use.setText("use");
                                    silverBalls_use.setText("use");
                                    preferenceManager.getInstance(getActivity()).setUserCurrentBall(constants.DIAMOND_BALL);
                                }


                                Toast.makeText(getActivity(), mainObj.getString("message"), Toast.LENGTH_SHORT).show();
                            } else {
                                showMessage(mainObj.getString("message"));
                            }

                        } catch (Exception e) {
                            Log.i(TAG, "JSON exception - " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (getActivity() != null && dialog != null && dialog.isShowing()) {
                    dialog.cancel();
                }
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

                showMessage(message);
                Log.i(TAG, volleyError + "");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("type", type);
                params.put("API_KEY", getResources().getString(R.string.server_side_api_key));
                params.put("token", preferenceManager.getInstance(getActivity()).getKeyToken());
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestHandler.getInstance(getActivity()).addToRequestQueue(request);
    }

    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
