package comsats.fyp.game.abbottabad.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import comsats.fyp.game.abbottabad.Models.messageModel;
import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.store;


public class inboxChatAdapter extends RecyclerView.Adapter<inboxChatAdapter.myHolder> {

    Context context;
    ArrayList<messageModel> messageList;
    int owner;

    public inboxChatAdapter(ArrayList<messageModel> messageList, int owner) {
        this.messageList = messageList;
        this.owner = owner;
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position, String operation, String name);
    }

    public int getItemViewType(int position) {
        if (messageList.get(position).getSender() == owner) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public myHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case 0:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_sender, parent, false);
                break;
            case 1:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_receiver, parent, false);
                break;
        }
        myHolder hold = new myHolder(v);
        return hold;
    }

    @Override
    public void onBindViewHolder(final myHolder holder, final int position) {

        holder.messageTime.setText(store.getTimeForMessages(messageList.get(position).getMessageTime()));
        holder.messageText.setText(messageList.get(position).getMessageText() + "");
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class myHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;

        public myHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();

            messageText = (TextView) itemView.findViewById(R.id.messageText);
            messageTime = (TextView) itemView.findViewById(R.id.messageTime);
        }
    }
}