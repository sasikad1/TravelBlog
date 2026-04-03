package uk.ac.wlv.travelblog.fragments;

import android.widget.ImageView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
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
    private MenuItem deleteMenuItem;
    private MenuItem cancelMenuItem;
    private boolean isSelectionMode = false;

    // Demo posts for guest users (if database empty)
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

        createDemoPosts();
    }

    private void createDemoPosts() {
        demoPosts = new ArrayList<>();

        Message post1 = new Message();
        post1.setId(1001);
        post1.setUserId(-1);
        post1.setTitle("Galle Fort Trip");
        post1.setContent("Exploring the historic Dutch fortress in southern Sri Lanka...");
        post1.setCreatedDate("2026-03-15 10:30:00");
        post1.setImagePath(null);
        demoPosts.add(post1);

        Message post2 = new Message();
        post2.setId(1002);
        post2.setUserId(-1);
        post2.setTitle("Mount Fuji Adventure");
        post2.setContent("An unforgettable sunrise hike to the summit...");
        post2.setCreatedDate("2026-03-10 05:45:00");
        post2.setImagePath(null);
        demoPosts.add(post2);

        Message post3 = new Message();
        post3.setId(1003);
        post3.setUserId(-1);
        post3.setTitle("Bali Beach Paradise");
        post3.setContent("White sandy beaches, crystal clear waters...");
        post3.setCreatedDate("2026-03-05 18:20:00");
        post3.setImagePath(null);
        demoPosts.add(post3);
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
            // Setup normal menu
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

    private void enterSelectionMode() {
        isSelectionMode = true;

        // Change toolbar for selection mode
        if (toolbar != null) {
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.selection_menu);
            toolbar.setTitle("Select memories");
            toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

            deleteMenuItem = toolbar.getMenu().findItem(R.id.action_delete);
            cancelMenuItem = toolbar.getMenu().findItem(R.id.action_cancel);

            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete) {
                    deleteSelectedMessages();
                    return true;
                } else if (item.getItemId() == R.id.action_cancel) {
                    exitSelectionMode();
                    return true;
                }
                return false;
            });
        }

        // Enable selection mode in adapter
        adapter.enableSelectionMode();
    }

    private void exitSelectionMode() {
        isSelectionMode = false;

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

        // Disable selection mode in adapter
        if (adapter != null) {
            adapter.disableSelectionMode();
        }
    }

    private void deleteSelectedMessages() {
        if (adapter == null) return;

        List<Integer> selectedIds = adapter.getSelectedMessageIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(getContext(), "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Memories")
                .setMessage("Are you sure you want to delete " + selectedIds.size() + " memory(s)?")
                .setPositiveButton("Delete", (dialog, which) -> {
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
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateSelectionCount(int count) {
        if (toolbar != null && isSelectionMode) {
            toolbar.setTitle(count + " selected");
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
            tvStats.setText("Explore featured travel memories");
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
                    // Selection mode handles click in adapter
                } else if (isGuest) {
                    Message clickedPost;
                    List<Message> currentList = adapter.getCurrentList();
                    if (position < currentList.size()) {
                        clickedPost = currentList.get(position);
                        showGuestPostDialog(clickedPost);
                    }
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
                    enterSelectionMode();
                    // Select current item
                    adapter.enableSelectionMode();
                    updateSelectionCount(adapter.getSelectedCount());
                }
            }

            @Override
            public void onSelectionChanged(int selectedCount) {
                if (selectedCount > 0) {
                    updateSelectionCount(selectedCount);
                } else if (isSelectionMode) {
                    exitSelectionMode();
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void showGuestPostDialog(Message post) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_guest_post, null);

        TextView tvTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView tvDate = dialogView.findViewById(R.id.dialogDate);
        TextView tvContent = dialogView.findViewById(R.id.dialogContent);
        TextView tvUser = dialogView.findViewById(R.id.dialogUser);
        ImageView ivImage = dialogView.findViewById(R.id.dialogImage);

        tvTitle.setText(post.getTitle());
        tvContent.setText(post.getContent());

        String formattedDate = formatDate(post.getCreatedDate());
        tvDate.setText(formattedDate);

        loadDialogImage(post.getImagePath(), ivImage);

        if (post.getUserId() > 0) {
            String userEmail = dbHelper.getUserEmailById(post.getUserId());
            tvUser.setText("Posted by: " + userEmail);
            tvUser.setVisibility(View.VISIBLE);
        } else {
            tvUser.setVisibility(View.GONE);
        }

        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void loadDialogImage(String imagePath, ImageView imageView) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                java.io.File imgFile = new java.io.File(requireContext().getFilesDir(), imagePath);
                if (imgFile.exists()) {
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                    return;
                }

                if (imagePath.startsWith("content://")) {
                    android.net.Uri imageUri = android.net.Uri.parse(imagePath);
                    imageView.setImageURI(imageUri);
                    imageView.setVisibility(View.VISIBLE);
                    return;
                }

                imageView.setVisibility(View.GONE);

            } catch (Exception e) {
                e.printStackTrace();
                imageView.setVisibility(View.GONE);
            }
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    private String formatDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return "No date";
        }
        if (dateTime.contains(" ")) {
            String datePart = dateTime.split(" ")[0];
            String[] parts = datePart.split("-");
            if (parts.length == 3) {
                String month = getMonthName(Integer.parseInt(parts[1]));
                return month + " " + parts[2] + ", " + parts[0];
            }
            return datePart;
        }
        return dateTime;
    }

    private String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return months[month - 1];
    }

    private void loadMessages() {
        if (!isGuest && userId != -1) {
            List<Message> messages = dbHelper.getAllMessagesAsList(userId);
            adapter.updateMessages(messages);
            adapter.setShowUserEmail(false);

            int count = messages.size();
            if (count == 0) {
                tvStats.setText("No memories yet. Tap + to create one!");
            } else if (count == 1) {
                tvStats.setText("1 memory saved");
            } else {
                tvStats.setText(count + " memories saved");
            }
        } else {
            List<Message> allMessages = dbHelper.getAllMessagesFromAllUsers();

            if (allMessages.isEmpty()) {
                adapter.updateMessages(demoPosts);
                adapter.setShowUserEmail(false);
                tvStats.setText(demoPosts.size() + " featured adventures");
            } else {
                adapter.updateMessages(allMessages);
                adapter.setShowUserEmail(true);
                tvStats.setText(allMessages.size() + " memories from travelers");
            }
        }
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