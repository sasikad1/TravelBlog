package uk.ac.wlv.travelblog.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.database.DatabaseHelper;

import android.net.Uri;
import android.media.Ringtone;
import android.media.RingtoneManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn, btnContinueAsGuest, btnCreateAccount;
    private CheckBox chkCloudSync;
    private TextView tvError;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    private MediaPlayer welcomeSound;  // Add MediaPlayer


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_login);

        // Initialize welcome sound
        welcomeSound = MediaPlayer.create(this, R.raw.welcome_sound);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("BlogAppPrefs", MODE_PRIVATE);

        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            navigateToMain();
            return;
        }

        initViews();
        setupClickListeners();
        loadSavedPreferences();
        setupFieldListeners();
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

    private void setupFieldListeners() {
        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) clearError(etEmail);
        });

        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) clearError(etPassword);
        });

        etEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    clearError(etEmail);
                    hideError();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    clearError(etPassword);
                    hideError();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
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

    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            showFieldError(etEmail, "Email address is required");
            return false;
        }

        if (email.length() > 100) {
            showFieldError(etEmail, "Email address is too long (max 100 characters)");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showFieldError(etEmail, "Please enter a valid email address");
            return false;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showFieldError(etEmail, "Email must contain '@' and '.'");
            return false;
        }

        String domain = email.substring(email.indexOf("@") + 1);
        if (domain.length() < 3) {
            showFieldError(etEmail, "Invalid email domain");
            return false;
        }

        if (!dbHelper.isEmailExists(email)) {
            showFieldError(etEmail, "No account found. Please register first.");
            return false;
        }

        return true;
    }

    private boolean validatePassword() {
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(password)) {
            showFieldError(etPassword, "Password is required");
            return false;
        }

        if (password.length() < 4) {
            showFieldError(etPassword, "Password must be at least 4 characters");
            return false;
        }

        if (password.length() > 50) {
            showFieldError(etPassword, "Password is too long (max 50 characters)");
            return false;
        }

        if (!password.matches("^[a-zA-Z0-9@#$%^&+=!._-]+$")) {
            showFieldError(etPassword, "Password contains invalid characters");
            return false;
        }

        return true;
    }

    private boolean validateCredentials(String email, String password) {
        if (!dbHelper.checkUser(email, password)) {
            showFieldError(etPassword, "Incorrect password. Please try again.");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void showFieldError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
        showError(message);
    }

    private void clearError(EditText field) {
        field.setError(null);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);

        tvError.postDelayed(() -> {
            if (tvError != null && tvError.getVisibility() == View.VISIBLE) {
                tvError.setVisibility(View.GONE);
            }
        }, 5000);
    }

    private void loginUser() {
        hideError();
        clearError(etEmail);
        clearError(etPassword);

        if (!validateEmail()) return;
        if (!validatePassword()) return;

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateCredentials(email, password)) return;

        int userId = dbHelper.getUserId(email);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putInt("userId", userId);
        editor.putString("userEmail", email);
        editor.putBoolean("isGuest", false);
        editor.apply();

        savePreferences();

        // ========== PLAY WELCOME SOUND ==========
        playWelcomeSound();
        // ======================================

        Toast.makeText(this, "Welcome back, " + email.split("@")[0] + "!", Toast.LENGTH_SHORT).show();
        navigateToMain();
    }

    // ========== PLAY WELCOME SOUND METHOD ==========
    private void playWelcomeSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ==============================================

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

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (welcomeSound != null) {
            welcomeSound.release();
            welcomeSound = null;
        }
    }
}