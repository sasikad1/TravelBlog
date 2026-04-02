package uk.ac.wlv.travelblog.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.activities.LoginActivity;
import uk.ac.wlv.travelblog.database.DatabaseHelper;

public class ProfileFragment extends Fragment {

    private ImageView profileAvatar;
    private TextView tvUserName, tvUserStatus;
    private TextView tvTotalMemories, tvPublishedCount, tvDraftsCount;
    private Button btnLogout;
    private View settingsItem, mapItem;

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
    }

    private void initViews(View view) {
        profileAvatar = view.findViewById(R.id.profileAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserStatus = view.findViewById(R.id.tvUserStatus);
        tvTotalMemories = view.findViewById(R.id.tvTotalMemories);
        tvPublishedCount = view.findViewById(R.id.tvPublishedCount);
        tvDraftsCount = view.findViewById(R.id.tvDraftsCount);
        btnLogout = view.findViewById(R.id.btnLogout);
        settingsItem = view.findViewById(R.id.settingsItem);
        mapItem = view.findViewById(R.id.mapItem);
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
            profileAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
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
        btnLogout.setOnClickListener(v -> logout());

        settingsItem.setOnClickListener(v -> {
            if (isGuest) {
                Toast.makeText(getContext(), "Please sign in to access settings", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Settings (Coming soon)", Toast.LENGTH_SHORT).show();
            }
        });

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
    }
}