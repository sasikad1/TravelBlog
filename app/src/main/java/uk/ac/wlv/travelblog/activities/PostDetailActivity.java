package uk.ac.wlv.travelblog.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.database.DatabaseHelper;
import uk.ac.wlv.travelblog.models.Message;
import uk.ac.wlv.travelblog.utils.BloggerUploadService;

public class PostDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE_ID = "message_id";

    private ImageView ivPostImage, fabBack;
    private TextView tvTitle, tvDate, tvContent, tvSqliteId;
    private ImageButton btnShare, btnEdit, btnDelete;
    private Button btnUploadStatus;

    private DatabaseHelper dbHelper;
    private int messageId;
    private Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        dbHelper = new DatabaseHelper(this);

        messageId = getIntent().getIntExtra(EXTRA_MESSAGE_ID, -1);

        if (messageId == -1) {
            Toast.makeText(this, "Error loading post", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadMessageData();
        setupClickListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void initViews() {
        ivPostImage = findViewById(R.id.ivPostImage);
        fabBack = findViewById(R.id.fabBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvDate = findViewById(R.id.tvDate);
        tvContent = findViewById(R.id.tvContent);
        tvSqliteId = findViewById(R.id.tvSqliteId);
        btnShare = findViewById(R.id.btnShare);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnUploadStatus = findViewById(R.id.btnUploadStatus);
    }

    private void loadMessageData() {
        message = dbHelper.getMessageAsObject(messageId);

        if (message != null) {
            tvTitle.setText(message.getTitle());
            tvContent.setText("\"" + message.getContent() + "\"");

            String formattedDate = formatDate(message.getCreatedDate());
            tvDate.setText(formattedDate);

            tvSqliteId.setText("SQLite ID: " + message.getId());

            loadImage(message.getImagePath());
        } else {
            Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imgFile = new File(getFilesDir(), imagePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ivPostImage.setImageBitmap(bitmap);
                    ivPostImage.setVisibility(ImageView.VISIBLE);
                    return;
                }

                if (imagePath.startsWith("content://")) {
                    Uri imageUri = Uri.parse(imagePath);
                    ivPostImage.setImageURI(imageUri);
                    ivPostImage.setVisibility(ImageView.VISIBLE);
                    return;
                }

                ivPostImage.setImageResource(android.R.drawable.ic_menu_gallery);
                ivPostImage.setVisibility(ImageView.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
                ivPostImage.setImageResource(android.R.drawable.ic_menu_gallery);
                ivPostImage.setVisibility(ImageView.VISIBLE);
            }
        } else {
            ivPostImage.setImageResource(android.R.drawable.ic_menu_gallery);
            ivPostImage.setVisibility(ImageView.VISIBLE);
        }
    }

    private String formatDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return "No date";
        }
        if (dateTime.contains(" ")) {
            String datePart = dateTime.split(" ")[0];
            String[] parts = datePart.split("-");
            if (parts.length == 3) {
                String month = getMonthName(Integer.parseInt(parts[1]));
                return month + " " + parts[2] + ", " + parts[0];
            }
            return datePart;
        }
        return dateTime;
    }

    private String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return months[month - 1];
    }

    private void setupClickListeners() {
        // Back button
        fabBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        // ========== SHARE BUTTON - SHOW PLATFORM LIST ==========
        btnShare.setOnClickListener(v -> showSharePlatformDialog());
        // ======================================================

        // Edit button
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(PostDetailActivity.this, EditPostActivity.class);
            intent.putExtra(EditPostActivity.EXTRA_MESSAGE_ID, messageId);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Delete button
        btnDelete.setOnClickListener(v -> deletePost());

        // Upload button (direct to Blogger)
        btnUploadStatus.setOnClickListener(v -> uploadToBlogger());
    }

    // ========== SHOW PLATFORM LIST DIALOG ==========
    private void showSharePlatformDialog() {
        String[] platforms = {
                "Blogger",
                "Pinterest",
                "Google+",
                "Twitter",
                "Facebook",
                "Instagram"
        };

        String[] icons = {
                "📝", "📌", "🔴", "🐦", "📘", "📷"
        };

        // Create custom list with icons
        String[] items = new String[platforms.length];
        for (int i = 0; i < platforms.length; i++) {
            items[i] = icons[i] + "  " + platforms[i];
        }

        new AlertDialog.Builder(this)
                .setTitle("Share to...")
                .setItems(items, (dialog, which) -> {
                    String selectedPlatform = platforms[which];

                    if (selectedPlatform.equals("Blogger")) {
                        // Upload to Blogger
                        uploadToBlogger();
                    } else if (selectedPlatform.equals("Pinterest")) {
                        shareToPinterest();
                    } else if (selectedPlatform.equals("Google+")) {
                        shareToGooglePlus();
                    } else if (selectedPlatform.equals("Twitter")) {
                        shareToTwitter();
                    } else if (selectedPlatform.equals("Facebook")) {
                        shareToFacebook();
                    } else if (selectedPlatform.equals("Instagram")) {
                        shareToInstagram();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void shareToPinterest() {
        if (message == null) return;
        String shareText = message.getTitle() + "\n\n" + message.getContent() + "\n\n- WanderLog";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, message.getTitle());
        startActivity(Intent.createChooser(shareIntent, "Share to Pinterest"));
    }

    private void shareToGooglePlus() {
        if (message == null) return;
        String shareText = message.getTitle() + "\n\n" + message.getContent() + "\n\n- WanderLog";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share to Google+"));
    }

    private void shareToTwitter() {
        if (message == null) return;
        String shareText = message.getTitle() + "\n\n" + message.getContent() + "\n\n- WanderLog";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share to Twitter"));
    }

    private void shareToFacebook() {
        if (message == null) return;
        String shareText = message.getTitle() + "\n\n" + message.getContent() + "\n\n- WanderLog";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share to Facebook"));
    }

    private void shareToInstagram() {
        if (message == null) return;
        // Instagram requires image sharing
        Toast.makeText(this, "Instagram sharing requires image. Use share intent with image.", Toast.LENGTH_LONG).show();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message.getTitle() + "\n\n" + message.getContent());
        startActivity(Intent.createChooser(shareIntent, "Share to Instagram"));
    }
    // ================================================

    // ========== UPLOAD TO BLOGGER ==========
    private void uploadToBlogger() {
        if (message == null) {
            Toast.makeText(this, "No post to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = message.getTitle();
        String content = message.getContent();

        new AlertDialog.Builder(this)
                .setTitle("Upload to Blogger")
                .setMessage("Do you want to publish this post to your Blogger blog?\n\n" +
                        "Title: " + title + "\n\n" +
                        "Blog: wanderlogtravels.blogspot.com")
                .setPositiveButton("Upload", (dialog, which) -> {
                    BloggerUploadService uploadService = new BloggerUploadService(this);
                    uploadService.setOnUploadListener(new BloggerUploadService.OnUploadListener() {
                        @Override
                        public void onSuccess(String postUrl) {
                            runOnUiThread(() -> {
                                btnUploadStatus.setText("Posted to Blogger");
                                btnUploadStatus.setEnabled(false);

                                new AlertDialog.Builder(PostDetailActivity.this)
                                        .setTitle("Upload Successful!")
                                        .setMessage("Your post has been published to Blogger!\n\n" + postUrl)
                                        .setPositiveButton("View Post", (d, w) -> {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(postUrl));
                                            startActivity(intent);
                                        })
                                        .setNegativeButton("OK", null)
                                        .show();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(PostDetailActivity.this,
                                        "Upload failed: " + error, Toast.LENGTH_LONG).show();
                            });
                        }
                    });

                    uploadService.authenticateAndUpload(title, content);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    // ======================================

    private void deletePost() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Memory")
                .setMessage("Are you sure you want to delete this memory?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int result = dbHelper.deleteMessage(messageId);
                    if (result > 0) {
                        Toast.makeText(this, "Memory deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}