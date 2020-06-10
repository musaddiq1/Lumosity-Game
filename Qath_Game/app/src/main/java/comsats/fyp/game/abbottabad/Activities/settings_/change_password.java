package comsats.fyp.game.abbottabad.Activities.settings_;

import android.app.ProgressDialog;
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

import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.RequestHandler;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;


public class change_password extends AppCompatActivity {

    EditText new_password, old_password;
    String TAG = "change_password";
    ProgressDialog customDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().hide();

        customDialog = new ProgressDialog(this);
        customDialog.setMessage("Updating name");
        customDialog.setCancelable(false);

        new_password = findViewById(R.id.new_password);
        old_password = findViewById(R.id.old_password);
    }


    public void change_the_password(View view) {
        final String new_pass_str = new_password.getText().toString().trim();
        final String old_pass_str = old_password.getText().toString().trim();
        Boolean isError = false;

        if (new_pass_str.isEmpty() || new_pass_str.length() < 8) {
            new_password.setError("Please provide the valid new password of length min 8 characters.");
            new_password.requestFocus();
            isError = true;
        }

        if (old_pass_str.isEmpty()) {
            old_password.setError("Please provide the old password.");
            old_password.requestFocus();
            isError = true;
        }

        if (!old_pass_str.equals(preferenceManager.getInstance(getApplicationContext()).getUserPassword())) {
            old_password.setError("Old Password is incorrect.");
            old_password.requestFocus();
            isError = true;
        }

        if (!isError) {

            customDialog.show();
            StringRequest request = new StringRequest(Request.Method.POST, URLs.UPDATE_PASSWORD,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                customDialog.dismiss();
                                JSONObject jsonObject = new JSONObject(response);
                                Boolean error = jsonObject.getBoolean("error");
                                if (!error) {
                                    preferenceManager.getInstance(getApplicationContext()).setUserPassword(new_pass_str);
                                    Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                    finish();
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
                    params.put("token", preferenceManager.getInstance(getApplicationContext()).getKeyToken());
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
    }

    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(change_password.this);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
