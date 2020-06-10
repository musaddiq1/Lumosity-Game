package comsats.fyp.game.abbottabad.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import comsats.fyp.game.abbottabad.R;

public class AboutGameRules extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_game_rules);
        getSupportActionBar().hide();
    }
}
