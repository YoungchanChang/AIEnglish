package com.techtown.ainglish;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;
import com.techtown.ainglish.JSON.TeacherInfoJSON;
import com.techtown.ainglish.JSON.ToServerJSON;
import com.techtown.ainglish.activity.AcitivityBlank;
import com.techtown.ainglish.singleton.SingletonNewHttp;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @LoginSplash 에서 넘어온 Activity
 * 해당 페이지는 로그인시 가장 처음 보는 페이지로,
 *   -> 회원가입페이지나
 *   -> 이메일 로그인 페이지로 이동한다.
 */
public class LoginLobby extends AppCompatActivity {


    private static final String TAG = "LoginLobby";


    private SessionCallback sessionCallback;

    Button btn_email_login;
    TextView text_register;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_lobby);

        btn_email_login = findViewById(R.id.btn_email_login);
        text_register = findViewById(R.id.text_register);


        sessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(sessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen();

        btn_email_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent goEmailLogin = new Intent(getApplicationContext(), LoginForEmail.class);
                    startActivity(goEmailLogin);
                    finish();

            }
        });


        text_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goRegister = new Intent(getApplicationContext(), RegisterAgreement.class);
                startActivity(goRegister);
                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);

            }
        });

        //움직이는 배경을 위한 코드
        ImageView img_background = (ImageView)findViewById(R.id.img_background);
        Glide.with(this).asGif().load(R.drawable.login_img_background).into(img_background);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(sessionCallback);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
    }



    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    int result = errorResult.getErrorCode();

                    if(result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),"로그인 도중 오류가 발생했습니다: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Toast.makeText(getApplicationContext(),"세션이 닫혔습니다. 다시 시도해 주세요: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                }

                /**
                 * 여기에서 성공시 처리를 한다.
                 * @param result
                 */
                @Override
                public void onSuccess(MeV2Response result) {
                    Log.d(TAG, "onSuccess: 닉네임" + result.getNickname());
                    Log.d(TAG, "onSuccess: ID값" + result.getId());
                    Log.d(TAG, "onSuccess: 프로필 이미지" + result.getProfileImagePath());
                    Log.d(TAG, "onSuccess: 속성" + result.getProperties());
                    Log.d(TAG, "onSuccess: 카카오 계정" + result.getKakaoAccount());
                    Log.d(TAG, "onSuccess: 카오정보" + result);


                    //성공시 응답처리 정의
                    SingletonNewHttp.getInstance().setHttpProperty(LoginLobby.this, new Response.Listener<String>(){
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "KAKAO결과값11: " + response);

                                findUser(response);

                        }
                    });


                    SingletonNewHttp.getInstance().putURI(SingletonNewHttp.URI_REGISTER);

                    //넣고싶은 Parameter
                    //서버에 Data를 보내기 전에 객체화한다.
                    ToServerJSON json = new ToServerJSON(Long.toString(result.getId()),"",
                            "", "", "", "", "");
                    json.setKakao_profile(result.getProfileImagePath());

                    Gson gson = new Gson();
                    final String to_server_json = gson.toJson(json);

                    Map<String,String> params = new HashMap<String,String>();
                    params.put("json", to_server_json);
                    Log.d(TAG, "show json result : " + to_server_json);
                    SingletonNewHttp.getInstance().putParams(params);
                    SingletonNewHttp.getInstance().makeRequest();

                }
            });
        }

        public void findUser(String response){
            Response.Listener<String> response_listener =  new Response.Listener<String>(){
                @Override
                public void onResponse(String response) {
                    //로그인 성공시에 유저 info 저장시에 쓰일 SharedPref
                    SharedPreferences pref = getSharedPreferences("USER_INFO", Activity.MODE_PRIVATE);;
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("json_info", response);
                    Log.d(TAG, "onResponse:json_info" + response);
                    editor.commit();

                    Intent goHome = new Intent(getApplicationContext(), MainHome.class);

                    goHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(goHome);
                    finish();
                    overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
                }
            };

            //서버에 보낼 파라미터를 설정한다.
            Map<String,String> params = new HashMap<String,String>();
            params.put("user_phone", response);
            SingletonNewHttp.getInstance().putParams(params);
            //서버에 보낼 목적지 URI를 설정한다.
            SingletonNewHttp.getInstance().putURI(SingletonNewHttp.USER_FIND);
            //성공시 처리한다.
            SingletonNewHttp.getInstance().setHttpProperty(LoginLobby.this, response_listener);

            SingletonNewHttp.getInstance().makeRequest();
        }



        @Override
        public void onSessionOpenFailed(KakaoException e) {
            Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: "+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }



}
