package uk.ac.wlv.travelblog.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.adapters.MessageAdapter;
import uk.ac.wlv.travelblog.database.DatabaseHelper;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView searchIcon, filterSettings;
    private RecyclerView recyclerView;
    private TextView tvNoResults;
    private DatabaseHelper dbHelper;
    private MessageAdapter adapter;
    private int userId;
    private boolean isGuest;
    private SharedPreferences sharedPreferences;

    // Filter tags
    private TextView tagParis, tagMountain, tagBeach, tagAdventure, tagNature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("BlogAppPrefs", MODE_PRIVATE);

        userId = sharedPreferences.getInt("userId", -1);
        isGuest = sharedPreferences.getBoolean("isGuest", false);

        initViews();
        setupRecyclerView();
        setupSearchListener();
        setupFilterTags();
        setupBottomNavigation();

        // Load all messages initially
        loadMessages("");
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        searchIcon = findViewById(R.id.searchIcon);
        filterSettings = findViewById(R.id.filterSettings);
        recyclerView = findViewById(R.id.recyclerView);
        tvNoResults = findViewById(R.id.tvNoResults);

        tagParis = findViewById(R.id.tagParis);
        tagMountain = findViewById(R.id.tagMountain);
        tagBeach = findViewById(R.id.tagBeach);
        tagAdventure = findViewById(R.id.tagAdventure);
        tagNature = findViewById(R.id.tagNature);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(this, null, new MessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int messageId) {
                if (isGuest) {
                    Toast.makeText(SearchActivity.this,
                            "Sign in to view memory", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SearchActivity.this,
                            "Opening memory: " + messageId, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemLongClick(int position, int messageId) {
                if (!isGuest) {
                    Toast.makeText(SearchActivity.this,
                            "Options for: " + messageId, Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                loadMessages(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Search icon click listener
        searchIcon.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            loadMessages(query);
        });

        // Filter settings icon
        filterSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Filter options coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupFilterTags() {
        View.OnClickListener tagListener = v -> {
            TextView tag = (TextView) v;
            String tagText = tag.getText().toString();
            etSearch.setText(tagText);
            loadMessages(tagText);

            // Highlight selected tag
            resetTagColors();
            tag.setTextColor(getColor(R.color.white));
            tag.setBackgroundColor(getColor(R.color.green_500));
        };

        tagParis.setOnClickListener(tagListener);
        tagMountain.setOnClickListener(tagListener);
        tagBeach.setOnClickListener(tagListener);
        tagAdventure.setOnClickListener(tagListener);
        tagNature.setOnClickListener(tagListener);
    }

    private void resetTagColors() {
        TextView[] tags = {tagParis, tagMountain, tagBeach, tagAdventure, tagNature};
        for (TextView tag : tags) {
            tag.setTextColor(getColor(R.color.green_500));
            tag.setBackgroundColor(getColor(R.color.green_50));
        }
    }

    private void loadMessages(String searchQuery) {
        if (!isGuest && userId != -1) {
            Cursor cursor;
            if (searchQuery.isEmpty()) {
                cursor = dbHelper.getAllMessagesForUser(userId);
            } else {
                cursor = dbHelper.searchMessages(userId, searchQuery);
            }
            adapter.swapCursor(cursor);

            int count = cursor != null ? cursor.getCount() : 0;
            if (count == 0) {
                tvNoResults.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                tvNoResults.setText("No memories found for \"" + searchQuery + "\"");
            } else {
                tvNoResults.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            adapter.swapCursor(null);
            tvNoResults.setVisibility(View.VISIBLE);
            tvNoResults.setText("Sign in to search your memories");
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_search);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            }
            else if (itemId == R.id.nav_search) {
                return true;
            }
            else if (itemId == R.id.nav_profile) {
                if (isGuest) {
                    Toast.makeText(this, "Sign in to view profile", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }
}