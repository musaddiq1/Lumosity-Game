package comsats.fyp.game.abbottabad;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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

import comsats.fyp.game.abbottabad.storeRoom.RequestHandler;
import comsats.fyp.game.abbottabad.storeRoom.URLs;

public class setNewPassword extends AppCompatActivity {

    EditText new_password;
    String TAG = "setNewPassword";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_password);

        getSupportActionBar().hide();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Changing Password");
        progressDialog.setCancelable(false);

        new_password = findViewById(R.id.new_password);
    }

    public void change_the_password(View view) {
        final String new_pass_str = new_password.getText().toString().trim();
        if (new_pass_str.isEmpty() || new_pass_str.length() < 8) {
            new_password.setError("Please provide the valid new password of length min 8 characters.");
            new_password.requestFocus();
        } else {
            progressDialog.show();

            StringRequest request = new StringRequest(Request.Method.POST, URLs.RESER_PASSWORD,
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
                                    AlertDialog.Builder builder = new AlertDialog.Builder(setNewPassword.this);
                                    builder.setMessage("Password changed successfully.\nPlease log in using the new password.");
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
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
                    params.put("email", getIntent().getExtras().getString("email"));
                    params.put("code", getIntent().getExtras().getString("code"));
                    params.put("password", new_pass_str);
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
    }// ends here

    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(setNewPassword.this);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
