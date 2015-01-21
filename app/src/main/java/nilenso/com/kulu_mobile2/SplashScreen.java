package nilenso.com.kulu_mobile2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class SplashScreen extends Activity implements
        GooglePlayServicesClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final int RC_SIGN_IN = 0;
    public static final String ACCOUNT_NAME = "AccountName";
    public static final String DISPLAY_NAME = "DisplayName";
    static final String LOGGED_IN = "LoggedIn";
    static final String SIGN_OUT = "SignOut";

    private static final String TAG = "SplashScreen" ;
    private GoogleApiClient mGoogleApiClient;

    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;

    private boolean mIntentInProgress;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();


        if (isSigningOut())  signOut();
        if (isUserLoggedIn()) startMainActivity();

        getActionBar().hide();
        setContentView(R.layout.splash_screen);


        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGoogleApiClient.isConnecting()) {
                    mSignInClicked = true;
                    pd=ProgressDialog.show(SplashScreen.this,"","Please Wait",false);
                    mGoogleApiClient.connect();
                    resolveSignInError();
                }
            }
        });
    }

    private boolean isSigningOut() {
        if (getIntent().getExtras() == null) return false;
        if (getIntent().getExtras().getString(SIGN_OUT) == null) return false;
        return getIntent().getExtras().getString(SIGN_OUT).equals("true");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    public void signOut() {
        clearUserInfo();
        mGoogleApiClient.disconnect();
        mSignInClicked = false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mSignInClicked = false;
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String personName = currentPerson.getDisplayName();
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            saveUserInfo(personName, email);
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            startMainActivity();
        }
    }

    private void saveUserInfo(String personName, String email) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ACCOUNT_NAME, email);
        editor.putString(DISPLAY_NAME, personName);
        editor.putString(LOGGED_IN, "true");
        editor.commit();
        Log.e(TAG, String.valueOf(sharedPref.contains(LOGGED_IN)));
    }

    private void clearUserInfo() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }

    private boolean isUserLoggedIn() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String logged_in = sharedPref.getString(LOGGED_IN, "false");
        return logged_in.equals("true");
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (pd != null) pd.dismiss();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDisconnected() {
        if (pd != null) pd.dismiss();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!mIntentInProgress) {
            // Store the ConnectionResult so that we can use it later when the user clicks
            // 'sign-in'.
            mConnectionResult = connectionResult;

            if (pd != null) pd.dismiss();

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult != null && mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    protected void onStop() {
        super.onStop();
        if (pd != null) pd.dismiss();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
