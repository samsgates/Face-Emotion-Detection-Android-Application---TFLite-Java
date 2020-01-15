package com.example.visionplayer;

import android.app.Activity;

import java.io.IOException;


public class ClassifierFloatMobileNet extends Classifier {

    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 255.0f;


    private float[][] labelProbArray = null;

     public ClassifierFloatMobileNet(Activity activity, Classifier.Device device, int numThreads)
            throws IOException {
        super(activity, device, numThreads);
        labelProbArray = new float[1][getNumLabels()];
    }

    @Override
    public int getImageSizeX() {
        return 48;
    }

    @Override
    public int getImageSizeY() {
        return 48;
    }

    @Override
    protected String getModelPath() {
        return "model.tflite";
    }

    @Override
    protected String getLabelPath() {
        return "labels.txt";
    }

    @Override
    protected int getNumBytesPerChannel() {
        return 4; // Float.SIZE / Byte.SIZE;
    }

    @Override
    protected void addPixelValue(int pixelValue) {
        float rChannel = (pixelValue >> 16) & 0xFF;
        float gChannel = (pixelValue >> 8) & 0xFF;
        float bChannel = (pixelValue) & 0xFF;

        float pixel = (rChannel + gChannel + bChannel) / 3 / 255.f;

       // imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imgData.putFloat(pixel);
    }

    @Override
    protected float getProbability(int labelIndex) {
        return labelProbArray[0][labelIndex];
    }

    @Override
    protected void setProbability(int labelIndex, Number value) {
        labelProbArray[0][labelIndex] = value.floatValue();
    }

    @Override
    protected float getNormalizedProbability(int labelIndex) {
        return labelProbArray[0][labelIndex];
    }

    @Override
    protected void runInference() {
        tflite.run(imgData, labelProbArray);
    }
}
