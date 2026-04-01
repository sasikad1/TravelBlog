package uk.ac.wlv.travelblog.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.adapters.MessageAdapter;
import uk.ac.wlv.travelblog.database.DatabaseHelper;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private DatabaseHelper dbHelper;
    private MessageAdapter adapter;
    private int userId;
    private String userEmail;
    private boolean isGuest;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("BlogAppPrefs", MODE_PRIVATE);

        userId = sharedPreferences.getInt("userId", -1);
        userEmail = sharedPreferences.getString("userEmail", "");
        isGuest = sharedPreferences.getBoolean("isGuest", false);

        if (userId == -1 && !isGuest) {
            // Not logged in, go to login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadMessages();

        fabAdd.setOnClickListener(v -> {
            if (isGuest) {
                Toast.makeText(this,
                        "Please sign in to create messages",
                        Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Open AddMessageActivity
                Toast.makeText(this, "Add message feature coming soon",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        if (isGuest) {
            tvWelcome.setText("Welcome, Guest!\nSign in to save your travel stories");
        } else {
            tvWelcome.setText("Welcome, " + userEmail + "!");
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(this, null, new MessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int messageId) {
                if (isGuest) {
                    Toast.makeText(DashboardActivity.this,
                            "Please sign in to view messages",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // TODO: Open ViewMessageActivity
                    Toast.makeText(DashboardActivity.this,
                            "View message: " + messageId,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemLongClick(int position, int messageId) {
                if (!isGuest) {
                    // TODO: Show edit/delete options
                    Toast.makeText(DashboardActivity.this,
                            "Long press on message: " + messageId,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadMessages() {
        if (!isGuest && userId != -1) {
            Cursor cursor = dbHelper.getAllMessagesForUser(userId);
            adapter.swapCursor(cursor);
        } else {
            adapter.swapCursor(null);  // No messages for guest
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMessages();
    }
}