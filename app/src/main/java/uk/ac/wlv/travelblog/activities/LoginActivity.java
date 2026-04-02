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
        tvError.setVisibility(android.view.View.GONE);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            showError("Email required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Enter valid email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showError("Password required");
            return;
        }

        if (password.length() < 4) {
            showError("Password must be at least 4 characters");
            return;
        }

        if (dbHelper.checkUser(email, password)) {
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
            showError("Invalid email or password");
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
        tvError.postDelayed(() -> tvError.setVisibility(android.view.View.GONE), 3000);
    }

    // CHANGE THIS METHOD - Navigate to MainActivity instead of DashboardActivity
    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}