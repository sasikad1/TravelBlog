package uk.ac.wlv.travelblog.activities;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.adapters.MessageAdapter;
import uk.ac.wlv.travelblog.database.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private DatabaseHelper dbHelper;
    private MessageAdapter adapter;
    private int userId;
    private String username;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("BlogAppPrefs", MODE_PRIVATE);

        userId = sharedPreferences.getInt("userId", -1);
        username = sharedPreferences.getString("username", "");

        if (userId == -1) {
            // Not logged in, go to login
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadMessages();

        fabAdd.setOnClickListener(v -> {
            // TODO: Open AddMessageActivity
        });
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        tvWelcome.setText("Welcome, " + username + "!");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(this, null, new MessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int messageId) {
                // TODO: Open ViewMessageActivity
            }

            @Override
            public void onItemLongClick(int position, int messageId) {
                // TODO: Show edit/delete options
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadMessages() {
        Cursor cursor = dbHelper.getAllMessagesForUser(userId);
        adapter.swapCursor(cursor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            // Database will be closed by adapter
        }
    }
}