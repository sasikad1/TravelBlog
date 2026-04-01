package uk.ac.wlv.travelblog.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.database.DatabaseHelper;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private Cursor cursor;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position, int messageId);
        void onItemLongClick(int position, int messageId);
    }

    public MessageAdapter(Context context, Cursor cursor, OnItemClickListener listener) {
        this.context = context;
        this.cursor = cursor;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            // Get column indices safely
            int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int titleColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE);
            int contentColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTENT);
            int dateColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CREATED_DATE);
            int imageColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_PATH);

            if (idColumnIndex >= 0 && titleColumnIndex >= 0 &&
                    contentColumnIndex >= 0 && dateColumnIndex >= 0) {

                final int messageId = cursor.getInt(idColumnIndex);
                String title = cursor.getString(titleColumnIndex);
                String content = cursor.getString(contentColumnIndex);
                String createdDate = cursor.getString(dateColumnIndex);
                String imagePath = imageColumnIndex >= 0 ? cursor.getString(imageColumnIndex) : null;

                holder.tvTitle.setText(title);

                // Truncate content if too long
                if (content.length() > 100) {
                    holder.tvContent.setText(content.substring(0, 100) + "...");
                } else {
                    holder.tvContent.setText(content);
                }

                holder.tvDate.setText(createdDate);

                // Handle image
                if (imagePath != null && !imagePath.isEmpty()) {
                    holder.ivImage.setVisibility(View.VISIBLE);
                    // TODO: Load image using Glide or other library
                    // Glide.with(context).load(imagePath).into(holder.ivImage);
                } else {
                    holder.ivImage.setVisibility(View.GONE);
                }

                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(position, messageId);
                    }
                });

                holder.itemView.setOnLongClickListener(v -> {
                    if (listener != null) {
                        listener.onItemLongClick(position, messageId);
                    }
                    return true;
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate;
        ImageView ivImage;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivImage = itemView.findViewById(R.id.ivImage);
        }
    }
}

