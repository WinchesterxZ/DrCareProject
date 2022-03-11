package com.example.drhello.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.drhello.MyCallBack;
import com.example.drhello.R;
import com.example.drhello.ui.chats.AsyncTaskDownloadAudio;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    Button btn_result_chest, btn_gallary, btn_result_tumor;
    TextView txt_result;
    ImageView img_corona;
    private static final int Gallary_REQUEST_CODE = 1;
    private Bitmap bitmap;
    private String[] stringsChest = {"Covid_19", "Lung_Opacity", "Normal", "Pneumonia"};
    private String[] stringsTumor =  { "Glioma_Tumor", "Meningioma Tumor", "No Tumor", "Pituitary Tumor"};
    PyObject main_program;
    public static ProgressDialog mProgress;
    PyObject str;
    ByteBuffer input;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgress = new ProgressDialog(getActivity());
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(getActivity()));//error is here!
        }
        final Python py = Python.getInstance();
        main_program = py.getModule("prolog");
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
                AsyncTaskD asyncTaskDownloadAudio = new AsyncTaskD("Corona");
                asyncTaskDownloadAudio.execute("");
            }
        });

        btn_result_tumor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTaskD asyncTaskDownloadAudio = new AsyncTaskD("Tumor");
                asyncTaskDownloadAudio.execute("");
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
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

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
              //  byte[] a = fromBitmap(bitmap);
            } catch (IOException e) {
                Log.e("gallary exception: ", e.getMessage());
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // Toast.makeText(getBaseContext(), "Canceled", Toast.LENGTH_SHORT).show();
        }

    }

    public class AsyncTaskD extends AsyncTask<String, String, String> {
        private String name_model ;

        public AsyncTaskD(String name_model){
            this.name_model = name_model;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setMessage("Image Processing..");
            mProgress.setCancelable(false);
            mProgress.show();
        }

        @Override
        protected String doInBackground(String... f_url) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            if(name_model.equals("Corona")){
                str = main_program.callAttr("call",byteArray,"Corona");
                input = ByteBuffer.allocateDirect(500 * 500 * 1 * 4)
                        .order(ByteOrder.nativeOrder());
            }else{
                str = main_program.callAttr("call",byteArray,"Tumor");
                input = ByteBuffer.allocateDirect(400 * 400 * 1 * 4)
                        .order(ByteOrder.nativeOrder());
            }

            String A = str.asList().toString();
            A = A.replace("[","");
            A = A.replace(",","");
            A = A.replace("]","");
            String[] s = A.split(" ");

            for (int y = 0; y < s.length; y++) {
                if(!s[y].equals("")) {
                    input.putFloat((float) (Float.parseFloat(s[y])/255.0));
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            if(name_model.equals("Corona")){
                imageModel("Chest_X_Ray_Model", 500, 500, stringsChest);
            }else{
                imageModel("Brain_Tumor_Model", 400, 400, stringsTumor);
            }
        }
    }
}