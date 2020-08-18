package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.gson.Gson;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.techtown.ainglish.JSON.ChatMessageJSON;
import com.techtown.ainglish.JSON.OCRDataJSON;
import com.techtown.ainglish.JSON.ToServerJSON;
import com.techtown.ainglish.singleton.SingletonNewHttp;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.bouncycastle.crypto.tls.TlsRSAUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;


/**
 * Logic
 * 1. 사진을 불러오기 ->img_translate에 사진 보여짐
 * 1-1> 사진찍기 버튼 안보여짐 / 다시찍기, 글자 인식하기 버튼이 보여짐
 * onActivityResult()
 *
 * 2. 글자인식하기 버튼 클릭하면
 * 2-1> 배경사진은 안 보인다. 글자인식 프레임창이 보이면 된다.
 * 2-2> 사진찍기 버튼이 안 보여짐 / 다시 찍기, 번역하기 버튼이 보여짐
 *
 * 3. 번역결과가 나오면
 * 3-1> 다시찍기/ 저장하기 버튼이 보여짐
 *
 * 4. 저장하기 버튼 누르면
 * 4-1> 서버에 저장하고 성공하면 Intent로 번역위치로 돌려보낸다.
 *
 * 다시찍기 버튼을 누르면, 사진찍기 버튼이 보여지고 나머지 버튼이 다 안보여짐
 * 번역용 프레임창도 안 보여지면 됨.
 */
public class DrawerTranslate extends AppCompatActivity {

    private static final String TAG = "DrawerTranslate";

    Toolbar toolbar_main;

    ImageView img_translate;
    TextView text_please; //번역할 사진을 올려주세요 텍스트

    //번역 결과가 나오는 파트
    FrameLayout frame_translate;
    TextView text_result_show;

    //기능관련 버튼들
    Button btn_text_ocr;
    View.OnClickListener listener_btn_text_ocr;

    Button btn_picture_load;
    View.OnClickListener listener_btn_picture_load;
    Button btn_picture_reload;
    View.OnClickListener listener_btn_picture_reload;

    Button btn_save;
    View.OnClickListener listener_btn_save;

    Button text_picture_translate;
    View.OnClickListener listener_text_picture_translate;

    //번역할 사진을 담는 공간
    Bitmap bitmap_translate;

    //번역이 끝났을 때 나오는 text
    String translate_target;

    //"번역중입니다" 표시
    ProgressDialog progress_ocr;

    //사진->영어 추출 OCR관련 객체
    TessBaseAPI tessBaseAPI;

    //Firebase의 영어를 한글로 도와주는 객체들
    FirebaseTranslatorOptions options;
    FirebaseTranslator englishKoreaTranslate;
    FirebaseModelDownloadConditions conditions;

    //gson
    Gson gson = new Gson();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_translate);

        toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //버튼 리스너를 초기화해줘야 버튼의 Listener에서 제대로 인식된다.
        btnListener();

        text_please = findViewById(R.id.text_please);

        btn_text_ocr = findViewById(R.id.btn_text_ocr);
        btn_text_ocr.setOnClickListener(listener_btn_text_ocr);

        btn_picture_load = findViewById(R.id.btn_picture_load);
        btn_picture_load.setOnClickListener(listener_btn_picture_load);

        btn_picture_reload = findViewById(R.id.btn_picture_reload);
        btn_picture_reload.setOnClickListener(listener_btn_picture_reload);

        btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(listener_btn_save);

        text_picture_translate = findViewById(R.id.text_picture_translate);
        text_picture_translate.setOnClickListener(listener_text_picture_translate);

        frame_translate = findViewById(R.id.frame_translate);
        img_translate = findViewById(R.id.img_translate);
        text_result_show = findViewById(R.id.text_result_show);


        //OCR관련 객체 초기화
        tessBaseAPI = new TessBaseAPI();
        String dir = getFilesDir() + "/tesseract";
        if(checkLanguageFile(dir+"/tessdata"))
            tessBaseAPI.init(dir, "eng");


        //영어->한글 Firebase 인식 설정.
        options = new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.EN)
                        .setTargetLanguage(FirebaseTranslateLanguage.KO)
                        .build();
        englishKoreaTranslate =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);

        conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        englishKoreaTranslate.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                Log.d(TAG, "initView: 모델 다운로드 완료");
                                // Model downloaded successfully. Okay to start translating.
                                // (Set a flag, unhide the translation UI, etc.)
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                            }
                        });
    }

    // * 1. 사진을 불러오기 ->img_translate에 사진 보여짐
    // * 1-1> 사진찍기 버튼 안보여짐 / 다시찍기, 글자 인식하기 버튼이 보여짐
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                ContentResolver resolver = getContentResolver();

                try {
                    InputStream instream = resolver.openInputStream(resultUri);
                    bitmap_translate = BitmapFactory.decodeStream(instream);
                    img_translate.setImageBitmap(bitmap_translate);

                    text_please.setVisibility(View.INVISIBLE);
                    // 사진찍기 버튼 안보여짐 / 다시찍기, 글자 인식하기 버튼이 보여짐
                    btn_picture_load.setVisibility(View.INVISIBLE);
                    btn_picture_reload.setVisibility(View.VISIBLE);
                    btn_text_ocr.setVisibility(View.VISIBLE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }






    // * 2. 글자인식하기 버튼 클릭하면
    // * 2-1> 배경사진은 안 보인다. 글자인식 프레임창이 보이면 된다.
    // * 2-2> 사진찍기 버튼이 안 보여짐 / 다시 찍기, 번역하기 버튼이 보여짐
    private class AsyncTess extends AsyncTask<Bitmap, Integer, String> {
        @Override
        protected String doInBackground(Bitmap... mRelativeParams) {
            tessBaseAPI.setImage(mRelativeParams[0]);
            return tessBaseAPI.getUTF8Text();
        }


        //실제 번역이 끝났을 때,
        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute: " +result);
            translate_target = result;

            progress_ocr.dismiss();

            //텍스트 글자를 프레임레이아웃의 글자두는데 둔다.
            text_result_show.setText(result);

            //텍스트 인식 성공했을 때의 처리.
            //텍스트 인식 버튼 사라지고, 사진 불러오기 안보여진다.
            //다시 찍기와 번역하기 버튼이 보인다.
            btn_picture_load.setVisibility(View.INVISIBLE);
            btn_text_ocr.setVisibility(View.INVISIBLE);
            btn_picture_reload.setVisibility(View.VISIBLE);
            text_picture_translate.setVisibility(View.VISIBLE);

        }
    }


