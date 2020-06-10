package comsats.fyp.game.abbottabad;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

import comsats.fyp.game.abbottabad.Activities.home;
import comsats.fyp.game.abbottabad.storeRoom.RequestHandler;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;

public class access_account extends AppCompatActivity {

    EditText email, password;
    ProgressDialog progressDialog;
    String TAG = "access_account";
    public static access_account access_account_obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_account);

        getSupportActionBar().hide();

        if (access_account.access_account_obj != null) {
            access_account.access_account_obj.finish();
        }
        access_account.access_account_obj = this;

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Accessing Account");
        progressDialog.setCancelable(false);

    }

    public void access_account_info(View view) {
        String emailStr = email.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();

        Boolean isError = false;

        if (passwordStr.isEmpty()) {
            password.setError("Please provide the valid password");
            password.requestFocus();
            isError = true;
        }

        if (emailStr.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            email.setError("Please provide the valid email");
            email.requestFocus();
            isError = true;
        }


        if (!isError) {
            accessAccountFromServer(emailStr, passwordStr);
        }
    }

    public void accessAccountFromServer(final String email, final String password) {
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, URLs.ACCESS_ACCOUNT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.i(TAG, response + "");

                        try {

                            JSONObject mainObj = new JSONObject(response);
                            Boolean error = mainObj.getBoolean("error");

                            if (!error) {

                                Toast.makeText(getApplicationContext(), mainObj.getString("message"), Toast.LENGTH_LONG).show();

                                JSONObject user = mainObj.getJSONObject("user");
                                final int ID = user.getInt("userID");
                                String key_token = mainObj.getString("token");
                                JSONObject profileOb = new JSONObject(user.getString("profile"));

                                preferenceManager.getInstance(getApplicationContext()).setActiveUser();

                                preferenceManager.getInstance(getApplicationContext()).setUserID(ID);

                                preferenceManager.getInstance(getApplicationContext()).setUserGameInfoID(user.getInt("gameInfoID"));
                                preferenceManager.getInstance(getApplicationContext()).setUserCoins(user.getInt("coins"));
                                preferenceManager.getInstance(getApplicationContext()).setGameLostCount(user.getInt("gamesLost"));
                                preferenceManager.getInstance(getApplicationContext()).setGameWonCount(user.getInt("gamesWon"));


                                preferenceManager.getInstance(getApplicationContext()).setKeyToken(key_token);
                                preferenceManager.getInstance(getApplicationContext()).setUserName(user.getString("username"));
                                preferenceManager.getInstance(getApplicationContext()).setUserEmail(email);
                                preferenceManager.getInstance(getApplicationContext()).setUserProvider(user.getString("provider"));
                                preferenceManager.getInstance(getApplicationContext()).setUserProviderID(user.getString("provider_id"));
                                preferenceManager.getInstance(getApplicationContext()).setUserProfile(profileOb.getString("image"));
                                preferenceManager.getInstance(getApplicationContext()).setUserProfileProvider(profileOb.getString("provider"));
                                preferenceManager.getInstance(getApplicationContext()).setAccountStatus(user.getString("status"));

                                preferenceManager.getInstance(getApplicationContext()).setUserCurrentBall(user.getString("currentBalls"));
                                preferenceManager.getInstance(getApplicationContext()).setUserPurchasedGoldBalls(user.getBoolean("goldPurhcased"));
                                preferenceManager.getInstance(getApplicationContext()).setUserPurchasedDiamondBalls(user.getBoolean("diamondPurhcased"));

                                preferenceManager.getInstance(getApplicationContext()).setRank(user.getString("rank"));
                                preferenceManager.getInstance(getApplicationContext()).setBestGameWon(user.getInt("best_game_won"));

                                if (login.loginObj != null) {
                                    login.loginObj.finish();
                                }

                                if (preferenceManager.getInstance(getApplicationContext()).getAccountStatus().trim().equalsIgnoreCase("unApproved")) {
                                    Bundle pack = new Bundle();
                                    pack.putString("state", "simple_login");
                                    Intent intent = new Intent(getApplicationContext(), verifyAccountCode.class);
                                    intent.putExtras(pack);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    startActivity(new Intent(getApplicationContext(), home.class));
                                    finish();
                                }

                            } else {
                                showMessage(mainObj.getString("message"));
                            }

                        } catch (Exception e) {
                            progressDialog.dismiss();
                            Log.i(TAG, "JSON exception - " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressDialog.dismiss();

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

                showMessage(message + "");
                Log.i(TAG, volleyError + "");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
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

    public void forgot_password(View view) {
        final String emailStr = email.getText().toString().trim();

        if (emailStr.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            email.setError("Please provide the valid email");
            email.requestFocus();
        } else {

            progressDialog.setMessage("Sending verification email");
            progressDialog.show();

            StringRequest request = new StringRequest(Request.Method.POST, URLs.RESEND_EMAIL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                JSONObject jsonObject = new JSONObject(response);
                                Boolean error = jsonObject.getBoolean("error");
                                if (!error) {
                                    Bundle pack = new Bundle();
                                    pack.putString("state", "forgot_password");
                                    pack.putString("email", emailStr);
                                    Intent intent = new Intent(getApplicationContext(), verifyAccountCode.class);
                                    intent.putExtras(pack);
                                    startActivity(intent);
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
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
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
                    params.put("email", emailStr);
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
    }

    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(access_account.this);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
