package pan.pan.mylogin;


import com.firebase.client.Firebase;

/**
 * Created by AndroidBash on 11/05/16
 */
//yes
public class CustomApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
