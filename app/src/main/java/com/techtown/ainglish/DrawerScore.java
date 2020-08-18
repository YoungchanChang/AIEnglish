package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.google.gson.Gson;
import com.techtown.ainglish.Adapter.OnTeacherItemClickListener;
import com.techtown.ainglish.Adapter.ScoreAdapter;
import com.techtown.ainglish.JSON.ScoreAllJSON;
import com.techtown.ainglish.JSON.TeacherInfoJSON;
import com.techtown.ainglish.JSON.ToServerJSON;
import com.techtown.ainglish.singleton.SingletonNewHttp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DrawerScore extends AppCompatActivity {
    Toolbar toolbar_main;
    private static final String TAG = "DrawerScoreLog";
    //채팅목록창을 위한 리싸이클러뷰이다.
    RecyclerView recycler_teacher;
    ScoreAdapter score_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_score);

        //리스너 관련 모음
        setListener();
        initView();

    }




    public void initView(){
        toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);





        Gson gson = new Gson();

        //SharedPref에서 유저정보를 가져와서 유저에게 전달한다.
        SharedPreferences pref= getSharedPreferences("USER_INFO", Activity.MODE_PRIVATE);
        String info = pref.getString("json_info", "");
        //gosn으로 객체 파싱한다.
        ToServerJSON shared_user_info = gson.fromJson(info, ToServerJSON.class);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        // 1.레이아웃 설정
        recycler_teacher = findViewById(R.id.recycler_score);
        recycler_teacher.setLayoutManager(layoutManager);
        // 2.아답터 결합
        score_adapter = new ScoreAdapter();
        recycler_teacher.setAdapter(score_adapter);


        //성공시 응답처리 정의
        SingletonNewHttp.getInstance().setHttpProperty(DrawerScore.this, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                //서버에서 return되는 값
                Log.d(TAG, "onResponseDrawerTeacherLog1: " + response);

                Gson gson = new Gson();
                ScoreAllJSON json_message = gson.fromJson(response, ScoreAllJSON.class);

                score_adapter.setItems(json_message.score_all_json);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        score_adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        Map<String,String> params = new HashMap<String,String>();
        params.put("user_id", shared_user_info.getServer_id());
        SingletonNewHttp.getInstance().putParams(params);

        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.SCORE_FIND);
        SingletonNewHttp.getInstance().makeRequest();
    }

    public void setListener(){

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
