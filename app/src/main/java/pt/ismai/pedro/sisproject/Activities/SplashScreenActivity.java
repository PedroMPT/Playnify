package pt.ismai.pedro.sisproject.Activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pt.ismai.pedro.sisproject.R;

public class SplashScreenActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Handler mWaitHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mAuth = FirebaseAuth.getInstance();
    }
    @Override
    protected void onStart(){
        super.onStart();

        mWaitHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    //user is already connected so we need to redirect him to MainActivity
                    executeActivity(MainActivity.class);
                }
                else{
                    Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        },3000);
    }

    private void executeActivity(Class<?> subActivity){
        FirebaseUser user = mAuth.getCurrentUser();
        Intent intent = new Intent(this,subActivity);
        intent.putExtra("userId", user.getUid());
        startActivity(intent);

    }
}
