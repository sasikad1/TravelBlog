package uk.ac.wlv.travelblog.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn, btnContinueAsGuest, btnCreateAccount;
    private CheckBox chkCloudSync;
    private TextView tvError;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove title bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("BlogAppPrefs", MODE_PRIVATE);

        // Check if already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            navigateToMain();
            return;
        }

        initViews();
        setupClickListeners();
        loadSavedPreferences();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnContinueAsGuest = findViewById(R.id.btnContinueAsGuest);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        chkCloudSync = findViewById(R.id.chkCloudSync);
        tvError = findViewById(R.id.tvError);
    }

    private void setupClickListeners() {
        btnSignIn.setOnClickListener(v -> loginUser());
        btnContinueAsGuest.setOnClickListener(v -> continueAsGuest());
        btnCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void loadSavedPreferences() {
        String savedEmail = sharedPreferences.getString("savedEmail", "");
        if (!savedEmail.isEmpty()) {
            etEmail.setText(savedEmail);
        }
        boolean cloudSyncEnabled = sharedPreferences.getBoolean("cloudSyncEnabled", false);
        chkCloudSync.setChecked(cloudSyncEnabled);
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedEmail", etEmail.getText().toString().trim());
        editor.putBoolean("cloudSyncEnabled", chkCloudSync.isChecked());
        editor.apply();
    }

    private void loginUser() {
        // Clear previous error
        tvError.setVisibility(android.view.View.GONE);

        // Remove any existing styling
        etEmail.setError(null);
        etPassword.setError(null);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // ========== VALIDATION ==========

        // Check if email is empty
        if (TextUtils.isEmpty(email)) {
            showError("Email is required");
            etEmail.setError("Email required");
            etEmail.requestFocus();
            return;
        }

        // Check if email is valid format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address");
            etEmail.setError("Invalid email format");
            etEmail.requestFocus();
            return;
        }

        // Check if password is empty
        if (TextUtils.isEmpty(password)) {
            showError("Password is required");
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        // Check password length
        if (password.length() < 4) {
            showError("Password must be at least 4 characters");
            etPassword.setError("Password too short (min 4 characters)");
            etPassword.requestFocus();
            return;
        }

        // ========== CHECK IN DATABASE ==========

        // Check if email exists in database
        if (!dbHelper.isEmailExists(email)) {
            showError("Email not found. Please register first.");
            etEmail.setError("Email not registered");
            etEmail.requestFocus();
            return;
        }

        // Check if credentials are correct
        if (dbHelper.checkUser(email, password)) {
            // Login successful
            int userId = dbHelper.getUserId(email);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putInt("userId", userId);
            editor.putString("userEmail", email);
            editor.putBoolean("isGuest", false);
            editor.apply();

            savePreferences();

            Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            // Wrong password
            showError("Incorrect password. Please try again.");
            etPassword.setError("Wrong password");
            etPassword.requestFocus();
            etPassword.setText(""); // Clear password field for security
        }
    }

    private void continueAsGuest() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putInt("userId", -1);
        editor.putString("userEmail", "Guest");
        editor.putBoolean("isGuest", true);
        editor.apply();

        Toast.makeText(this, "Continuing as Guest", Toast.LENGTH_SHORT).show();
        navigateToMain();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(android.view.View.VISIBLE);

        // Auto hide after 4 seconds
        tvError.postDelayed(() -> {
            if (tvError != null) {
                tvError.setVisibility(android.view.View.GONE);
            }
        }, 4000);
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}