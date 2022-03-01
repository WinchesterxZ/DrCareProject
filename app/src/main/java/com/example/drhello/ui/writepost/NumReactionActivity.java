package com.example.drhello.ui.writepost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.drhello.R;
import com.example.drhello.StateOfUser;
import com.example.drhello.adapter.TabAdapter;
import com.example.drhello.databinding.ActivityNumReactionBinding;
import com.example.drhello.model.Posts;
import com.example.drhello.model.UserAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class NumReactionActivity extends AppCompatActivity {

    private ActivityNumReactionBinding activityNumReactionBinding;
    private ArrayList<String>strings=new ArrayList<>();
    private Posts posts;
    private ArrayList<UserAccount> userAccountArrayList = new ArrayList<>();
    private int likeItem=0,loveItem=0,hahaItem=0,sadItem=0,wowItem=0,angryItem=0;
    public static ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_num_reaction);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        mProgress = new ProgressDialog(this);

        activityNumReactionBinding = DataBindingUtil.setContentView(NumReactionActivity.this, R.layout.activity_num_reaction);
        setSupportActionBar(activityNumReactionBinding.toolbarReaction);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        posts = (Posts) getIntent().getSerializableExtra("post");


        activityNumReactionBinding.imgBackReaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if (posts != null) {
            Collection<String> values = posts.getReactions().values();
            ArrayList<String> list = new ArrayList(values);

            for (int i = 0; i < list.size(); i++) {
                switch (list.get(i)) {
                    case "Like":
                        if (!strings.contains("Like")) {
                            strings.add("Like");
                        }
                        likeItem++;
                        break;
                    case "Love":
                        if (!strings.contains("Love")) {
                            strings.add("Love");
                        }
                        loveItem++;
                        break;
                    case "Haha":
                        if (!strings.contains("Haha")) {
                            strings.add("Haha");
                        }
                        hahaItem++;
                        break;
                    case "Sad":
                        if (!strings.contains("Sad")) {
                            strings.add("Sad");
                        }
                        sadItem++;
                        break;
                    case "Wow":
                        if (!strings.contains("Wow")) {
                            strings.add("Wow");
                        }
                        wowItem++;
                        break;
                    case "Angry":
                        if (!strings.contains("Angry")) {
                            strings.add("Angry");
                        }
                        angryItem++;
                        break;
                }

            }

        }


        activityNumReactionBinding.TabReaction.addTab(activityNumReactionBinding.TabReaction.newTab().setText("All "+posts.getReactions().size()),0);
        for (int i=0;i<strings.size();i++){
            switch (strings.get(i)){
                case "Like":
                    activityNumReactionBinding.TabReaction.addTab(activityNumReactionBinding.TabReaction.newTab().setIcon(R.drawable.ic_like).setText(" "+likeItem));
                    break;
                case "Love":
                    activityNumReactionBinding.TabReaction.addTab(activityNumReactionBinding.TabReaction.newTab().setIcon(R.drawable.ic_love).setText(" "+loveItem));
                    break;
                case "Haha":
                    activityNumReactionBinding.TabReaction.addTab(activityNumReactionBinding.TabReaction.newTab().setIcon(R.drawable.ic_haha).setText(" "+hahaItem));
                    break;
                case "Sad":
                    activityNumReactionBinding.TabReaction.addTab(activityNumReactionBinding.TabReaction.newTab().setIcon(R.drawable.ic_sad).setText(" "+sadItem));
                    break;
                case "Wow":
                    activityNumReactionBinding.TabReaction.addTab(activityNumReactionBinding.TabReaction.newTab().setIcon(R.drawable.ic_wow).setText(" "+wowItem));
                    break;
                case "Angry":
                    activityNumReactionBinding.TabReaction.addTab(activityNumReactionBinding.TabReaction.newTab().setIcon(R.drawable.ic_angry).setText(" "+angryItem));
                    break;
                default:
                    break;
            }
        }


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                Log.e("onEvent: ","NUMREACTION");
                userAccountArrayList.clear();
                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                    UserAccount userAccount = document.toObject(UserAccount.class);
                    userAccountArrayList.add(userAccount);
                }

                Log.e("onEvent: ",userAccountArrayList.size()+"");

                TabAdapter adapter = new TabAdapter(NumReactionActivity.this, getSupportFragmentManager(),
                        activityNumReactionBinding.TabReaction.getTabCount(), posts.getReactions(), userAccountArrayList, strings);
                activityNumReactionBinding.viewPager.setAdapter(adapter);

                activityNumReactionBinding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(activityNumReactionBinding.TabReaction));

                activityNumReactionBinding.TabReaction.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        activityNumReactionBinding.viewPager.setCurrentItem(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });


                Log.e("hossops :",userAccountArrayList.size()+"");

            }
        });

        activityNumReactionBinding.imgBackReaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        StateOfUser stateOfUser = new StateOfUser();
        stateOfUser.changeState("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        StateOfUser stateOfUser = new StateOfUser();
        stateOfUser.changeState("Offline");
    }
}