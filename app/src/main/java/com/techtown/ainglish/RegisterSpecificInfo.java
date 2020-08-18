package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.google.gson.Gson;
import com.techtown.ainglish.JSON.ToServerJSON;
import com.techtown.ainglish.activity.AcitivityBlank;
import com.techtown.ainglish.customView.PermissionUtils;
import com.techtown.ainglish.singleton.SingletonHttp;
import com.techtown.ainglish.singleton.SingletonNewHttp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 세부정보입력 Activity
 * 이전 Activity - RegisterEssentialInfo
 * 이후 Activity - MainHome, 액티비티 스택 제거 해야함
 *
 */

public class RegisterSpecificInfo extends AppCompatActivity {

    private static final String TAG = "RegisterSpecificInfo";

    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;


    Toolbar toolbar_main;

    //이미지 클릭 관련 버튼
    ImageButton btn_profile_img;
    TextView text_input_profile;

    //이름 관련 객체들
    Button btn_nickname;
    EditText edit_nickname;

    //날짜 관련 객체들
    Button btn_specific_complete;
    DatePicker date_picker;
    int month, year, day;

    //Gallery에서 가져오는 이미지를 담는 Stream
    InputStream profile_img_data;

    //다이얼로그같은 중복되는 뷰들의 처리를 위한 객체
    SingletonView singleton_view;
    SingletonHttp singleton_http;


    //사진 파일 이름을 구분하지 않는 이유는 서버에서 구분할 것이기 때문이다.
    File camera_file;
    public static final String FILE_NAME = "profile.jpg";

