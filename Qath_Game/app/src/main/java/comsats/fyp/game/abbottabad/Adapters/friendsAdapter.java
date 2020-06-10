package comsats.fyp.game.abbottabad.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import comsats.fyp.game.abbottabad.GamePlay.online_with_friends;
import comsats.fyp.game.abbottabad.Models.friendModel;
import comsats.fyp.game.abbottabad.Models.onlineFriendModel;
import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;
import comsats.fyp.game.abbottabad.storeRoom.store;


public class friendsAdapter extends RecyclerView.Adapter<friendsAdapter.viewHolder> {

    Context context;
    ArrayList<friendModel> friendModels;
    String TAG = "friendsAdapter";
    ArrayList<onlineFriendModel> onlineFriendModelArrayList;

    public friendsAdapter(ArrayList<friendModel> friendModels, ArrayList<onlineFriendModel> onlineFriendModelArrayList, OnItemClicked listener) {
        this.friendModels = friendModels;
        this.listener = listener;
        this.onlineFriendModelArrayList = onlineFriendModelArrayList;
    }

    private OnItemClicked listener;

    public interface OnItemClicked {
        void onItemClick(View view, int itemPosition, String type);
    }

    class viewHolder extends RecyclerView.ViewHolder {
        TextView username, status;
        LinearLayout playNow;
        ImageView user_dp, remove;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();

            status = itemView.findViewById(R.id.status);
            playNow = itemView.findViewById(R.id.playNow);
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

                onlineFriendModel friendModel = null;

                String name = "";
                String profileAdd = "";
                int friendID = -99;

                if (preferenceManager.getInstance(context).getUserID() == friendModels.get(position).getSender()) {
                    // show receiver data
                    name = friendModels.get(position).getReceiverName();
                    profileAdd = friendModels.get(position).getReceiverProfile();
                    friendModel = getFriendModel(friendModels.get(position).getReceiver());
                    friendID = friendModels.get(position).getReceiver();
                } else {
                    // show sender data
                    name = friendModels.get(position).getSenderName();
                    profileAdd = friendModels.get(position).getSenderProfile();
                    friendModel = getFriendModel(friendModels.get(position).getSender());
                    friendID = friendModels.get(position).getSender();
                }

                if (friendModel != null) {

                    JSONObject stateOb = new JSONObject(friendModel.getState());

                    if (stateOb.getString("type").trim().equalsIgnoreCase("offline")) {
                        // user is offline - check user last seen
                        holder.playNow.setVisibility(View.INVISIBLE);
                        if (!friendModel.getLast_seen().trim().equalsIgnoreCase("none")) {
                            if (!friendModel.getLast_seen().trim().equalsIgnoreCase("online")) {
                                Long lastSeen = Long.valueOf(friendModel.getLast_seen());
                                String d = store.getTimeAgo(lastSeen);
                                if (d.trim().length() != 0) {
                                    holder.status.setText("Last seen " + d);
                                } else {
                                    holder.status.setText("");
                                }
                            } else {
                                holder.status.setText("Offline");
                            }
                        } else {
                            holder.status.setText("Offline");
                        }
                    } else if (stateOb.getString("type").trim().equalsIgnoreCase("requestQueueWithMe")) {
                        // user has requested game with me
                        holder.playNow.setVisibility(View.VISIBLE);
                        Long requestedAt = stateOb.getLong("time");
                        String text = "";

                        String d = store.getTimeAgo(requestedAt);
                        if (d.trim().length() != 0) {
                            text = "Waiting for you (Start Game)\nRequested game " + d;
                        } else {
                            text = "Waiting for you (Start Game)";
                        }
                        holder.status.setText(text);
                        final int finalFriendID = friendID;
                        final String finalName = name;
                        holder.playNow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle pack = new Bundle();
                                pack.putInt("friendID", finalFriendID);
                                pack.putString("friendName", finalName);
                                pack.putString("way", "adapter");
                                Intent intent = new Intent(context, online_with_friends.class);
                                intent.putExtras(pack);
                                context.startActivity(intent);
                            }
                        });
                    } else if (stateOb.getString("type").trim().equalsIgnoreCase("requestQueue")) {
                        // user has requested game
                        holder.playNow.setVisibility(View.INVISIBLE);
                        Long requestedAt = stateOb.getLong("time");
                        holder.status.setText("Requested game " + store.getTimeAgo(requestedAt));
                    } else if (stateOb.getString("type").trim().equalsIgnoreCase("playingGame")) {
                        holder.playNow.setVisibility(View.INVISIBLE);
                        holder.status.setText("Playing Game");
                    } else if (stateOb.getString("type").trim().equalsIgnoreCase("onlineAndIdle")) {
                        holder.playNow.setVisibility(View.VISIBLE);
                        holder.status.setText("Idle");

                        final int finalFriendID = friendID;
                        final String finalName = name;
                        holder.playNow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle pack = new Bundle();
                                pack.putInt("friendID", finalFriendID);
                                pack.putString("friendName", finalName);
                                pack.putString("way", "adapter");
                                Intent intent = new Intent(context, online_with_friends.class);
                                intent.putExtras(pack);
                                context.startActivity(intent);
                            }
                        });
                    }
                } else {
                    holder.playNow.setVisibility(View.INVISIBLE);
                    holder.status.setText("");
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


                holder.remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(v, position, "removeFriend");
                    }
                });

            } catch (Exception e) {
                Log.i("qweqe", e + "");
            }

        }


    }

    public onlineFriendModel getFriendModel(int ID) {
        for (onlineFriendModel m : onlineFriendModelArrayList) {
            if (m.getFriendID() == ID) {
                return m;
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        if (friendModels.size() == 0) {
            return 1;
        }
        return friendModels.size();
    }
}
