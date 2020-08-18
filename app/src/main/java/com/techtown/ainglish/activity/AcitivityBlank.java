package com.techtown.ainglish.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.techtown.ainglish.Adapter.ChatAdapter;
import com.techtown.ainglish.DrawerCoin;
import com.techtown.ainglish.DrawerProfile;
import com.techtown.ainglish.DrawerScore;
import com.techtown.ainglish.DrawerTeacher;
import com.techtown.ainglish.JSON.TeacherInfoJSON;
import com.techtown.ainglish.JSON.ToServerJSON;
import com.techtown.ainglish.MainChat;
import com.techtown.ainglish.MainHome;
import com.techtown.ainglish.MainStream;
import com.techtown.ainglish.MainTranslate;
import com.techtown.ainglish.R;
import com.techtown.ainglish.singleton.SingletonNewHttp;

import java.util.HashMap;
import java.util.Map;

/**
 * useSingletonHttp() - 싱글톤 객체 활용하여 서버에 메시지 주고받을 때 쓰는 메소드
 * buttonListener() - 버튼의 리스너를 따로 설정할 때 쓰이는 메소드
 * setProgress() - 진행중입니다 표시 뜰 때 쓰이는 메소드
 * getShared() - SharedPref에서 데이터 가져올 때 쓰이는 메소드
 * setShared() - SharedPref에 데이터 저장할 때 쓰이는 메소드
 *
 * setIntent(), getIntet(), byeIntent() - 인텐트 관련 메소드
 *
 * setHandler() - 최신 핸들러 관련 메소드
 *
 * setToolbar() - 상단에 툴바 설치 관련 메소드
 * setNavigation() - Drawer가 들어가 있는 Toolbar설치
 * setBottomNav() - BottomNav설치
 */

public class AcitivityBlank extends AppCompatActivity {

    private static final String TAG = "MainChatLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "와닝", Toast.LENGTH_SHORT).show();
    }
    //서버에 메시지 보낼때 관련 메소드
    public void useSingletonHttp(){

        Response.Listener<String> response_listener =  new Response.Listener<String>(){
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
        SingletonNewHttp.getInstance().setHttpProperty(AcitivityBlank.this, response_listener);

        SingletonNewHttp.getInstance().makeRequest();

    }


    //버튼 리스너 설정
    public void buttonListener(){
        View.OnClickListener btn_listener;
        btn_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        };

        Button button = findViewById(R.id.btn_pwd_confirm);
        button.setOnClickListener(btn_listener);
    }
    public static final String URI_REGISTER = "MySQL/USER_Create.php";
    public void setGlide(){
        ImageView img_from_server = findViewById(R.id.img_from_server);
                    Glide.with(this)
                    .load(URI_REGISTER)
                    .into(img_from_server);
    }

    //프로그래스바 관련 메소드
    public void setProgress(){
        ProgressDialog  progress_login;
        progress_login = ProgressDialog.show(this, null, "접속중입니다");
        progress_login.dismiss();
    }


    public void getShared(){
        Gson gson = new Gson();

        //SharedPref에서 유저정보를 가져와서 유저에게 전달한다.
        SharedPreferences pref= getSharedPreferences("USER_INFO", Activity.MODE_PRIVATE);
        String info = pref.getString("json_info", "");
        //gosn으로 객체 파싱한다.
        ToServerJSON shared_user_info = gson.fromJson(info, ToServerJSON.class);

    }

    public void setShared(){
        Gson gson = new Gson();

        //객체를 toJson형태로 만든다.
        TeacherInfoJSON json_format_message = new TeacherInfoJSON();
        String to_server_json = gson.toJson(json_format_message);

        //로그인 성공시에 유저 info 저장시에 쓰일 SharedPref
        SharedPreferences pref = getSharedPreferences("USER_INFO", Activity.MODE_PRIVATE);;
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("json_info", to_server_json);
        editor.commit();
    }




    public void getIntet(){
        //유저아이디 바탕으로 DB에서 정보를 추출해온다.
        Intent get_intent = getIntent();
        String user_id = get_intent.getStringExtra("user_id");
        //String to_server_json = gson.toJson(json_format_message);
    }

    public void setIntent(){
        //메인 홈으로 보내기
        Intent goHome = new Intent(getApplicationContext(), MainHome.class);
        //user_id를 전달하면 메인홈에서 바로 SELECT문으로 회원정보 가져올 것이다.
        goHome.putExtra("user_id", "test");
        startActivity(goHome);
        finish();
        overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);

        //모든 ActivityStack제거
        goHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    public void byeIntent(){
        finish();
        overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
    }


    //해당 메소드는 많이 쓰인다.
    public void setHandler(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }





    public void setToolbar(){
        Toolbar toolbar_main;
        toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }


    public void setNavigation(){
        Toolbar toolbar;
        //DrawerLayout와 NavigationView는 한 셋트이다.
        DrawerLayout drawer;
        NavigationView navigationView;
        /**
         * @toolbar 에 drawer를 붙이는 코드.
         * */
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        /**
         * @sideNav 설정. 변경사항은 Listener에서 관리
         * */
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
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
        });
    }

    public void setBottomNav(){
        BottomNavigationView bottomNav;

        bottomNav = findViewById(R.id.bottom_navigaion);
        bottomNav.setSelectedItemId(R.id.nav_bottom_chat);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_bottom_home:
                        Intent goHome = new Intent(getApplicationContext(), MainHome.class);
                        startActivity(goHome);
                        finish();
                        return true;

                    case R.id.nav_bottom_translate:
                        Intent goTranslate = new Intent(getApplicationContext(), MainTranslate.class);
                        startActivity(goTranslate);
                        finish();
                        return true;

                    case R.id.nav_bottom_chat:
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

        });
    }

    /**
     * 리싸이클러뷰
     * 데이터 -> Adapter -> 뷰
     * 1. 데이터
     * 1-1. ArrayList(*)
     * 1-2. *에 들어가는 Class
     *
     * 2. 뷰
     * 2-1. Activity내부에 RecyclerView
     * 2-2. Recycler내부에 뷰 (이 안에 객체들 봐야한다)
     * 2-3. 리싸이클러뷰가 어떤 층으로 쌓일지 결정하는 layoutManager
     *
     */
    public void setRecycler(){
        //채팅목록창을 위한 리싸이클러뷰이다.
        RecyclerView recycler_chat;
        ChatAdapter chat_adapter;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        // 1.레이아웃 설정
        recycler_chat = findViewById(R.id.recycler_chat);
        recycler_chat.setLayoutManager(layoutManager);
        // 2.아답터 결합
        //chat_adapter = new ChatAdapter(getApplicationContext());
        //recycler_chat.setAdapter(chat_adapter);
    }


}
