package comsats.fyp.game.abbottabad.Activities.settings_;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;

import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.constants;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;
import comsats.fyp.game.abbottabad.storeRoom.store;


public class setProfilePicture extends AppCompatActivity {

    ImageView user_dp;
    Uri finalUri = null;
    String finalPicturePath = null;
    Integer RESULT_LOAD_IMG = 109;
    private int STORAGE_PERMISSION_CODE = 23;
    byte[] imgBytes;
    Bitmap bitmap = null;
    Boolean isSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile_picture);

        getSupportActionBar().hide();

        if (!isReadStorageAllowed()) {
            requestStoragePermission();
        }
        isSelected = false;
        user_dp = (ImageView) findViewById(R.id.user_dp);


        if (preferenceManager.getInstance(getApplicationContext()).getUserProfileProvider().trim().equals("Qath")) {
            // use qath api to get image
            if (!preferenceManager.getInstance(getApplicationContext()).getUserProfile().trim().equals(constants.UNDEFINED)) {
                if (!preferenceManager.getInstance(getApplicationContext()).getUserProfile().trim().equals("none")) {
                    Picasso.get().
                            load(URLs.UPLOADED_FILES(preferenceManager.getInstance(getApplicationContext()).getUserProfile().trim()))
                            .placeholder(R.drawable.user)
                            .error(R.drawable.user)
                            .into(user_dp);
                }
            }
        } else {
            if (!preferenceManager.getInstance(getApplicationContext()).getUserProfile().trim().equals(constants.UNDEFINED)) {
                if (!preferenceManager.getInstance(getApplicationContext()).getUserProfile().trim().equals("none")) {
                    Picasso.get().
                            load(preferenceManager.getInstance(getApplicationContext()).getUserProfile().trim())
                            .placeholder(R.drawable.user)
                            .error(R.drawable.user)
                            .into(user_dp);
                }
            }
        }

        user_dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReadStorageAllowed()) {
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMG);
                } else {
                    Toast.makeText(getApplicationContext(), "We dont have permission to look into your Gallery", Toast.LENGTH_LONG).show();
                    requestStoragePermission();
                }
            }
        });
    }

    private boolean isReadStorageAllowed() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        return false;
    }

    //Requesting permission
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {
            try {
                Uri imageUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                finalUri = imageUri;
                finalPicturePath = picturePath;
                isSelected = true;
                user_dp.setImageBitmap(store.resize(bitmap, 1200, 1200));
            } catch (Exception e) {
                Log.i("123132", e + "");
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } // if ends here
    } // function ends here

    public void uploadNow(View view) {
        if (finalUri == null || !isSelected || finalPicturePath == null) {
            Toast.makeText(getApplicationContext(), "Select image first.", Toast.LENGTH_LONG).show();
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading profile picture");
            progressDialog.setCancelable(false);
            progressDialog.show();

            final File fileToUpload = new File(finalPicturePath);
            Ion.with(setProfilePicture.this)
                    .load("POST", URLs.UPLOAD_PROFILE)
                    .setLogging("1312wWOD", Log.INFO)
                    .uploadProgressHandler(new ProgressCallback() {
                        @Override
                        public void onProgress(long uploaded, long total) {
                            double progress = (100.0 * uploaded) / total;
                            progressDialog.setMessage("Uploading profile picture " + ((int) progress) + " %");
                        }
                    })
                    .setMultipartFile("image", "image/jpeg", fileToUpload)
                    .setMultipartParameter("token", preferenceManager.getInstance(getApplicationContext()).getKeyToken())
                    .setMultipartParameter("API_KEY", getResources().getString(R.string.server_side_api_key))
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            progressDialog.dismiss();
                            Log.i("312333", result + "  " + e + "");
                            if (result != null) {
                                try {
                                    JSONObject mainObject = new JSONObject(result);
                                    String message = mainObject.getString("message");
                                    if (!mainObject.getBoolean("error")) {
                                        String imageURL = mainObject.getString("profile");
                                        preferenceManager.getInstance(getApplicationContext()).setUserProfile(imageURL.trim());
                                        preferenceManager.getInstance(getApplicationContext()).setUserProfileProvider(mainObject.getString("provider"));
                                        preferenceManager.getInstance(getApplicationContext()).setKeyToken(mainObject.getString("token"));
                                        Toast.makeText(setProfilePicture.this, message, Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        showMessage(message);
                                    }
                                } catch (Exception ex) {
                                    Log.i("312333", ex + "");
                                }
                            } else {
                                showMessage("Error while uploading profile picture.\n" + e.getMessage());
                            }
                        }
                    });
        }
    }//method ends here

    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(setProfilePicture.this);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
