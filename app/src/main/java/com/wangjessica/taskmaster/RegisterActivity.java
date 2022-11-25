package com.wangjessica.taskmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Button createAccountButton;
    private EditText userEmail, userPassword;
    private TextView loginLink;

    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private String userId;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeFields();

        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        System.out.println(auth);
        System.out.println(email+" " + password);

        loadingBar.setTitle("Creating New Account");
        loadingBar.setMessage("Creating new account...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                userId = auth.getCurrentUser().getUid();
                addUserProfile();
                System.out.println("finished");
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("failure:"+e);
            }
        });
    }

    private void addUserProfile(){
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("Coins", 0);
        info.put("Color 0", -1);
        info.put("Color 1", -1);
        info.put("Color 2", -1);
        info.put("Color 3", -1);
        info.put("Name", "User");
        rootRef.child("Users").child(userId).child("Profile").setValue(info);
    }
    private void initializeFields() {
        createAccountButton = findViewById(R.id.register_button);
        userEmail = findViewById(R.id.register_email);
        userPassword = findViewById(R.id.register_password);
        loginLink = findViewById(R.id.login_link);
        loadingBar = new ProgressDialog(this);
    }
}