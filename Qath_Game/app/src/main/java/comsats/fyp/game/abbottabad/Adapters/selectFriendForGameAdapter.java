package comsats.fyp.game.abbottabad.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;

import comsats.fyp.game.abbottabad.Models.friendModel;
import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;


public class selectFriendForGameAdapter extends RecyclerView.Adapter<selectFriendForGameAdapter.viewHolder> {

    Context context;
    ArrayList<friendModel> friendModels;
    String TAG = "friendsAdapter";

    public selectFriendForGameAdapter(ArrayList<friendModel> friendModels, OnItemClicked listener) {
        this.friendModels = friendModels;
        this.listener = listener;
    }

    private OnItemClicked listener;

    public interface OnItemClicked {
        void onItemClick(View view, int itemPosition, String type);
    }

    class viewHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageView user_dp, remove;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();

            user_dp = itemView.findViewById(R.id.user_dp);
            username = itemView.findViewById(R.id.username);
            remove = itemView.findViewById(R.id.remove);
        }
    }

    public int getItemViewType(int position) {
        if (friendModels.size() == 0) {
            return 0;
        } else {
            return 1;
        }
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case 0:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.no_friend, parent, false);
                v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                break;
            case 1:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_single_row, parent, false);
                v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                break;
        }
        viewHolder hold = new viewHolder(v);
        return hold;
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolder holder, final int position) {

        if (getItemViewType(position) == 1) {

            try {

                String name = "";
                String profileAdd = "";

                if (preferenceManager.getInstance(context).getUserID() == friendModels.get(position).getSender()) {
                    // show receiver data
                    name = friendModels.get(position).getReceiverName();
                    profileAdd = friendModels.get(position).getReceiverProfile();
                } else {
                    // show sender data
                    name = friendModels.get(position).getSenderName();
                    profileAdd = friendModels.get(position).getSenderProfile();
                }

                holder.username.setText(name + "");

                JSONObject profileOb = new JSONObject(profileAdd);

                if (profileOb.getString("provider").trim().equals("Qath")) {
                    // use qath api to get image
                    if (!profileOb.getString("image").trim().equals("none")) {
                        Picasso.get().
                                load(URLs.UPLOADED_FILES(profileOb.getString("image").trim()))
                                .placeholder(R.drawable.user)
                                .error(R.drawable.user)
                                .into(holder.user_dp);
                    }
                } else {
                    if (!profileOb.getString("image").trim().equals("none")) {
                        Picasso.get().
                                load(profileOb.getString("image").trim())
                                .placeholder(R.drawable.user)
                                .error(R.drawable.user)
                                .into(holder.user_dp);
                    }
                }

            } catch (Exception e) {
                Log.i("qweqe", e + "");
            }

        }


    }

    @Override
    public int getItemCount() {
        if (friendModels.size() == 0) {
            return 1;
        }
        return friendModels.size();
    }
}
