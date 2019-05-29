package pt.ismai.pedro.sisproject.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;
import pt.ismai.pedro.sisproject.R;



public class EditProfileActivity extends AppCompatActivity {

    TextView input_name,input_email,input_password;
    CircleImageView profile_photo;
    private FirebaseAuth mAuth;
    static int REQUESTCODE = 1;
    Uri pickedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        mAuth = FirebaseAuth.getInstance();

        input_name = findViewById(R.id.input_name);
        input_email = findViewById(R.id.input_email);
        input_password = findViewById(R.id.input_password);
        profile_photo = findViewById(R.id.profile_photo);

        loadUserInfo();

        profile_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openGallery();
            }
        });

        input_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                executeActivity(EditNameActivity.class);
            }
        });

        input_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeActivity(EditEmailActivity.class);
            }
        });

        input_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeActivity(EditPasswordActivity.class);
            }
        });
    }

    private void loadUserInfo() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null){
            input_name.setText(user.getDisplayName());
            input_email.setText(user.getEmail());
            if (user.getPhotoUrl() != null){

                Glide.with(this ).load(user.getPhotoUrl().toString()).into(profile_photo);
            }
        }
    }

    private void updateUserPhoto(Uri pickedImage, final FirebaseUser currentUser) {

        // First we need to upload user photo to firebase storage and get uri
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mStorage.child(pickedImage.getLastPathSegment());
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
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    // user info updated successfully
                                    toastMessage("PhotoUpdated");
                                }
                            }
                        });
                    }
                });
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null){

            //the user has successfully picked an image
            // we need to save it's reference to a Uri variable
            pickedImage = data.getData();
            profile_photo.setImageURI(pickedImage);
            FirebaseUser user = mAuth.getCurrentUser();
            updateUserPhoto(pickedImage,user);


        }
    }

    private void openGallery() {

        //TODO: open gallery intent and wait for user to pick an image
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESTCODE);
    }


    private void toastMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

    private void executeActivity(Class<?> subActivity){
        FirebaseUser user = mAuth.getCurrentUser();
        Intent intent = new Intent(this,subActivity);
        intent.putExtra("objectId", user.getUid());
        startActivity(intent);
        finish();
    }
}
