package com.example.drhello.ui.writecomment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.drhello.StateOfUser;
import com.example.drhello.textclean.RequestPermissions;
import com.example.drhello.firebaseservice.FcmNotificationsSender;
import com.example.drhello.model.ReactionType;
import com.example.drhello.databinding.ActivityWriteCommentBinding;
import com.example.drhello.model.CommentModel;
import com.example.drhello.ui.main.MainActivity;
import com.example.drhello.ui.writepost.WritePostsActivity;
import com.example.drhello.viewmodel.CommentViewModel;
import com.example.drhello.model.Posts;
import com.example.drhello.R;
import com.example.drhello.viewmodel.UserViewModel;
import com.example.drhello.adapter.OnCommentClickListener;
import com.example.drhello.adapter.WriteCommentAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class WriteCommentActivity extends AppCompatActivity implements OnCommentClickListener {

    private static final int Gallary_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private FirebaseAuth mAuth;
    Posts posts = null;
    private FirebaseFirestore db;
    CommentModel commentModel = new CommentModel();
    private CommentViewModel commentViewModel;
    private WriteCommentAdapter writeCommentAdapter;
    public static ProgressDialog mProgress;
    private final ArrayList<CommentModel> commentModels = new ArrayList<>();
    private Bitmap bitmap;
    private boolean check_img = false;
    public static ActivityWriteCommentBinding MainCommentBinding;
    private String postID;
    private RequestPermissions requestPermissions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_comment);
        requestPermissions = new RequestPermissions(WriteCommentActivity.this,WriteCommentActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        commentViewModel = new CommentViewModel();
        commentViewModel = ViewModelProviders.of(WriteCommentActivity.this).get(CommentViewModel.class);

        postID = getIntent().getStringExtra("postID");

        if (postID != null) {
            Log.e("write : ", postID+"  hoos");
            db.collection("posts").document(postID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        posts = task.getResult().toObject(Posts.class);

                        commentViewModel.getComments(db, posts, null);
                        commentViewModel.commentsMutableLiveData.observe(WriteCommentActivity.this, commentModels -> {
                            writeCommentAdapter = new WriteCommentAdapter(WriteCommentActivity.this, commentModels,
                                    WriteCommentActivity.this,getSupportFragmentManager());
                            MainCommentBinding.recycleComments.setAdapter(writeCommentAdapter);
                            mProgress.dismiss();
                        });

                    } else {
                        Log.e("noti error", task.getException().getMessage());
                    }
                }
            });
            Log.e("notification", postID);
        } else {
            posts = (Posts) getIntent().getSerializableExtra("post");

            commentViewModel.getComments(db, posts, null);
            commentViewModel.commentsMutableLiveData.observe(WriteCommentActivity.this, commentModels -> {
                writeCommentAdapter = new WriteCommentAdapter(WriteCommentActivity.this, commentModels,
                        WriteCommentActivity.this,getSupportFragmentManager());
                MainCommentBinding.recycleComments.setAdapter(writeCommentAdapter);
                mProgress.dismiss();
            });

          //  Log.e("posts : ", posts.getPostId());
        }


        MainCommentBinding = DataBindingUtil.setContentView(this, R.layout.activity_write_comment);


        mProgress = new ProgressDialog(this);


        MainCommentBinding.constraintCommentRoot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = MainCommentBinding.constraintCommentRoot.getRootView().getHeight()
                        - MainCommentBinding.constraintCommentRoot.getHeight();

                if (heightDiff > 400) { // Value should be less than keyboard's height
                    Log.e("MyActivity", "keyboard opened" + heightDiff);
                    if (check_img == false) {
                        MainCommentBinding.constraintSend.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e("MyActivity", "keyboard closed");
                    String text=MainCommentBinding.editMessage.getText().toString().trim();

                    if (check_img == false) {
                        if (bitmap!=null){
                            MainCommentBinding.relImage.setVisibility(View.VISIBLE);
                        }else {
                            MainCommentBinding.relImage.setVisibility(View.GONE);
                        }
                        MainCommentBinding.linOption.setVisibility(View.VISIBLE);
                        
                        if (!text.isEmpty() || bitmap!=null){
                            MainCommentBinding.constraintSend.setVisibility(View.VISIBLE);
                        }else {
                            MainCommentBinding.constraintSend.setVisibility(View.GONE);
                        }
                    } else {
                        MainCommentBinding.relImage.setVisibility(View.VISIBLE);
                        MainCommentBinding.linOption.setVisibility(View.GONE);
                        MainCommentBinding.constraintSend.setVisibility(View.VISIBLE);
                    }

                }
            }
        });




        MainCommentBinding.btnCancel.setOnClickListener(v -> {
            bitmap = null;
            check_img = false;
            MainCommentBinding.linOption.setVisibility(View.VISIBLE);
            MainCommentBinding.relImage.setVisibility(View.GONE);
            MainCommentBinding.constraintSend.setVisibility(View.GONE);
        });


        UserViewModel userViewModel;
        userViewModel = ViewModelProviders.of(WriteCommentActivity.this).get(UserViewModel.class);
        userViewModel.getUser(mAuth, db);

        userViewModel.UserMutableLiveData.observe(WriteCommentActivity.this, userAccount -> {
            commentModel.setUser_image(userAccount.getImg_profile());
            commentModel.setUser_id(userAccount.getId());
            commentModel.setUser_name(userAccount.getName());
            commentModel.setPost_id(posts.getPostId());
        });


        MainCommentBinding.fabImage.setOnClickListener(view -> {

            if (requestPermissions.permissionStorageRead()) {
                ActivityCompat.requestPermissions(WriteCommentActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_CAMERA_PERMISSION_CODE);
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                String[] mimetypes = {"image/*", "video/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                startActivityForResult(intent, Gallary_REQUEST_CODE);
            } else {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                String[] mimetypes = {"image/*", "video/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                startActivityForResult(intent, Gallary_REQUEST_CODE);
            }
        });
        MainCommentBinding.fabCamera.setOnClickListener(view -> {

            if (requestPermissions.permissionGallery()) {
                ActivityCompat.requestPermissions(WriteCommentActivity.this, new String[]{Manifest.permission.CAMERA},
                        MY_CAMERA_PERMISSION_CODE);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }

        });

        MainCommentBinding.imageSend.setOnClickListener(view -> {

            mProgress.setMessage("Uploading..");
            mProgress.show();
            if (bitmap != null) {

                byte[] bytesOutImg;
                commentModel.setComment(MainCommentBinding.editMessage.getText().toString());
                commentModel.setDate(getDateTime());
                ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytesStream);
                bytesOutImg = bytesStream.toByteArray();
                commentViewModel.uploadComment(db, bytesOutImg, posts, commentModel, null);
                MainCommentBinding.editMessage.setText("");
                bitmap = null;
                Log.e("image : ", "EROR");

            } else {
                Log.e("bitmap : ", bitmap + "");
                commentModel.setComment_image(null);
                commentModel.setComment(MainCommentBinding.editMessage.getText().toString());
                commentModel.setDate(getDateTime());
                commentViewModel.uploadComment(db, null, posts, commentModel, null);
                MainCommentBinding.editMessage.setText("");
            }



            FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(posts.getTokneId(),
                    mAuth.getCurrentUser().getUid(),
                    "Comment",
                    commentModel.getUser_name() + " commented on your post",
                    getApplicationContext(),
                    WriteCommentActivity.this,
                    commentModel.getUser_image() ,
                    posts.getPostId());
            fcmNotificationsSender.SendNotifications();


            check_img = false;
            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(MainCommentBinding.editMessage.getWindowToken(), 0);


            posts.setCommentNum(posts.getCommentNum()+1);

            db.collection("posts").document(posts.getPostId())
                    .set(posts).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "ok  set", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "error  set", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        });


        if (posts != null) {
            db.collection("posts").document(posts.getPostId())
                    .collection("comments").orderBy("date", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {
                        commentModels.clear();
                        assert value != null;
                        for (DocumentSnapshot document : value.getDocuments()) {
                            CommentModel commentModel = document.toObject(CommentModel.class);
                            commentModels.add(commentModel);
                        }

                        writeCommentAdapter = new WriteCommentAdapter(WriteCommentActivity.this,
                                commentModels, WriteCommentActivity.this,getSupportFragmentManager());
                        MainCommentBinding.recycleComments.setAdapter(writeCommentAdapter);
                    });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            check_img = true;
            try {
                bitmap = null;
                bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    MainCommentBinding.showImage.setImageBitmap(bitmap);
                    MainCommentBinding.relImage.setVisibility(View.VISIBLE);
                    MainCommentBinding.linOption.setVisibility(View.GONE);
                } else {
                    MainCommentBinding.linOption.setVisibility(View.VISIBLE);
                    MainCommentBinding.relImage.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.e("camera exception: ", e.getMessage());
            }
        } else if (requestCode == Gallary_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            check_img = true;
            try {
                bitmap = null;
                bitmap = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), data.getData());
                if (bitmap != null) {
                    MainCommentBinding.showImage.setImageBitmap(bitmap);
                    MainCommentBinding.relImage.setVisibility(View.VISIBLE);
                    MainCommentBinding.linOption.setVisibility(View.GONE);
                } else {
                    MainCommentBinding.linOption.setVisibility(View.VISIBLE);
                    MainCommentBinding.relImage.setVisibility(View.GONE);
                }
            } catch (IOException e) {
                Log.e("gallary exception: ", e.getMessage());
            }
        }

    }


    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(WriteCommentActivity.this, MainActivity.class);
        intent.putExtra("openPost","openPost");
        startActivity(intent);
        finish();

    }



    @Override
    public void onClickComment(CommentModel commentModel) {
        Log.e("commentclikc : ", commentModel.getDate());
        Intent intent = new Intent(WriteCommentActivity.this, InsideCommentActivity.class);
        intent.putExtra("commentModel", commentModel);
        intent.putExtra("postsModel", posts);
        startActivity(intent);
    }

    @Override
    public void selectedReaction(String reaction, CommentModel commentModel) {
        ReactionType reactionType = new ReactionType(reaction,mAuth.getCurrentUser().getUid());
        Log.e("reactionType" , reactionType.getReactionType());  // new
        Map<String,String> arrayList = posts.getReactions();
        if(reactionType.getReactionType().equals(posts.getReactions().get(mAuth.getCurrentUser().getUid()))){
            arrayList.remove(mAuth.getCurrentUser().getUid());
        }else{
            arrayList.put(mAuth.getCurrentUser().getUid(),reactionType.getReactionType());
        }
        commentModel.setReactions(arrayList);
        db.collection("posts").document(posts.getPostId()).
                collection("comments").document(commentModel.getComment_id()).set(commentModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            onResume();
                            Log.e("equals " , "isSuccessful");
                            Toast.makeText(WriteCommentActivity.this, "reaction commentModel", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(WriteCommentActivity.this, "false commentModel ", Toast.LENGTH_SHORT).show();
                        }
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