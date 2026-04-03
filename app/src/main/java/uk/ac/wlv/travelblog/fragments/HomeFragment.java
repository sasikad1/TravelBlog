package uk.ac.wlv.travelblog.fragments;

import android.content.Intent;
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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.activities.PostDetailActivity;
import uk.ac.wlv.travelblog.adapters.MessageAdapter;
import uk.ac.wlv.travelblog.database.DatabaseHelper;
import uk.ac.wlv.travelblog.models.Message;

public class HomeFragment extends Fragment {

    private TextView tvWelcome, tvStats;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private Toolbar toolbar;
    private DatabaseHelper dbHelper;
    private MessageAdapter adapter;
    private int userId;
    private String userEmail;
    private boolean isGuest;
    private SharedPreferences sharedPreferences;

    // Demo posts for guest users
    private List<Message> demoPosts;

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

        Log.d("HomeFragment", "onCreate: userId=" + userId + ", isGuest=" + isGuest);

        // Create demo posts for guest
        createDemoPosts();
    }

    private void createDemoPosts() {
        demoPosts = new ArrayList<>();

        // Demo Post 1
        Message post1 = new Message();
        post1.setId(1001);
        post1.setTitle("Galle Fort Trip");
        post1.setContent("Exploring the historic Dutch fortress in southern Sri Lanka. The cobblestone streets and colonial architecture made it feel like stepping back in time. We spent hours wandering through the narrow alleys, discovering hidden cafes and boutique shops.");
        post1.setCreatedDate("2026-03-15 10:30:00");
        post1.setImagePath(null);
        demoPosts.add(post1);

        // Demo Post 2
        Message post2 = new Message();
        post2.setId(1002);
        post2.setTitle("Mount Fuji Adventure");
        post2.setContent("An unforgettable sunrise hike to the summit of Japan's most iconic mountain. The journey was challenging but the view from the top was absolutely breathtaking. Standing above the clouds as the sun rose was a moment I'll never forget.");
        post2.setCreatedDate("2026-03-10 05:45:00");
        post2.setImagePath(null);
        demoPosts.add(post2);

        // Demo Post 3
        Message post3 = new Message();
        post3.setId(1003);
        post3.setTitle("Bali Beach Paradise");
        post3.setContent("White sandy beaches, crystal clear waters, and stunning sunsets. Bali exceeded all my expectations. The local culture, delicious food, and friendly people made this trip truly special.");
        post3.setCreatedDate("2026-03-05 18:20:00");
        post3.setImagePath(null);
        demoPosts.add(post3);

        // Demo Post 4
        Message post4 = new Message();
        post4.setId(1004);
        post4.setTitle("Kyoto Cherry Blossoms");
        post4.setContent("Walking through the ancient streets of Kyoto during cherry blossom season was like stepping into a dream. The pink petals falling like snow, the historic temples, and the peaceful gardens created an unforgettable experience.");
        post4.setCreatedDate("2026-02-28 14:15:00");
        post4.setImagePath(null);
        demoPosts.add(post4);

        // Demo Post 5
        Message post5 = new Message();
        post5.setId(1005);
        post5.setTitle("Swiss Alps Trekking");
        post5.setContent("The Swiss Alps offer some of the most spectacular trekking routes in the world. Green meadows, snow-capped peaks, and crystal-clear lakes. Every turn revealed a postcard-perfect view.");
        post5.setCreatedDate("2026-02-20 09:00:00");
        post5.setImagePath(null);
        demoPosts.add(post5);
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
        setupToolbar();

        fabAdd.setOnClickListener(v -> {
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
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setupToolbar() {
        toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.main_menu);
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_logout) {
                    logout();
                    return true;
                }
                return false;
            });
        }
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getContext(), uk.ac.wlv.travelblog.activities.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvStats = view.findViewById(R.id.tvStats);
        recyclerView = view.findViewById(R.id.recyclerView);
        fabAdd = view.findViewById(R.id.fabAdd);

        if (isGuest) {
            tvWelcome.setText("Hello, Explorer!");
            tvStats.setText("Sign in to save your own memories");
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
                    // For guest, show demo posts (but can't edit/delete)
                    Message clickedPost = demoPosts.get(position);
                    Toast.makeText(getContext(),
                            "Demo Post: " + clickedPost.getTitle() + "\nSign in to create your own memories!",
                            Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getContext(), uk.ac.wlv.travelblog.activities.PostDetailActivity.class);
                    intent.putExtra(uk.ac.wlv.travelblog.activities.PostDetailActivity.EXTRA_MESSAGE_ID, messageId);
                    startActivity(intent);
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }

            @Override
            public void onItemLongClick(int position, int messageId) {
                if (!isGuest) {
                    Toast.makeText(getContext(), "Options for memory: " + messageId, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSelectionChanged(int selectedCount) {
                // Selection mode not needed for guest
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadMessages() {
        if (!isGuest && userId != -1) {
            // Real user - load only their messages
            List<Message> messages = dbHelper.getAllMessagesAsList(userId);
            adapter.updateMessages(messages);
            adapter.setShowUserEmail(false); // Don't show user email

            int count = messages.size();
            if (count == 0) {
                tvStats.setText("No memories yet. Tap + to create one!");
            } else if (count == 1) {
                tvStats.setText("1 memory saved");
            } else {
                tvStats.setText(count + " memories saved");
            }
        } else {
            // Guest user - show ALL messages from ALL users
            List<Message> allMessages = dbHelper.getAllMessagesFromAllUsers();

            if (allMessages.isEmpty()) {
                // If no messages in database, show demo posts
                allMessages = demoPosts;
                tvStats.setText(allMessages.size() + " featured adventures (Demo)");
            } else {
                tvStats.setText(allMessages.size() + " memories from travelers");
            }

            adapter.updateMessages(allMessages);
            adapter.setShowUserEmail(true); // Show which user posted
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMessages();
    }
}