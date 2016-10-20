package com.dasyel.dasyelwillems_pset6;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dasyel.dasyelwillems_pset6.persistence.external_db.FirebaseDbManager;
import com.dasyel.dasyelwillems_pset6.persistence.external_db.interfaces.NewUserChecker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.dasyel.dasyelwillems_pset6.models.Contract.DEFAULT_LIST;
import static com.dasyel.dasyelwillems_pset6.models.Contract.SEARCH_RESULTS_LIST;
import static com.dasyel.dasyelwillems_pset6.models.Contract.SUGGESTIONS_LIST;

/**
 * This activity opens when the user is not authenticated according to firebase
 * This activity handles the creation of accounts and logging in
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener, NewUserChecker {
    private ProgressDialog progressDialog;

    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private FirebaseDbManager firebaseDbManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);

        mEmailField = (EditText) findViewById(R.id.email);
        mPasswordField = (EditText) findViewById(R.id.password);

        findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.create_account_button).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                progressDialog.hide();
                if (user != null) {
                    firebaseDbManager = FirebaseDbManager.getInstance(user);
                    firebaseDbManager.checkForUser(LoginActivity.this);
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void createAccount(String email, String password) {
        if (!validateForm()) {
            return;
        }

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.hide();
                    }
                });
    }

    private void signIn(String email, String password) {
        if (!validateForm()) {
            return;
        }

        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.hide();
                    }
                });
    }

    // Helper function to validate the login form
    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    // Gets called whenever a button is clicked
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.create_account_button) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.login_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }

    // This method gets called when the results on user existence return from the database
    @Override
    public void handleNewUser(boolean userExists) {
        if(!userExists){
            firebaseDbManager.addUser();
            firebaseDbManager.addMovieList(DEFAULT_LIST);
            firebaseDbManager.addMovieList(SUGGESTIONS_LIST);
            firebaseDbManager.addMovieList(SEARCH_RESULTS_LIST);
        }
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}