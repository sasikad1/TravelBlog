package uk.ac.wlv.travelblog.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.database.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp, btnBackToLogin;
    private TextView tvError;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupClickListeners();
        setupFieldListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        tvError = findViewById(R.id.tvError);
    }

    private void setupClickListeners() {
        btnSignUp.setOnClickListener(v -> registerUser());
        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });
    }

    private void setupFieldListeners() {
        // Clear errors when user starts typing
        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) clearError(etEmail);
        });

        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) clearError(etPassword);
        });

        etConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) clearError(etConfirmPassword);
        });

        // Real-time validation for email
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

        // Real-time validation for password
        etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    clearError(etPassword);
                    hideError();
                    // Check password match when user types
                    checkPasswordMatch();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Real-time validation for confirm password
        etConfirmPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    checkPasswordMatch();
                } else {
                    clearError(etConfirmPassword);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void checkPasswordMatch() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {
            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
            } else {
                etConfirmPassword.setError(null);
            }
        }
    }

    // ========== COMPLETE VALIDATION ==========

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
            showFieldError(etEmail, "Please enter a valid email address (e.g., name@example.com)");
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

        if (dbHelper.isEmailExists(email)) {
            showFieldError(etEmail, "Email already registered. Please login or use another email.");
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

        // Check password strength (optional)
        if (!password.matches(".*[A-Z].*")) {
            showFieldError(etPassword, "Password should contain at least one uppercase letter");
            return false;
        }

        if (!password.matches(".*[0-9].*")) {
            showFieldError(etPassword, "Password should contain at least one number");
            return false;
        }

        return true;
    }

    private boolean validateConfirmPassword() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(confirmPassword)) {
            showFieldError(etConfirmPassword, "Please confirm your password");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showFieldError(etConfirmPassword, "Passwords do not match");
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

    // ========== REGISTER USER ==========

    private void registerUser() {
        hideError();
        clearError(etEmail);
        clearError(etPassword);
        clearError(etConfirmPassword);

        // Validate all fields
        if (!validateEmail()) return;
        if (!validatePassword()) return;
        if (!validateConfirmPassword()) return;

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        long result = dbHelper.registerUser(email, password);

        if (result != -1) {
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        } else {
            showError("Registration failed. Please try again.");
        }
    }
}