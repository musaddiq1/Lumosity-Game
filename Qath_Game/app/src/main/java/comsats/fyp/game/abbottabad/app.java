package comsats.fyp.game.abbottabad;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class app extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Configure default configuration for Realm
        Realm.init(this);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("comsats.fyp.game.abbottabad.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

    }
}
