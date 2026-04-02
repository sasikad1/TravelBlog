package uk.ac.wlv.travelblog.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.adapters.MessageAdapter;
import uk.ac.wlv.travelblog.database.DatabaseHelper;

public class HomeFragment extends Fragment {

    private TextView tvWelcome, tvStats;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private DatabaseHelper dbHelper;
    private MessageAdapter adapter;
    private int userId;
    private String userEmail;
    private boolean isGuest;
    private SharedPreferences sharedPreferences;

    public HomeFragment() {
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
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        loadMessages();

        fabAdd.setOnClickListener(v -> {
            if (isGuest) {
                Toast.makeText(getContext(), "Please sign in to add memories", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Add new memory (Coming soon)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvStats = view.findViewById(R.id.tvStats);
        recyclerView = view.findViewById(R.id.recyclerView);
        fabAdd = view.findViewById(R.id.fabAdd);

        if (isGuest) {
            tvWelcome.setText("Hello, Explorer!");
            tvStats.setText("Sign in to save your memories");
        } else {
            String name = userEmail.split("@")[0];
            tvWelcome.setText("Welcome back, " + name + "!");
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MessageAdapter(getContext(), null, new MessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int messageId) {
                if (isGuest) {
                    Toast.makeText(getContext(), "Sign in to view memory", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Opening memory: " + messageId, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemLongClick(int position, int messageId) {
                if (!isGuest) {
                    Toast.makeText(getContext(), "Options for memory: " + messageId, Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadMessages() {
        if (!isGuest && userId != -1) {
            Cursor cursor = dbHelper.getAllMessagesForUser(userId);
            adapter.swapCursor(cursor);

            int count = cursor != null ? cursor.getCount() : 0;
            if (count == 0) {
                tvStats.setText("No memories yet");
            } else if (count == 1) {
                tvStats.setText("1 memory saved");
            } else {
                tvStats.setText(count + " memories saved");
            }
        } else {
            adapter.swapCursor(null);
            tvStats.setText("Sign in to save memories");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMessages();
    }
}