package pt.ismai.pedro.sisproject.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import pt.ismai.pedro.sisproject.Models.User;
import pt.ismai.pedro.sisproject.R;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private FirebaseAuth.AuthStateListener mAuthListener;
    static int PReqCode = 1;
    static int REQUESTCODE = 1;
    EditText input_name, input_email, input_password;
    TextView link_login;
    AppCompatButton btn_signup;
    CircleImageView image;
    Uri pickedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        input_name = findViewById(R.id.input_name);
        input_email = findViewById(R.id.input_email);
        input_password = findViewById(R.id.input_password);
        btn_signup = findViewById(R.id.btn_signup);
        image = findViewById(R.id.profilePhoto);
        link_login = findViewById(R.id.link_login);


        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 22){
                    checkAndRequestForPermissions();
                }
                else{
                    openGallery();
                }
            }
        });

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String name = input_name.getText().toString();
                final String email = input_email.getText().toString();
                final String pass = input_password.getText().toString();

                if(email.equals("") || name.equals("") || pass.equals("")){
                   // Something goes wrong
                   // We need to display an error message
                   toastMessage("Please fill the required fields");
                }
                else{
                    // everything is ok all fields are filled now we can start creating user account
                    // CreateUserAccount method will try to create user if the email is valid
                    CreateUserAccount(email,name,pass);
                    hideSoftKeyboard();
                }
            }
        });

        link_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeActivity(LoginActivity.class);
            }
        });
    }

    private void CreateUserAccount(final String email, final String name, String pass) {
        btn_signup.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("We're just creating your awesome account!!!");
        progressDialog.show();

        // this method creates user account with specific email and password
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                if (task.isSuccessful()) {
                                    User user = new User();
                                    user.setUsername(name);
                                    user.setEmail(email);
                                    user.setUser_id(mAuth.getUid());

                                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                            .build();
                                    mDB.setFirestoreSettings(settings);

                                    DocumentReference newUserRef = mDB
                                            .collection(getString(R.string.collection_users))
                                            .document(Objects.requireNonNull(mAuth.getUid()));

                                    newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                updateUserInfo(name, pickedImage, mAuth.getCurrentUser());

                                            } else {
                                                toastMessage("Account failed to create!" + Objects.requireNonNull(task.getException()).getMessage());
                                            }
                                        }
                                    });
                                } else {
                                    // account creation failed
                                    toastMessage("Account failed to create!" + Objects.requireNonNull(task.getException()).getMessage());
                                }
                                btn_signup.setEnabled(true);
                                progressDialog.dismiss();
                            }

                        }, 4000);
            }
        });
    }

    //update user photo and name method
    private void updateUserInfo(final String name, Uri pickedImage, final FirebaseUser currentUser) {
        if (pickedImage != null){
            // First we need to upload user photo to firebase storage and get uri
            StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
            final StorageReference imageFilePath = mStorage.child(Objects.requireNonNull(pickedImage.getLastPathSegment()));
            imageFilePath.putFile(pickedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // image uploaded successfully
                    // now we can get our image uri
                    imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //uri contain user image uri
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest
                                    .Builder()
                                    .setDisplayName(name)
                                    .setPhotoUri(uri)
                                    .build();
                            currentUser.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mDB.collection("Users").document(currentUser
                                                .getUid()).update("avatar",currentUser.getPhotoUrl().toString());
                                        // user info updated successfully
                                        toastMessage("Account created");
                                        executeActivity(MainActivity.class);
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }else{
            toastMessage("Don't be shy. We just need a photo for the hall of fame :D Please add one");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null){
            //the user has successfully picked an image
            // we need to save it's reference to a Uri variable
            pickedImage = data.getData();
            image.setImageURI(pickedImage);

        }
    }

    private void openGallery() {
        //TODO: open gallery intent and wait for user to pick an image
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESTCODE);
    }

    private void checkAndRequestForPermissions() {
        if(ContextCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(SignUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                toastMessage("Please accept for required permission");
            }
            else{
                ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode );
            }
        }
        else{
            openGallery();
        }

    }

    private void toastMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

    private void executeActivity(Class<?> subActivity){
        FirebaseUser user = mAuth.getCurrentUser();
        Intent intent = new Intent(this,subActivity);
        if (user != null) {
            intent.putExtra("objectId", user.getUid());
        }
        startActivity(intent);
        finish();
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

}
