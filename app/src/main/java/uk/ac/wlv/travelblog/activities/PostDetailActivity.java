package uk.ac.wlv.travelblog.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.database.DatabaseHelper;
import uk.ac.wlv.travelblog.models.Message;

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

            // Format date
            String formattedDate = formatDate(message.getCreatedDate());
            tvDate.setText(formattedDate);

            // Set SQLite ID
            tvSqliteId.setText("SQLite ID: " + message.getId());

            // Load image if exists
            if (message.getImagePath() != null && !message.getImagePath().isEmpty()) {
                // Load image using Glide or set directly
            }
        } else {
            Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show();
            finish();
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

        // Share button
        btnShare.setOnClickListener(v -> sharePost());

        // Edit button
        btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Edit feature coming soon", Toast.LENGTH_SHORT).show();
        });

        // Delete button
        btnDelete.setOnClickListener(v -> deletePost());
    }

    private void sharePost() {
        if (message != null) {
            String shareText = message.getTitle() + "\n\n" + message.getContent() + "\n\n- WanderLog";
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        }
    }

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}