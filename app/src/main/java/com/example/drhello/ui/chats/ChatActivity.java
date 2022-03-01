package com.example.drhello.ui.chats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordPermissionHandler;
import com.example.drhello.StateOfUser;
import com.example.drhello.firebaseservice.FcmNotificationsSender;
import com.example.drhello.model.LastMessages;
import com.example.drhello.R;
import com.example.drhello.textclean.RequestPermissions;
import com.example.drhello.adapter.Recycle_Message_Adapter;
import com.example.drhello.databinding.ActivityChatBinding;
import com.example.drhello.model.ChatChannel;
import com.example.drhello.model.ChatModel;
import com.example.drhello.model.UserAccount;
import com.example.drhello.ui.main.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tougee.recorderview.AudioRecordView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

public class ChatActivity extends AppCompatActivity  {
    private static final int Gallary_REQUEST_CODE = 1, SONGS_REQUEST_CODE = 2, PERMISSION_ALL = 4000, PERMISSION_ALL_STORAGE = 5000;
    private static final int CAMERA_REQUEST_CODE = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int Location_REQUEST_CODE = 2000;
    private final int REQUESTPERMISSIONSFINE_LOCATION = 1001;
    private final int REQUESTPERMISSIONSLOCATION = 10;
    final int MY_Record_PERMISSION_CODE = 0;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    //public static ProgressDialog mProgress;
    private ActivityChatBinding activityChatBinding;
    private boolean flag_check_message = false, flagFirstTime = true;
    private String geoUri = "", iDChannel = "", mFileName;
    private ArrayList<ChatModel> chatsArrayList = new ArrayList<>();
    private LastMessages lastmassage;
    private UserAccount userAccountme, friendAccount;

