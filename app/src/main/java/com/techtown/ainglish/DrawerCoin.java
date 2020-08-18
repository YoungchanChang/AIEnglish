package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.techtown.ainglish.JSON.KakaoPayJSON;
import com.techtown.ainglish.JSON.ToServerJSON;
import com.techtown.ainglish.singleton.SingletonNewHttp;

import java.util.HashMap;
import java.util.Map;

/**
 * 1. 코인채우기. StartActivityForResult로 완성한다. - OK
 * TODO
 * 2. 선생님 선택하기. 선생님 값은 미리 넣어놓는다.
 * 선생님 선택하면 코인 나가게. 프로필수정은 그냥 형식상 넣어놓고. 탈퇴기능 넣어놓는다.
 * SharedPref넣어놓기.
 * 3. openCV활용하기
 * 4. Crop기능 다시 활용하기 이미지 View재정의
 */


public class DrawerCoin extends AppCompatActivity {

    private static final String TAG = "DrawerCoin";

    public static final int REQUEST_KAKAO_PAY = 101;

    Toolbar toolbar_main;


    //결제 관련 버튼과 리스너
    Button btn_value_1000;
    View.OnClickListener btn_value_1000_listener;
    Button btn_value_2000;
    View.OnClickListener btn_value_2000_listener;
    Button btn_value_3000;
    View.OnClickListener btn_value_3000_listener;

    static RequestQueue requestQueue;

    //결제 관련 변수명들.
    String item_name = "아잉글리쉬 코인";
    String total_amount;
    String quantity;

    LinearLayout layout_payment_coin;
    ConstraintLayout layout_payment_before;
    ConstraintLayout layout_payment_end;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_coin);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }
        initView();
    }


    public void initView(){
        toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        btnListener();

        btn_value_1000 = findViewById(R.id.btn_value_1000);
        btn_value_1000.setOnClickListener(btn_value_1000_listener);
        btn_value_2000 = findViewById(R.id.btn_value_2000);
        btn_value_2000.setOnClickListener(btn_value_2000_listener);
        btn_value_3000 = findViewById(R.id.btn_value_3000);
        btn_value_3000.setOnClickListener(btn_value_3000_listener);

        layout_payment_coin = findViewById(R.id.layout_payment_coin);
        layout_payment_before = findViewById(R.id.layout_payment_before);
        layout_payment_end = findViewById(R.id.layout_payment_end);
    }

    public void btnListener(){

        btn_value_1000_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPay("1000", "10");
            }
        };
        btn_value_2000_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPay("2000", "20");
            }
        };
        btn_value_3000_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPay("3000", "30");
            }
        };
    }

    public void sendPay(String total_amount, String quantity){

        this.total_amount = total_amount;
        this.quantity = quantity;

        //성공시 응답처리 정의
        SingletonNewHttp.getInstance().setHttpProperty(DrawerCoin.this, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                Log.d("카카오결과", "onResponse: " + response);
                Gson gson = new Gson();
                KakaoPayJSON pay_info = gson.fromJson(response, KakaoPayJSON.class);
                Log.d(TAG, "onResponse: " + pay_info.getAndroid_app_scheme());
                Log.d(TAG, "onResponse: " + pay_info.getNext_redirect_app_url());
                Log.d(TAG, "onResponse: " + pay_info.getNext_redirect_mobile_url());
                Log.d(TAG, "onResponse: " + pay_info.getTid());
                String url = pay_info.getAndroid_app_scheme();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivityForResult(intent, REQUEST_KAKAO_PAY);

            }
        });


        Map<String, String> params = new HashMap<String, String>();
        params.put("cid", "TC0ONETIME");
        params.put("partner_order_id", "1001");
        params.put("partner_user_id", "test@koitt.com");
        params.put("item_name", item_name);
        params.put("quantity", quantity);
        params.put("total_amount", total_amount);
        params.put("vat_amount", "0");
        params.put("tax_free_amount", "0");
        params.put("approval_url", "https://youngchanserver.tk");
        params.put("fail_url", "https://youngchanserver.tk");
        params.put("cancel_url", "https://youngchanserver.tk");

        SingletonNewHttp.getInstance().putParams(params);
        SingletonNewHttp.getInstance().kakaoPayRequest();
    }

    /**
     * TODO
     * 여기서 카카오 페이의 결과값이 도착한다.
     * resultcode도 확인한다.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_KAKAO_PAY){
            makeCoinRequest();
        }

    }
    public void makeCoinRequest(){

        //SharedPref에서 유저정보를 가져와서 유저에게 전달한다.
        SharedPreferences pref= getSharedPreferences("USER_INFO", Activity.MODE_PRIVATE);
        String info = pref.getString("json_info", "");

        Gson gson = new Gson();
        ToServerJSON shared_user_info = gson.fromJson(info, ToServerJSON.class);
        //

        SingletonNewHttp.getInstance().setHttpProperty(DrawerCoin.this, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "makeCoinRequestResult: " + response);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layout_payment_coin.setVisibility(View.INVISIBLE);
                        layout_payment_before.setVisibility(View.INVISIBLE);
                        layout_payment_end.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.COIN_UPDATE);

        Map<String,String> params = new HashMap<String,String>();
        params.put("user_phone", shared_user_info.getServer_phone());
        params.put("user_coin", quantity);
        SingletonNewHttp.getInstance().putParams(params);
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
