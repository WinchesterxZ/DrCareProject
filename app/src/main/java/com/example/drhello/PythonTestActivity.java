package com.example.drhello;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class PythonTestActivity extends AppCompatActivity {
    PyObject main_program;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_python_test);
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));//error is here!
        }
        final Python py = Python.getInstance();
        main_program = py.getModule("prolog");

        PyObject str = main_program.callAttr("prolog_treatment","hallo python");
        Log.e("22222223: ", str.toString());
    }
}