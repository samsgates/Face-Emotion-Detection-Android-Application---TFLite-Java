package com.example.visionplayer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CaptureClassifier extends AppCompatActivity {

    private static final int pic_id = 123;
    MediaPlayer mp = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_classifier);

        imageView = (ImageView) findViewById(R.id.faceImageView);


        loadDependencies();

              dispatchTakePictureIntent();

//        Intent camera_intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Start the activity with camera_intent,
        // and request pic id
     //   startActivityForResult(camera_intent, pic_id);

    }

/*
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {

        // Match the request 'pic id with requestCode
        if (requestCode == pic_id) {

            // BitMap is data structure of image file
            // which stor the image in memory
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            // Set the image in imageview for display
            imageView.setImageBitmap(photo);
        }
    }
 */

    public void loadDependencies() {
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


        } catch (Exception e) {
            Log.i("File Not Found", "face cascade not found");
            e.printStackTrace();
        }
    }

    ImageView imageView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap grayBitMap, imageBitMap, outputBitMap;
    Classifier classifier;
    InputStream inputStream = null;
    FileOutputStream outputStream = null;
    File mCascadeFile;
    CascadeClassifier cascadeClassifier;
    int absoluteFaceSize;

    private Classifier.Model model = Classifier.Model.FLOAT;
    private Classifier.Device device = Classifier.Device.CPU;
    private int numThreads = -1;
    ContentValues values;
    Uri imageUri;

    private void dispatchTakePictureIntent() {
        /*
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }*/
        values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }


/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitMap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitMap);
            Log.d("err","Image Returned");
            convertToGray();
        }
    }
*/
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {

        case REQUEST_IMAGE_CAPTURE:
            if (requestCode == REQUEST_IMAGE_CAPTURE)
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        imageBitMap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), imageUri);
                      //  imageView.setRotation(imageView.getRotation()+90);
                      //  imageView.setImageBitmap(imageBitMap);
                        convertToGray();
                        //imageurl = getRealPathFromURI(imageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
    }
}

    public void convertToGray() {

        Matrix matrix = new Matrix();

        matrix.postRotate(90);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitMap, imageBitMap.getWidth(), imageBitMap.getHeight(), true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        imageBitMap = rotatedBitmap;

      //  imageView.setImageBitmap(imageBitMap);

        if (imageBitMap != null) {

            Mat rgba = new Mat(48, 48, CvType.CV_32F);
            Mat gray = new Mat(48, 48, CvType.CV_32F);
            Mat final_face = new Mat(48, 48, CvType.CV_32F);
            MatOfRect faces = new MatOfRect();


            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inDither = false;
            o.inSampleSize = 4;

            // Setting Size of Image to be Processed
            int height = imageBitMap.getHeight();
            int width = imageBitMap.getWidth();
            absoluteFaceSize = (int) (height * 0.1);

            Log.d("err","Height: "+height+" Width :"+width+" size :"+absoluteFaceSize);

            // Allocating Space to Store Gray Image BitMap
            grayBitMap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            // Bitmap for Final Image
            outputBitMap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);


            // Converting BitMap image to Matrix for Processing
            Utils.bitmapToMat(imageBitMap, rgba);


            // Converting Color Matrix to Gray Matrix
            Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGB2GRAY);

            // Detecting faces from Gray Image
            if (cascadeClassifier != null) {
                cascadeClassifier.detectMultiScale(gray, faces, 1.1, 3, 2);
            } else {
                Toast.makeText(getApplicationContext(), "Cascasde Failed", Toast.LENGTH_LONG).show();
            }

            // Getting Detected Faces
            Rect[] facesArray = faces.toArray();

            // Printing Number of Faces Detected
            System.out.println("Number of Faces :" + facesArray.length);


            Log.i("err","FACES :"+facesArray.length);

            if (facesArray.length > 0) {


                // Drawing Rectagle Surrounding faces from Color Image
                for (int i = 0; i < facesArray.length; i++)
                    Imgproc.rectangle(rgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);


               // Toast.makeText(getApplicationContext(),"FACES:"+facesArray.length,Toast.LENGTH_LONG).show();

                // Cropping Only the Face point representing Left Top and Right Bottom Points
                Point p1, p2;
                p1 = facesArray[0].tl();
                p2 = facesArray[0].br();

                int x, y, w, h;
                x = (int) p1.x;
                y = (int) p1.y;
                w = (int) (p2.x - p1.x + 1);
                h = (int) (p2.y - p1.y + 1);




                // New Matrix to Store Cropped Face from Gray Image for Classification
                Rect faceRect = new Rect(x, y, w, h);
                Mat face_cropped = rgba.submat(faceRect);


                // RESIZING
                Imgproc.resize(face_cropped, final_face, new Size(48, 48));

                /*
                // Displaying the Image
                Core.normalize(rgba, rgba, 0, 255, Core.NORM_MINMAX, CvType.CV_32FC1, new Mat());
                Core.convertScaleAbs(rgba, rgba);
                */
                Utils.matToBitmap(final_face, outputBitMap);



                Toast.makeText(getApplicationContext(), "Faces :" + facesArray.length, Toast.LENGTH_LONG).show();

                imageView.setImageBitmap(outputBitMap);

                Log.i("err","FACE SET");

                //Classify
                Mat temp = new Mat(48, 48, CvType.CV_32FC1);

                // Normalizing Gray Image from 0 to 1
                //   Core.divide(final_face,new Scalar(255.0),temp);
                // Core.divide(final_face,d,temp);

                //Core.divide(final_face,new Scalar(255.0),temp);
                //System.out.println(temp.dump());



            try {
                classifier = Classifier.create(this, model, device, numThreads);

                if (classifier == null) {
                    Toast.makeText(getApplicationContext(), "Classifier Initalization Failed", Toast.LENGTH_LONG).show();
                } else {
                    final List<Classifier.Recognition> results = classifier.recognizeImage(outputBitMap);

                    // System.out.println(results.get(0).toString());
                    Toast.makeText(getApplicationContext(), results.get(0).toString(), Toast.LENGTH_LONG).show();

                    String emo = results.get(0).toString().substring(3,7);
                    playSong(emo);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            } else {
                Toast.makeText(getApplicationContext(), "No Face Found", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "NULL ERR", Toast.LENGTH_LONG).show();
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

