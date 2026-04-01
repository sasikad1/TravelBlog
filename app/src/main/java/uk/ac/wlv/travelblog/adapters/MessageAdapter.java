package uk.ac.wlv.travelblog.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int titleColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE);
            int dateColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CREATED_DATE);

            if (idColumnIndex >= 0 && titleColumnIndex >= 0 && dateColumnIndex >= 0) {
                final int messageId = cursor.getInt(idColumnIndex);
                String title = cursor.getString(titleColumnIndex);
                String createdDate = cursor.getString(dateColumnIndex);

                // Format date to show only date part (YYYY-MM-DD)
                String formattedDate = formatDate(createdDate);

                holder.tvTitle.setText(title);
                holder.tvDate.setText(formattedDate);

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

    private String formatDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return "No date";
        }
        // Format: "2024-04-01 14:30:00" -> "2024-04-01"
        if (dateTime.contains(" ")) {
            return dateTime.split(" ")[0];
        }
        return dateTime;
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
        TextView tvTitle, tvDate;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}