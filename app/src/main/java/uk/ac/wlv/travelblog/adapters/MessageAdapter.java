package uk.ac.wlv.travelblog.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import uk.ac.wlv.travelblog.R;
import uk.ac.wlv.travelblog.models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> messageList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position, int messageId);
        void onItemLongClick(int position, int messageId);
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

        // Format date to show only date part (YYYY-MM-DD)
        String formattedDate = formatDate(message.getCreatedDate());
        holder.tvDate.setText(formattedDate);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, message.getId());
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(position, message.getId());
            }
            return true;
        });
    }

    private String formatDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return "No date";
        }
        if (dateTime.contains(" ")) {
            return dateTime.split(" ")[0];
        }
        return dateTime;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void updateMessages(List<Message> newMessages) {
        this.messageList = newMessages != null ? newMessages : new ArrayList<>();
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