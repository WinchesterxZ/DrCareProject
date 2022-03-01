package com.example.drhello.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.drhello.AddPersonActivity;
import com.example.drhello.model.AddPersonModel;
import com.example.drhello.model.LastMessages;
import com.example.drhello.textclean.RequestPermissions;
import com.example.drhello.ui.chats.ChatActivity;
import com.example.drhello.adapter.FriendsAdapter;
import com.example.drhello.R;
import com.example.drhello.adapter.OnFriendsClickListener;
import com.example.drhello.adapter.UserStateAdapter;
import com.example.drhello.model.ChatModel;
import com.example.drhello.model.UserState;
import com.example.drhello.model.UserAccount;
import com.example.drhello.viewmodel.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatFragment extends Fragment implements OnFriendsClickListener {

    private RecyclerView recyclerView, recyclerView_state;
    private ArrayList<UserState> userStates = new ArrayList<>();
    private UserViewModel userViewModel;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ArrayList<LastMessages> userAccountArrayList = new ArrayList<>();
    private UserAccount userAccount1;
    private FloatingActionButton add_user;
    private final int PERMISSION_ALL_STORAGE = 1000;
    private RequestPermissions requestPermissions;
    Map<String, AddPersonModel> mapFriend = new HashMap<>();
    private CircleImageView img_cur_user;
    public ChatFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.rec_view);
        recyclerView_state = view.findViewById(R.id.recycle_users);
        add_user = view.findViewById(R.id.add_user);
        img_cur_user = view.findViewById(R.id.img_cur_user);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        requestPermissions = new RequestPermissions(getContext(),getActivity());

        userViewModel = new UserViewModel();
        userViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        userViewModel.getUser(mAuth,db);
        userViewModel.UserMutableLiveData.observe(getActivity(), new Observer<UserAccount>() {
            @Override
            public void onChanged(UserAccount userAccount) {
                try{
                    Glide.with(getActivity()).load(userAccount.getImg_profile()).placeholder(R.drawable.user).
                            error(R.drawable.user).into(img_cur_user);
                }catch (Exception e){
                    img_cur_user.setImageResource(R.drawable.user);
                }
            }
        });


        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        Log.e("task : ", " tast");
                        if (mAuth.getCurrentUser() != null) {
                            userAccount1 = value.toObject(UserAccount.class);
                            mapFriend = userAccount1.getFriendsmap();
                            userAccountArrayList.clear();
                            Log.e("userAccount1 : ", userAccount1.getName());
                            for (Map.Entry<String, AddPersonModel> entry : userAccount1.getFriendsmap().entrySet()) {
                                LastMessages lastMessages = new LastMessages();
                                lastMessages.setId(entry.getValue().getId());
                                lastMessages.setImage_person(entry.getValue().getImage_person());
                                lastMessages.setName_person(entry.getValue().getName_person());
                                if(userAccount1.getMap().containsKey(entry.getKey())){
                                    lastMessages.setMessage(userAccount1.getMap().get(entry.getKey()).getMessage());
                                    lastMessages.setDate(userAccount1.getMap().get(entry.getKey()).getDate());
                                    lastMessages.setNameSender(userAccount1.getMap().get(entry.getKey()).getNameSender());
                                    lastMessages.setSenderid(userAccount1.getMap().get(entry.getKey()).getSenderid());
                                    lastMessages.setRecord(userAccount1.getMap().get(entry.getKey()).getRecord());
                                    lastMessages.setImage(userAccount1.getMap().get(entry.getKey()).getImage());
                                    lastMessages.setRecieveid(userAccount1.getMap().get(entry.getKey()).getRecieveid());
                                }else{
                                    lastMessages.setMessage("");
                                    lastMessages.setDate("");
                                    lastMessages.setNameSender("");
                                    lastMessages.setSenderid("");
                                    lastMessages.setRecord("");
                                    lastMessages.setImage("");
                                    lastMessages.setRecieveid("");
                                }
                                userAccountArrayList.add(lastMessages);
                                FriendsAdapter adapter = new FriendsAdapter(getActivity(),
                                        userAccountArrayList, ChatFragment.this, userAccount1);
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    }
                });

        db.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (mAuth.getCurrentUser() != null) {
                    userStates.clear();
                    for (DocumentSnapshot document : value.getDocuments()) {
                        UserAccount userAccount = document.toObject(UserAccount.class);
                        if(mapFriend.containsKey(userAccount.getId())){
                            UserState userState = new UserState(userAccount.getImg_profile(),
                                    userAccount.getState(),userAccount.getName());
                            userStates.add(userState);
                        }
                    }
                    UserStateAdapter userStateAdapter = new UserStateAdapter(getActivity(), userStates);
                    recyclerView_state.setAdapter(userStateAdapter);
                }
            }
        });




        add_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddPersonActivity.class);
                intent.putExtra("userAccount",userAccount1);
                startActivity(intent);
            }
        });

        return view;
    }



    @Override
    public void onClick(LastMessages friendAccount) {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("friendAccount", friendAccount);
            intent.putExtra("userAccount", userAccount1);
            ChatModel chatModel = (ChatModel) getActivity().getIntent().getSerializableExtra("message");
            if (chatModel != null) {
                Log.e("getActivity:", chatModel.getMessage());
                intent.putExtra("message", chatModel);
            }
            getActivity().startActivity(intent);
    }
}