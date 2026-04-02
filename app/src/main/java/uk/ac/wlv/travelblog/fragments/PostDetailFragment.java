package uk.ac.wlv.travelblog.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.database.DatabaseHelper;
import uk.ac.wlv.travelblog.models.Message;

public class PostDetailFragment extends Fragment {

    private static final String ARG_MESSAGE_ID = "message_id";

    private ImageView ivPostImage, fabBack;
    private TextView tvTitle, tvDate, tvContent, tvSqliteId;
    private ImageButton btnShare, btnEdit, btnDelete;
    private Button btnUploadStatus;

    private DatabaseHelper dbHelper;
    private int messageId;
    private Message message;

    public static PostDetailFragment newInstance(int messageId) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MESSAGE_ID, messageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());

        if (getArguments() != null) {
            messageId = getArguments().getInt(ARG_MESSAGE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadMessageData();
        setupClickListeners();
    }

    private void initViews(View view) {
        ivPostImage = view.findViewById(R.id.ivPostImage);
        fabBack = view.findViewById(R.id.fabBack);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvDate = view.findViewById(R.id.tvDate);
        tvContent = view.findViewById(R.id.tvContent);
        tvSqliteId = view.findViewById(R.id.tvSqliteId);
        btnShare = view.findViewById(R.id.btnShare);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnUploadStatus = view.findViewById(R.id.btnUploadStatus);
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
                // For now, keep default
            }

            // Update upload status button
            btnUploadStatus.setText("Saved to SQLite");
        }
    }

    private String formatDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return "No date";
        }
        // Format: "2024-04-01 14:30:00" -> "March 10, 2026"
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
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Share button
        btnShare.setOnClickListener(v -> {
            sharePost();
        });

        // Edit button
        btnEdit.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit feature coming soon", Toast.LENGTH_SHORT).show();
        });

        // Delete button
        btnDelete.setOnClickListener(v -> {
            deletePost();
        });
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
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Memory")
                .setMessage("Are you sure you want to delete this memory?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int result = dbHelper.deleteMessage(messageId);
                    if (result > 0) {
                        Toast.makeText(getContext(), "Memory deleted", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}