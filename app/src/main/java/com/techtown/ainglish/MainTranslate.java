package com.techtown.ainglish;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.techtown.ainglish.Adapter.OnOCRAllItemListener;
import com.techtown.ainglish.Adapter.OCRAllAdapter;
import com.techtown.ainglish.JSON.OCRAllDataJSON;
import com.techtown.ainglish.JSON.OCRDataJSON;
import com.techtown.ainglish.JSON.ToServerJSON;
import com.techtown.ainglish.singleton.SingletonNewHttp;

import java.util.HashMap;
import java.util.Map;


/**
 * @LoginbLobby 에서 넘어온 페이지
 * Home화면 역할로 사용자에게 주요 정보를 보여준다.
 */
public class MainTranslate extends AppCompatActivity {


    private static final String TAG = "MainTranslateLog";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_translate);

        //초기 뷰설정관련 함수들이 모여있는 곳.
        initView();

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

        initDataSet();

    }



    //채팅목록창을 위한 리싸이클러뷰이다.
    RecyclerView recycler_translate;
    OCRAllAdapter ocr_all_adapter;

    public void initDataSet(){
        /**
         * 리싸이클러의 구성요소
         * 1. 한 Layout이 차지하는 모양이 어떤지 LinearLayout
         * 2. 리싸이클러뷰에 데이터를 넣으면 View로 변환시켜줄 Adapter
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        // 1.레이아웃 설정
        recycler_translate = findViewById(R.id.recycler_translate);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recycler_translate.setLayoutManager(layoutManager);
        // 2.아답터 결합
        ocr_all_adapter = new OCRAllAdapter(this);
        recycler_translate.setAdapter(ocr_all_adapter);



        ocr_all_adapter.setOnItemClickListener(new OnOCRAllItemListener() {
            @Override
            public void onItemClick(OCRAllAdapter.ViewHolder holder, View view, int position) {

                OCRDataJSON item = ocr_all_adapter.getItem(position);
                Gson gson = new Gson();
                String json_message = gson.toJson(item);

                Intent go_specific = new Intent(MainTranslate.this, MainTranslateSpecific.class);
                go_specific.putExtra("ocr_info", json_message);
                startActivity(go_specific);
                finish();
                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);

            }
        });


        //성공시 응답처리 정의
        SingletonNewHttp.getInstance().setHttpProperty(MainTranslate.this, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                //서버에서 return되는 값
                Log.d(TAG, "onResponseDrawerOCRLog1: " + response);

                Gson gson = new Gson();
                OCRAllDataJSON json_message = gson.fromJson(response, OCRAllDataJSON.class);

                ocr_all_adapter.setItems(json_message.ocr_all_json);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ocr_all_adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        //서버에 보낼 파라미터를 설정한다.
        //유저아이디보내기
        Gson gson = new Gson();

        //SharedPref에서 유저정보를 가져와서 유저에게 전달한다.
        SharedPreferences pref= getSharedPreferences("USER_INFO", Activity.MODE_PRIVATE);
        String info = pref.getString("json_info", "");
        //gosn으로 객체 파싱한다.
        ToServerJSON shared_user_info = gson.fromJson(info, ToServerJSON.class);


        Map<String,String> params = new HashMap<String,String>();
        params.put("user_info", shared_user_info.getServer_id());
        Log.d(TAG, "initDataSetSendID" + shared_user_info.getServer_id());
        SingletonNewHttp.getInstance().putParams(params);
        //서버에 보낼 목적지 URI를 설정한다.
        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.OCR_DATA_LOAD);
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



        //서버에 보낼 파라미터를 설정한다.
        Map<String,String> params = new HashMap<String,String>();
        params.put("user_phone", shared_user_info.getServer_phone());
        SingletonNewHttp.getInstance().putParams(params);
        //서버에 보낼 목적지 URI를 설정한다.
        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.USER_FIND);
        //성공시 처리한다.
        SingletonNewHttp.getInstance().setHttpProperty(MainTranslate.this, response_listener);

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
        bottomNav.setSelectedItemId(R.id.nav_bottom_translate);
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

                String server_nickname = json_message.getServer_nickname();
                String user_image = json_message.getServer_image();
                String user_coin = json_message.getServer_coin();

                //뷰에 반영한다.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getApplicationContext())
                                .load(user_image)
                                .into(img_profile);
                        text_nickname.setText(server_nickname);
                        text_coin.setText("보유 코인 : " + user_coin);

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

}