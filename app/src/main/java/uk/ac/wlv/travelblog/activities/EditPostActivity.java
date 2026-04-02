package uk.ac.wlv.travelblog.activities;

import android.content.Intent;
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
import uk.ac.wlv.travelblog.models.Message;

public class EditPostActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE_ID = "message_id";

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;

    private ImageButton btnBack;
    private ImageView ivImagePreview;
    private EditText etLocation, etStory;
    private Button btnCamera, btnGallery, btnSave;

    private DatabaseHelper dbHelper;
    private int messageId;
    private Message message;
    private String selectedImagePath = null;
    private boolean imageChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        // Remove action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dbHelper = new DatabaseHelper(this);

        // Get message ID from intent
        messageId = getIntent().getIntExtra(EXTRA_MESSAGE_ID, -1);

        if (messageId == -1) {
            Toast.makeText(this, "Error loading post", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadMessageData();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivImagePreview = findViewById(R.id.ivImagePreview);
        etLocation = findViewById(R.id.etLocation);
        etStory = findViewById(R.id.etStory);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnSave = findViewById(R.id.btnSave);
    }

    private void loadMessageData() {
        message = dbHelper.getMessageAsObject(messageId);

        if (message != null) {
            etLocation.setText(message.getTitle());
            etStory.setText(message.getContent());
            selectedImagePath = message.getImagePath();

            // Load existing image if any
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                loadImage(selectedImagePath);
            }
        }
    }

    private void loadImage(String imagePath) {
        try {
            // Check if it's a file path (from camera or saved gallery)
            File imgFile = new File(getFilesDir(), imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ivImagePreview.setImageBitmap(bitmap);
                ivImagePreview.setVisibility(ImageView.VISIBLE);
                return;
            }

            // Check if it's a content URI (from gallery - old data)
            if (imagePath.startsWith("content://")) {
                Uri imageUri = Uri.parse(imagePath);
                ivImagePreview.setImageURI(imageUri);
                ivImagePreview.setVisibility(ImageView.VISIBLE);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> savePost());
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
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                // Camera - Get thumbnail image
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    selectedImagePath = saveBitmapToFile(imageBitmap);
                    ivImagePreview.setImageBitmap(imageBitmap);
                    ivImagePreview.setVisibility(ImageView.VISIBLE);
                    imageChanged = true;
                    Toast.makeText(this, "Photo captured", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                // Gallery - Save image permanently
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    selectedImagePath = saveGalleryImageToFile(imageUri);
                    if (selectedImagePath != null) {
                        // Load and display the saved image
                        File imgFile = new File(getFilesDir(), selectedImagePath);
                        if (imgFile.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            ivImagePreview.setImageBitmap(bitmap);
                            ivImagePreview.setVisibility(ImageView.VISIBLE);
                        }
                    }
                    imageChanged = true;
                    Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // ========== SAVE IMAGE FROM GALLERY TO LOCAL FILE ==========
    private String saveGalleryImageToFile(Uri imageUri) {
        try {
            // Create filename with timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = "IMG_" + timeStamp + ".jpg";

            // Open input stream from content URI
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            // Save to app's private storage
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

    // Save camera image to file
    private String saveBitmapToFile(Bitmap bitmap) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = "IMG_" + timeStamp + ".jpg";

            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();

            return filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // ===========================================================

    private void savePost() {
        String location = etLocation.getText().toString().trim();
        String story = etStory.getText().toString().trim();

        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Location required");
            etLocation.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(story)) {
            etStory.setError("Story required");
            etStory.requestFocus();
            return;
        }

        // Update in database
        int result = dbHelper.updateMessage(messageId, location, story, selectedImagePath);

        if (result > 0) {
            Toast.makeText(this, "Memory updated successfully!", Toast.LENGTH_SHORT).show();

            // If image was changed and old image exists, delete old image file
            if (imageChanged && message != null && message.getImagePath() != null) {
                File oldImage = new File(getFilesDir(), message.getImagePath());
                if (oldImage.exists()) {
                    oldImage.delete();
                }
            }

            // Go back to detail page
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_MESSAGE_ID, messageId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        } else {
            Toast.makeText(this, "Failed to update memory", Toast.LENGTH_SHORT).show();
        }
    }
}