package com.example.test_04.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test_04.R;
import com.example.test_04.utils.PreferencesUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class MerchantLogin extends AppCompatActivity {

    private Button btnLogin;
    private EditText etUsername;
    private EditText etPassword;
    private TextView tvTitle;
    private TextView tvForgotPass;
    ProgressDialog progressDialog;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_login);

        init();

    }

    private void init() {
        setUpFirebase();
        checkLoginStatus();
        findViews();
        setListeners();

        setStatusBar();
        setPageTitle("Merchant Login");
    }

    private void checkLoginStatus() {
        String name = getMerchantDetail("Merchant Name");
        if (name == null) {
            mAuth.signOut();
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            startMerchantHome();
            return;
        }
    }

    private String getMerchantDetail(String key) {
        SharedPreferences sp = getSharedPreferences("Merchant", MODE_PRIVATE);
        return sp.getString(key, null);
    }

    private void setUpFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void setPageTitle(String title) {
        tvTitle.setText(title);
    }

    private void setStatusBar() {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.darker_darkest));
    }

    private void setListeners() {

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("Logging in");
                authenticateUser();
            }
        });

        tvForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialogResetPassword = new Dialog(MerchantLogin.this);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                dialogResetPassword.setCanceledOnTouchOutside(true);
                dialogResetPassword.setContentView(R.layout.layout_reset_password);
                dialogResetPassword.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = ((EditText) dialogResetPassword.findViewById(R.id.et_email)).getText().toString();

                        if (email.trim().isEmpty()) {
                            Toast.makeText(MerchantLogin.this, "Please enter your registered email", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        showProgressDialog("Sending Reset Email");

                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(MerchantLogin.this, "The instructions to reset your password have been sent to your email", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MerchantLogin.this, "Invalid email", Toast.LENGTH_SHORT).show();
                                        }

                                        progressDialog.dismiss();
                                        dialogResetPassword.dismiss();
                                    }
                                });
                    }
                });

                dialogResetPassword.show();

                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialogResetPassword.getWindow().setAttributes(params);
            }
        });
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(MerchantLogin.this);
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void authenticateUser() {

        final String email = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    saveMerchantDetails(email, password);
                } else {
                    Toast.makeText(MerchantLogin.this, "Username or password is incorrect", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void saveMerchantDetails(String email, String password) {
        db.collection("Merchants")
                .whereEqualTo("Email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            if (task.getResult().getDocuments().size() < 1) {
                                Toast.makeText(MerchantLogin.this, "Failed to login", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                return;
                            }
                            setPreferences(task.getResult().getDocuments().get(0).getData());
                            startMerchantHome();
                        } else {
                            Toast.makeText(MerchantLogin.this, "An error occurred", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    private void startMerchantHome() {
        startActivity(new Intent(MerchantLogin.this, MerchantHome.class));
        finish();
    }

    private void setPreferences(Map<String, Object> merchant) {
        SharedPreferences sp = getSharedPreferences("Merchant", MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        String[] keys = {"Merchant Name", "Merchant Description", "Email", "Products", "Merchant Rating", "Website"};
        for(String key : keys){
            spe.putString(key, merchant.get(key).toString());
        }
        spe.apply();
    }

    private void findViews() {
        btnLogin = findViewById(R.id.btn_login);
        tvTitle = findViewById(R.id.tv_title);
        etPassword = findViewById(R.id.et_password);
        etUsername = findViewById(R.id.et_username);
        tvForgotPass = findViewById(R.id.tv_forgot_pass);
    }
}
