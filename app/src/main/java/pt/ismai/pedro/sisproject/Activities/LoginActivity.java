package pt.ismai.pedro.sisproject.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Objects;

import pt.ismai.pedro.sisproject.Models.User;
import pt.ismai.pedro.sisproject.Models.UserSingleton;
import pt.ismai.pedro.sisproject.R;

import static pt.ismai.pedro.sisproject.Constants.Constants.ERROR_DIALOG_REQUEST;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    EditText input_email, input_password;
    AppCompatButton btn_login;
    TextView link_signup, forgot_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        input_email = findViewById(R.id.input_email);
        input_password = findViewById(R.id.input_password);
        btn_login = findViewById(R.id.btn_login);
        link_signup = findViewById(R.id.link_signup);
        forgot_password = findViewById(R.id.link_forgot_password);

        fireBaseSetup();

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = input_email.getText().toString();
                String password = input_password.getText().toString();

                // Validating the google services version and catching the empty forms
                if(isServicesOK()){
                    if(!email.equals("") && !password.equals("")){
                        signIn(email,password);
                        hideSoftKeyboard();
                    }else{
                        toastMessage("Please fill all the fields");
                    }
                }
            }
        });

        link_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(),ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fireBaseSetup(){

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){

                    //First we set up the Firebase Firestore database
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().build();
                    db.setFirestoreSettings(settings);

                    //Then we get a firebase document reference for user collection with the specific document to the authenticated user
                    //Firestore Database tree schema:
                    //Collections -> Documents -> Fields
                    DocumentReference userRef = db.collection(getString(R.string.collection_users)).document(Objects.requireNonNull(mAuth.getUid()));

                    //Lastly the information for that specific user is retrieved and we get the results to our User class
                    // and create a singleton object for that user
                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                                ((UserSingleton)(getApplicationContext())).setUser(user);
                            }
                        }
                    });
                }
            }
        };

    }

    private void signIn(String email, String password) {
        //Create a process dialog to give the user acknowledgement that a login request is initiate
        btn_login.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        //Using the Firebase authentication method to sign the user to the app
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(final @NonNull Task<AuthResult> task) {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                // On complete call either onLoginSuccess or onLoginFailed
                                if(task.isSuccessful()) {
                                    executeActivity(MainActivity.class);
                                    finish();
                                }
                                else{
                                    toastMessage(Objects.requireNonNull(task.getException()).getMessage());
                                }
                                btn_login.setEnabled(true);
                                progressDialog.dismiss();
                            }
                        }, 2000);
            }
        });
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

    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            //user is already connected so we need to redirect him to MainActivity
            executeActivity(MainActivity.class);
        }
    }

    public boolean isServicesOK(){
        //isServicesOK: checking google services version
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this);
        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(LoginActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
