package pt.ismai.pedro.sisproject.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;
import pt.ismai.pedro.sisproject.R;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Button btn_sign_out;
    CircleImageView profile_photo;
    TextView username;
    LinearLayout profile_edit, game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_profile);
        btn_sign_out = findViewById(R.id.btn_sign_out);
        profile_photo = findViewById(R.id.profilePhoto);
        username = findViewById(R.id.username);
        profile_edit = findViewById(R.id.profile_edit);
        game = findViewById(R.id.game);

        profile_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeActivity(EditProfileActivity.class);
            }
        });

        game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeActivity(ScheduleGameActivity.class);
            }
        });

        loadUserInfo();

        btn_sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Destroying the current authenticated user token
                mAuth.signOut();
                //Redirect user to the login activity
                Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }

    private void loadUserInfo() {
        //We're getting the authenticated user name and profile photo and setting in the activity
        //For loading the image we use a library call "Glide"
        //Glide is a fast and efficient open source media management and image loading framework for Android
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            username.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null){
                Glide.with(this ).load(user.getPhotoUrl().toString()).into(profile_photo);
            }
        }
    }

    private void executeActivity(Class<?> subActivity){
        FirebaseUser user = mAuth.getCurrentUser();
        Intent intent = new Intent(this,subActivity);
        if (user != null) {
            intent.putExtra("userId", user.getUid());
        }
        startActivity(intent);
        finish();
    }
}