String translate_text;
    public void btnListener(){
        //텍스트 인식하기 처리
        listener_btn_text_ocr = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //번역사진은 안 보여지게 되고 프레임레이아웃은 보이게 된다.
                img_translate.setVisibility(View.INVISIBLE);
                frame_translate.setVisibility(View.VISIBLE);

                progress_ocr = ProgressDialog.show(DrawerTranslate.this, null, "텍스트 인식중입니다.");

                new AsyncTess().execute(bitmap_translate);
            }
        };

        //사진 불러오기 -> 결과값은 onActivityResult()에 있따.
        listener_btn_picture_load = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(DrawerTranslate.this);
            }
        };

        //사진 다시 불러오기 처리. 관련 버튼 초기화한다.
        listener_btn_picture_reload = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_please.setVisibility(View.VISIBLE);

                btn_picture_load.setVisibility(View.VISIBLE);

                //나머지 버튼이 전부 안 보여지게 한다.
                btn_text_ocr.setVisibility(View.INVISIBLE);
                btn_picture_reload.setVisibility(View.INVISIBLE);
                btn_save.setVisibility(View.INVISIBLE);
                text_picture_translate.setVisibility(View.INVISIBLE);

                //프레임도 안 보여진다.
                img_translate.setVisibility(View.VISIBLE);
                frame_translate.setVisibility(View.INVISIBLE);
            }
        };


        // * 3. 번역결과가 나오면
        // * 3-1> 다시찍기/ 저장하기 버튼이 보여짐
        listener_text_picture_translate = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick:번역시작함");

                ProgressDialog progress_translate;
                progress_translate = ProgressDialog.show(DrawerTranslate.this, null, "번역중입니다.");

                englishKoreaTranslate.translate(translate_target)
                        .addOnSuccessListener(
                                new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(@NonNull String translatedText) {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progress_translate.dismiss();
                                                translate_text = translatedText;
                                                text_result_show.setText(translatedText);
                                                btn_picture_reload.setVisibility(View.VISIBLE);
                                                btn_save.setVisibility(View.VISIBLE);
                                            }
                                        });

                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: " + e.getMessage());
                                        // Error.
                                        // ...
                                    }
                                });
            }
        };

        //저장하기 버튼. 이미지를 먼저 저장한다.
        //이미지 저장성공해서 URI값 반환하면 사용자ID/URI/텍스트 데이터/저장날짜로 저장한다.
        listener_btn_save = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendStringImg(bitmap_translate);
            }
        };

    }


    /**
     * 데이터에 저장하는 것은 2중 Request를 수행한다.
     * 1Request -> 이미지를 저장한다.
     * Response -> 이미지 저장 경로
     *
     * 2Request -> user_id / 이미지 저장 경로 / 생성시간을 JSON으로 세이브
     * Response -> 성공적으로 저장 -> Intent로 다음 액티비티로 넘어간다.
     *
     */
    String encodedImage;
    public void sendStringImg(Bitmap bitmap_profile){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap_profile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();

        encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.d(TAG, "onActivityResult: CAMERA 이미지 String 확인 :" + encodedImage);

        //DB서버에 이미지를 보낸다. return값은 저장경로
        useSingletonHttp(encodedImage);
    }


    //저장경로를 메시지로 보낸다. 그러면 glide에서 해당 경로로 이미지 반환
    public void useSingletonHttp(String encodedImage){

        Response.Listener<String> response_listener =  new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                //이미지가 저장된 경로를 반환한다.
                Log.d(TAG, "FirstResponseImg" + response);

                String picture_path = response;
                //이미지가 저장되었다는 경로를 바탕으로 JSON을 만든다.
                String json_to_server = sendMessage(picture_path);
                Log.d(TAG, " :" + json_to_server);

                //데이터 저장 성공시에 서버에 전달한다.
                chat_save_listener = new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, "SecondResponse: " + response);
                        Intent go_translate = new Intent(getApplicationContext(), MainTranslate.class);
                        startActivity(go_translate);
                        finish();
                        overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);

                    }
                };
                useOCRDataSave(json_to_server);

            }
        };
        //서버에 보낼 파라미터를 설정한다.
        Map<String,String> params = new HashMap<String,String>();
        params.put("send_ocr_img", encodedImage);
        SingletonNewHttp.getInstance().putParams(params);
        //return값은 저장경로.
        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.OCR_IMG_SAVE);
        //성공시 처리한다.
        SingletonNewHttp.getInstance().setHttpProperty(DrawerTranslate.this, response_listener);
        SingletonNewHttp.getInstance().makeRequest();

    }



    //1.OCR_MESSAGE_메시지 객체 생성한다. 메시지 객체란???
    //2. 경로 생성해야한다.

    //is_picture에 TRUE이면 메시지가 사진의 URL을 담고 있고, FALSE이면 메시지이다.
    public String sendMessage(String img_path_url){

        //SharedPref에서 유저정보를 가져와서 유저에게 전달한다.
        SharedPreferences pref= getSharedPreferences("USER_INFO", Activity.MODE_PRIVATE);
        String info = pref.getString("json_info", "");
        //gosn으로 객체 파싱한다.
        ToServerJSON shared_user_info = gson.fromJson(info, ToServerJSON.class);


        OCRDataJSON json_format_message = new OCRDataJSON();


        //보내는 사람의 식별자, 사진 이미지 저장 경로, 번역결과물
        json_format_message.setUser_info(shared_user_info.getServer_id());
        json_format_message.setImg_path(img_path_url);
        json_format_message.setOcr_data(translate_text);

        //보내는 현재 시간을 구한다.
        SimpleDateFormat date_format = new SimpleDateFormat("hh:mm a");
        json_format_message.setOcr_create_time(date_format.format(System.currentTimeMillis()));

        return gson.toJson(json_format_message);
    }



    //채팅 서버에 저장한다 => 그 이후에 보낸다.
    //처음 시작할 때 서버에서 가져와서 아답터로 붙인다.
    Response.Listener<String> chat_save_listener;

    public void useOCRDataSave(String chat_data){
        //서버에 보낼 파라미터를 설정한다.
        Map<String,String> params = new HashMap<String,String>();
        params.put("ocr_data", chat_data);
        SingletonNewHttp.getInstance().putParams(params);
        //서버에 보낼 목적지 URI를 설정한다.
        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.OCR_DATA_SAVE);
        //성공시 처리한다.
        SingletonNewHttp.getInstance().setHttpProperty(DrawerTranslate.this, chat_save_listener);

        SingletonNewHttp.getInstance().makeRequest();
    }



    //OCR관련 파일 생성하는 메소드
    boolean checkLanguageFile(String dir)
    {
        File file = new File(dir);
        if(!file.exists() && file.mkdirs())
            createFiles(dir);
        else if(file.exists()){
            String filePath = dir + "/eng.traineddata";
            File langDataFile = new File(filePath);
            if(!langDataFile.exists())
                createFiles(dir);
        }
        return true;
    }

    //OCR관련 교육데이터 가져오는 메소드
    private void createFiles(String dir)
    {
        AssetManager assetMgr = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = assetMgr.open("eng.traineddata");

            String destFile = dir + "/eng.traineddata";

            outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 1. 상단 Toolbar에서 뒤로가기 버튼을 눌렀을 때 2. BackPress됬을 때를 위한 메소드
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
    }
}
