package com.edu.licenta.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.delta.activities.R;

import java.util.Date;

/**
 * Created by naritc
 * on 03-Jul-18.
 */

public class BubbleLevelCompass extends ViewGroup {

    private Paint mainPaint;
    private Paint pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap pointer;

    float posX = 0;
    float posY = 0;
    float angle;
    boolean finishedDrawing = false;

    Matrix mMatrix;

    public BubbleLevelCompass(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        requestFocus();
        setPadding(20, 20, 20, 20);
        setBackgroundColor(Color.GRAY);

        mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainPaint.setTextSize(20);
        mainPaint.setColor(Color.BLUE);
        textPaint.setTextSize(30.0f);
        linePaint.setStrokeWidth(10);
        linePaint.setColor(ContextCompat.getColor(context, R.color.saddleBrown));
        borderPaint.setStrokeWidth(30);
        borderPaint.setColor(ContextCompat.getColor(context, R.color.saddleBrown));
        pointer = BitmapFactory.decodeResource(getResources(), R.drawable.pointer_resized);
        mMatrix = new Matrix();
    }

    public void setValues(float posX, float posY, float angle) {
        this.posX = posX;
        this.posY = posY;
        this.angle = angle;
        if (finishedDrawing) {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        finishedDrawing = false;
        /*canvas.drawCircle(getPosX() , getPosY(), pointerRadius, pointerPaint);
        canvas.drawText(String.format("%.1f",getAccX()), 270.0f,200.0f, textPaint);
        canvas.drawText(String.format("%.1f",getAccY()), 270.0f,250.0f, textPaint);
        canvas.drawText(String.format("%.1f",getAccZ()), 270.0f,300.0f, textPaint);*/
        canvas.drawText(String.valueOf(angle), 270.0f, 350.0f, textPaint);

        canvas.drawLine(0, 0, 1080, 0, borderPaint);
        canvas.drawLine(1080, 0, 1080, 1760, borderPaint);
        canvas.drawLine(0, 1760, 1080, 1760, borderPaint);
        canvas.drawLine(0, 0, 0, 1760, borderPaint);

        canvas.drawLine(0, 480, 210, 480, linePaint);
        canvas.drawLine(285, 480, 795, 480, linePaint);
        canvas.drawLine(870, 480, 1080, 480, linePaint);

        canvas.drawLine(0, 1230, 210, 1230, linePaint);
        canvas.drawLine(285, 1230, 795, 1230, linePaint);
        canvas.drawLine(870, 1230, 1080, 1230, linePaint);

        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.rotate(-90);

        Matrix matrix = mMatrix;
        matrix.reset();

        matrix.postTranslate(-pointer.getWidth() / 2, -pointer.getHeight() /2);

        matrix.postTranslate(posX, posY);

        matrix.postTranslate(-posX, -posY);
        matrix.postRotate((float) Math.toDegrees(angle));
        matrix.postTranslate(posX, posY);

        canvas.drawBitmap(pointer, matrix ,pointerPaint);
        finishedDrawing = true;
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return 400;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return (int) 400;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(1080, 1760);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

}
