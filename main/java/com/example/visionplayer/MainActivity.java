package com.example.visionplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;
    Mat m1,m2,m3;
    BaseLoaderCallback baseLoaderCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if(OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"OpenCV Init Success",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(),"OpenCV Failed",Toast.LENGTH_LONG).show();
        }

/*

        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.camView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch (status){
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                            onManagerConnected(status);
                            break;
                }
            }
        };
        */

    }

    @Override
    protected  void onPause(){
        super.onPause();

        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();


        if(cameraBridgeViewBase != null){
            cameraBridgeViewBase.disableView();
        }

    }
    @Override
    protected  void onResume(){
        super.onResume();

        if(!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"OpenCV Failed to Resume",Toast.LENGTH_LONG).show();
        }else {
        //   baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }




    @Override
    public void onCameraViewStarted(int width, int height) {
        m1 = new Mat(width,height, CvType.CV_8UC4);
        m2 = new Mat(width,height, CvType.CV_8UC4);
        m3 = new Mat(width,height, CvType.CV_8UC4);

    }

    @Override
    public void onCameraViewStopped() {
        m1.release();
        m2.release();
        m3.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        m1 = inputFrame.rgba();

     //   Core.transpose(m1,m2);

      //  Imgproc.resize(m2,m3,m3.size(),0,0,0);

     //   Core.flip(m3,m1,1);


        return m1;
    }

    public void captureClassifier(View view){
        startActivity(new Intent(getApplicationContext(),CaptureClassifier.class));
    }

    public void loadClassifier(View view){
        startActivity(new Intent(getApplicationContext(),loadClassifier.class));
    }


}
