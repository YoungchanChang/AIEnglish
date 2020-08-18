package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.techtown.ainglish.customView.PermissionUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.imread;


//앨범에서(URL위치) 사진을 불러와서(bitmap)으로 변환시키고
//bitmap을 mat으로 변환시킨다.
//TODO
//날짜에 따라서 사진을 불러온다.
//Day01 - 1 dog, 2 bycicle
//Day02 - 1.boat 3.char
//Day03 - man is holding a dog


//onClick();
//처음 권한 요청시 startGalleryChooser()에서 onRequestPermissionResult로 간다.
//그 다음부터는 startGalleryChooser() -> onActivityResult()
public class OpenCVtestSecond extends AppCompatActivity implements  View.OnClickListener{

    private static final String TAG = "OpenCVtestSecondLog";

    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    Button btn_gallery;
    ImageView image_test;

    private Net net_dnn;
    private static final String[] classNames = {"background",
            "aeroplane", "bicycle", "bird", "boat",
            "bottle", "bus", "car", "cat", "chair",
            "cow", "diningtable", "dog", "horse",
            "motorbike", "person", "pottedplant",
            "sheep", "sofa", "train", "tvmonitor"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_c_vtest_second);
        Bitmap bmp = null;

        btn_gallery = findViewById(R.id.btn_gallery);
        btn_gallery.setOnClickListener(this);
        image_test = findViewById(R.id.image_test);

    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_gallery:
                Log.d(TAG, "onClick:btn_GalleryClicked");
                startGalleryChooser();
                break;
            default:
                break;
        }
    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }


    //URI -> bitmap -> MAT으로 변환
    InputStream input_gallery_data;
    Bitmap img_bitmap;
    Mat img_mat;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            ContentResolver resolver = getContentResolver();
            try {
                //1. URI -> bitmap
                input_gallery_data = resolver.openInputStream(fileUri);
                img_bitmap = BitmapFactory.decodeStream(input_gallery_data);


                //2. bitmap -> mat
                img_mat = new Mat();
                Bitmap bmp32 = img_bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Utils.bitmapToMat(bmp32, img_mat);
                Log.d(TAG, "onActivityResult1111: " + img_mat);

                //Imgproc(이미지처리 객체) src->dest로 색상 변경한다.
                Imgproc.cvtColor(img_mat, img_mat, Imgproc.COLOR_RGBA2RGB);

                //이미지 재조정
                //rows()가로, cols()세로, 이미지크기, 담을 Scalar를 통해 새로운 객체 생성
                Mat image_resized = new Mat(img_mat.rows(), img_mat.cols(), CV_8UC3, new Scalar(255, 255, 255));

                //Mat의 사이즈를 재조정한다. 160의 width로
                //본래 width는 얼마만큼인데? down or up to specified size.
                float resizeRatio = resize(img_mat, image_resized, 160);


                //훈련의 가중치를 저장하는 이진 파일
                String pro = copyFile("MobileNetSSD_deploy.prototxt.txt", this);
                //네트워크 구성을 저장하고 있는 텍스트 파일
                String caff = copyFile("MobileNetSSD_deploy.caffemodel", this);
                net_dnn = Dnn.readNetFromCaffe(pro, caff);

//                String pro = copyFile("deploy.prototxt.txt", this);
//                String caff = copyFile("bvlc_googlenet.caffemodel", this);
//                net_dnn = Dnn.readNet(pro, caff);

                final int IN_WIDTH = 300;
                final int IN_HEIGHT = 300;
                final double MEAN_VAL = 127.5;
                final double IN_SCALE_FACTOR = 0.007843;

                //MAT으로부터 blob객체 생성 WHY?
                Mat blob = Dnn.blobFromImage(image_resized, IN_SCALE_FACTOR,
                        //학습과정에서 사용한 입력 영상, 출력 영상의 크기
                        new Size(IN_WIDTH, IN_HEIGHT),
                        //입력 영상 각 채널에서 뺄 평균값
                        new Scalar(MEAN_VAL, MEAN_VAL, MEAN_VAL), /*swapRB*/false, /*crop*/false);
                net_dnn.setInput(blob);
                //학습 수행
                Mat detections = net_dnn.forward();

//                Mat blob = Dnn.blobFromImage(img_mat, 1,
//                        //학습과정에서 사용한 입력 영상, 출력 영상의 크기
//                        new Size(224, 224),
//                        new Scalar(104, 117, 123));

                final double THRESHOLD = 0.2;


                int cols = image_resized.cols();
                int rows = image_resized.rows();
                detections = detections.reshape(1, (int)detections.total() / 7);


                //아 MAT객체 안에 있는 detection의 정보들이
                //0은 classId이고,
                //detections에 검출된 배열이 있네???
                //get(i)는 순서이고, col은 1,2,3,4,5,6으로 되어있네
                //1은 classId이다.
                for (int i = 0; i < detections.rows(); ++i) {
                    double confidence = detections.get(i, 2)[0];
                    if (confidence > THRESHOLD) {
                        int classId = (int)detections.get(i, 1)[0];

                        String label = classNames[classId] + ": " + String.format("%,4.2f", confidence);;
                        Log.d(TAG, "showConfidence:" + label);

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    public float resize(Mat img_src, Mat img_resize, int resize_width){

        float scale = resize_width / (float)img_src.cols() ;
        if (img_src.cols() > resize_width) {
            int new_height = Math.round(img_src.rows() * scale);
            Imgproc.resize(img_src, img_resize, new Size(resize_width, new_height));
        }
        else {
            img_resize = img_src;
        }
        return scale;
    }

    private static String copyFile(String filename, Context context) {

        Mat img1 = imread("lenna.bmp", IMREAD_GRAYSCALE);
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = context.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }

        return pathDir;

    }
}
