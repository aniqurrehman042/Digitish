package com.example.test_04.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test_04.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class CustomerLogin extends AppCompatActivity {

    private static final int RC_SIGN_IN = 0;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        init();


    }

    private void init() {
        setUpSignInServices();
        checkSignUp();

        findViews();

        setListeners();

        setupGoogleLogin();
    }

    private void setUpSignInServices() {

    }

    private void checkSignUp() {
        TextView tvTitle = findViewById(R.id.tv_title);
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            boolean signUp = extrasBundle.getBoolean("signUp");
            if (signUp) {
                tvTitle.setText("Sign in with google");
            }
        } else {
            tvTitle.setText("Customer Login");
        }
    }

    private String getCustomerDetail(String key) {
        SharedPreferences sp = getSharedPreferences("Customer", MODE_PRIVATE);
        return sp.getString(key, null);
    }

    private void setupGoogleLogin() {
        mAuth = FirebaseAuth.getInstance();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        String name = getCustomerDetail("Customer Name");
        if (name == null) {
            mAuth.signOut();
            mGoogleSignInClient.signOut();
        }


        if (account != null) {
            startCustomerHome();
        }
    }

    private void setListeners() {
        ((Button) findViewById(R.id.btn_login_google)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    private void showProgressDialog(String title) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(title);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void findViews() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            firebaseAuthWithGoogle(task.getResult());
        }
    }

    private void handleSignInResult(GoogleSignInAccount acct) {

        showProgressDialog("Logging in");

        final String name = acct.getDisplayName();
        final String email = acct.getEmail();
        if (email == null) {
            Toast.makeText(this, "Couldn't sign in", Toast.LENGTH_SHORT).show();
            return;
        }

        int lastIndex = email.indexOf('@');
        char[] emailChars = new char[lastIndex];
        for (int i = 0; i < lastIndex; i++)
            emailChars[i] = email.charAt(i);
        final String username = String.valueOf(emailChars);
        final String profilePicture = acct.getPhotoUrl().toString();

        db.collection("Customers")
                .whereEqualTo("Email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().size() > 0) {

                            saveCustomerPreferences(task.getResult().getDocuments().get(0).getData());

                            Map<String, Object> data = new HashMap<>();
                            if (name != null) {
                                data.put("Customer Name", name);
                            }
                            if (profilePicture != null) {
                                data.put("Profile Picture", profilePicture);
                            }
                            data.put("Username", username);
                            String customerId = task.getResult().getDocuments().get(0).getId();
                            db.collection("Customers")
                                    .document(customerId)
                                    .update(data)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressDialog.dismiss();
                                                startCustomerHome();
                                            } else {
                                                Toast.makeText(CustomerLogin.this, "Failed. Please try again.", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }
                                    });
                        } else {

                            long points = 0;

                            Map<String, Object> data = new HashMap<>();
                            data.put("Customer Name", name);
                            data.put("Email", email);
                            data.put("Profile Picture", profilePicture);
                            data.put("Username", username);
                            data.put("Points", points);
                            db.collection("Customers")
                                    .add(data)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                db.collection("Customers")
                                                        .whereEqualTo("Email", email)
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    saveCustomerPreferences(task.getResult().getDocuments().get(0).getData());

                                                                    progressDialog.dismiss();
                                                                    startCustomerHome();
                                                                } else {
                                                                    Toast.makeText(CustomerLogin.this, "Failed. Please try again.", Toast.LENGTH_SHORT).show();
                                                                    return;
                                                                }
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(CustomerLogin.this, "Failed. Please try again.", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }
                                    });

                        }
                    }
                });


    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            handleSignInResult(acct);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(CustomerLogin.this, "Failed to sign in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveCustomerPreferences(Map<String, Object> data) {
        SharedPreferences.Editor sp = getSharedPreferences("Customer", MODE_PRIVATE).edit();
        sp.putString("Customer Name", (String) data.get("Customer Name"));
        sp.putString("Email", (String) data.get("Email"));
        sp.putString("Phone", (String) data.get("Phone"));
        sp.putString("Points", String.valueOf(data.get("Points")));
        sp.putString("Profile Picture", (String) data.get("Profile Picture"));
        sp.putString("Username", (String) data.get("Username"));
        sp.apply();
    }

    private void startCustomerHome() {
        startActivity(new Intent(CustomerLogin.this, CustomerHome.class));
        finish();
    }
}
