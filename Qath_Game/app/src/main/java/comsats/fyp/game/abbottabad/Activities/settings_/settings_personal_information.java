package comsats.fyp.game.abbottabad.Activities.settings_;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.constants;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;


public class settings_personal_information extends AppCompatActivity {

    LinearLayout changePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_personal_information);

        getSupportActionBar().hide();

        changePassword = findViewById(R.id.changePassword);

        getSupportActionBar().setTitle("Settings");

        if (!preferenceManager.getInstance(getApplicationContext()).getUserProvider().trim().equals(constants.USER_PROVIDER_APP_AUTH)) {
            changePassword.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView name = findViewById(R.id.username);
        name.setText(preferenceManager.getInstance(getApplicationContext()).getUserName());
    }

    public void change_name(View view) {
        startActivity(new Intent(getApplicationContext(), setting_change_name.class));
    }

    public void change_password(View view) {
        startActivity(new Intent(getApplicationContext(), change_password.class));
    }

    public void change_profile(View view) {
        startActivity(new Intent(getApplicationContext(), setProfilePicture.class));
    }

}
