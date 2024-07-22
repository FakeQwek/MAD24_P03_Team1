package sg.edu.np.mad.inkwell;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Message> messageList;

    private ChatActivity chatActivity;

    public MessageAdapter(ArrayList<Message> messageList, ChatActivity chatActivity) {
        this.messageList = messageList;
        this.chatActivity = chatActivity;
    }

    // Returns an int based on whether the message type
    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getType().equals("sent")) {
            return 0;
        } else {
            return 2;
        }
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sent_message, viewGroup, false);
            RecyclerView.ViewHolder holder = new MessageViewHolder(view);
            return holder;
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.received_message, viewGroup, false);
            RecyclerView.ViewHolder holder = new MessageViewHolder(view);
            return holder;
        }
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
        Message message = (Message) messageList.get(position);
        messageViewHolder.message.setText(message.getMessage());
    }

    public int getItemCount() { return messageList.size(); }
}
