package com.techtown.ainglish;

import androidx.appcompat.app.AppCompatActivity;

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

import com.android.volley.Response;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.techtown.ainglish.JSON.TeacherInfoJSON;
import com.techtown.ainglish.JSON.ToServerJSON;
import com.techtown.ainglish.singleton.SingletonNewHttp;
import com.techtown.ainglish.singleton.SingletonNewView;

import java.util.HashMap;
import java.util.Map;

public class DrawerTeacherSpecific extends AppCompatActivity {

    private static final String TAG = "DrawerSpecificLog";

    ImageView img_teacher_profile;
    TextView text_teacher_nickname;
    TextView text_teacher_brief;
    TextView text_teacher_description;

    Button btn_chatting_pay;
    Button btn_stream_pay;

    TeacherInfoJSON teacher_info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_teacher_specific);

        buttonListener();

        Intent get_intent = getIntent();
        String json_teacher_info = get_intent.getStringExtra("teacher_info");

        //json형태의 string을 객체로 변환한다
        Gson gson = new Gson();
        teacher_info = gson.fromJson(json_teacher_info, TeacherInfoJSON.class);


        img_teacher_profile = findViewById(R.id.img_teacher_profile);
        Glide.with(this)
                .load(teacher_info.getTeacher_image())
                .into(img_teacher_profile);

        text_teacher_nickname = findViewById(R.id.text_teacher_nickname);
        text_teacher_nickname.setText(teacher_info.getTeacher_nickname());

        text_teacher_brief = findViewById(R.id.text_teacher_brief);
        text_teacher_brief.setText(teacher_info.getTeacher_brief());

        text_teacher_description = findViewById(R.id.text_teacher_description);
        text_teacher_description.setText(teacher_info.getTeacher_description());

        btn_chatting_pay =findViewById(R.id.btn_chatting_pay);
        btn_chatting_pay.setOnClickListener(btn_chat_pay_listener);
        btn_stream_pay = findViewById(R.id.btn_stream_pay);
        btn_stream_pay.setOnClickListener(btn_stream_pay_listener);

        if(teacher_info.getTeacher_streaming().equals("NO")){
            btn_stream_pay.setEnabled(false);
        }

        //disable시키기
    }

    //버튼
    View.OnClickListener btn_chat_pay_listener;
    View.OnClickListener btn_stream_pay_listener;
    public void buttonListener(){

        btn_stream_pay_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingletonNewView.getInstance().dialogueCustom(DrawerTeacherSpecific.this, "결제 확인창", "결제하시겠습니까?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //1. 사용자 식별정보를 param으로 서버에 보낸다.
                        Gson gson = new Gson();

                        //SharedPref에서 유저정보를 가져와서 유저에게 전달한다.
                        SharedPreferences pref= getSharedPreferences("USER_INFO", Activity.MODE_PRIVATE);
                        String info = pref.getString("json_info", "");
                        //gosn으로 객체 파싱한다.
                        ToServerJSON shared_user_info = gson.fromJson(info, ToServerJSON.class);



                        Response.Listener<String> response_listener =  new Response.Listener<String>(){
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "onResponse:ForTeacherAdd:" + response);

                                if(response.equals("teacher_user_add_fail")){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(DrawerTeacherSpecific.this, "이미 결제하셨습니다." +
                                                    "", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }

                                //서버에 사용자 + 선생님 정보 합치기
                                //채팅창으로 보내기
                                Intent go_stream = new Intent(getApplicationContext(), MainStream.class);
                                startActivity(go_stream);
                                finish();
                                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);

                            }
                        };

                        //서버에 보낼 파라미터를 설정한다.
                        Map<String,String> params = new HashMap<String,String>();
                        //유저아이디와 선생님 정보 보내기
                        params.put("teacher_id", teacher_info.getTeacher_id());
                        params.put("user_id", shared_user_info.getServer_id());
                        Log.d(TAG, "onClick:teacherID" + teacher_info.getTeacher_id() + "userID:"+shared_user_info.getServer_id());

                        SingletonNewHttp.getInstance().putParams(params);
                        //서버에 보낼 목적지 URI를 설정한다.
                        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.TEACHER_USER_ADD_STREAM);
                        //성공시 처리한다.
                        SingletonNewHttp.getInstance().setHttpProperty(DrawerTeacherSpecific.this, response_listener);

                        SingletonNewHttp.getInstance().makeRequest();




                    }
                });
            }
        };

        btn_chat_pay_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingletonNewView.getInstance().dialogueCustom(DrawerTeacherSpecific.this, "결제 확인창", "결제하시겠습니까?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //1. 사용자 식별정보를 param으로 서버에 보낸다.
                        Gson gson = new Gson();

                        //SharedPref에서 유저정보를 가져와서 유저에게 전달한다.
                        SharedPreferences pref= getSharedPreferences("USER_INFO", Activity.MODE_PRIVATE);
                        String info = pref.getString("json_info", "");
                        //gosn으로 객체 파싱한다.
                        ToServerJSON shared_user_info = gson.fromJson(info, ToServerJSON.class);



                        Response.Listener<String> response_listener =  new Response.Listener<String>(){
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "onResponse:ForTeacherAdd:" + response);
                                if(response.equals("teacher_user_add_fail")){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(DrawerTeacherSpecific.this, "이미 결제하셨습니다." +
                                                    "", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }
                                //서버에 사용자 + 선생님 정보 합치기
                                //채팅창으로 보내기
                                Intent go_chat = new Intent(getApplicationContext(), MainChat.class);
                                startActivity(go_chat);
                                finish();
                                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);

                            }
                        };

                        //서버에 보낼 파라미터를 설정한다.
                        Map<String,String> params = new HashMap<String,String>();
                        //유저아이디와 선생님 정보 보내기
                        params.put("teacher_id", teacher_info.getTeacher_id());
                        params.put("user_id", shared_user_info.getServer_id());
                        Log.d(TAG, "onClick:teacherID" + teacher_info.getTeacher_id() + "userID:"+shared_user_info.getServer_id());

                        SingletonNewHttp.getInstance().putParams(params);
                        //서버에 보낼 목적지 URI를 설정한다.
                        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.TEACHER_USER_ADD);
                        //성공시 처리한다.
                        SingletonNewHttp.getInstance().setHttpProperty(DrawerTeacherSpecific.this, response_listener);

                        SingletonNewHttp.getInstance().makeRequest();




                    }
                });
            }
        };

    }

}

