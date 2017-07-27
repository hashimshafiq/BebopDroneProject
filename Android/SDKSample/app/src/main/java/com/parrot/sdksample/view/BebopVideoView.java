package com.parrot.sdksample.view;


        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.Canvas;
        import android.graphics.Matrix;
        import android.graphics.SurfaceTexture;
        import android.media.MediaCodec;
        import android.media.MediaFormat;
        import android.util.AttributeSet;
        import android.util.DisplayMetrics;
        import android.util.Log;
        import android.view.MenuItem;
        import android.view.MotionEvent;
        import android.view.ScaleGestureDetector;
        import android.view.Surface;
        import android.view.TextureView;
        import android.view.View;
        import android.widget.ImageView;
        import android.widget.SeekBar;
        import android.widget.TextView;

        import com.parrot.arsdk.arcontroller.ARFrame;
        import com.parrot.sdksample.R;


        import org.opencv.android.CameraBridgeViewBase;
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
        import org.opencv.objdetect.Objdetect;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.nio.ByteBuffer;

        import static android.content.ContentValues.TAG;


public class BebopVideoView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final String CLASS_NAME = BebopVideoView.class.getSimpleName();

    private static final String VIDEO_MIME_TYPE = "video/avc";
    private static final int VIDEO_DEQUEUE_TIMEOUT = 33000;
    private static final int VIDEO_WIDTH = 640;
    private static final int VIDEO_HEIGHT = 368;
    private Surface surface;
    private MediaCodec mediaCodec;
    private boolean surfaceCreated = false;
    private boolean codecConfigured = false;
    private ByteBuffer[] buffers;
    Mat firstMat = new Mat();
    public Bitmap source;
    int counter=0;
    public boolean tryz=false;
    public threading thread;
    boolean starter=false;
    ImageView img;

    //////////////////////////////////




    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    private static final int TM_SQDIFF = 0;
    private static final int TM_SQDIFF_NORMED = 1;
    private static final int TM_CCOEFF = 2;
    private static final int TM_CCOEFF_NORMED = 3;
    private static final int TM_CCORR = 4;
    private static final int TM_CCORR_NORMED = 5;


    private int learn_frames = 0;
    private Mat teplateR;
    private Mat teplateL;
    int method = 0;

    // matrix for zooming
    private Mat mZoomWindow;
    private Mat mZoomWindow2;

    private MenuItem mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    // private MenuItem               mItemType;

    private Mat                    mRgba=new Mat();
    private Mat                    mGray=new Mat();
    private File mCascadeFile;
    private File                   mCascadeFileEye;
    private CascadeClassifier mJavaDetector;
    private CascadeClassifier      mJavaDetectorEye;


    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private CameraBridgeViewBase mOpenCvCameraView;
    private SeekBar mMethodSeekbar;
    private TextView mValue;

    double xCenter = -1;
    double yCenter = -1;








    ////////////////////////////////////////////////////////

    public BebopVideoView(Context context) {
        this(context, null);
        setSurfaceTextureListener(this);

    }

    public BebopVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        setSurfaceTextureListener(this);
    }

    public BebopVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSurfaceTextureListener(this);
    }


    public void displayFrame(final ByteBuffer spsBuffer, final ByteBuffer ppsBuffer, ARFrame frame) {
        if(!starter) {


            try {
                // load cascade file from application resources
                InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                File cascadeDir = getContext().getDir("cascade", Context.MODE_PRIVATE);
                mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                FileOutputStream os = new FileOutputStream(mCascadeFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();

                // load cascade file from application resources
                InputStream ise = getResources().openRawResource(R.raw.haarcascade_lefteye_2splits);
                File cascadeDirEye = getContext().getDir("cascade", Context.MODE_PRIVATE);
                mCascadeFileEye = new File(cascadeDirEye, "haarcascade_lefteye_2splits.xml");
                FileOutputStream ose = new FileOutputStream(mCascadeFileEye);

                while ((bytesRead = ise.read(buffer)) != -1) {
                    ose.write(buffer, 0, bytesRead);
                }
                ise.close();
                ose.close();

                mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                if (mJavaDetector.empty()) {
                    Log.e(TAG, "Failed to load cascade classifier");
                    mJavaDetector = null;
                } else
                    Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                mJavaDetectorEye = new CascadeClassifier(mCascadeFileEye.getAbsolutePath());
                if (mJavaDetectorEye.empty()) {
                    Log.e(TAG, "Failed to load cascade classifier for eye");
                    mJavaDetectorEye = null;
                } else
                    Log.i(TAG, "Loaded cascade classifier from " + mCascadeFileEye.getAbsolutePath());

                cascadeDir.delete();
                cascadeDirEye.delete();

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
            }



            thread = new threading();
            thread.start();
            starter=true;
        }


        if (!surfaceCreated || spsBuffer == null) {
            return;
        }

        if (!codecConfigured) {
            configureMediaCodec(spsBuffer, ppsBuffer);
        }

        // Here we have either a good PFrame, or an IFrame
        int index = -1;

        try {
            index = mediaCodec.dequeueInputBuffer(VIDEO_DEQUEUE_TIMEOUT);
        } catch (IllegalStateException e) {
            Log.e(CLASS_NAME, "Error while dequeue input buffer");
        }
        if (index >= 0) {
            ByteBuffer b;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                b = mediaCodec.getInputBuffer(index);
            } else {
                b = buffers[index];
                b.clear();
            }

            if (b != null) {
                b.put(frame.getByteData(), 0, frame.getDataSize());
            }

            try {
                mediaCodec.queueInputBuffer(index, 0, frame.getDataSize(), 0, 0);
            } catch (IllegalStateException e) {
                Log.e(CLASS_NAME, "Error while queue input buffer");
            }
        }

        // Try to display previous frame
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int outIndex;
        try {
            outIndex = mediaCodec.dequeueOutputBuffer(info, 0);

            while (outIndex >= 0) {
                mediaCodec.releaseOutputBuffer(outIndex, true);
                outIndex = mediaCodec.dequeueOutputBuffer(info, 0);
            }



        } catch (IllegalStateException e) {
            Log.e(CLASS_NAME, "Error while dequeue input buffer (outIndex)");
        }
    }

    private void configureMediaCodec(final ByteBuffer spsBuffer, final ByteBuffer ppsBuffer) {
        try {
            final MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, VIDEO_WIDTH, VIDEO_HEIGHT);
            format.setByteBuffer("csd-0", spsBuffer);
            format.setByteBuffer("csd-1", ppsBuffer);

            mediaCodec = MediaCodec.createDecoderByType(VIDEO_MIME_TYPE);
            mediaCodec.configure(format, surface, null, 0);
            mediaCodec.start();

            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                buffers = mediaCodec.getInputBuffers();
            }

            codecConfigured = true;
        } catch (Exception e) {
            Log.e(CLASS_NAME, "configureMediaCodec", e);
        }


    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surface = new Surface(surface);
        surfaceCreated = true;


    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mediaCodec != null) {
            if (codecConfigured) {
                mediaCodec.stop();
                mediaCodec.release();
            }
            codecConfigured = false;
            mediaCodec = null;
        }

        if (surface != null) surface.release();
        if (this.surface != null) this.surface.release();


        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {


    }
    Mat img1=new Mat();
   public Bitmap newimg;

    class threading extends Thread{
        @Override
        public void run() {
            while (true) {
              //  System.out.println("In Thread");
               // firstMat = new Mat();


try {
    source = getBitmap();

    Utils.bitmapToMat(source, img1);
    if(newimg==null)
      newimg= Bitmap.createBitmap(img1.cols(), img1.rows(), Bitmap.Config.ARGB_8888);
    Imgproc.cvtColor(img1, mRgba, Imgproc.COLOR_BGR2RGBA);

    Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_BGR2GRAY);


  //  Imgproc.Canny(img1, img1, 10, 100, 3, true);
    img1=onCameraFrame();
    Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGBA2BGRA);



    Utils.matToBitmap(img1, newimg);
    System.out.println("done");
}
catch(Exception E)
{
   E.printStackTrace();
    System.out.println(E.getMessage());;
}



 //               System.out.println("After Counter " + (++counter));

            }
        }

    }
    public Mat onCameraFrame() {


        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }

        }

        if (mZoomWindow == null || mZoomWindow2 == null)
            CreateAuxiliaryMats();

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
        {	Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),
                FACE_RECT_COLOR, 3);
            xCenter = (facesArray[i].x + facesArray[i].width + facesArray[i].x) / 2;
            yCenter = (facesArray[i].y + facesArray[i].y + facesArray[i].height) / 2;
            Point center = new Point(xCenter, yCenter);

          /*  Imgproc.circle(mRgba, center, 10, new Scalar(255, 0, 0, 255), 3);

            Imgproc.putText(mRgba, "[" + center.x + "," + center.y + "]",
                    new Point(center.x + 20, center.y + 20),
                    Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
                            255));*/

            Rect r = facesArray[i];
            // compute the eye area
            Rect eyearea = new Rect(r.x + r.width / 8,
                    (int) (r.y + (r.height / 4.5)), r.width - 2 * r.width / 8,
                    (int) (r.height / 3.0));
            // split it
            Rect eyearea_right = new Rect(r.x + r.width / 16,
                    (int) (r.y + (r.height / 4.5)),
                    (r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));
            Rect eyearea_left = new Rect(r.x + r.width / 16
                    + (r.width - 2 * r.width / 16) / 2,
                    (int) (r.y + (r.height / 4.5)),
                    (r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));
            // draw the area - mGray is working grayscale mat, if you want to
            // see area in rgb preview, change mGray to mRgba



            //For EyeBox Removals (left And right)
            /*Imgproc.rectangle(mRgba, eyearea_left.tl(), eyearea_left.br(),
                    new Scalar(255, 0, 0, 255), 2);
            Imgproc.rectangle(mRgba, eyearea_right.tl(), eyearea_right.br(),
                    new Scalar(255, 0, 0, 255), 2);*/






            if (learn_frames < 5) {
                teplateR = get_template(mJavaDetectorEye, eyearea_right, 24);
                teplateL = get_template(mJavaDetectorEye, eyearea_left, 24);
                learn_frames++;
            } else {
                // Learning finished, use the new templates for template
                // matching
                match_eye(eyearea_right, teplateR, method);
                match_eye(eyearea_left, teplateL, method);

            }


            // cut eye areas and put them to zoom windows

            /*
            Imgproc.resize(mRgba.submat(eyearea_left), mZoomWindow2,
                    mZoomWindow2.size());
            Imgproc.resize(mRgba.submat(eyearea_right), mZoomWindow,
                    mZoomWindow.size());

                    */


        }

        return mRgba;
    }



    private void match_eye(Rect area, Mat mTemplate, int type) {
        Point matchLoc;
        Mat mROI = mGray.submat(area);
        int result_cols = mROI.cols() - mTemplate.cols() + 1;
        int result_rows = mROI.rows() - mTemplate.rows() + 1;
        // Check for bad template size
        if (mTemplate.cols() == 0 || mTemplate.rows() == 0) {
            return ;
        }
        Mat mResult = new Mat(result_cols, result_rows, CvType.CV_8U);

        switch (type) {
            case TM_SQDIFF:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF);
                break;
            case TM_SQDIFF_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mResult,
                        Imgproc.TM_SQDIFF_NORMED);
                break;
            case TM_CCOEFF:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF);
                break;
            case TM_CCOEFF_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mResult,
                        Imgproc.TM_CCOEFF_NORMED);
                break;
            case TM_CCORR:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR);
                break;
            case TM_CCORR_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mResult,
                        Imgproc.TM_CCORR_NORMED);
                break;
        }

        Core.MinMaxLocResult mmres = Core.minMaxLoc(mResult);
        // there is difference in matching methods - best match is max/min value
        if (type == TM_SQDIFF || type == TM_SQDIFF_NORMED) {
            matchLoc = mmres.minLoc;
        } else {
            matchLoc = mmres.maxLoc;
        }

        Point matchLoc_tx = new Point(matchLoc.x + area.x, matchLoc.y + area.y);
        Point matchLoc_ty = new Point(matchLoc.x + mTemplate.cols() + area.x,
                matchLoc.y + mTemplate.rows() + area.y);

        Imgproc.rectangle(mRgba, matchLoc_tx, matchLoc_ty, new Scalar(255, 255, 0,
                255));
        Rect rec = new Rect(matchLoc_tx,matchLoc_ty);


    }




    private Mat get_template(CascadeClassifier clasificator, Rect area, int size) {
        Mat template = new Mat();
        Mat mROI = mGray.submat(area);
        MatOfRect eyes = new MatOfRect();
        Point iris = new Point();
        Rect eye_template = new Rect();
        clasificator.detectMultiScale(mROI, eyes, 1.15, 2,
                Objdetect.CASCADE_FIND_BIGGEST_OBJECT
                        | Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30),
                new Size());

        Rect[] eyesArray = eyes.toArray();
        for (int i = 0; i < eyesArray.length;) {
            Rect e = eyesArray[i];
            e.x = area.x + e.x;
            e.y = area.y + e.y;
            Rect eye_only_rectangle = new Rect((int) e.tl().x,
                    (int) (e.tl().y + e.height * 0.4), (int) e.width,
                    (int) (e.height * 0.6));
            mROI = mGray.submat(eye_only_rectangle);
            Mat vyrez = mRgba.submat(eye_only_rectangle);


            Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);

            Imgproc.circle(vyrez, mmG.minLoc, 2, new Scalar(255, 255, 255, 255), 2);
            iris.x = mmG.minLoc.x + eye_only_rectangle.x;
            iris.y = mmG.minLoc.y + eye_only_rectangle.y;
            eye_template = new Rect((int) iris.x - size / 2, (int) iris.y
                    - size / 2, size, size);
            Imgproc.rectangle(mRgba, eye_template.tl(), eye_template.br(),
                    new Scalar(255, 0, 0, 255), 2);
            template = (mGray.submat(eye_template)).clone();
            return template;
        }
        return template;
    }


    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void CreateAuxiliaryMats() {
        if (mGray.empty())
            return;

        int rows = mGray.rows();
        int cols = mGray.cols();

        if (mZoomWindow == null) {
            mZoomWindow = mRgba.submat(rows / 2 + rows / 10, rows, cols / 2
                    + cols / 10, cols);
            mZoomWindow2 = mRgba.submat(0, rows / 2 - rows / 10, cols / 2
                    + cols / 10, cols);
        }

    }

    public void onRecreateClick(View v)
    {
        learn_frames = 0;
    }
}