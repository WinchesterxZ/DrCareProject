package com.example.drhello.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.drhello.model.AddPersonModel;
import com.example.drhello.R;
import com.example.drhello.model.ReactionModel;
import com.example.drhello.model.UserAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NumReactionAdapter extends RecyclerView.Adapter<NumReactionAdapter.ReactionsViewHolder> {
    Context context;
    ArrayList<ReactionModel> reactionModels = new ArrayList<>();
    private ArrayList<UserAccount> userAccountArrayList;
    private UserAccount userAccountme;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public NumReactionAdapter(Context context, ArrayList<ReactionModel> reactionModels, ArrayList<UserAccount> userAccountArrayList, UserAccount userAccountme) {
        this.context = context;
        this.reactionModels = reactionModels;
        this.userAccountArrayList = userAccountArrayList;
        this.userAccountme = userAccountme;
        Log.e("Models : ", userAccountArrayList.size() + "");
    }

    @NonNull
    @Override
    public NumReactionAdapter.ReactionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NumReactionAdapter.
                ReactionsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemnumreaction, parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReactionsViewHolder holder, int position) {
        ReactionModel reactionModel = reactionModels.get(position);
        holder.name_user.setText(reactionModel.getName_user());
        // holder.reaction.setImageResource(reactionModel.getReaction());
        if (userAccountme.getFriendsmap().containsKey(userAccountArrayList.get(position).getId())) {
            holder.btn_add.setVisibility(View.GONE);
            holder.btn_cancel.setVisibility(View.GONE);
        }else if (userAccountme.getId().equals(userAccountArrayList.get(position).getId())) {
            holder.btn_add.setVisibility(View.GONE);
            holder.btn_cancel.setVisibility(View.GONE);
        } else if (userAccountme.getRequestSsent().containsKey(userAccountArrayList.get(position).getId())) {
            holder.btn_add.setVisibility(View.GONE);
            holder.btn_cancel.setVisibility(View.VISIBLE);
        } else {
            holder.btn_cancel.setVisibility(View.GONE);
            holder.btn_add.setVisibility(View.VISIBLE);
        }


        try {
            Glide.with(context).load(reactionModel.getImg_user()).placeholder(R.drawable.ic_chat).
                    error(R.drawable.ic_chat).into(holder.img_user);
        } catch (Exception e) {
            holder.img_user.setImageResource(R.drawable.ic_chat);
        }

        switch (reactionModel.getReaction()) {
            case "Like":
                holder.reaction.setImageResource(R.drawable.ic_like);
                break;
            case "Love":
                holder.reaction.setImageResource(R.drawable.ic_love);
                break;
            case "Haha":
                holder.reaction.setImageResource(R.drawable.ic_haha);
                break;
            case "Sad":
                holder.reaction.setImageResource(R.drawable.ic_sad);
                break;
            case "Wow":
                holder.reaction.setImageResource(R.drawable.ic_wow);
                break;
            case "Angry":
                holder.reaction.setImageResource(R.drawable.ic_angry);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return reactionModels.size();
    }

    public class ReactionsViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView img_user;
        private TextView name_user;
        private ImageView reaction;
        private Button btn_add, btn_cancel;
        private FirebaseFirestore db = FirebaseFirestore.getInstance();

        public ReactionsViewHolder(@NonNull View itemView) {
            super(itemView);
            img_user = itemView.findViewById(R.id.img_cur_user);
            name_user = itemView.findViewById(R.id.txt_name_user);
            reaction = itemView.findViewById(R.id.ic_reaction);
            btn_add = itemView.findViewById(R.id.btn_add);
            btn_cancel = itemView.findViewById(R.id.btn_cancel);

            btn_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //    onClickAddPersonListener.onClick(addPersonAdapterArrayList.get(getAdapterPosition()),"add");

                    UserAccount friendsAccount = userAccountArrayList.get(getAdapterPosition());

                    Map<String, AddPersonModel> friends = friendsAccount.getRequests();
                    friends.put(userAccountme.getId(), new AddPersonModel(userAccountme.getName(), userAccountme.getImg_profile(), userAccountme.getId()));
                    friendsAccount.setRequests(friends);

                    Map<String, AddPersonModel> requestsSent = userAccountme.getRequestSsent();
                    requestsSent.put(friendsAccount.getId(), new AddPersonModel(friendsAccount.getName(), friendsAccount.getImg_profile(), friendsAccount.getId()));
                    userAccountme.setRequestSsent(requestsSent);

                    db.collection("users").
                            document(friendsAccount.getId()).set(friendsAccount).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            ;
                            Toast.makeText(context, "Requests successful ", Toast.LENGTH_SHORT).show();
                        }
                    });

                    db.collection("users").
                            document(userAccountme.getId()).set(userAccountme).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "successful ", Toast.LENGTH_SHORT).show();
                        }
                    });


                    btn_cancel.setVisibility(View.VISIBLE);
                    btn_add.setVisibility(View.GONE);
                }
            });

            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //  onClickAddPersonListener.onClick(addPersonAdapterArrayList.get(getAdapterPosition()),"cancel");
                    UserAccount friendsAccount = userAccountArrayList.get(getAdapterPosition());

                    Map<String, AddPersonModel> requests = friendsAccount.getRequests();
                    requests.remove(userAccountme.getId());

                    Map<String, AddPersonModel> requestsSent = userAccountme.getRequestSsent();
                    requestsSent.remove(friendsAccount.getId());

                    friendsAccount.setRequests(requests);
                    userAccountme.setRequestSsent(requestsSent);

                    db.collection("users").
                            document(friendsAccount.getId()).set(friendsAccount).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.e("fady: ", "req suc");
                        }
                    });

                    db.collection("users").
                            document(userAccountme.getId()).set(userAccountme).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
//                    Toast.makeText(getActivity(), "Requests successful ", Toast.LENGTH_SHORT).show();
                        }
                    });

                    btn_add.setVisibility(View.VISIBLE);
                    btn_cancel.setVisibility(View.GONE);
                }
            });
        }
    }
}