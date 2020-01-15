package com.example.visionplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import  org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class loadClassifier extends AppCompatActivity {

    ImageView imageView;
    Bitmap grayBitMap,imageBitMap,outputBitMap;

    Classifier classifier;

    InputStream inputStream= null;
    FileOutputStream outputStream =null;
    File mCascadeFile;
    CascadeClassifier cascadeClassifier;
    int absoluteFaceSize;
    MediaPlayer mp = null;

    private Classifier.Model model = Classifier.Model.FLOAT;
    private Classifier.Device device = Classifier.Device.CPU;
    private int numThreads = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_classifier);

        OpenCVLoader.initDebug();

        imageView = (ImageView)findViewById(R.id.imageView);

        loadDependencies();

        // 48 X 48 Images

        float f = 255.6f;
        System.out.println("Float Value:");
        System.out.println(f);
    }

    public static String getPath( Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }



    public void loadDependencies(){
        try {

            inputStream = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            outputStream = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());


        }catch (Exception e){
            Log.i("File Not Found","face cascade not found");
            e.printStackTrace();
        }
    }

    public void openGallery(View v){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);


        startActivityForResult(i,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int orientation;
        String filename;
        if(requestCode == 100 && resultCode== RESULT_OK && data != null){
            Uri imageuri = data.getData();
           // imageView.setImageURI(imageuri);

            try {

                imageBitMap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageuri);


                filename = getPath( getApplicationContext(), imageuri);

                ExifInterface exif = new ExifInterface(filename);

                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

                System.out.println("ORIENTATION"+ orientation);
              //  Toast.makeText(getApplicationContext(),orientation,Toast.LENGTH_LONG).show();

                if(orientation ==6) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    imageBitMap = Bitmap.createBitmap(imageBitMap, 0, 0, imageBitMap.getWidth(), imageBitMap.getHeight(), matrix, true);
                }

                imageView.setImageBitmap(imageBitMap);


            }catch (IOException e){
                e.printStackTrace();
            }

        }

    }

    public void convertToGray(View v){
        Mat rgba = new Mat(48,48,CvType.CV_32F);
        Mat gray = new Mat(48,48,CvType.CV_32F);
        Mat final_face = new Mat(48,48,CvType.CV_32F);
        MatOfRect faces = new MatOfRect();


        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inDither = false;
        o.inSampleSize = 4;

        // Setting Size of Image to be Processed
        int height = imageBitMap.getHeight();
        int width = imageBitMap.getWidth();

        Toast.makeText(getApplicationContext(),"Height :"+height+ " Width :"+width,Toast.LENGTH_LONG).show();

        absoluteFaceSize = (int) (height * 0.1);

        Log.d("err","Height: "+height+" Width :"+width+" size :"+absoluteFaceSize);


        Toast.makeText(getApplicationContext(),"ABS FACE :"+absoluteFaceSize,Toast.LENGTH_LONG).show();

        // Allocating Space to Store Gray Image BitMap
        grayBitMap = Bitmap.createBitmap(width,height, Bitmap.Config.RGB_565);
        // Bitmap for Final Image
        outputBitMap = Bitmap.createBitmap(48,48, Bitmap.Config.ARGB_8888);




        // Converting BitMap image to Matrix for Processing
        Utils.bitmapToMat(imageBitMap,rgba);


        // Converting Color Matrix to Gray Matrix
        Imgproc.cvtColor(rgba,gray,Imgproc.COLOR_RGB2GRAY);

        // Detecting faces from Gray Image
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(gray, faces, 1.1, 3, 2);
        }else{
            Toast.makeText(getApplicationContext(),"Cascasde Failed",Toast.LENGTH_LONG).show();
        }

        // Getting Detected Faces
        Rect[] facesArray = faces.toArray();

        // Printing Number of Faces Detected
        System.out.println("Number of Faces :"+facesArray.length);
        Toast.makeText(getApplicationContext(),"FACES:"+facesArray.length,Toast.LENGTH_LONG).show();

        if(facesArray.length > 0){

        // Drawing Rectagle Surrounding faces from Color Image
        for (int i = 0; i <facesArray.length; i++)
            Imgproc.rectangle(rgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);





        // Cropping Only the Face point representing Left Top and Right Bottom Points
        Point p1,p2;
        p1 = facesArray[0].tl();
        p2 = facesArray[0].br();

        int x,y,w,h;
        x = (int)p1.x;
        y = (int)p1.y;
        w = (int) (p2.x-p1.x+1);
        h = (int) (p2.y-p1.y+1);


        // New Matrix to Store Cropped Face from Gray Image for Classification
        Rect faceRect = new Rect(x,y,w,h);
        Mat face_cropped= rgba.submat(faceRect);


        // RESIZING
        Imgproc.resize(face_cropped,final_face,new Size(48,48));


        // Displaying the Image
      //  Core.normalize(rgba,rgba, 0, 255, Core.NORM_MINMAX, CvType.CV_32FC1, new Mat());
       // Core.convertScaleAbs(rgba, rgba);
        Utils.matToBitmap(final_face,outputBitMap);
       // imageView.setImageBitmap(outputBitMap);

        /*
        //Classify
        Mat temp =  new Mat(48,48, CvType.CV_32FC1);


        System.out.println("FACE IMAGE");

        MatOfFloat mof = new MatOfFloat(rgba);
        System.out.println(mof);

        // Normalizing Gray Image from 0 to 1
        //   Core.divide(final_face,new Scalar(255.0),temp);
        // Core.divide(final_face,d,temp);

        //Core.divide(final_face,new Scalar(255.0),temp);
        //System.out.println(temp.dump());

        */

        try {
            classifier = Classifier.create(this, model, device, numThreads);

            if(classifier==null){
                Toast.makeText(getApplicationContext(),"Classifier Initalization Failed",Toast.LENGTH_LONG).show();
            }else{
                final List<Classifier.Recognition> results = classifier.recognizeImage(outputBitMap);

                String emo = results.get(0).toString().substring(3,7);
                // System.out.println(results.get(0).toString());
                Toast.makeText(getApplicationContext(),results.get(1).toString().substring(3,7),Toast.LENGTH_LONG).show();


                Toast.makeText(getApplicationContext(),"Emotion: "+results.get(0).toString(),Toast.LENGTH_LONG).show();

                playSong(emo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        }else{
            Toast.makeText(getApplicationContext(),"NO FACE FOUND",Toast.LENGTH_LONG).show();
        }
    }

    public void playSong(String emotion) {

        emotion = emotion.trim();
        if (emotion.equals("Hap")) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.happy);
        } else if (emotion.equals("Neu")) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.neutral);
        } else if (emotion.equals("Sad")) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.sad);
        } else if (emotion.equals("Ang")) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.angry);
        }else if(emotion.equals("Sur")){
            mp = MediaPlayer.create(getApplicationContext(), R.raw.surprise);
        }else {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.happy);
        }
        mp.start();

    }

    public void stop(View view){
        mp.stop();
    }
}

