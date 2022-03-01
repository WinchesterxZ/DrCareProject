package com.example.drhello.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.work.Operation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.drhello.CompleteInfoActivity;
import com.example.drhello.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class HomeFragment extends Fragment {
    Button btn_result_chest, btn_gallary, btn_result_tumor;
    TextView txt_result;
    ImageView img_corona;
    private static final int Gallary_REQUEST_CODE = 1;
    private Bitmap bitmap;
    private String[] stringsChest = {"Covid_19", "Lung_Opacity", "Normal", "Pneumonia"};
    private String[] stringsTumor =  { "Glioma_Tumor", "Meningioma Tumor", "No Tumor", "Pituitary Tumor"};

    public static ProgressDialog mProgress;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgress = new ProgressDialog(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btn_result_chest = view.findViewById(R.id.btn_result_chest);
        btn_gallary = view.findViewById(R.id.btn_gallary);
        txt_result = view.findViewById(R.id.txt_result);
        img_corona = view.findViewById(R.id.img_corona);
        btn_result_tumor = view.findViewById(R.id.btn_result_tumor);


        btn_result_chest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageModel("Chest_X_Ray_Model", 500, 500, stringsChest);
            }
        });


        btn_result_tumor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageModel("Brain_Tumor_Model", 400, 400, stringsTumor);
            }
        });

        btn_gallary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                String[] mimetypes = {"image/*", "video/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                startActivityForResult(intent, Gallary_REQUEST_CODE);
            }
        });

        return view;
    }

    private void imageModel(String name_model, int width, int height, String[] stringArrayList) {
        mProgress.setMessage("Uploading..");
        mProgress.show();
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        ByteBuffer input = ByteBuffer.allocateDirect(width * height * 1 * 4).order(ByteOrder.nativeOrder());
        for (int y = 0; y < width; y++) {
            for (int x = 0; x < height; x++) {
                int px = bitmap.getPixel(x, y);
                // Get channel values from the pixel value.
                int r = Color.red(px);
                float rf = r / 255.0f;
                input.putFloat(rf);
            }
        }

        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelDownloader.getInstance()
                .getModel(name_model, DownloadType.LOCAL_MODEL, conditions)
                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                    @Override
                    public void onSuccess(CustomModel model) {
                        // which you can use to instantiate a TensorFlow Lite interpreter.
                        File modelFile = model.getFile();
                        if (modelFile != null) {
                            Interpreter interpreter = new Interpreter(modelFile);
                            int bufferSize = 4 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
                            ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
                            interpreter.run(input, modelOutput);
                            modelOutput.rewind();
                            FloatBuffer probabilities = modelOutput.asFloatBuffer();
                            double max = probabilities.get(0);
                            int k = 0;
                            Log.e("capacity: ", probabilities.capacity() + "");
                            for (int i = 0; i < probabilities.capacity(); i++) {
                                Log.e("probabilities: ", probabilities.get(i) + "");
                                if (max < probabilities.get(i)) {
                                    k = i;
                                    max = probabilities.get(i);
                                }
                            }
                            if (k == 0) {
                                txt_result.setText(stringArrayList[0] + " :" + max);
                            } else if (k == 1) {
                                txt_result.setText(stringArrayList[1] + " :" + max);
                            } else if (k == 2) {
                                txt_result.setText(stringArrayList[2] + " :" + max);
                            } else {
                                txt_result.setText(stringArrayList[3] + " :" + max);
                            }
                            mProgress.dismiss();
                            interpreter.close();
                            Log.e("probabilities: ", max + "     " + k);
                        }
                    }
                });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallary_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                img_corona.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.e("gallary exception: ", e.getMessage());
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // Toast.makeText(getBaseContext(), "Canceled", Toast.LENGTH_SHORT).show();
        }

    }
}