    // private  MediaRecorder mRecorder;
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    public static MediaRecorder recorder = null;
    private File recordFile;
    private int currentFormat = 0;
    private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4,MediaRecorder.OutputFormat.THREE_GPP };
    private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP };
    private static final String PHOTOS_FOLDER = "Photos";
    //RecordAudio recordAudio;
    private RequestQueue mRequestQueue;
    private Recycle_Message_Adapter recycle_message_adapter;
    private Bitmap bitmap;
    private RequestPermissions requestPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        requestPermissions = new RequestPermissions(ChatActivity.this, ChatActivity.this);

        init();
        playRecordVoice();
        activityChatBinding.editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    flag_check_message = true;

                    activityChatBinding.imageviewSend.setVisibility(View.VISIBLE);
                    //      activityChatBinding.imageRecord.setImageResource(R.drawable.ic_send);
                } else {
                    flag_check_message = false;
                    activityChatBinding.imageviewSend.setVisibility(View.GONE);
                    //    activityChatBinding.imageRecord.setImageResource(R.drawable.ic_mic);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (activityChatBinding.editMessage.getText().toString().length() > 0) {
                    activityChatBinding.imgCamera.setVisibility(View.GONE);
                    activityChatBinding.recordButton.setVisibility(View.GONE);
                    activityChatBinding.imageviewSend.setVisibility(View.VISIBLE);
                } else {
                    activityChatBinding.imgCamera.setVisibility(View.VISIBLE);
                    activityChatBinding.recordButton.setVisibility(View.VISIBLE);
                    activityChatBinding.imageviewSend.setVisibility(View.GONE);
                }
            }
        });

        activityChatBinding.imgBackChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recycle_message_adapter != null) {
                    recycle_message_adapter.stoppingPlayer();
                }
                finish();
            }
        });

        activityChatBinding.imgAttachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog();
            }
        });

        activityChatBinding.imgCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (requestPermissions.permissionGallery()) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CAMERA},
                            MY_CAMERA_PERMISSION_CODE);
                } else {

                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                }
            }
        });

        activityChatBinding.imageviewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!activityChatBinding.editMessage.getText().toString().equals("")) {
                    ChatModel chatModel = new ChatModel(activityChatBinding.editMessage.getText().toString(),
                            getDateTime(), mAuth.getCurrentUser().getUid(),
                            friendAccount.getId(), "", userAccountme.getName(), "");
                    activityChatBinding.editMessage.setText("");
                    storeMessageOnFirebase(chatModel);
                    sendNotification(chatModel.getMessage());


                } else {
                    Toast.makeText(ChatActivity.this, "Empty", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void init() {
        activityChatBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        mRequestQueue = Volley.newRequestQueue(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        //  mProgress = new ProgressDialog(this);

        //   mRecorder = new MediaRecorder();
//        recordAudio = new RecordAudio();

        lastmassage = (LastMessages) getIntent().getSerializableExtra("friendAccount");

        String friendIdIntent = getIntent().getStringExtra("chatchannel");
        String id_massage;
        if (friendIdIntent != null) {
            id_massage = friendIdIntent;

            db.collection("users").whereEqualTo("id", mAuth.getCurrentUser().getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.e("task : ", " tast");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            userAccountme = document.toObject(UserAccount.class);
                        }
                    }
                }
            });

        } else {
            id_massage = lastmassage.getId();
            userAccountme = (UserAccount) getIntent().getSerializableExtra("userAccount");
        }
        db.collection("users").whereEqualTo("id", id_massage)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.e("task : ", " tast");
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        friendAccount = document.toObject(UserAccount.class);
                        createChannelOnFirebase(friendAccount.getId());
                        retriveDate();
                    }
                }
            }
        });

    }

    private void playRecordVoice(){


        //IMPORTANT

        activityChatBinding.recordButton.setRecordView(activityChatBinding.recordView);

        //ListenForRecord must be false ,otherwise onClick will not be called
        activityChatBinding.recordButton.setOnRecordClickListener(new OnRecordClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("RecordButton", "RECORD BUTTON CLICKED");
            }
        });

        //Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 8
        activityChatBinding.recordView.setCancelBounds(8);


        activityChatBinding.recordView.setSmallMicColor(Color.parseColor("#c2185b"));

        //prevent recording under one Second
        activityChatBinding.recordView.setLessThanSecondAllowed(false);


        activityChatBinding.recordView.setSlideToCancelText("Slide To Cancel");


        activityChatBinding.recordView.setCustomSounds(R.raw.record_start, R.raw.record_finished, 0);


        activityChatBinding.recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                activityChatBinding.imgCamera.setVisibility(View.GONE);
                activityChatBinding.clEdit.setVisibility(View.GONE);
                activityChatBinding.imgAttachFile.setVisibility(View.GONE);
                recordFile = new File(getFilename());
                startRecording(recordFile.getPath());


                Log.d("RecordView", "onStart");
            }

            @Override
            public void onCancel() {
                activityChatBinding.imgCamera.setVisibility(View.VISIBLE);
                activityChatBinding.clEdit.setVisibility(View.VISIBLE);
                activityChatBinding.imgAttachFile.setVisibility(View.VISIBLE);


                stopRecording();

                Log.d("RecordView", "onCancel");

            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                Toast.makeText(ChatActivity.this, "record:\n"+recorder.toString(), Toast.LENGTH_SHORT).show();
                uploadAudio(Uri.fromFile(new File(recordFile.getPath())));
                stopRecording();



                activityChatBinding.imgCamera.setVisibility(View.VISIBLE);
                activityChatBinding.clEdit.setVisibility(View.VISIBLE);
                activityChatBinding.imgAttachFile.setVisibility(View.VISIBLE);

            }

            @Override
            public void onLessThanSecond() {

                activityChatBinding.imgCamera.setVisibility(View.VISIBLE);
                activityChatBinding.clEdit.setVisibility(View.VISIBLE);
                activityChatBinding.imgAttachFile.setVisibility(View.VISIBLE);
                stopRecording();


                Toast.makeText(ChatActivity.this, "OnLessThanSecond", Toast.LENGTH_SHORT).show();
                Log.d("RecordView", "onLessThanSecond");
            }
        });

        activityChatBinding.recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                Log.d("RecordView", "Basket Animation Finished");
            }
        });

        activityChatBinding.recordView.setRecordPermissionHandler(new RecordPermissionHandler() {
            @Override
            public boolean isPermissionGranted() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return true;
                }

//                int com=ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                int ad=ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO);
//                boolean recordPermissionAvailable =
//                        ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED;
//                if (recordPermissionAvailable) {
//                    return true;
//                }
//
//
//                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
//
//                return false;
                if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_ALL_STORAGE);

                } else {
                    if ( ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_ALL_STORAGE);
                    } else {
                        Log.e("recordAudio: ", "FIRST");
                        return true;
                    }
                }

                return false;
            }
        });
    }

    private void retriveDate() {
        try {
            Glide.with(getBaseContext()).asBitmap().load(friendAccount.getImg_profile()).placeholder(R.drawable.user)
                    .error(R.drawable.user).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    activityChatBinding.profileImageChat.setImageBitmap(resource);
                    //     Log.e(" bitmap.toString : ",  resource+"");
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                }
            });
        } catch (Exception e) {
            activityChatBinding.profileImageChat.setImageResource(R.drawable.user);
        }

        activityChatBinding.txtNameChat.setText(friendAccount.getName());

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (recycle_message_adapter != null) {
            recycle_message_adapter.stoppingPlayer();
        }
        Intent intent = new Intent(ChatActivity.this, MainActivity.class);
        intent.putExtra("chatactivity", "chatactivity");
        startActivity(intent);
    }

    private void alertDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ChatActivity.this);
        View viewDailog = LayoutInflater.from(ChatActivity.this).inflate(R.layout.alert_dialog_chat_item, null);
        alertBuilder.setView(viewDailog);

        ImageView image_audio_file = viewDailog.findViewById(R.id.image_audio_file);
        ImageView image_gallary_file = viewDailog.findViewById(R.id.image_gallary_file);
        ImageView image_location = viewDailog.findViewById(R.id.image_location);

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.y = 150;
        window.setAttributes(wlp);

        // alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        image_audio_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_ALL_STORAGE);
                } else {

                    Intent song_intent = new Intent();
                    song_intent.setAction(android.content.Intent.ACTION_PICK);
                    song_intent.setData(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(song_intent, SONGS_REQUEST_CODE);
                    alertDialog.dismiss();
                    // Permission has already been granted
                }

            }
        });

        image_gallary_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                String[] mimetypes = {"image/*", "video/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                startActivityForResult(intent, Gallary_REQUEST_CODE);
                alertDialog.dismiss();

            }
        });

        image_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //////////////////to get location

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {
                        if (isGPSEnabled(ChatActivity.this)) {
                            GPSTracker currentLocation = new GPSTracker(getApplicationContext());
                            geoUri = "http://www.google.com/maps/place/" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "";
                        } else {
                            statusCheck();
                        }
                        Log.e("checkRunTime : ", "true");


                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUESTPERMISSIONSLOCATION);
                    }
                }
                if (!geoUri.equals("")) {
                    activityChatBinding.editMessage.setText(geoUri);
                } else {
                    Toast.makeText(getApplicationContext(), "please , Check premmision of location !!", Toast.LENGTH_SHORT).show();
                }
                alertDialog.dismiss();
            }
        });

    }


    private void sendNotification(String text) {
        FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(friendAccount.getTokenID(),
                mAuth.getCurrentUser().getUid(),
                "Message",
                userAccountme.getName() + " : " + text,
                getApplicationContext(),
                ChatActivity.this,
                userAccountme.getImg_profile(), userAccountme.getId());
        fcmNotificationsSender.SendNotifications();
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Location_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                }
                return;

            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }

            case PERMISSION_ALL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) ==
                            PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_GRANTED) {

                        Log.e("recordAudio : ", "second time");
                        //  recordAudio.startRecording();
                        return;
                    }
                }
                break;
            case REQUESTPERMISSIONSLOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.e("checkRunTime : ", "true");
                        // make a buidler for GoogleApiClient //
                        return;
                    }
                } else {
                    Log.e("onRequestPermissions : ", "false");

                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) ChatActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // If User Checked 'Don't Show Again' checkbox for runtime permission, then navigate user to Settings
                        AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
                        dialog.setTitle("Permission Required");
                        dialog.setCancelable(false);
                        dialog.setMessage("You have to Allow permission to access user location");
                        dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package",
                                        ChatActivity.this.getPackageName(), null));
                                //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivityForResult(i, REQUESTPERMISSIONSFINE_LOCATION);
                            }
                        });
                        AlertDialog alertDialog = dialog.create();
                        alertDialog.show();
                    }
                    //code for deny
                }

                return;

        }
    }


    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SS a", Locale.US);
        Date date = new Date();
        return dateFormat.format(date);
    }

    private String[] splitDateTime(String dateFormat) {
        return dateFormat.split(" ");
    }

    private void createChannelOnFirebase(String id_friend) {

        db.collection("users").
                document(id_friend).
                collection("channels")
                .document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {

                    String id_channel = Objects.requireNonNull(documentSnapshot.get("id")).toString();
                    Log.e("id", id_channel);

                    iDChannel = id_channel;
                } else {

                    DocumentReference idChannel = FirebaseFirestore.getInstance().collection("users").document();
                    ChatChannel chatChannel = new ChatChannel(idChannel.getId());


                    db.collection("users").
                            document(mAuth.getCurrentUser().getUid()).
                            collection("channels")
                            .document(id_friend).set(chatChannel);

                    db.collection("users").
                            document(id_friend).
                            collection("channels")
                            .document(mAuth.getCurrentUser().getUid()).set(chatChannel);

                    iDChannel = idChannel.getId();
                }
                ChatModel chatModel = (ChatModel) getIntent().getSerializableExtra("message");
                if (chatModel != null) {
                    Log.e("chatActivity:", chatModel.getMessage());
                    // if(!iDChannel.equals("")){
                    Log.e("iDChannel:", chatModel.getMessage());
                    chatModel.setDate(getDateTime());
                    chatModel.setSenderid(mAuth.getCurrentUser().getUid());
                    chatModel.setRecieveid(friendAccount.getId());
                    chatModel.setNameSender(userAccountme.getName());
                    storeMessageOnFirebase(chatModel);

                    //}
                }
                getMessages(iDChannel);
            }
        });
    }

    private void getMessages(String iDChannel) {
        getBitmapFromImage();

        db.collection("chatsChannel").document(iDChannel).collection("messages").
                orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                chatsArrayList.clear();
                for (DocumentSnapshot document : value.getDocuments())
                    if (document.exists()) {
                        ChatModel chatModel = document.toObject(ChatModel.class);
                        Log.e("all : ", chatModel.getDate());
                        chatsArrayList.add(chatModel);
                        recycle_message_adapter = new Recycle_Message_Adapter(chatsArrayList, ChatActivity.this, bitmap);
                        activityChatBinding.rvChatFriend.setAdapter(recycle_message_adapter);
                    }
            }
        });
    }

    private void getBitmapFromImage() {
        try {
            bitmap = ((BitmapDrawable) activityChatBinding.profileImageChat.getDrawable()).getBitmap();
        } catch (Exception E) {
            bitmap = Bitmap.createBitmap(activityChatBinding.profileImageChat.getDrawable().getIntrinsicWidth(), activityChatBinding.profileImageChat.getDrawable().getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            activityChatBinding.profileImageChat.getDrawable().setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            activityChatBinding.profileImageChat.getDrawable().draw(canvas);
        }
    }

    private void storeMessageOnFirebase(ChatModel chatModel) {

        db.collection("chatsChannel").
                document(iDChannel).
                collection("messages")
                .add(chatModel);

        Map<String, ChatModel> map = userAccountme.getMap();
        map.put(friendAccount.getId(), chatModel);
        userAccountme.setMap(map);
        db.collection("users").
                document(mAuth.getCurrentUser().getUid())
                .set(userAccountme);

        map = friendAccount.getMap();
        map.put(userAccountme.getId(), chatModel);
        friendAccount.setMap(map);
        db.collection("users").
                document(friendAccount.getId())
                .set(friendAccount);
        //   mProgress.dismiss();

    }


    private void uploadImage(Bitmap bitmap) {
        ByteArrayOutputStream output_image = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output_image);
        byte[] data_image = output_image.toByteArray();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/imagesChat/" + userAccountme.getId());
        storageReference.putBytes(data_image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.e("uri.toString() : ", uri.toString());
                        ChatModel chatModel = new ChatModel("", getDateTime(),
                                mAuth.getCurrentUser().getUid(), friendAccount.getId(), uri.toString(),
                                userAccountme.getName(), "");
                        storeMessageOnFirebase(chatModel);
                        sendNotification("Send an Image");
                        // Toast.makeText(getBaseContext(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("uri ", "Successful Upload");
                //Toast.makeText(getApplicationContext(),"Successful Upload ",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(getApplicationContext(),"UnSuccessful Upload ",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                uploadImage(bitmap);
            } catch (Exception e) {
                Log.e("camera exception: ", e.getMessage());
            }
        } else if (requestCode == Gallary_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), data.getData());
                uploadImage(bitmap);
                //     Toast.makeText(getBaseContext(), "Successful", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e("gallary exception: ", e.getMessage());
            }
        } else if (requestCode == SONGS_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            //    progress.setVisibility(View.VISIBLE);
            Uri uri = data.getData();
            Log.e("uri : ", uri + "");
            uploadAudio(Uri.fromFile(new File(getRealPathFromURI(uri))));

        } else if (resultCode == Activity.RESULT_CANCELED) {
            // Toast.makeText(getBaseContext(), "Canceled", Toast.LENGTH_SHORT).show();
        }

    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }



    public void uploadAudio(Uri uri) {
        //mProgress.setMessage("Uploading Audio ...");
        //mProgress.show();
//        UUID audioName=UUID.randomUUID();
        StorageReference storageReference = FirebaseStorage.getInstance().
                getReference().child("audios/audiosChat/" + userAccountme.getId());


        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.e("uri.toString() : ", uri.toString());
                        ChatModel chatModel = new ChatModel("", getDateTime(),
                                mAuth.getCurrentUser().getUid(), friendAccount.getId(), "",
                                userAccountme.getName(), uri.toString());
                        storeMessageOnFirebase(chatModel);
                        sendNotification("Send an Record");
                        // Toast.makeText(getBaseContext(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("uri ", "Successful Upload");
                //Toast.makeText(getApplicationContext(),"Successful Upload ",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(getApplicationContext(),"UnSuccessful Upload ",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void statusCheck() {
        LocationManager manager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isGPSEnabled(Context mContext) {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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

    public String getFilename(){

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
                ,AUDIO_RECORDER_FOLDER);
        Log.e("file : ", file.getAbsolutePath());
        if(!file.exists()){
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + "a" + ".mp3");
    }


    public void startRecording(String path){
        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        //recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(path);

        try {
            recorder.prepare();
            recorder.start();
            Log.e("prepare : ","start");
        } catch (IllegalStateException e) {
            Log.e("prepareERR:", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("IOException : ", e.getMessage());

            e.printStackTrace();
        }
    }



    public void stopRecording(){
        if (recordFile!=null){
            recordFile.delete();
        }
        try {
            recorder.stop();
            recorder.release();
            recorder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}