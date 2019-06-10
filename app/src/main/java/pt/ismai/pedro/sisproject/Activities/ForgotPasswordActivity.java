package pt.ismai.pedro.sisproject.Activities;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import pt.ismai.pedro.sisproject.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText input_email;
    AppCompatButton btn_forgot_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        input_email = findViewById(R.id.input_email);
        btn_forgot_password = findViewById(R.id.btn_forgot_password);

        btn_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Create a process dialog to give the user acknowledgement that we're trying to reset his password
                final ProgressDialog progressDialog = new ProgressDialog(ForgotPasswordActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Processing...");
                progressDialog.show();
                String email = input_email.getText().toString();

                //Firebase give us the possibility to send a reset password request to the authenticated email
                //We can reset our password with a specific link
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(final @NonNull Task<Void> task) {
                        new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    // On complete call either onLoginSuccess or onLoginFailed
                                    if(task.isSuccessful()) {
                                        toastMessage("Password sent to your email");
                                    }
                                    else{
                                        toastMessage(Objects.requireNonNull(task.getException()).getMessage());
                                    }
                                    btn_forgot_password.setEnabled(true);
                                    progressDialog.dismiss();
                                }
                            }, 3000);
                    }
                });
            }
        });
    }

    private void toastMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }
}
