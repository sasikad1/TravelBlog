package uk.ac.wlv.travelblog.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.database.DatabaseHelper;

public class CreatePostFragment extends Fragment {

    private static final String TAG = "CreatePostFragment";

    // UI Components
    private ImageButton btnClose, btnSaveIcon;
    private Button btnCamera, btnGallery, btnSaveSQLite, btnDeleteDraft;
    private EditText etLocation, etStory;
    private ImageView ivImagePreview;

    private DatabaseHelper dbHelper;
    private int userId;
    private boolean isGuest;
    private String selectedImagePath = null;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;

    public CreatePostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());

        SharedPreferences prefs = requireActivity().getSharedPreferences("BlogAppPrefs", 0);
        userId = prefs.getInt("userId", -1);
        isGuest = prefs.getBoolean("isGuest", false);

        Log.d(TAG, "onCreate: userId=" + userId + ", isGuest=" + isGuest);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();

        if (isGuest) {
            Toast.makeText(getContext(), "Please sign in to create memories", Toast.LENGTH_LONG).show();
            btnSaveSQLite.setEnabled(false);
        }
    }

    private void initViews(View view) {
        btnClose = view.findViewById(R.id.btnClose);
        btnSaveIcon = view.findViewById(R.id.btnSaveIcon);
        btnCamera = view.findViewById(R.id.btnCamera);
        btnGallery = view.findViewById(R.id.btnGallery);
        btnSaveSQLite = view.findViewById(R.id.btnSaveSQLite);
        btnDeleteDraft = view.findViewById(R.id.btnDeleteDraft);
        etLocation = view.findViewById(R.id.etLocation);
        etStory = view.findViewById(R.id.etStory);
        ivImagePreview = view.findViewById(R.id.ivImagePreview);
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        btnSaveIcon.setOnClickListener(v -> savePost());
        btnSaveSQLite.setOnClickListener(v -> savePost());

        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());

        btnDeleteDraft.setOnClickListener(v -> clearForm());
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(getContext(), "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK && data != null) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    selectedImagePath = saveBitmapToFile(imageBitmap);
                    if (ivImagePreview != null) {
                        ivImagePreview.setImageBitmap(imageBitmap);
                        ivImagePreview.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(getContext(), "Photo captured", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    selectedImagePath = imageUri.toString();
                    if (ivImagePreview != null) {
                        ivImagePreview.setImageURI(imageUri);
                        ivImagePreview.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(getContext(), "Image selected", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String saveBitmapToFile(Bitmap bitmap) {
        try {
            String filename = "img_" + System.currentTimeMillis() + ".jpg";
            java.io.FileOutputStream fos = requireContext().openFileOutput(filename, 0);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();
            return filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void savePost() {
        if (isGuest) {
            Toast.makeText(getContext(), "Please sign in to save memories", Toast.LENGTH_SHORT).show();
            return;
        }

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

        if (userId == -1) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.addMessage(userId, location, story, selectedImagePath);

        if (result != -1) {
            Toast.makeText(getContext(), "Memory saved successfully!", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(getContext(), "Failed to save memory", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        etLocation.setText("");
        etStory.setText("");
        selectedImagePath = null;
        if (ivImagePreview != null) {
            ivImagePreview.setVisibility(View.GONE);
            ivImagePreview.setImageResource(0);
        }
        Toast.makeText(getContext(), "Form cleared", Toast.LENGTH_SHORT).show();
    }
}