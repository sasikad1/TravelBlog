package uk.ac.wlv.travelblog.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> messageList;
    private OnItemClickListener listener;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedPositions = new HashSet<>();

    public interface OnItemClickListener {
        void onItemClick(int position, int messageId);
        void onItemLongClick(int position, int messageId);
        void onSelectionChanged(int selectedCount);
    }

    public MessageAdapter(Context context, List<Message> messageList, OnItemClickListener listener) {
        this.context = context;
        this.messageList = messageList != null ? messageList : new ArrayList<>();
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.tvTitle.setText(message.getTitle());

        // Truncate content if too long
        String content = message.getContent();
        if (content.length() > 100) {
            holder.tvContent.setText(content.substring(0, 100) + "...");
        } else {
            holder.tvContent.setText(content);
        }

        // Format date
        String formattedDate = formatDate(message.getCreatedDate());
        holder.tvDate.setText(formattedDate);

        // Load image
        loadImage(message.getImagePath(), holder.ivMessageImage);

        // Selection mode
        if (isSelectionMode) {
            holder.cbSelect.setVisibility(View.VISIBLE);
            holder.cbSelect.setChecked(selectedPositions.contains(position));
            holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedPositions.add(position);
                } else {
                    selectedPositions.remove(position);
                }
                if (listener != null) {
                    listener.onSelectionChanged(selectedPositions.size());
                }
            });
        } else {
            holder.cbSelect.setVisibility(View.GONE);
            holder.cbSelect.setOnCheckedChangeListener(null);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                // In selection mode, toggle checkbox
                boolean isChecked = selectedPositions.contains(position);
                if (isChecked) {
                    selectedPositions.remove(position);
                    holder.cbSelect.setChecked(false);
                } else {
                    selectedPositions.add(position);
                    holder.cbSelect.setChecked(true);
                }
                if (listener != null) {
                    listener.onSelectionChanged(selectedPositions.size());
                }
            } else {
                if (listener != null) {
                    listener.onItemClick(position, message.getId());
                }
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                // Enter selection mode on long press
                enableSelectionMode();
                // Select current item
                selectedPositions.add(position);
                notifyItemChanged(position);
                if (listener != null) {
                    listener.onSelectionChanged(selectedPositions.size());
                    listener.onItemLongClick(position, message.getId());
                }
            }
            return true;
        });
    }

    public void enableSelectionMode() {
        isSelectionMode = true;
        notifyDataSetChanged();
    }

    public void disableSelectionMode() {
        isSelectionMode = false;
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public List<Integer> getSelectedMessageIds() {
        List<Integer> selectedIds = new ArrayList<>();
        for (int position : selectedPositions) {
            if (position < messageList.size()) {
                selectedIds.add(messageList.get(position).getId());
            }
        }
        return selectedIds;
    }

    public int getSelectedCount() {
        return selectedPositions.size();
    }

    public void deleteSelectedMessages() {
        // Remove selected items from list
        List<Message> newList = new ArrayList<>();
        for (int i = 0; i < messageList.size(); i++) {
            if (!selectedPositions.contains(i)) {
                newList.add(messageList.get(i));
            }
        }
        messageList = newList;
        selectedPositions.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
    }

    private void loadImage(String imagePath, ImageView imageView) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imgFile = new File(context.getFilesDir(), imagePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                    return;
                }

                if (imagePath.startsWith("content://")) {
                    Uri imageUri = Uri.parse(imagePath);
                    imageView.setImageURI(imageUri);
                    imageView.setVisibility(View.VISIBLE);
                    return;
                }

                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                imageView.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                imageView.setVisibility(View.VISIBLE);
            }
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            imageView.setVisibility(View.VISIBLE);
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

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void updateMessages(List<Message> newMessages) {
        this.messageList = newMessages != null ? newMessages : new ArrayList<>();
        selectedPositions.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate, tvStatus;
        ImageView ivMessageImage;
        CheckBox cbSelect;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivMessageImage = itemView.findViewById(R.id.ivMessageImage);
            cbSelect = itemView.findViewById(R.id.cbSelect);
        }
    }
}