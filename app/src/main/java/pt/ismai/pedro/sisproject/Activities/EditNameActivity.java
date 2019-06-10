package pt.ismai.pedro.sisproject.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;

import pt.ismai.pedro.sisproject.R;

public class EditNameActivity extends AppCompatActivity {

    EditText input_name;
    Button btn_edit;
    private FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name);

        //Set action bar e title
        Objects.requireNonNull(getSupportActionBar()).setTitle("EDIT NAME");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        input_name = findViewById(R.id.input_name);
        btn_edit = findViewById(R.id.btn_edit);

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateName();
            }
        });

    }

    private void updateName(){
        //Method for name updating using firebase
        String name = input_name.getText().toString();
        user = mAuth.getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        if (user != null && !name.equals("")) {
            // after the correct validations we update the email on the background using firebase async task
            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                     if (task.isSuccessful()){
                         toastMessage("Name Changed");
                     }
                     else {
                         toastMessage(Objects.requireNonNull(task.getException()).getMessage());
                     }
                    executeActivity(EditProfileActivity.class);
                }
            });
        }
        else {
            toastMessage("Name field required");
        }

    }

    private void toastMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
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

    @Override
    public boolean onSupportNavigateUp() {
        // Action bar back button
        onBackPressed();
        executeActivity(EditProfileActivity.class);
        return true;
    }
}
