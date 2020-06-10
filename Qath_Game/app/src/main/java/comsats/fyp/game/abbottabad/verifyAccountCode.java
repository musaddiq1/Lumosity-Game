package comsats.fyp.game.abbottabad;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import comsats.fyp.game.abbottabad.Activities.home;
import comsats.fyp.game.abbottabad.storeRoom.RequestHandler;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;

public class verifyAccountCode extends AppCompatActivity {

    TextView emailAddress, orView;
    EditText code;
    Button log_out;
    Bundle unPacker;
    String state;
    String userEmail;
    ProgressDialog progressDialog;
    String TAG = "verifyAccountCode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_account_code);

        getSupportActionBar().hide();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        emailAddress = findViewById(R.id.emailAddress);
        code = findViewById(R.id.code);
        orView = findViewById(R.id.orView);
        log_out = findViewById(R.id.log_out);
        unPacker = getIntent().getExtras();
        state = unPacker.getString("state").trim();

        if (state.equalsIgnoreCase("forgot_password")) {
            emailAddress.setText(unPacker.getString("email"));
            orView.setVisibility(View.GONE);
            log_out.setVisibility(View.GONE);
        } else {
            emailAddress.setText(preferenceManager.getInstance(getApplicationContext()).getUserEmail());
        }

        userEmail = emailAddress.getText().toString().trim();

        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                preferenceManager.getInstance(getApplicationContext()).removeActiveUser();
                Intent intent = new Intent(getApplicationContext(), login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }

    public void verifyCode(View view) {
        String codeText = code.getText().toString().trim();
        if (codeText.isEmpty() || codeText.length() < 4) {
            code.setError("Please enter valid code");
            code.requestFocus();
        } else {
            verifyEmailAddress(codeText);
        }
    }

    public void resendEmail(View view) {
        progressDialog.setMessage("Resending verification email");
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
                                Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
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
                params.put("email", userEmail);
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

    public void verifyEmailAddress(final String code) {
        progressDialog.setMessage("Verifying user account");
        progressDialog.show();

        String from = "";
        if (state.equalsIgnoreCase("forgot_password")) {
            from = "forgot_password";
        } else {
            from = "verify_account";
        }

        final String finalFrom = from;
        StringRequest request = new StringRequest(Request.Method.POST, URLs.VERIFY_EMAIL,
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
                                finish();
                                if (state.equalsIgnoreCase("forgot_password")) {
                                    Toast.makeText(getApplicationContext(), "Code accepted", Toast.LENGTH_SHORT).show();
                                    Bundle pack = new Bundle();
                                    pack.putString("email", userEmail);
                                    pack.putString("code", code);
                                    Intent intent = new Intent(getApplicationContext(), setNewPassword.class);
                                    intent.putExtras(pack);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                    preferenceManager.getInstance(getApplicationContext()).setAccountStatus("approved");
                                    startActivity(new Intent(getApplicationContext(), home.class));
                                }

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
                params.put("email", userEmail);
                params.put("code", code);
                params.put("from", finalFrom);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(verifyAccountCode.this);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
