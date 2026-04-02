package uk.ac.wlv.travelblog.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.adapters.MessageAdapter;
import uk.ac.wlv.travelblog.database.DatabaseHelper;
import uk.ac.wlv.travelblog.models.Message;

public class SearchFragment extends Fragment {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private TextView tvNoResults;
    private DatabaseHelper dbHelper;
    private MessageAdapter adapter;
    private int userId;
    private boolean isGuest;
    private SharedPreferences sharedPreferences;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
        sharedPreferences = requireActivity().getSharedPreferences("BlogAppPrefs", 0);

        userId = sharedPreferences.getInt("userId", -1);
        isGuest = sharedPreferences.getBoolean("isGuest", false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSearchListener();
        setupFilterTags(view);
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvNoResults = view.findViewById(R.id.tvNoResults);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MessageAdapter(getContext(), new ArrayList<>(), new MessageAdapter.OnItemClickListener() {
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

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterTags(View view) {
        TextView tagParis = view.findViewById(R.id.tagParis);
        TextView tagMountain = view.findViewById(R.id.tagMountain);
        TextView tagBeach = view.findViewById(R.id.tagBeach);
        TextView tagAdventure = view.findViewById(R.id.tagAdventure);
        TextView tagNature = view.findViewById(R.id.tagNature);

        View.OnClickListener tagListener = v -> {
            TextView tag = (TextView) v;
            String tagText = tag.getText().toString();
            etSearch.setText(tagText);
            performSearch(tagText);
        };

        if (tagParis != null) tagParis.setOnClickListener(tagListener);
        if (tagMountain != null) tagMountain.setOnClickListener(tagListener);
        if (tagBeach != null) tagBeach.setOnClickListener(tagListener);
        if (tagAdventure != null) tagAdventure.setOnClickListener(tagListener);
        if (tagNature != null) tagNature.setOnClickListener(tagListener);
    }

    private void performSearch(String query) {
        if (!isGuest && userId != -1) {
            List<Message> messages;
            if (query.isEmpty()) {
                messages = dbHelper.getAllMessagesAsList(userId);
            } else {
                messages = dbHelper.searchMessagesAsList(userId, query);
            }

            // Use updateMessages instead of swapCursor
            adapter.updateMessages(messages);

            int count = messages.size();
            if (count == 0 && !query.isEmpty()) {
                tvNoResults.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                tvNoResults.setText("No memories found for \"" + query + "\"");
            } else {
                tvNoResults.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        } else if (isGuest) {
            tvNoResults.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvNoResults.setText("Sign in to search your memories");
        } else {
            // Load all messages if logged in but no query
            List<Message> messages = dbHelper.getAllMessagesAsList(userId);
            adapter.updateMessages(messages);
            tvNoResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String query = etSearch != null ? etSearch.getText().toString() : "";
        performSearch(query);
    }
}