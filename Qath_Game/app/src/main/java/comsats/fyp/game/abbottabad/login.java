package comsats.fyp.game.abbottabad;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import comsats.fyp.game.abbottabad.Activities.home;
import comsats.fyp.game.abbottabad.storeRoom.RequestHandler;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.constants;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;

public class login extends AppCompatActivity {

    String TAG = "login123";

    TextView text;

    // Facebook login
    LoginButton facebookButton;
    private CallbackManager callbackManager;

    // Google login
    private SignInButton googleSignInButton;
    private GoogleSignInClient googleSignInClient;

    ProgressDialog progressDialog;

    public static login loginObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (login.loginObj != null) {
            login.loginObj.finish();
        }
        login.loginObj = this;
        getSupportActionBar().hide();

        googleSignInButton = findViewById(R.id.sign_in_button);
        facebookButton = findViewById(R.id.login_button);
        text = findViewById(R.id.text);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Accessing Account Information");
        progressDialog.setCancelable(false);


        callbackManager = CallbackManager.Factory.create();

        // facebook Callback registration
        facebookButton.setReadPermissions("email", "public_profile");
        //checkLoginStatus();

        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                loadUserProfile(loginResult.getAccessToken());
                Log.i(TAG, loginResult + " ::: success");
            }

            @Override
            public void onCancel() {
                Log.i(TAG, " ::: onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.i(TAG, exception + " ::: exception");
            }
        });

        // Google Sign in configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);
            }
        });


        if (preferenceManager.getInstance(getApplicationContext()).isUserActive()) {
            // user is logged in
            if (preferenceManager.getInstance(getApplicationContext()).getUserProvider().equalsIgnoreCase(constants.USER_PROVIDER_FACEBOOK)) {
                // user is logged in using the facebook -- additional check to make sure user token is valid
                if (AccessToken.getCurrentAccessToken() != null) {
                    // user is logged in using the facebook
                    //loadUserProfile(AccessToken.getCurrentAccessToken());
                    startActivity(new Intent(getApplicationContext(), home.class));
                    finish();
                }
            } else if (preferenceManager.getInstance(getApplicationContext()).getUserProvider().equalsIgnoreCase(constants.USER_PROVIDER_GMAIL)) {
                // user is logged in using the Google -- additional check to make sure user token is valid
                GoogleSignInAccount alreadyloggedAccount = GoogleSignIn.getLastSignedInAccount(this);
                if (alreadyloggedAccount != null) {
                    // user is logged in using the Google
                    // onLoggedIn(alreadyloggedAccount);
                    startActivity(new Intent(getApplicationContext(), home.class));
                    finish();
                }
            } else if (preferenceManager.getInstance(getApplicationContext()).getUserProvider().equalsIgnoreCase(constants.USER_PROVIDER_APP_AUTH)) {
                // user is logged in by creating the account on app
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
                preferenceManager.getInstance(getApplicationContext()).removeActiveUser();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 101) {
                try {
                    // The Task returned from this call is always completed, no need to attach a listener.
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    onLoggedIn(account);
                } catch (ApiException e) {
                    // The ApiException status code indicates the detailed failure reason.
                    Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                }
            } else {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void onLoggedIn(GoogleSignInAccount googleSignInAccount) {
        // user is logged in successfully using the Gmail login -- store user in database

        String userName = googleSignInAccount.getDisplayName();
        String userEmail = googleSignInAccount.getEmail();
        String userProviderID = googleSignInAccount.getId();
        String userProfile = String.valueOf(googleSignInAccount.getPhotoUrl());

        //Picasso.get().load().centerInside().fit().into(profileImage);

        accessAccountFromServer(userName, userEmail, constants.USER_PROVIDER_GMAIL, userProviderID, userProfile, "none");

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

                                Toast.makeText(getApplicationContext(), "Log in successful", Toast.LENGTH_LONG).show();

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
                                preferenceManager.getInstance(getApplicationContext()).setAccountStatus(user.getString("status"));
                                preferenceManager.getInstance(getApplicationContext()).setUserProfile(profileOb.getString("image"));
                                preferenceManager.getInstance(getApplicationContext()).setUserProfileProvider(profileOb.getString("provider"));

                                preferenceManager.getInstance(getApplicationContext()).setUserCurrentBall(user.getString("currentBalls"));
                                preferenceManager.getInstance(getApplicationContext()).setUserPurchasedGoldBalls(user.getBoolean("goldPurhcased"));
                                preferenceManager.getInstance(getApplicationContext()).setUserPurchasedDiamondBalls(user.getBoolean("diamondPurhcased"));

                                preferenceManager.getInstance(getApplicationContext()).setRank(user.getString("rank"));
                                preferenceManager.getInstance(getApplicationContext()).setBestGameWon(user.getInt("best_game_won"));


                                startActivity(new Intent(getApplicationContext(), home.class));
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


    private void loadUserProfile(AccessToken newAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String email = "none";

                    String first_name = object.getString("first_name");
                    String last_name = object.getString("last_name");
                    String id = object.getString("id");
                    // image types = small, normal, album, large, square
                    String image_url = "https://graph.facebook.com/" + id + "/picture?type=large";

                    if (object.has("email")) {
                        email = object.getString("email");
                    }

                    if (!preferenceManager.getInstance(getApplicationContext()).isUserActive()) {
                        accessAccountFromServer(first_name + " " + last_name, email, constants.USER_PROVIDER_FACEBOOK, id, image_url, "none");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, e + "");
                }

            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();

    }


    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(login.this);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void start_create_account(View view) {
        startActivity(new Intent(getApplicationContext(), createAccount.class));
    }

    public void start_access_account_act(View view) {
        startActivity(new Intent(getApplicationContext(), access_account.class));
    }

   /* private void checkLoginStatus() {
        if (AccessToken.getCurrentAccessToken() != null) {
            loadUserProfile(AccessToken.getCurrentAccessToken());
        }
    }*/
}
