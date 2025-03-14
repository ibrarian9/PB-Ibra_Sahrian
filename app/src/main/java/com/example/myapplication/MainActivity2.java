package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Models.UserDetails;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity2 extends AppCompatActivity {

    Button signUpBtn;
    TextInputEditText usernameSignUp, passwordSignUp, nimPengguna, emailPengguna;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        signUpBtn = findViewById(R.id.signUpBtn);
        usernameSignUp = findViewById(R.id.usernameSignUp);
        emailPengguna = findViewById(R.id.emailPengguna);
        passwordSignUp = findViewById(R.id.passwordSingUp); // Fixed typo
        nimPengguna = findViewById(R.id.nimPengguna);

        signUpBtn.setOnClickListener(view -> {
            String username = Objects.requireNonNull(usernameSignUp.getText()).toString().trim();
            String email = Objects.requireNonNull(emailPengguna.getText()).toString().trim();
            String password = Objects.requireNonNull(passwordSignUp.getText()).toString().trim();
            String NIM = Objects.requireNonNull(nimPengguna.getText()).toString().trim();

            if (username.isEmpty()) {
                usernameSignUp.setError("Enter Username");
                usernameSignUp.requestFocus();
            } else if (email.isEmpty()) {
                emailPengguna.setError("Enter Email");
                emailPengguna.requestFocus();
            } else if (password.isEmpty()) {
                passwordSignUp.setError("Enter Password");
                passwordSignUp.requestFocus();
            } else if (password.length() < 6) {
                passwordSignUp.setError("Password must be at least 6 characters");
                passwordSignUp.requestFocus();
            } else if (NIM.isEmpty()) {
                nimPengguna.setError("Please Insert your NIM");
                nimPengguna.requestFocus();
            } else {
                registerUser(username, email, password, NIM);
            }
        });
    }

    private void registerUser(String username, String email, String password, String NIM) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity2.this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser fUser = auth.getCurrentUser();
                        if (fUser != null) {
                            fUser.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                                if (verificationTask.isSuccessful()) {
                                    String uid = fUser.getUid();
                                    UserDetails userDetails = new UserDetails(uid, username, email, password, NIM);

                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                    reference.child(uid).setValue(userDetails)
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    Toast.makeText(MainActivity2.this, "Account created", Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(MainActivity2.this, HomeActivity.class));
                                                    finish();
                                                }
                                            }).addOnFailureListener(e ->
                                                    Toast.makeText(MainActivity2.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                                } else {
                                    Toast.makeText(MainActivity2.this, "Failed to send verification email", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            Toast.makeText(MainActivity2.this, "User creation failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                            Toast.makeText(MainActivity2.this, "Password too weak!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity2.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(MainActivity2.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
