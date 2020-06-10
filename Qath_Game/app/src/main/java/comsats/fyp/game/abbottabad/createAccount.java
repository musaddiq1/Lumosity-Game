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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import comsats.fyp.game.abbottabad.storeRoom.RequestHandler;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.constants;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;

public class createAccount extends AppCompatActivity {

    EditText email, password, name;
    ProgressDialog progressDialog;
    String TAG = "createAccount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        getSupportActionBar().hide();

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        name = (EditText) findViewById(R.id.name);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Account");
        progressDialog.setCancelable(false);

    }

    public void sign_up_user(View view) {
        String emailStr = email.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();
        String nameStr = name.getText().toString().trim();


        Pattern pattern = Pattern.compile(new String("^[a-zA-Z\\s]*$"));
        Matcher matcher = pattern.matcher(nameStr);

        Boolean isError = false;

        if (emailStr.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            email.setError("Please provide the valid email");
            email.requestFocus();
            isError = true;
        }
        if (passwordStr.isEmpty() || passwordStr.length() < 8) {
            password.setError("Please provide the valid password of length min 8 characters");
            password.requestFocus();
            isError = true;
        }
        if (nameStr.isEmpty() || !matcher.matches()) {
            name.setError("Please provide the valid name");
            name.requestFocus();
            isError = true;
        }

        if (!isError) {
            accessAccountFromServer(nameStr, emailStr, constants.USER_PROVIDER_APP_AUTH, "-99", "none", passwordStr);
        }

    }

    public void accessAccountFromServer(final String name, final String email, final String provider, final String providerID, final String profileAddress, final String password) {
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, URLs.CREATE_ACCOUNT,
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
                                preferenceManager.getInstance(getApplicationContext()).setUserName(name);
                                preferenceManager.getInstance(getApplicationContext()).setUserEmail(email);
                                preferenceManager.getInstance(getApplicationContext()).setUserProvider(provider);
                                preferenceManager.getInstance(getApplicationContext()).setUserProviderID(providerID);
                                preferenceManager.getInstance(getApplicationContext()).setUserProfile(profileOb.getString("image"));
                                preferenceManager.getInstance(getApplicationContext()).setUserProfileProvider(profileOb.getString("provider"));
                                preferenceManager.getInstance(getApplicationContext()).setAccountStatus(user.getString("status"));
                                preferenceManager.getInstance(getApplicationContext()).setRank("Ace");

                                if (login.loginObj != null) {
                                    login.loginObj.finish();
                                }

                                Bundle pack = new Bundle();
                                pack.putString("state", "simple_login");
                                Intent intent = new Intent(getApplicationContext(), verifyAccountCode.class);
                                intent.putExtras(pack);
                                startActivity(intent);
                                finish();
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
                params.put("username", name);
                params.put("email", email);
                params.put("provider_id", providerID);
                params.put("provider", provider);
                params.put("profile", profileAddress);
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

    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(createAccount.this);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
