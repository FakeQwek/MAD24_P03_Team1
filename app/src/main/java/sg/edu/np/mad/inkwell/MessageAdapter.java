package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private ArrayList<Message> messageList;

    private ChatActivity chatActivity;

    public MessageAdapter(ArrayList<Message> messageList, ChatActivity chatActivity) {
        this.messageList = messageList;
        this.chatActivity = chatActivity;
    }

    public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message, viewGroup, false);
        MessageViewHolder holder = new MessageViewHolder(view);
        return holder;
    }

    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.message.setText(message.getMessage());
    }

    public int getItemCount() { return messageList.size(); }
}
