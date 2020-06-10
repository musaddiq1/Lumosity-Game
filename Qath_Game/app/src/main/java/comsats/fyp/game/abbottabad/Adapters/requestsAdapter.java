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


public class requestsAdapter extends RecyclerView.Adapter<requestsAdapter.viewHolder> {

    Context context;
    ArrayList<friendModel> friendModels;
    String TAG = "friendsAdapter";

    public requestsAdapter(ArrayList<friendModel> friendModels, OnItemClicked listener) {
        this.friendModels = friendModels;
        this.listener = listener;
    }

    private OnItemClicked listener;

    public interface OnItemClicked {
        void onItemClick(View view, int itemPosition, String type);
    }

    class viewHolder extends RecyclerView.ViewHolder {
        TextView username, buttonText, driverName, PriceForRoute;
        ImageView user_dp;
        LinearLayout buttonLayout;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();

            buttonLayout = itemView.findViewById(R.id.buttonLayout);
            buttonText = itemView.findViewById(R.id.buttonText);
            user_dp = itemView.findViewById(R.id.user_dp);
            username = itemView.findViewById(R.id.username);
        }
    }

    public int getItemViewType(int position) {
        if (friendModels.size() == 0) {
            return 0;
        } else {
            return 2;
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
            case 2:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_single_row_pending, parent, false);
                v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                break;
        }
        viewHolder hold = new viewHolder(v);
        return hold;
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolder holder, final int position) {

        if (getItemViewType(position) != 0) {

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

        if (getItemViewType(position) == 2) {

            if (friendModels.get(position).getSender() == preferenceManager.getInstance(context).getUserID()) {
                holder.buttonText.setText("Cancel Friend Request");
            } else {
                holder.buttonText.setText("Accept Friend Request");
            }

            holder.buttonLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (friendModels.get(position).getSender() == preferenceManager.getInstance(context).getUserID()) {
                        //Cancel Friend Request
                        listener.onItemClick(v, position, "cancelRequest");
                    } else {
                        //Accept Friend Request
                        listener.onItemClick(v, position, "acceptRequest");
                    }
                }
            });

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
