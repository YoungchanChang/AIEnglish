package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.techtown.ainglish.JSON.ToServerJSON;
import com.techtown.ainglish.singleton.SingletonNewHttp;

import java.util.HashMap;
import java.util.Map;


/**
 * @참고
 * 1. Main부터 리스너는 따로 관리하기 시작했음.
 * 이유는 이게 더 편하단 것을 알아버렸음.
 * 2. Http통신을 singletonNewHttp객체로 만듬.
 *
 */


public class MainHome extends AppCompatActivity implements  View.OnClickListener{

    private static final String TAG = "MainHomeLog";

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

    TextView text_my_progress;
    Button btn_quiz_start;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_home);



        //초기 뷰설정관련 함수들이 모여있는 곳.
        initView();



    }

    @Override
    protected void onResume() {
        super.onResume();
        initInfoSet();
        //클릭시어떻게해?
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

        text_my_progress = findViewById(R.id.text_my_progress);
        btn_quiz_start = findViewById(R.id.btn_quiz_start);
        btn_quiz_start.setOnClickListener(this);
    }

    //서버에 메시지 보낼때 관련 메소드
    Response.Listener<String> get_daily_quiz;
    public void useSingletonHttp(){

        get_daily_quiz =  new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                //뷰에 반영한다.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                    }
                });
            }
        };

        //서버에 보낼 파라미터를 설정한다.
        Map<String,String> params = new HashMap<String,String>();
        params.put("user_phone", "param");
        SingletonNewHttp.getInstance().putParams(params);
        //서버에 보낼 목적지 URI를 설정한다.
        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.USER_FIND);
        //성공시 처리한다.
        SingletonNewHttp.getInstance().setHttpProperty(MainHome.this, response_listener);

        SingletonNewHttp.getInstance().makeRequest();

    }

    /**
     * Logic
     * 1. 사용자 식별정보를 param으로 서버에 보낸다.
     * 2. 서버는 JSON형태로 값을 반환한다.
     * 3. 반환값은 3-1)SharedPref에 저장하고, 3-2)객체로 파싱해서 활용한다.
     */
    public void initInfoSet(){
        //1. 사용자 식별정보를 param으로 서버에 보낸다.
        Gson gson = new Gson();

        //SharedPref에서 유저정보를 가져와서 유저에게 전달한다.
        SharedPreferences pref= getSharedPreferences("USER_INFO", Activity.MODE_PRIVATE);
        String info = pref.getString("json_info", "");
        //gosn으로 객체 파싱한다.
        ToServerJSON shared_user_info = gson.fromJson(info, ToServerJSON.class);

        Log.d(TAG, "initInfoSet:확인하기info" + info);


        //1. 서버에 보낼 파라미터를 설정한다.
        Map<String,String> params = new HashMap<String,String>();
        params.put("user_phone", shared_user_info.getServer_phone());
        SingletonNewHttp.getInstance().putParams(params);

        //2. 서버에 보낼 목적지 URI를 설정한다.
        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.USER_FIND);

        //3. 성공시 반환값을 처리한다.
        SingletonNewHttp.getInstance().setHttpProperty(MainHome.this, response_listener);

        //4. 실제로 보낸다.
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
        bottomNav.setSelectedItemId(R.id.nav_bottom_home);
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

                //2. 서버는 JSON형태로 값을 반환한다.
                Log.d(TAG, "onResponse 서버 리턴 값: " + response);

                //반환값은 3-1)SharedPref에 저장하고,
                pref= getSharedPreferences("USER_INFO", Activity.MODE_PRIVATE);
                editor = pref.edit();
                editor.putString("json_info", response);
                editor.commit();

                //3-2)객체로 파싱해서 활용한다.
                Gson gson = new Gson();
                ToServerJSON json_message = gson.fromJson(response, ToServerJSON.class);

                //유저정보를 json으로 가져온다.
                String server_nickname = json_message.getServer_nickname();
                String user_image = json_message.getServer_image();
                String user_coin = json_message.getServer_coin();
                String user_study_day = json_message.getServer_study_day();

                //뷰에 반영한다.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getApplicationContext())
                                .load(user_image)
                                .into(img_profile);
                        text_nickname.setText(server_nickname);
                        text_coin.setText("보유 코인 : " + user_coin);
                        text_my_progress.setText("나의 진도 : Day" + user_study_day);
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
                        Intent drawer_profile = new Intent(getApplicationContext(), DrawerProfile.class);
                        startActivity(drawer_profile);
                        overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
                        break;
                    case R.id.drawer_score:
                        Intent drawer_score = new Intent(getApplicationContext(), DrawerScore.class);
                        startActivity(drawer_score);
                        overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
                        break;
                    case R.id.drawer_teacher:
                        Intent drawer_teacher = new Intent(getApplicationContext(), DrawerTeacher.class);
                        startActivity(drawer_teacher);
                        overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
                        break;

                    case R.id.drawer_translate:
                        Intent drawer_translate = new Intent(getApplicationContext(), DrawerTranslate.class);
                        startActivity(drawer_translate);
                        overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
                        break;

                    case R.id.drawer_coin:
                        Intent drawer_coin = new Intent(getApplicationContext(), DrawerCoin.class);
                        startActivity(drawer_coin);
                        overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
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
                    case R.id.nav_bottom_home:
                        Log.d(TAG, "onNavigationItemSelected: home");
                        Intent goHome = new Intent(getApplicationContext(), MainHome.class);
                        startActivity(goHome);
                        finish();
                        return true;

                    case R.id.nav_bottom_translate:
                        Log.d(TAG, "onNavigationItemSelected: nav_bottom_translate");
                        Intent goTranslate = new Intent(getApplicationContext(), MainTranslate.class);
                        startActivity(goTranslate);
                        finish();
                        return true;

                    case R.id.nav_bottom_chat:
                        Log.d(TAG, "onNavigationItemSelected: nav_bottom_chat");
                        Intent goChat = new Intent(getApplicationContext(), MainChat.class);
                        startActivity(goChat);
                        finish();
                        return true;

                    case R.id.nav_bottom_streaming:
                        Intent goStreaming = new Intent(getApplicationContext(), MainStream.class);
                        startActivity(goStreaming);
                        finish();
                        return true;
                }

                return false;
            }

        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_quiz_start:
                Log.d(TAG, "onClick:btn_quiz_start_click");
                Intent goHome = new Intent(getApplicationContext(), Quiz.class);
                //user_id를 전달하면 메인홈에서 바로 SELECT문으로 회원정보 가져올 것이다.
                startActivity(goHome);
                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);

                break;

            default:
                break;
        }
    }
}