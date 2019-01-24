package com.control.niuhongbin.openxhsjava;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {
    Button btnProcess;
    Button btnMatch;
    Bitmap srcBitmap;
    Bitmap grayBitmap;
    ImageView imgHuaishi;
    private static boolean flag = true;
    //private static boolean isFirst = true;
    private static final String TAG = "MainActivity";

    //OpenCV库加载并初始化成功后的回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "成功加载");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "加载失败");
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        btnProcess.setOnClickListener(new ProcessClickListener());
        btnMatch.setOnClickListener(new MatchClickListener());

    }

    public void initUI() {
        btnMatch = findViewById(R.id.btn_match);
        btnProcess = (Button) findViewById(R.id.btn_gray_process);
        imgHuaishi = (ImageView) findViewById(R.id.img_huaishi);
        Log.i(TAG, "initUI sucess...");

    }

    public void procSrc2Gray() {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B.
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat
        Utils.matToBitmap(grayMat, grayBitmap); //convert mat to bitmap
        Log.i(TAG, "procSrc2Gray sucess...");
    }


    public void PictureMatch() {
        Mat g_tem = Imgcodecs.imread(Environment.getExternalStorageDirectory() + "/Pictures/xhsTheEnd.png");
        Mat g_src = Imgcodecs.imread(Environment.getExternalStorageDirectory() + "/Pictures/xhs_temp.png");
//        Mat g_src = Imgcodecs.imread(Environment.getExternalStorageDirectory() + "/Pictures/xhs_big.png");

        int result_rows = g_src.rows() - g_tem.rows() + 1;
        int result_cols = g_src.cols() - g_tem.cols() + 1;
        Mat g_result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        Imgproc.matchTemplate(g_src, g_tem, g_result, Imgproc.TM_CCOEFF_NORMED);
        Core.normalize(g_result, g_result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Point matchLocation = new Point();
        Core.MinMaxLocResult mmlr = Core.minMaxLoc(g_result);
        matchLocation = mmlr.maxLoc;
        // 此处使用maxLoc还是minLoc取决于使用的匹配算法
        Imgproc.rectangle(g_src, matchLocation, new Point(matchLocation.x + g_tem.cols(), matchLocation.y + g_tem.rows()), new Scalar(0, 255, 0));
        Imgcodecs.imwrite(Environment.getExternalStorageDirectory() + "/Pictures/xhs_pic.png", g_src);

    }

    public void ShowPicture() {
        String path = Environment.getExternalStorageDirectory() + "/Pictures/src1.jpg";
        Log.v("pictual_path", path);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
    }


    private class MatchClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            PictureMatch();
//            ShowPicture();
        }

    }


    private class ProcessClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
//            if(isFirst)
//            {
            procSrc2Gray();
//                isFirst = false;
//            }
            if (flag) {
                imgHuaishi.setImageBitmap(grayBitmap);
                btnProcess.setText("查看原图");
                flag = false;
            } else {
                imgHuaishi.setImageBitmap(srcBitmap);
                btnProcess.setText("灰度化");
                flag = true;
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}