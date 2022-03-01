package com.example.drhello.ui.writepost;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.drhello.StateOfUser;
import com.example.drhello.model.UserAccount;
import com.example.drhello.textclean.PreprocessingStrings;
import com.example.drhello.textclean.RequestPermissions;
import com.example.drhello.firebaseservice.FcmNotificationsSender;
import com.example.drhello.databinding.ActivityWritePostsBinding;
import com.example.drhello.model.Posts;
import com.example.drhello.ui.main.MainActivity;
import com.example.drhello.viewmodel.PostsViewModel;
import com.example.drhello.R;
import com.example.drhello.viewmodel.UserViewModel;
import com.example.drhello.adapter.ImagePostsAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class WritePostsActivity extends AppCompatActivity {


    private final List<Bitmap> bitmaps=new ArrayList<>();
    private final List<String> uriImage=new ArrayList<>();
    private final List<Uri> uriImage2=new ArrayList<>();
    private final List<byte[]> bytes=new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private StorageReference ref;
    private Posts posts;
    public static ProgressDialog mProgress;
    private PostsViewModel postsViewModel;
    private static final String TAG = "Posts Activity";
    private ActivityWritePostsBinding activityWritePostsBinding;
    private RequestPermissions requestPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_posts);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        requestPermissions = new RequestPermissions(WritePostsActivity.this,WritePostsActivity.this);
        inti();

        activityWritePostsBinding = DataBindingUtil.setContentView(this, R.layout.activity_write_posts);

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        activityWritePostsBinding.imgBackPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WritePostsActivity.this, MainActivity.class);
                intent.putExtra("postsView","postsView");
                startActivity(intent);
            }
        });

        UserViewModel userViewModel;
        userViewModel = ViewModelProviders.of(WritePostsActivity.this).get(UserViewModel.class);
        userViewModel.getUser(mAuth,db);
        userViewModel.UserMutableLiveData.observe(WritePostsActivity.this, userAccount -> {
            posts.setNameUser(userAccount.getName());
            posts.setImageUser(userAccount.getImg_profile());
            posts.setDate(getDateTime());
            posts.setTokneId(userAccount.getTokenID());
            Log.e("posts.UserMu ",posts.getImageUser());

            activityWritePostsBinding.userAddress.setText(userAccount.getUserInformation().getCity());
            activityWritePostsBinding.userName.setText(userAccount.getName());
            try{
                Glide.with(WritePostsActivity.this).load(userAccount.getImg_profile()).
                        placeholder(R.drawable.user).
                        error(R.drawable.user).into(activityWritePostsBinding.imageUser);
            }catch (Exception e){
                activityWritePostsBinding.imageUser.setImageResource(R.drawable.user);
            }

        });



    //to upload post

        activityWritePostsBinding.imgPost.setOnClickListener(v -> {
            mProgress.setMessage("Uploading..");
            mProgress.show();
            String post=activityWritePostsBinding.editPost.getText().toString().trim();
/*
      PreprocessingStrings preprocessingStrings = new PreprocessingStrings(WritePostsActivity.this);
            float[] pad = preprocessingStrings.loadModel(preprocessingStrings.tokensAndStemming(preprocessingStrings.preprocessingEN(post)),
                    "word_dict.json");


 */
      /*      CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                    .requireWifi()
                    .build();
            FirebaseModelDownloader.getInstance()
                    .getModel("hate_abusiveModel", DownloadType.LOCAL_MODEL, conditions)
                    .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                        @Override
                        public void onSuccess(CustomModel model) {
                            // Download complete. Depending on your app, you could enable
                            // the ML feature, or switch from the local model to the remote
                            // model, etc.
                            float[][] s = new float[][]{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5534, 1260, 22}};
                          //  float[][] s = new float[][]{pad};
                            File modelFile = model.getFile();

                            if (modelFile != null) {
                                Interpreter interpreter = new Interpreter(modelFile);
                                int bufferSize = 1000 * Float.SIZE / Byte.SIZE;
                                ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());

                                interpreter.run(s, modelOutput);

                                modelOutput.rewind();
                                FloatBuffer probabilities = modelOutput.asFloatBuffer();

                                float probability = probabilities.get(0);
                           //     if(probability > 0.5){
                                    Toast.makeText(getApplicationContext(),"REFUSE THIS TEXT",Toast.LENGTH_SHORT).show();
                                    Log.e("REFUSE THIS TEXT" ,probability+" ");
                                    mProgress.dismiss();
                            //    }else{

                                }
                      //      }
                        }
                    });
*/
            Log.e("posts.getnameuser ",posts.getImageUser());

            posts.setReactions(new HashMap<>());
            posts.setWritePost(post);
            posts.setUserId(mAuth.getUid());
            postsViewModel.uploadImages(db,storageReference,bytes,uriImage,posts);
            postsViewModel.isfinish.observe(WritePostsActivity.this, integer -> {
                Log.d(TAG, "Image: " + integer + "  uriImage.size() : "+ bytes.size());
                if (integer == bytes.size()) {
                    Log.d(TAG, "uploadImage: " + integer);
                    Log.e("int image ","fcm");
                    FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender("/topics/all",
                            mAuth.getCurrentUser().getUid(),
                            "Post",
                            posts.getNameUser() + " Upload a new post ",
                            getApplicationContext(),
                            WritePostsActivity.this,
                            posts.getImageUser());
                    fcmNotificationsSender.SendNotifications();

                    mProgress.dismiss();
                    // finish();
                    Intent intent = new Intent(WritePostsActivity.this, MainActivity.class);
                    intent.putExtra("postsView","postsView");
                    startActivity(intent);

                }
            });


            /*
            if(post.matches("[a-zA-Z]+")){
                PreprocessingStrings preprocessingStrings = new PreprocessingStrings(WritePostsActivity.this);
                preprocessingStrings.loadModel("Hate_AbusiveEN", preprocessingStrings.tokensAndStemming(preprocessingStrings.preprocessingEN(post)),"word_dict.json");
                Log.e("loadModel " ,"Hate_AbusiveEN");

            }else{
                PreprocessingStrings preprocessingStrings = new PreprocessingStrings(WritePostsActivity.this);
                preprocessingStrings.loadModel("ArabivHate_abusive", preprocessingStrings.tokensAndStemming(preprocessingStrings.preprocessingEN(post)),"word_dictAR.json");
                Log.e("loadModel " ,"ArabivHate_abusive");

            }
             */



        });

        activityWritePostsBinding.addImage.setOnClickListener(v -> {
            if (requestPermissions.permissionStorageRead()) {
                ActivityCompat.requestPermissions(WritePostsActivity.this,
                        new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},100);
            }
            Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
            intent.setType("image/*");
            startActivityForResult(intent,1);
        });
    }


    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
        Date date = new Date();
        return dateFormat.format(date);
    }



    private void inti() {
        mProgress = new ProgressDialog(this);
        Toolbar toolbar = findViewById(R.id.toolbar_posts);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        posts=new Posts();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        postsViewModel = new PostsViewModel();
        postsViewModel = ViewModelProviders.of(this).get(PostsViewModel.class);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK){

            assert data != null;
            ClipData clipData=data.getClipData();
            if (clipData!=null){
                for (int i=0;i<clipData.getItemCount();i++){
                    Uri imgUri=clipData.getItemAt(i).getUri();
                    Bitmap bitmap;
                    uriImage2.add(imgUri);
                    try {
                        //To save in FirebaseStorage
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
                        ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytesStream);
                        byte[] bytesOutImg = bytesStream.toByteArray();
                        bytes.add(bytesOutImg);

                        //To show in the same activity
                        InputStream is=getContentResolver().openInputStream(imgUri);
                        Bitmap bitmap_really= BitmapFactory.decodeStream(is);
                        bitmaps.add(bitmap_really);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }else {
                Uri imgUri=data.getData();
                Bitmap bitmap;
                try {
                    //To save in FirebaseStorage
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
                    ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytesStream);
                    byte[] bytesOutImg = bytesStream.toByteArray();
                    bytes.add(bytesOutImg);

                    //To show in the same activity
                    InputStream is=getContentResolver().openInputStream(imgUri);
                    Bitmap bitmap_really= BitmapFactory.decodeStream(is);

                    bitmaps.add(bitmap_really);

                } catch (IOException e){
                    e.printStackTrace();
                }
            }

            if (bitmaps.size()==1){
                activityWritePostsBinding.editPost.setHint("Say something about this photo...");
            }else if (bitmaps.size()>1){
                activityWritePostsBinding.editPost.setHint("Say something about these photos...");
            }else{
                activityWritePostsBinding.editPost.setHint("What’s on your mind?");
            }
            ImagePostsAdapter imagePostsAdapter = new ImagePostsAdapter(WritePostsActivity.this, bitmaps);
            GridLayoutManager recycleLayoutManager = new GridLayoutManager(this, 2,GridLayoutManager.VERTICAL, false);
            recycleLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    // grid items to take 1 column
                    if (bitmaps.size() % 2 == 0) {
                        return 1;
                    } else {
                        return (position == bitmaps.size()-1) ? 2 : 1;
                    }
                }
            });
            activityWritePostsBinding.recycleImages.setLayoutManager(recycleLayoutManager);
            Objects.requireNonNull(activityWritePostsBinding.recycleImages.getLayoutManager()).scrollToPosition(imagePostsAdapter.getItemCount() - 1);
            activityWritePostsBinding.recycleImages.setAdapter(imagePostsAdapter);

        }
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