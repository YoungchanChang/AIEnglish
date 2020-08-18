package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.google.gson.Gson;
import com.kakao.auth.ApiErrorCode;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.techtown.ainglish.singleton.SingletonNewHttp;
import com.techtown.ainglish.singleton.SingletonNewView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class LoginForEmail extends AppCompatActivity {

    private static final String TAG = "LoginForEmailLog";
    Toolbar toolbar_main;

    //서버에 전달할 이메일과 패스워드 입력 EditText;
    EditText edit_login_email;
    EditText edit_login_pwd;

    //이메일 패스워드 입력 후 로그인 시도 버튼
    Button btn_login;
    View.OnClickListener btn_login_listen;

    //패스워드랑 아이디 찾기 버튼은 편의상 카카오톡 탈퇴기능을 넣었다.
    //TODO
    //실제 기능으로 넣어야한다.
    Button btn_pwd_find;
    View.OnClickListener btn_pwd_find_listen;

    Button btn_id_find;
    View.OnClickListener btn_id_find_listen;

    TextView text_teacher_login;
    View.OnClickListener text_teacher_login_listen;

    Button btn_login_teacher;
    View.OnClickListener btn_login_teacher_listen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_for_email);

        //기본 ToolBar초기화
        toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        listenerGroup();
        edit_login_email = findViewById(R.id.edit_login_email);
        edit_login_pwd = findViewById(R.id.edit_login_pwd);

        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(btn_login_listen);

        btn_id_find = findViewById(R.id.btn_id_find);
        btn_id_find.setOnClickListener(btn_pwd_find_listen);

        //테스트상 패스워드 찾기는 카카오톡 탈퇴 기능을 넣었다.
        btn_pwd_find = findViewById(R.id.btn_pwd_find);
        btn_pwd_find.setOnClickListener(btn_id_find_listen);

        //선생님로그인으로 버튼 변경하는 메시지
        text_teacher_login =findViewById(R.id.text_teacher_login);
        text_teacher_login.setOnClickListener(text_teacher_login_listen);

        //선생님 버튼
        btn_login_teacher = findViewById(R.id.btn_login_teacher);
        btn_login_teacher.setOnClickListener(btn_login_teacher_listen);

    }


    public void listenerGroup(){
        btn_login_listen = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SingletonNewView.getInstance().showProgressDialogue(LoginForEmail.this, "로그인 중입니다.");

                //성공시 응답처리 정의
                SingletonNewHttp.getInstance().setHttpProperty(LoginForEmail.this, new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponseLogin: " + response);
                        SingletonNewView.getInstance().dismissProgressDialogue();
                        //아이디와 비밀번호가 일치하면 유저 정보를 다 갖고온다.
                        if(response.equals("login_success")){
                            findUser(edit_login_email.getText().toString());
                        }
                    }
                });
                SingletonNewHttp.getInstance().putURI(SingletonNewHttp.URI_LOGIN);

                Map<String,String> params = new HashMap<String,String>();
                params.put("login_email", edit_login_email.getText().toString());
                params.put("login_pwd", edit_login_pwd.getText().toString());

                SingletonNewHttp.getInstance().putParams(params);
                SingletonNewHttp.getInstance().makeRequest();

            }
        };

        text_teacher_login_listen = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_login.setVisibility(View.INVISIBLE);
                btn_login_teacher.setVisibility(View.VISIBLE);
            }
        };

        //선생님찾기 정의
        btn_login_teacher_listen = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingletonNewView.getInstance().showProgressDialogue(LoginForEmail.this, "로그인 중입니다.");

                Response.Listener<String> response_listener =  new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {
                        //다이얼로그 제거
                        SingletonNewView.getInstance().dismissProgressDialogue();

                        //로그인 성공시에 teacher 저장에 쓰일 info
                        SharedPreferences pref = getSharedPreferences("TEACHER_INFO", Activity.MODE_PRIVATE);;
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("teacher_info", response);
                        Log.d(TAG, "onResponse:json_info" + response);
                        editor.commit();

                        Intent go_teacher_chat = new Intent(getApplicationContext(), TeacherChatting.class);
                        go_teacher_chat.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(go_teacher_chat);
                        finish();
                        overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
                    }
                };

                //서버에 보낼 파라미터를 설정한다.
                Map<String,String> params = new HashMap<String,String>();
                params.put("user_email", edit_login_email.getText().toString());
                Log.d(TAG, "onClick:teacher_find" + edit_login_email.getText().toString());
                SingletonNewHttp.getInstance().putParams(params);
                //서버에 보낼 목적지 URI를 설정한다.
                SingletonNewHttp.getInstance().putURI(SingletonNewHttp.TEACHER_LOGIN_FIND);
                //성공시 처리한다.
                SingletonNewHttp.getInstance().setHttpProperty(LoginForEmail.this, response_listener);

                SingletonNewHttp.getInstance().makeRequest();
            }
        };



        //로그아웃, 회원탈외용
        btn_id_find_listen = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
        btn_pwd_find_listen = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
    }


    public void findUser(String response){
        Response.Listener<String> response_listener =  new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                //다이얼로그 제거
                SingletonNewView.getInstance().dismissProgressDialogue();

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
        params.put("user_email", response);
        SingletonNewHttp.getInstance().putParams(params);
        //서버에 보낼 목적지 URI를 설정한다.
        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.USER_FIND);
        //성공시 처리한다.
        SingletonNewHttp.getInstance().setHttpProperty(LoginForEmail.this, response_listener);

        SingletonNewHttp.getInstance().makeRequest();
    }



    /**
     * 뒤로가기는 싱글톤으로 처리
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                SingletonNewView.getInstance().dialogueBack(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        SingletonNewView.getInstance().dialogueBack(this);
    }


}
