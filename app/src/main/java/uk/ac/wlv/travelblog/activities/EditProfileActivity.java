package uk.ac.wlv.travelblog.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.database.DatabaseHelper;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int CAMERA_REQUEST_CODE = 101;

    private ImageButton btnCancel;
    private ImageView ivProfileImage;
    private EditText etUsername, etEmail, etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangeImage, btnSaveChanges;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private String currentEmail;
    private String selectedImagePath = null;
    private boolean dataChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("BlogAppPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);
        currentEmail = sharedPreferences.getString("userEmail", "");

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        btnCancel = findViewById(R.id.btnCancel);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangeImage = findViewById(R.id.btnChangeImage);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
    }

    private void loadUserData() {
        String userEmail = dbHelper.getUserEmail(userId);
        etEmail.setText(userEmail);

        String username = userEmail.split("@")[0];
        etUsername.setText(username);

        // Load saved profile image
        String savedImagePath = sharedPreferences.getString("profileImage", null);
        if (savedImagePath != null && !savedImagePath.isEmpty()) {
            try {
                File imgFile = new File(getFilesDir(), savedImagePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                    // Scale and crop to fit circle
                    Bitmap roundedBitmap = getRoundedCroppedBitmap(bitmap, 240);
                    ivProfileImage.setImageBitmap(roundedBitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Helper method to create round bitmap
    private Bitmap getRoundedCroppedBitmap(Bitmap bitmap, int diameter) {
        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(output);

        final int color = 0xff424242;
        final android.graphics.Paint paint = new android.graphics.Paint();
        final android.graphics.Rect rect = new android.graphics.Rect(0, 0, diameter, diameter);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(diameter / 2, diameter / 2, diameter / 2, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> finish());

        btnChangeImage.setOnClickListener(v -> showImagePickerDialog());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery", "Cancel"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Choose Profile Picture");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else if (which == 1) {
                openGallery();
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    selectedImagePath = saveImageToFile(imageUri);
                    ivProfileImage.setImageURI(imageUri);
                    dataChanged = true;
                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    selectedImagePath = saveBitmapToFile(imageBitmap);
                    ivProfileImage.setImageBitmap(imageBitmap);
                    dataChanged = true;
                }
            }
        }
    }

    private String saveImageToFile(Uri imageUri) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = "profile_" + timeStamp + ".jpg";

            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            inputStream.close();

            return filename;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String saveBitmapToFile(Bitmap bitmap) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = "profile_" + timeStamp + ".jpg";

            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();

            return filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveChanges() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean hasChanges = false;

        // Check if email changed
        if (!TextUtils.isEmpty(email) && !email.equals(currentEmail)) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update email in database
            // Note: You need to add updateEmail method in DatabaseHelper
            boolean emailUpdated = dbHelper.updateUserEmail(userId, email);
            if (emailUpdated) {
                currentEmail = email;
                hasChanges = true;
            }
        }

        // Check if password needs to be changed
        if (!TextUtils.isEmpty(currentPassword) && !TextUtils.isEmpty(newPassword)) {
            if (!dbHelper.checkUser(currentEmail, currentPassword)) {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 4) {
                Toast.makeText(this, "New password must be at least 4 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update password in database
            boolean passwordUpdated = dbHelper.updateUserPassword(userId, newPassword);
            if (passwordUpdated) {
                hasChanges = true;
            }
        }

        // Save profile image
        if (selectedImagePath != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("profileImage", selectedImagePath);
            editor.apply();
            hasChanges = true;
        }

        // Update SharedPreferences with new email
        if (!currentEmail.equals(email)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userEmail", currentEmail);
            editor.putString("savedEmail", currentEmail);
            editor.apply();
            hasChanges = true;
        }

        if (hasChanges) {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_LONG).show();

            // Set result to indicate changes were made
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "No changes were made", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}