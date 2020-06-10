package comsats.fyp.game.abbottabad.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import comsats.fyp.game.abbottabad.Activities.AboutGameRules;
import comsats.fyp.game.abbottabad.Activities.settings_.settings_personal_information;
import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.login;
import comsats.fyp.game.abbottabad.storeRoom.constants;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;

public class settingsFragment extends Fragment {

    String TAG = "settingsFragment";
    private GoogleSignInClient googleSignInClient;

    public settingsFragment() {
        // Required empty public constructor
    }

    Switch soundSwitch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        soundSwitch = view.findViewById(R.id.soundSwitch);
        soundSwitch.setChecked(preferenceManager.getInstance(getActivity()).isGameSoundEnabled());
        soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getActivity(), "Sound is enabled", Toast.LENGTH_SHORT).show();
                    preferenceManager.getInstance(getActivity()).setGameSound(true);
                } else {
                    Toast.makeText(getActivity(), "Sound is disabled", Toast.LENGTH_SHORT).show();
                    preferenceManager.getInstance(getActivity()).setGameSound(false);
                }
            }
        });

        view.findViewById(R.id.aboutGameRules).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AboutGameRules.class));
            }
        });

        view.findViewById(R.id.personalInfoSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), settings_personal_information.class));
            }
        });

        view.findViewById(R.id.signOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log_out_user();
            }
        });

        return view;
    }

    public void log_out_user() {

        Log.i(TAG, preferenceManager.getInstance(getActivity()).getUserProvider());

        if (preferenceManager.getInstance(getActivity()).isUserActive()) {
            // user is logged in
            if (preferenceManager.getInstance(getActivity()).getUserProvider().equalsIgnoreCase(constants.USER_PROVIDER_FACEBOOK)) {
                // user is logged in using the facebook
                LoginManager.getInstance().logOut();
                preferenceManager.getInstance(getActivity()).removeActiveUser();
                Intent intent = new Intent(getActivity(), login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else if (preferenceManager.getInstance(getActivity()).getUserProvider().equalsIgnoreCase(constants.USER_PROVIDER_GMAIL)) {
                // user is logged in using the Google

                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

                googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            preferenceManager.getInstance(getActivity()).removeActiveUser();
                            Intent intent = new Intent(getActivity(), login.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, task.getException() + "");
                        }
                    }
                });

            } else if (preferenceManager.getInstance(getActivity()).getUserProvider().equalsIgnoreCase(constants.USER_PROVIDER_APP_AUTH)) {
                // user is logged in by creating the account on app
                preferenceManager.getInstance(getActivity()).removeActiveUser();
                Intent intent = new Intent(getActivity(), login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }
}
