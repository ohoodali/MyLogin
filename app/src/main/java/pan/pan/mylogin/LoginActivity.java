package pan.pan.mylogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Arrays;

/**
 * Created by AndroidBash on 11/05/16
 */

public class LoginActivity extends AppCompatActivity {

    private Firebase myFirebaseRef;
    public User user;
    private EditText email;
    private EditText password;
    private ProgressBar progressBar;
    //FaceBook
    private CallbackManager callbackManager;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FaceBook
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                saveFacebookLoginData("facebook", loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "" + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        //

        //Add YOUR Firebase Reference URL instead of the following URL
        myFirebaseRef = new Firebase("https://my-login-fac74.firebaseio.com/");

    }

    @Override
    protected void onStart() {
        super.onStart();
        email = (EditText) findViewById(R.id.edit_text_email_id);
        password = (EditText) findViewById(R.id.edit_text_password);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_login);
        checkUserLogin();
    }

    //FaceBook
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    //

    protected void setUpUser() {
        user = new User();
        user.setEmail(email.getText().toString());
        user.setPassword(password.getText().toString());
    }

    public void onSignUpClicked(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void onLoginClicked(View view) {
        progressBar.setVisibility(View.VISIBLE);
        setUpUser();
        aunthenticateUserLogin();
    }

    //FaceBook
    public void onFacebookLogInClicked( View view ){
        LoginManager
                .getInstance()
                .logInWithReadPermissions(
                        this,
                        Arrays.asList("public_profile", "user_friends", "email")
                );
    }
   //

    private void checkUserLogin() {
        //getAuth Returns the current authentication state of the Firebase client. If the client is unauthenticated, this method will return null.
        // Otherwise, the return value will be an object containing at least the fields such as uid,provider,token,expires,auth
        // https://www.firebase.com/docs/web/api/firebase/getauth.html,
        if (myFirebaseRef.getAuth() != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            String uid = myFirebaseRef.getAuth().getUid();
            intent.putExtra("user_id", uid);
            startActivity(intent);
            finish();
        }
    }


    private void aunthenticateUserLogin() {
        //authWithPassword method attempts to authenticate to Firebase with the given credentials.
        //Paramters Are :
        // email - The email for the user to authenticate
        // password - The password for the account
        // handler - A handler which will be called with the result of the authentication attempt
        myFirebaseRef.authWithPassword(
                user.getEmail(),
                user.getPassword(),
                new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        //Log.i("TOKEN",authData.getToken());
                        //Log.i("PROVIDER",authData.getProvider());
                        //Log.i("UID",authData.getUid());
                        //Log.i("AUTH_MAP",authData.getAuth().toString());
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        String uid = myFirebaseRef.getAuth().getUid();
                        intent.putExtra("user_id", uid);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        Toast.makeText(getApplicationContext(), "" + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
        );
    }

    //FaceBook
    private void saveFacebookLoginData(String provider, AccessToken accessToken){
        String token=accessToken.getToken();
        setUpUser();
        if( token != null ){

            myFirebaseRef.authWithOAuthToken(
                    provider,
                    token,
                    new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            String uid=authData.getUid();
                            String name=authData.getProviderData().get("displayName").toString();
                            String email=authData.getProviderData().get("email").toString();
                            String image=authData.getProviderData().get("profileImageURL").toString();
                            user.setId(authData.getUid());
                            user.setName(name);
                            user.setEmail(email);
                            user.saveUser();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("user_id",uid);
                            intent.putExtra("profile_picture",image);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {
                            Toast.makeText(getApplicationContext(), "" + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
        else{
            myFirebaseRef.unauth();
        }
    }
    //

}
