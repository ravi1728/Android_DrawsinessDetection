package com.edunomics.drawsinessdetection.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import com.google.android.gms.vision.face.Face;

class EyesGraphics extends GraphicOverlay.Graphic {

    private static final String TAG = "DrawsinessDetection";

    private static final float EYE_RADIUS_PROPORTION = 0.45f;

    private Paint mEyeRedPaint;
    private Paint mEyeGreenPaint;
    private Paint mTextGreenPaint;
    private Paint mTextRedPaint;

    private volatile int counter=60;
    private volatile Face face;

    private volatile PointF mLeftPosition;
    private volatile boolean mLeftOpen;


    private volatile PointF mRightPosition;
    private volatile boolean mRightOpen;

    //==============================================================================================
    // Methods
    //==============================================================================================

    public EyesGraphics(GraphicOverlay overlay) {
        super(overlay);
        Log.d(TAG, "EyesGraphics:: Running constructor");


        mEyeRedPaint = new Paint();
        mEyeRedPaint.setColor(Color.RED);
        mEyeRedPaint.setStyle(Paint.Style.STROKE);
        mEyeRedPaint.setStrokeWidth(2);

        mTextRedPaint = new Paint();
        mTextRedPaint.setColor(Color.RED);
        mTextRedPaint.setTextSize(80);
        mTextRedPaint.setStyle(Paint.Style.FILL);

        mEyeGreenPaint = new Paint();
        mEyeGreenPaint.setColor(Color.GREEN);
        mEyeGreenPaint.setStyle(Paint.Style.STROKE);
        mEyeGreenPaint.setStrokeWidth(2);

        mTextGreenPaint = new Paint();
        mTextGreenPaint.setColor(Color.GREEN);
        mTextGreenPaint.setTextSize(80);
        mTextGreenPaint.setStyle(Paint.Style.FILL);

    }

    /**
     * Updates the eye positions and state from the detection of the most recent frame.  Invalidates
     * the relevant portions of the overlay to trigger a redraw.
     */
    void updateEyes(Face fc, PointF leftPosition, boolean leftOpen,
                    PointF rightPosition, boolean rightOpen, int c) {

        Log.d(TAG, "EyesGraphics:: Running updateEyes");
        face=fc;
        counter=c;

        mLeftPosition = leftPosition;
        mLeftOpen = leftOpen;

        mRightPosition = rightPosition;
        mRightOpen = rightOpen;

        Log.d(TAG, "EyesGraphics:: Running postInvalidate");
        postInvalidate();
    }

    /**
     * Draws the current eye state to the supplied canvas.  This will draw the eyes at the last
     * reported position from the tracker, and the iris positions according to the physics
     * simulations for each iris given motion and other forces.
     */
    @Override
    public void draw(Canvas canvas) {
        Log.d(TAG, "EyesGraphics:: Running overriden draw");
        Face f=face;

        PointF detectLeftPosition = mLeftPosition;
        PointF detectRightPosition = mRightPosition;
        if ((detectLeftPosition == null) || (detectRightPosition == null)) {
            return;
        }

        float FaceTopLeft_x=f.getPosition().x;
        float FaceTopLeft_y=f.getPosition().y;
        float FaceBotRight_x=f.getPosition().x+f.getWidth();
        float FaceBotRight_y=f.getPosition().y+f.getHeight();

        float Text_x= translateX(FaceTopLeft_x+f.getWidth()/2);
        float Text_y= translateY(FaceTopLeft_y);

        RectF rect =new RectF(translateX(FaceTopLeft_x), translateY(FaceTopLeft_y),translateX(FaceBotRight_x) ,translateY(FaceBotRight_y));



        PointF leftPosition =
                new PointF(translateX(detectLeftPosition.x), translateY(detectLeftPosition.y));
        PointF rightPosition =
                new PointF(translateX(detectRightPosition.x), translateY(detectRightPosition.y));


        // Use the inter-eye distance to set the size of the eyes.
        float distance = (float) Math.sqrt(
                Math.pow(rightPosition.x - leftPosition.x, 2) +
                        Math.pow(rightPosition.y - leftPosition.y, 2));
        float eyeRadius = EYE_RADIUS_PROPORTION * distance;


        Log.d(TAG, "EyesGraphics:: Drawing eyes and FaceBox");
        if (mLeftOpen && mRightOpen) {
            canvas.drawText (String.valueOf(counter), Text_x, Text_y, mTextGreenPaint);
            canvas.drawRect(rect, mEyeGreenPaint);
            canvas.drawCircle(leftPosition.x, leftPosition.y, eyeRadius, mEyeGreenPaint);
            canvas.drawCircle(rightPosition.x, rightPosition.y, eyeRadius, mEyeGreenPaint);
        }
        else {
            canvas.drawText (String.valueOf(counter),Text_x, Text_y, mTextRedPaint);
            canvas.drawRect(rect, mEyeRedPaint);
            canvas.drawCircle(leftPosition.x, leftPosition.y, eyeRadius, mEyeRedPaint);
            canvas.drawCircle(rightPosition.x, rightPosition.y, eyeRadius, mEyeRedPaint);
        }

    }


}
