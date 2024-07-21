package sg.edu.np.mad.inkwell;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    RelativeLayout relativeLayout;

    TextView message;

    CardView cardView;

    public MessageViewHolder(View view) {
        super(view);

        relativeLayout = view.findViewById(R.id.relativeLayout);

        message = view.findViewById(R.id.message);

        cardView = view.findViewById(R.id.cardView);
    }
}
