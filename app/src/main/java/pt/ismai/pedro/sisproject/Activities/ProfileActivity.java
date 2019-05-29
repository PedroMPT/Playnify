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
    LinearLayout profile_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_profile);
        btn_sign_out = findViewById(R.id.btn_sign_out);
        profile_photo = findViewById(R.id.profilePhoto);
        username = findViewById(R.id.username);
        profile_edit = findViewById(R.id.profile_edit);

        profile_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                executeActivity(EditProfileActivity.class);
            }
        });

        loadUserInfo();

        btn_sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.signOut();
                Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(loginIntent);

            }
        });
    }

    private void loadUserInfo() {

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
        intent.putExtra("userId", user.getUid());
        startActivity(intent);
        finish();
    }
}
