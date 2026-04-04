package uk.ac.wlv.travelblog.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.File;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.activities.EditProfileActivity;
import uk.ac.wlv.travelblog.activities.LoginActivity;
import uk.ac.wlv.travelblog.database.DatabaseHelper;

public class ProfileFragment extends Fragment {

    private ImageView profileAvatar;
    private TextView tvUserName, tvUserStatus;
    private TextView tvTotalMemories, tvPublishedCount, tvDraftsCount;
    private LinearLayout btnLogout;
    private View mapItem;


    // Settings dropdown
    private LinearLayout settingsHeader, settingsItems;
    private ImageView ivDropdownIcon;
    private View editProfileItem;
    private boolean isDropdownOpen = false;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private String userEmail;
    private boolean isGuest;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
        sharedPreferences = requireActivity().getSharedPreferences("BlogAppPrefs", 0);

        userId = sharedPreferences.getInt("userId", -1);
        userEmail = sharedPreferences.getString("userEmail", "");
        isGuest = sharedPreferences.getBoolean("isGuest", false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadProfileData();
        loadStatistics();
        setupClickListeners();
        loadProfileImage();
        setupDropdownAnimation();
    }

    private void initViews(View view) {
        profileAvatar = view.findViewById(R.id.profileAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserStatus = view.findViewById(R.id.tvUserStatus);
        tvTotalMemories = view.findViewById(R.id.tvTotalMemories);
        tvPublishedCount = view.findViewById(R.id.tvPublishedCount);
        tvDraftsCount = view.findViewById(R.id.tvDraftsCount);
        btnLogout = view.findViewById(R.id.btnLogout);
        mapItem = view.findViewById(R.id.mapItem);

        // Settings dropdown views
        settingsHeader = view.findViewById(R.id.settingsHeader);
        settingsItems = view.findViewById(R.id.settingsItems);
        ivDropdownIcon = view.findViewById(R.id.ivDropdownIcon);
        editProfileItem = view.findViewById(R.id.editProfileItem);

        if (isGuest) {
            settingsHeader.setEnabled(false);
            settingsHeader.setAlpha(0.5f);
        }

    }

    private void setupDropdownAnimation() {
        settingsHeader.setOnClickListener(v -> {
            if (isGuest) {
                Toast.makeText(getContext(), "Please sign in to access settings", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isDropdownOpen) {
                // Close dropdown
                Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
                settingsItems.startAnimation(slideUp);
                settingsItems.setVisibility(View.GONE);
                ivDropdownIcon.setImageResource(android.R.drawable.ic_menu_more);
                isDropdownOpen = false;
            } else {
                // Open dropdown
                settingsItems.setVisibility(View.VISIBLE);
                Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
                settingsItems.startAnimation(slideDown);
                ivDropdownIcon.setImageResource(android.R.drawable.ic_menu_revert);
                isDropdownOpen = true;
            }
        });

        // Edit Profile item click
        editProfileItem.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadProfileImage() {
        String savedImagePath = sharedPreferences.getString("profileImage", null);
        if (savedImagePath != null && !savedImagePath.isEmpty()) {
            try {
                File imgFile = new File(requireContext().getFilesDir(), savedImagePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                    // Scale and crop to fit circle
                    Bitmap roundedBitmap = getRoundedCroppedBitmap(bitmap, 200);
                    profileAvatar.setImageBitmap(roundedBitmap);
                } else {
                    profileAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
                }
            } catch (Exception e) {
                e.printStackTrace();
                profileAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
            }
        } else {
            profileAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }

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

    private void loadProfileData() {
        if (isGuest) {
            tvUserName.setText("Guest User");
            tvUserStatus.setText("Offline Mode");
            tvUserStatus.setTextColor(getResources().getColor(R.color.orange_700));
            profileAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
        } else {
            String name = userEmail.split("@")[0];
            tvUserName.setText(name);
            tvUserStatus.setText("Online");
            tvUserStatus.setTextColor(getResources().getColor(R.color.green_500));
        }
    }

    private void loadStatistics() {
        if (!isGuest && userId != -1) {
            Cursor cursor = dbHelper.getAllMessagesForUser(userId);
            int totalCount = cursor != null ? cursor.getCount() : 0;

            int publishedCount = totalCount;
            int draftsCount = 0;

            tvTotalMemories.setText(String.valueOf(totalCount));
            tvPublishedCount.setText(String.valueOf(publishedCount));
            tvDraftsCount.setText(String.valueOf(draftsCount));

            if (cursor != null) {
                cursor.close();
            }
        } else {
            tvTotalMemories.setText("0");
            tvPublishedCount.setText("0");
            tvDraftsCount.setText("0");
        }
    }

    private void setupClickListeners() {
        // Logout Button

        btnLogout.setOnClickListener(v -> logout());

        // Travel Map Menu Item
        mapItem.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Travel Map (Coming soon)", Toast.LENGTH_SHORT).show();
        });
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
        loadProfileImage();
        loadProfileData();
    }
}