    //사진, 갤러리에서 오는 이미지data를 bitmap으로 변환시키기 위한 객체.
    // 서버로 String으로 변환시켜 보낸다. encodedImage는 String형태이다.
    Bitmap bitmap_profile;
    String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_specific_info);
        initView();
        singleton_view = new SingletonView(this);
        singleton_http = new SingletonHttp(this);
    }

    public void initView(){
        toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        btn_profile_img = findViewById(R.id.btn_profile_img);
        text_input_profile = findViewById(R.id.text_input_profile);

        edit_nickname = findViewById(R.id.edit_nickname);
        btn_nickname = findViewById(R.id.btn_nickname);

        date_picker = (DatePicker) findViewById(R.id.simple_datepicker);

        btn_specific_complete = findViewById(R.id.btn_specific_complete);

        btn_listener();
    }

    /**
     * 버튼 관리를 위한 메소드
     */
    public void btn_listener(){

        DatePicker.OnDateChangedListener listener = new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String strDate = year + "/" + (monthOfYear + 1) + "/" + dayOfMonth;

            }
        };

        date_picker.init(2020, 6, 11, listener);

        btn_profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogueGetProfile();
            }
        });

        btn_specific_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeShow();
            }
        });

        TextWatcher edit_nickname_listener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //ignore
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //ignore
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(!(edit_nickname.getText().toString().length() >= 1)){
                    btn_nickname.setSelected(false);
                    btn_specific_complete.setEnabled(false);
                }else{
                    btn_nickname.setSelected(true);
                    btn_specific_complete.setEnabled(true);
                }
            }
        };
        edit_nickname.addTextChangedListener(edit_nickname_listener);
    }



    /**
     * startCamera()와 openGallery()를 선택하는 다이얼로그
     * onActivityResult()는 사진을 받아와서 picture_data에 저장하고
     *
     */
    void dialogueGetProfile()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("사진 고르기 선택");
        builder.setMessage("어디서 사진을 가져오시겠습니까?");

        builder.setPositiveButton("카메라",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startCamera();
                    }
                });
        builder.setNegativeButton("앨범",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startGalleryChooser();
                    }
                });
        builder.show();
    }


    public File getCameraFile() {
        File storageDir = getApplicationContext().getFilesDir();
        return new File(storageDir, FILE_NAME);
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //카메라는 파일이 저장될 위치를 명시하고, fileProvider에 authority를 명시해야한다.
            // 1. Manifest의 authority확인 2. Manifest안에 Provider에 존재하는 meta-data확인
            // 3. meta-data의 저장경로 확인(cache에 저장할것인지 EXTERNAL에 저장할 것인지)
            // file객체의 이름은 서버에서 처리할 것이기 때문에 똑같이 처리해도 된다.

            if (camera_file == null) {
                camera_file = getCameraFile();
            }
            Uri photoUri = FileProvider.getUriForFile(this, "org.techtown.english.fileprovider", camera_file);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);

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

    /**
     * PermissionUtils참조
     * 1. camera, gallery에서 requestPermission()을 수행한다.
     * 2. 권한이 없으면 PermissionUtils에서 권한 추가하고 false를 return한다
     * 3. permissionResult에서 다시 한 번 권한을 물어봄으로써 권한 기능 수행
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }


    /**
     * startCamera()와 startGalleryChooser()의 결과
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //입력요청 문구 안보이게
        text_input_profile.setVisibility(View.INVISIBLE);
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            //★파일의 현재 고유 주소값 C:\\windows\\~ 뭐이런느낌?
            Uri fileUri = data.getData();
            ContentResolver resolver = getContentResolver();
            try {
                //여기있는 picture_data이 upload되게 된다.
                profile_img_data = resolver.openInputStream(fileUri);
                bitmap_profile = BitmapFactory.decodeStream(profile_img_data);
                Log.d(TAG, "onActivityResult: Gallery 이미지 확인" + bitmap_profile);
                sendStringImg(bitmap_profile);

                //이미지가 사용자 UI 보여지게 하기 위한 코드
                InputStream profile_img_show = resolver.openInputStream(fileUri);
                Bitmap imgBitmap = BitmapFactory.decodeStream(profile_img_show);
                btn_profile_img.setImageBitmap(imgBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {

            //사진 이미지가 커서 업로드 되지 않는다면 주석처리한 코드로 설정
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 8;
//            Bitmap bitmap = BitmapFactory.decodeFile(camera_file.getAbsolutePath(), options);

            bitmap_profile = BitmapFactory.decodeFile(camera_file.getAbsolutePath());
            btn_profile_img.setImageBitmap(bitmap_profile);
            sendStringImg(bitmap_profile);

        }

    }

    /**
     * 이미지를 bitmap으로 바꾼 뒤 String으로 보내기 위한 메소드
     * @param bitmap_profile
     */
    public void sendStringImg(Bitmap bitmap_profile){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap_profile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.d(TAG, "onActivityResult: CAMERA 이미지 String 확인 :" + encodedImage);
    }


    /**
     * 완료시 본인이 입력한 정보가 맞는지 확인하는 Dialogue창
     * 이미지버튼을 클릭 안 했을 경우나 생년월일이 맞는지 확인하기 위한 버튼이다.
     *
     */
    void completeShow()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("회원가입 마지막 창입니다. 입력하신 정보가 맞습니까?");
        edit_nickname.getText().toString();

        month = date_picker.getMonth();
        year = date_picker.getYear();
        day = date_picker.getDayOfMonth();

        if(profile_img_data == null){
            builder.setMessage("이미지 : 기본이미지\n닉네임 :"+ edit_nickname.getText().toString() +
                    "\n생년월일" + year  + "/" + month + "/" +day);
        }else{
            builder.setMessage("이미지 : 선택됨\n닉네임 :"+ edit_nickname.getText().toString() +
                    "\n생년월일" + year  + "/" +month  + "/" +day);
        }

        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = getIntent();

                        //서버에 Data를 보내기 전에 객체화한다.
                        ToServerJSON json = new ToServerJSON(intent.getStringExtra("user_phone"),intent.getStringExtra("user_email"),
                                intent.getStringExtra("user_password")
                                , edit_nickname.getText().toString(), Integer.toString(year)
                        , Integer.toString(month),  Integer.toString(day));

                        json.setServer_image(encodedImage);
                        Gson gson = new Gson();
                        final String to_server_json = gson.toJson(json);

                        //3. 서버에 이미지 Request 메소드
                        // JSON형태의 String으로 보낸다.

                        ProgressDialog progress_login;
                        progress_login = ProgressDialog.show(RegisterSpecificInfo.this, null, "접속중입니다");


                        Response.Listener<String> response_listener =  new Response.Listener<String>(){
                            @Override
                            public void onResponse(String response) {

                                //뷰에 반영한다.
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "runResponse:" + response);
                                        progress_login.dismiss();
                                        Intent goHome = new Intent(RegisterSpecificInfo.this, LoginLobby.class);

                                        //회원가입이 완료되면 이전에 있던 1-4까지 ACTIVITY는 사라져야한다.
//                                        goHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(goHome);
                                        finish();
                                        overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
                                    }
                                });
                            }
                        };

                        Map<String,String> params = new HashMap<String,String>();
                        params.put("json", to_server_json);

                        SingletonNewHttp.getInstance().putParams(params);
                        //서버에 보낼 목적지 URI를 설정한다.
                        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.URI_REGISTER);
                        //성공시 처리한다.
                        SingletonNewHttp.getInstance().setHttpProperty(RegisterSpecificInfo.this, response_listener);

                        SingletonNewHttp.getInstance().makeRequest();


                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }


    /**
     * 1. 상단 Toolbar에서 뒤로가기 버튼을 눌렀을 때 2. BackPress됬을 때를 위한 메소드
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                singleton_view.dialogueBack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        singleton_view.dialogueBack();
    }

}





