package uk.ac.wlv.travelblog.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.adapters.MessageAdapter;
import uk.ac.wlv.travelblog.database.DatabaseHelper;
import uk.ac.wlv.travelblog.models.Message;

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

        Log.d("HomeFragment", "onCreate: userId=" + userId);
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
            Log.d("HomeFragment", "FAB clicked - isGuest=" + isGuest);

            if (isGuest) {
                Toast.makeText(getContext(), "Please sign in to add memories", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    CreatePostFragment createPostFragment = new CreatePostFragment();
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                    );
                    transaction.replace(R.id.fragmentContainer, createPostFragment);
                    transaction.addToBackStack("create_post");
                    transaction.commit();
                    Log.d("HomeFragment", "Transaction committed");
                    Toast.makeText(getContext(), "Opening create memory...", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("HomeFragment", "Error: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
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
        adapter = new MessageAdapter(getContext(), new ArrayList<>(), new MessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int messageId) {
                if (isGuest) {
                    Toast.makeText(getContext(), "Sign in to view memory", Toast.LENGTH_SHORT).show();
                } else {
                    // ========== OPEN POST DETAIL FRAGMENT ==========
                    // Get the message object
                    List<Message> messages = dbHelper.getAllMessagesAsList(userId);
                    if (position < messages.size()) {
                        Message selectedMessage = messages.get(position);

                        // Create PostDetailFragment with message data
                        PostDetailFragment postDetailFragment = PostDetailFragment.newInstance(selectedMessage.getId());

                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left,
                                R.anim.slide_in_left,
                                R.anim.slide_out_right
                        );
                        transaction.replace(R.id.fragmentContainer, postDetailFragment);
                        transaction.addToBackStack("post_detail");
                        transaction.commit();

                        Toast.makeText(getContext(), "Opening: " + selectedMessage.getTitle(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error opening memory", Toast.LENGTH_SHORT).show();
                    }
                    // ==============================================
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
            List<Message> messages = dbHelper.getAllMessagesAsList(userId);
            adapter.updateMessages(messages);

            int count = messages.size();
            if (count == 0) {
                tvStats.setText("No memories yet");
            } else if (count == 1) {
                tvStats.setText("1 memory saved");
            } else {
                tvStats.setText(count + " memories saved");
            }
        } else {
            adapter.updateMessages(new ArrayList<>());
            tvStats.setText("Sign in to save memories");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMessages();
    }
}