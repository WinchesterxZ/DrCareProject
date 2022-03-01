package com.example.drhello.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.drhello.model.LastMessages;
import com.example.drhello.R;
import com.example.drhello.model.ChatModel;
import com.example.drhello.model.UserAccount;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {
    private Context context;
    private ArrayList<LastMessages> userAccountArrayList = new ArrayList<>();
    private OnFriendsClickListener onFriendsClickListener;
    private UserAccount userAccount1;
    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SS a", Locale.US);

    public FriendsAdapter(Context context, ArrayList<LastMessages> userAccountArrayList, OnFriendsClickListener onFriendsClickListener, UserAccount userAccount1) {
        this.context = context;
        this.userAccountArrayList = userAccountArrayList;
        this.onFriendsClickListener = onFriendsClickListener;
        this.userAccount1 = userAccount1;

        for (int i = 0; i < userAccountArrayList.size(); i++) {
            for (int j = i + 1; j < userAccountArrayList.size(); j++) {
                if (userAccount1.getMap().containsKey(userAccountArrayList.get(i).getId()) &&
                        userAccount1.getMap().containsKey(userAccountArrayList.get(j).getId())) {
                    try {
                        Date date1 = dateFormat.parse(userAccount1.getMap().get(userAccountArrayList.get(i).getId()).getDate());
                        Date date2 = dateFormat.parse(userAccount1.getMap().get(userAccountArrayList.get(j).getId()).getDate());
                        if (date1.getTime() < date2.getTime()) {
                            Collections.swap(userAccountArrayList, i, j);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAdapter.FriendsViewHolder holder, int position) {
        LastMessages userAccount = userAccountArrayList.get(position);

        holder.name_user.setText(userAccount.getName_person());

        if (userAccount1.getMap() != null) {
            if (userAccount1.getMap().containsKey(userAccount.getId())) {
                ChatModel chatModel = userAccount1.getMap().get(userAccount.getId());
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SS a", Locale.US);
                if (chatModel.getMessage().equals("") && chatModel.getRecord().equals("")) {
                    holder.last_message.setText(userAccount.getNameSender() + " Sent a Photo ");
                } else if (chatModel.getMessage().equals("") && chatModel.getImage().equals("")) {
                    holder.last_message.setText(userAccount.getNameSender() + " Sent a Record ");
                } else {
                    if(chatModel.getMessage().contains("http")){
                        holder.last_message.setText(userAccount.getNameSender() + " Sent a Location ");

                    }else{
                        holder.last_message.setText(chatModel.getMessage());
                    }
                }

                try {
                    Date date1 = dateFormat.parse(getDateTime());
                    Date date2 = dateFormat.parse(chatModel.getDate());
                    long data = date1.getTime() - date2.getTime();
                    int timeInSeconds = (int) (data / 1000);
                    int days = timeInSeconds / (3600 * 24);
                    if (days >= 1) {
                        holder.date.setText(chatModel.getDate().split(" ")[0]);
                    } else {
                        String time = splitDateTime(chatModel.getDate())[1].substring(0, splitDateTime(chatModel.getDate())[1].length() - 6) + " ";
                        String timestamp = time + splitDateTime(chatModel.getDate())[2];
                        holder.date.setText(timestamp);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            Glide.with(context).load(userAccount.getImage_person()).placeholder(R.drawable.ic_chat).
                    error(R.drawable.ic_chat).into(holder.img_user);
        } catch (Exception e) {
            holder.img_user.setImageResource(R.drawable.ic_chat);
        }

    }

    @Override
    public int getItemCount() {
        return userAccountArrayList.size();
    }

    private String[] splitDateTime(String dateFormat) {
        return dateFormat.split(" ");
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SS a", Locale.US);
        Date date = new Date();
        return dateFormat.format(date);
    }


    public class FriendsViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView img_user;
        private TextView name_user, last_message, date;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            img_user = itemView.findViewById(R.id.img_cur_user);
            name_user = itemView.findViewById(R.id.txt_name_user);
            last_message = itemView.findViewById(R.id.last_message);
            date = itemView.findViewById(R.id.date);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onFriendsClickListener.onClick(userAccountArrayList.get(getAdapterPosition()));
                }
            });
        }
    }
}