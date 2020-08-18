package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.techtown.ainglish.Adapter.OnTeacherItemClickListener;
import com.techtown.ainglish.Adapter.TeacherAllAdapter;
import com.techtown.ainglish.JSON.TeacherAllJson;
import com.techtown.ainglish.JSON.TeacherInfoJSON;
import com.techtown.ainglish.JSON.ToServerJSON;
import com.techtown.ainglish.singleton.SingletonNewHttp;

import java.util.HashMap;
import java.util.Map;

public class DrawerTeacher extends AppCompatActivity {


    private static final String TAG = "DrawerTeacherLog";


    Toolbar toolbar_main;

    //채팅목록창을 위한 리싸이클러뷰이다.
    RecyclerView recycler_teacher;
    TeacherAllAdapter teacher_all_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_teacher);

        initView();
    }



    public void initView(){
        toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        /**
         * 리싸이클러의 구성요소
         * 1. 한 Layout이 차지하는 모양이 어떤지 LinearLayout
         * 2. 리싸이클러뷰에 데이터를 넣으면 View로 변환시켜줄 Adapter
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        // 1.레이아웃 설정
        recycler_teacher = findViewById(R.id.recycler_teacher);
        recycler_teacher.setLayoutManager(layoutManager);
        // 2.아답터 결합
        teacher_all_adapter = new TeacherAllAdapter(this);
        recycler_teacher.setAdapter(teacher_all_adapter);

        teacher_all_adapter.setOnItemClickListener(new OnTeacherItemClickListener() {
            @Override
            public void onItemClick(TeacherAllAdapter.ViewHolder holder, View view, int position) {
                TeacherInfoJSON item = teacher_all_adapter.getItem(position);
                Gson gson = new Gson();
                String json_message = gson.toJson(item);

                Intent go_specific = new Intent(DrawerTeacher.this, DrawerTeacherSpecific.class);
                go_specific.putExtra("teacher_info", json_message);
                startActivity(go_specific);
                finish();
                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
            }
        });


        //성공시 응답처리 정의
        SingletonNewHttp.getInstance().setHttpProperty(DrawerTeacher.this, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                //서버에서 return되는 값
                Log.d(TAG, "onResponseDrawerTeacherLog1: " + response);

                Gson gson = new Gson();
                TeacherAllJson json_message = gson.fromJson(response, TeacherAllJson.class);

                teacher_all_adapter.setItems(json_message.teacher_all_json);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        teacher_all_adapter.notifyDataSetChanged();
                    }
                });
            }
        });


        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.TEACHER_FIND);
        SingletonNewHttp.getInstance().makeRequest();
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
