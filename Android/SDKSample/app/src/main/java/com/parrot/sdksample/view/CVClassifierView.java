package com.parrot.sdksample.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.opencv.core.CvType.CV_8U;

/**
 * Shell M. Shrader - 12/1/16
 *
 * All Rights Reserved
 */

public class CVClassifierView extends View {

    private final static String CLASS_NAME = CVClassifierView.class.getSimpleName();
    private final Context ctx;

    private CascadeClassifier faceClassifier;
    private CascadeClassifier palmClassifier;
    private CascadeClassifier fistClassifier;

    private Handler openCVHandler = new Handler();
    private Thread openCVThread = null;

    private BebopVideoView bebopVideoView = null;
    private ImageView cvPreviewView = null;

    private Rect[] facesArray = null;

    private Paint paint;

    private final Object lock = new Object();
    boolean starter=false;

    public CVClassifierView(Context context) {
        this(context, null);
    }

    public CVClassifierView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CVClassifierView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ctx = context;

        // initialize our opencv cascade classifiers


        // initialize our canvas paint object
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
    }

    private String cascadeFile(final int id) {
        final InputStream is = getResources().openRawResource(id);

        final File cascadeDir = ctx.getDir("cascade", Context.MODE_PRIVATE);
        final File cascadeFile = new File(cascadeDir, String.format(Locale.US, "%d.xml", id));

        try {
            final FileOutputStream os = new FileOutputStream(cascadeFile);
            final byte[] buffer = new byte[4096];

            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            is.close();
            os.close();
        } catch (Exception e) {
            Log.e(CLASS_NAME, "unable to open cascade file: " + cascadeFile.getName(), e);
            return null;
        }

        return cascadeFile.getAbsolutePath();
    }

    public void resume(final BebopVideoView bebopVideoView, final ImageView cvPreviewView) {
        if (getVisibility() == View.VISIBLE) {
            this.bebopVideoView = bebopVideoView;
            this.cvPreviewView = cvPreviewView;

            openCVThread = new CascadingThread(ctx);
            openCVThread.start();
        }
    }

    public void pause() {
        if (getVisibility() == View.VISIBLE) {
            openCVThread.interrupt();

            try {
                openCVThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private float mX = 0;
    private float mY = 0;

    private class CascadingThread extends Thread {
        private final Handler handler;
        boolean interrupted = false;

        private CascadingThread(final Context ctx) {
            handler = new Handler(ctx.getMainLooper());
        }

        public void interrupt() {
            interrupted = true;
        }

        @Override
        public void run() {
            Log.d(CLASS_NAME, "cascadeRunnable");

            final Mat firstMat = new Mat();
            final Mat mat = new Mat();

            while (!interrupted) {
                System.out.println("Rinning");
                final Bitmap source = bebopVideoView.getBitmap();

                if (source != null) {
                    Utils.bitmapToMat(source, firstMat);
                    firstMat.assignTo(mat);

                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY);

                    final int minRows = Math.round(mat.rows() * .12f);

                    final Size minSize = new Size(minRows, minRows);
                    final Size maxSize = new Size(0, 0);

                    final MatOfRect faces = new MatOfRect();

                    faceClassifier.detectMultiScale(mat, faces);

                    synchronized (lock) {
                        facesArray = faces.toArray();

                        mX = firstMat.width() / mat.width();
                        mY = firstMat.height() / mat.height();

                        faces.release();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                invalidate();
                            }
                        });
                    }
                }

                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }

            firstMat.release();
            mat.release();
        }

        private void runOnUiThread(Runnable r) {
            handler.post(r);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
//        Log.d(CLASS_NAME, "onDraw");

        synchronized(lock) {
            if (facesArray != null && facesArray.length > 0) {
                for (Rect target : facesArray) {
                    Log.i(CLASS_NAME, "found face size=" + target.area());
                    paint.setColor(Color.RED);
                    canvas.drawRect((float) target.tl().x * mX, (float) target.tl().y * mY, (float) target.br().x * mX, (float) target.br().y * mY, paint);
                }
            }
        }

        super.onDraw(canvas);
    }
}