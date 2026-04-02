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

    // Selection mode
    private android.view.MenuItem deleteMenuItem;
    private android.view.MenuItem cancelMenuItem;
    private boolean isSelectionMode = false;

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
        setupToolbar();

        fabAdd.setOnClickListener(v -> {
            if (isSelectionMode) {
                exitSelectionMode();
            } else if (isGuest) {
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

    private void showSelectionToolbar() {
        if (toolbar != null) {
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.selection_menu);
            toolbar.setTitle("Select memories");
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

            deleteMenuItem = toolbar.getMenu().findItem(R.id.action_delete);
            cancelMenuItem = toolbar.getMenu().findItem(R.id.action_cancel);

            toolbar.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_delete) {
                    deleteSelectedMessages();
                    return true;
                } else if (itemId == R.id.action_cancel) {
                    exitSelectionMode();
                    return true;
                }
                return false;
            });
        }
    }

    private void exitSelectionMode() {
        isSelectionMode = false;
        if (adapter != null) {
            adapter.disableSelectionMode();
        }
        // Restore normal toolbar
        if (toolbar != null) {
            toolbar.setTitle("WanderLog");
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

    private void deleteSelectedMessages() {
        if (adapter == null) return;

        List<Integer> selectedIds = adapter.getSelectedMessageIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(getContext(), "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert to int array
        int[] ids = new int[selectedIds.size()];
        for (int i = 0; i < selectedIds.size(); i++) {
            ids[i] = selectedIds.get(i);
        }

        // Delete from database
        int deletedCount = dbHelper.deleteMultipleMessages(ids);

        if (deletedCount > 0) {
            Toast.makeText(getContext(), deletedCount + " memories deleted", Toast.LENGTH_SHORT).show();
            loadMessages(); // Refresh list
        } else {
            Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
        }

        exitSelectionMode();
    }

    private void updateSelectionCount(int count) {
        if (toolbar != null && isSelectionMode) {
            toolbar.setTitle(count + " selected");
        }
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
                if (isSelectionMode) {
                    // Already handled in adapter
                } else if (isGuest) {
                    Toast.makeText(getContext(), "Sign in to view memory", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getContext(), PostDetailActivity.class);
                    intent.putExtra(PostDetailActivity.EXTRA_MESSAGE_ID, messageId);
                    startActivity(intent);
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }

            @Override
            public void onItemLongClick(int position, int messageId) {
                if (!isGuest && !isSelectionMode) {
                    isSelectionMode = true;
                    showSelectionToolbar();
                    adapter.enableSelectionMode();
                }
            }

            @Override
            public void onSelectionChanged(int selectedCount) {
                if (selectedCount > 0) {
                    updateSelectionCount(selectedCount);
                } else {
                    exitSelectionMode();
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

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getContext(), uk.ac.wlv.travelblog.activities.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isSelectionMode) {
            exitSelectionMode();
        }
        loadMessages();
    }
}