package nilenso.com.kulu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SplashScreen extends Activity {
    public static final String TEAM_NAME = "TeamName";
    public static final String ACCOUNT_NAME = "AccountName";
    public static final String TOKEN = "token";
    static final String LOGGED_IN = "LoggedIn";
    static final String SIGN_OUT = "SignOut";
    static final String ERROR = "error";

    private static final String TAG = "SplashScreen";
    public static final String URL = "https://kulu-backend.herokuapp.com/login";

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isSigningOut()) signOut();
        if (isUserLoggedIn()) startMainActivity();

        setContentView(R.layout.splash_screen);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String orgName = ((EditText) findViewById(R.id.loginOrgName)).getText().toString();
                String email = ((EditText) findViewById(R.id.loginEmail)).getText().toString();
                String password = ((EditText) findViewById(R.id.loginPassword)).getText().toString();
                pd = ProgressDialog.show(SplashScreen.this, "", "Please Wait", false);

                new LoginClient().login(URL, orgName, email, password, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {

                        if (!response.isSuccessful())
                            if (response.code() == 401){
                                Toast.makeText(SplashScreen.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(SplashScreen.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        try {
                            String token = new JSONObject(response.body().string()).getString("token");
                            String teamName = ((EditText) findViewById(R.id.loginOrgName)).getText().toString();
                            String email = ((EditText) findViewById(R.id.loginEmail)).getText().toString();
                            saveUserInfo(teamName, email, token);
                            pd.dismiss();
                            startMainActivity();
                        } catch (JSONException e) {
                            pd.dismiss();
                            Toast.makeText(SplashScreen.this, "Couldn't clog in", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        final Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        findViewById(R.id.kuluLogo).startAnimation(animationFadeIn);
    }

    private boolean isSigningOut() {
        if (getIntent().getExtras() == null) return false;
        if (getIntent().getExtras().getString(SIGN_OUT) == null) return false;
        return getIntent().getExtras().getString(SIGN_OUT).equals("true");
    }

    public void signOut() {
        clearUserInfo();
    }

    private void saveUserInfo(String teamName, String email, String token) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(TEAM_NAME, teamName);
        editor.putString(ACCOUNT_NAME, email);
        editor.putString(TOKEN, token);
        editor.putString(LOGGED_IN, "true");
        editor.apply();
        Log.e(TAG, String.valueOf(sharedPref.contains(LOGGED_IN)));
    }

    private void clearUserInfo() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
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

    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
