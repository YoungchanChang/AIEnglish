package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.techtown.ainglish.Adapter.OnUserAllItemClickListener;
import com.techtown.ainglish.Adapter.UserAllAdapter;
import com.techtown.ainglish.JSON.TeacherInfoJSON;
import com.techtown.ainglish.JSON.ToServerJSON;
import com.techtown.ainglish.JSON.UserAllJSON;
import com.techtown.ainglish.singleton.SingletonNewHttp;

import java.util.HashMap;
import java.util.Map;

public class TeacherStreaming extends AppCompatActivity {

    private static final String TAG = "TeacherChattingLog";

    Toolbar toolbar;

    //DrawerLayout와 NavigationView는 한 셋트이다.
    DrawerLayout drawer;
    NavigationView navigationView;
    NavigationView.OnNavigationItemSelectedListener nav_listen;
    View nav_header_view;

    //Navigation 안에 있는 뷰들
    ImageView img_profile;
    TextView text_nickname;
    TextView text_coin;

    //바탐 네비게이션
    BottomNavigationView bottomNav;
    BottomNavigationView.OnNavigationItemSelectedListener bottom_listener;

    //로그인 성공시에 유저 info 저장시에 쓰일 SharedPref
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    //성공시 응답처리
    Response.Listener<String> response_listener;


    //채팅목록창을 위한 리싸이클러뷰이다.
    RecyclerView recycler_user_info;
    UserAllAdapter user_all_adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_streaming);

        //초기 뷰설정관련 함수들이 모여있는 곳.
        initView();


        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // 1.레이아웃 설정
        recycler_user_info = findViewById(R.id.recycler_user_info);
        recycler_user_info.setLayoutManager(layoutManager);

        // 2.아답터 결합
        user_all_adapter = new UserAllAdapter(this);
        recycler_user_info.setAdapter(user_all_adapter);

        //클릭했을 때 처리
        user_all_adapter.setOnItemClickListener(new OnUserAllItemClickListener() {
            @Override
            public void onItemClick(UserAllAdapter.ViewHolderUserAll holder, View view, int position) {
                ToServerJSON item = user_all_adapter.getItem(position);

                Intent go_streaming = new Intent(TeacherStreaming.this, StreamRoom.class);
                go_streaming.putExtra("teacher_info", shared_teacher_info.getTeacher_id());
                go_streaming.putExtra("user_info", item.getServer_id());
                go_streaming.putExtra("position", "teacher");
                go_streaming.putExtra("user_profile", shared_teacher_info.getTeacher_image());
                go_streaming.putExtra("user_nickname", shared_teacher_info.getTeacher_nickname());

                Log.d(TAG, "onItemClickSendValue" + shared_teacher_info.getTeacher_id() + "그리고" + item.getServer_id());
                startActivity(go_streaming);
                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        initInfoSet();
    }

    /**
     * @init 하는 것들 bottom 네비게이션, side 네비게이션
     * InfoSet은 서버로부터 정보 받아오는 것
     */
    public void initView() {
        //리스너를 먼저 설정해줘야 한다.
        setListener();
        setDrawer();
        //사용자 관련 정보 불러오기
        initInfoSet();



    }


    /**
     * Logic
     * 1. 사용자 식별정보를 param으로 서버에 보낸다.
     * 2. 서버는 JSON형태로 값을 반환한다.
     * 3. 반환값은 3-1)SharedPref에 저장하고, 3-2)객체로 파싱해서 활용한다.
     */
    TeacherInfoJSON shared_teacher_info;
    public void initInfoSet(){
        //1. 사용자 식별정보를 param으로 서버에 보낸다.
        Gson gson = new Gson();

        //SharedPref에서 유저정보를 가져와서 유저에게 전달한다.
        pref= getSharedPreferences("TEACHER_INFO", Activity.MODE_PRIVATE);
        String info = pref.getString("teacher_info", "");
        //gosn으로 객체 파싱한다.
        shared_teacher_info = gson.fromJson(info, TeacherInfoJSON.class);

        Log.d(TAG, "initInfoSet:확인하기info" + info);


        String teacher_nickname = shared_teacher_info.getTeacher_nickname();
        String teacher_image = shared_teacher_info.getTeacher_image();
        String teacher_coin = shared_teacher_info.getTeacher_coin();

        //뷰에 반영한다.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Glide.with(getApplicationContext())
                        .load(teacher_image)
                        .into(img_profile);
                text_nickname.setText(teacher_nickname);
                text_coin.setText("보유 코인 : " + teacher_coin);

            }
        });

        /**
         *
         */
        //학생 선생의 릴레이숀쉽을 보낸다.
        Map<String,String> params = new HashMap<String,String>();
        params.put("teacher_id", shared_teacher_info.getTeacher_id());
        SingletonNewHttp.getInstance().putParams(params);
        Log.d(TAG, "initInfoSet:sendTeacherID " + shared_teacher_info.getTeacher_id());
        //서버에 보낼 목적지 URI를 설정한다.
        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.TEACHER_USER_T_STREAM_FIND);
        //성공시 처리한다.
        SingletonNewHttp.getInstance().setHttpProperty(TeacherStreaming.this, response_listener);

        SingletonNewHttp.getInstance().makeRequest();
    }


    /**
     * @drawerNavigation 관련 코드, setDrawer()와 onBackPressed()관련
     */
    public void setDrawer(){
        //toolbar 에 drawer를 붙이는 코드.
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //drawer 네이게이션 관련 뷰 초기화
        navigationView = findViewById(R.id.nav_view);
        nav_header_view = navigationView.getHeaderView(0);
        img_profile = nav_header_view.findViewById(R.id.img_translate);
        text_nickname = nav_header_view.findViewById(R.id.text_nickname);
        text_coin = nav_header_view.findViewById(R.id.text_coin);

        //drawer 네비게이션 뷰의 리스너 초기화
        navigationView.setNavigationItemSelectedListener(nav_listen);

        //bottom 네비게이션 뷰의 초기화
        bottomNav = findViewById(R.id.bottom_navigaion);
        bottomNav.setSelectedItemId(R.id.nav_bottom_streaming);
        bottomNav.setOnNavigationItemSelectedListener(bottom_listener);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }


    public void setListener(){

        response_listener =  new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                Log.d(TAG, "onResponse:mainChat" + response);

                Gson gson = new Gson();
                UserAllJSON json_message = gson.fromJson(response, UserAllJSON.class);

                 Log.d(TAG, "onResponseServerID " + json_message.user_all_json.get(0).getServer_id());
                user_all_adapter.setItems(json_message.user_all_json);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        user_all_adapter.notifyDataSetChanged();
                    }
                });


            }
        };

        nav_listen = new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.drawer_profile:
                        break;
                }

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        };

        bottom_listener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_bottom_chat:
                        Log.d(TAG, "onNavigationItemSelected: nav_bottom_chat");
                        Intent teacher_chat = new Intent(getApplicationContext(), TeacherChatting.class);
                        startActivity(teacher_chat);
                        finish();
                        return true;

                    case R.id.nav_bottom_streaming:
                        Log.d(TAG, "onNavigationItemSelected: nav_bottom_chat");
                        Intent teacher_steaming = new Intent(getApplicationContext(), TeacherStreaming.class);
                        startActivity(teacher_steaming);
                        finish();
                        return true;
                }

                return false;
            }

        };
    }

}
