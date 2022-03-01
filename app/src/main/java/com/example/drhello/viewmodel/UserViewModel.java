package com.example.drhello.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drhello.model.UserAccount;
import com.example.drhello.ui.profile.UserInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class UserViewModel extends ViewModel {

    private static final String TAG = "PostsViewModel";
    public MutableLiveData<UserAccount> UserMutableLiveData = new MutableLiveData<>();
    public MutableLiveData<UserInformation> UserInfoMutableLiveData = new MutableLiveData<>();
    public MutableLiveData<Integer>  isfinishUser = new MutableLiveData<>();

    public void getUser(FirebaseAuth mAuth, FirebaseFirestore db){
                db.collection("users").whereEqualTo("id",mAuth.getUid())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            Log.e("task : " , " tast");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                UserAccount userAccount = document.toObject(UserAccount.class);
                                UserMutableLiveData.setValue(userAccount);
                            }
                        }
                    }
                });
    }

    public  void getUserReaction(FirebaseFirestore db ){
                db.collection("users")
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                UserAccount userAccount = document.toObject(UserAccount.class);
                                Log.e("thread : " , userAccount.getName());
                                UserMutableLiveData.setValue(userAccount);
                            }
                        }
                    }
                });
    }



}
