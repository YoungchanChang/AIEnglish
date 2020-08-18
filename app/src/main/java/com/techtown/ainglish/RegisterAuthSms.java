package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.techtown.ainglish.singleton.SingletonHttp;

import java.util.HashMap;
import java.util.Map;

/**
 * 휴대폰 번호 인증 관련 Activity
 * 이전 Activity - RegisterAgreement
 * 이후 Activity - RegisterEsseintialInfo
 * URL대입시에 authURL와 testURL비교!
 */
public class RegisterAuthSms extends AppCompatActivity {

    private static final String TAG = "RegisterAuthSmsLog";

    Toolbar toolbar_main;

    //휴대폰 번호 서버로부터 인증 받기 관련 객체 모음
    ConstraintLayout layout_get_auth;
    EditText edit_phone;
    Button btn_get_auth;

    //휴대폰 인증 번호 입력, 시간 관련 객체 모음
    ConstraintLayout layout_confirm;
    EditText edit_auth_confirm;
    TextView text_auth_time;
    Button btn_auth_confirm;

    //인증의 시간초 카운트다운 쓰레드를 위해 필요한 객체
    Handler handler_count_down;
    BackgroundThread thread_count_down;

    //테스트는 인증번호 값으로 111111의 값을 return한다.
    String authURL = "/smsAuth/send-sms.php";
    String testURL = "/test.php";

    //인증 완료 후 나타나는 레이아웃
    ConstraintLayout layout_auth_success;

    //다음 Activity를 위한 버튼
    Button btn_sms_next;

    //HTTPS통신을 위한 객체
    RequestQueue requestQueue;

    //다이얼로그같은 중복되는 뷰들의 처리를 위한 객체
    SingletonView singleton_view;
    SingletonHttp singleton_http;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_auth_sms);

        initView();

        singleton_view = new SingletonView(this);
        singleton_http = new SingletonHttp(this);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }
    }


    public void initView(){
        toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        layout_confirm = findViewById(R.id.layout_confirm);
        btn_auth_confirm = findViewById(R.id.btn_auth_confirm);
        text_auth_time = findViewById(R.id.text_auth_time);
        edit_auth_confirm = findViewById(R.id.edit_auth_confirm);

        layout_get_auth = findViewById(R.id.layout_get_auth);
        edit_phone = findViewById(R.id.edit_phone);
        btn_get_auth = findViewById(R.id.btn_get_auth);

        layout_auth_success = findViewById(R.id.layout_auth_success);
        btn_sms_next = findViewById(R.id.btn_sms_next);

        handler_count_down = new Handler();
        btnListener();
        initCountThread();
    }

    /**
     * 핸들러에서 시간이 경과됬을 때, Activity의 값을 초기화를 위해서 메소드화함.
     */
    public void initCountThread(){

        thread_count_down = new BackgroundThread(handler_count_down, this);
    }



    public void btnListener(){
        /**
         * 핸드폰 문자 관련 기능
         * 1. 문자가 핸드폰 형식인지 확인
         * 2. 카운트다운 쓰레드 시작
         * 3. 서버에 6자리 난수 Request
         */
        btn_get_auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.핸드폰번호에 맞을 때 쓰레드가 시작된다.
                if(edit_phone.getText().toString().length() < 9){
                    String to  = Integer.toString(edit_phone.getText().toString().length());
                    edit_phone.setError("핸드폰번호 형식이 잘못되었습니다.");
                }else{
                    //뷰관련 처리해야 할 사항들이 많으므로 싱글톤에서 처리하지 않음.
                    //2-1.서버에 요청 -> 2-2중복된 핸드폰 번호가 없을 때 카운트다운 쓰레드 실행
                    requestSMS();
                }

            }
        });

        /**
         * 만약에 인증 번호가 맞다면 (1)쓰레드를 멈추게 한다.
         * (2)layout_auth_success를 보여주고
         * (3)인증관련 2개의 layout을 안 보여주게 한다. layout_get_auth, layout_confirm
         */
        btn_auth_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 응답 결과값 보여줘" + singleton_http.getHttpResult());
                if(edit_auth_confirm.getText().toString().equals(singleton_http.getHttpResult())){
                    thread_count_down.interrupt();
                    btn_sms_next.setEnabled(true);
                    layout_auth_success.setVisibility(View.VISIBLE);
                    layout_get_auth.setVisibility(View.INVISIBLE);
                    layout_confirm.setVisibility(View.INVISIBLE);
                }
            }
        });




        btn_sms_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goEssentialInfo = new Intent(getApplicationContext(), RegisterEssentialInfo.class);
                goEssentialInfo.putExtra("user_phone", edit_phone.getText().toString());
                startActivity(goEssentialInfo);
                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
            }
        });
    }




    /**
     * 관련 메소드 정리
     * 1. 상단 Toolbar에서 뒤로가기 버튼을 눌렀을 때 2. BackPress됬을 때를 위한 메소드
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                singleton_view.dialogueBack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        singleton_view.dialogueBack();
    }


    /**
     * 서버에서 sms번호를 갖고오기 위한 메소드
     * onResponse에 result에 값을 갖고 있다.
     * timeCountStart()에서 실제로 카운트다운과 관련된 뷰 실행한다.
     */
    public void requestSMS(){

        String myDomain = "youngchanserver.tk";
        String uri = "/MySQL/USER_phone_Read.php";
        String url = "https://"+myDomain+ uri;

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: 중복된 값인지 확인" + response);

                        //중복됬다면 SMS받아오는 메서드 실행하지 않는다.
                        if(response.equals("duplicate")){
                            handler_count_down.post(new Runnable() {
                                @Override
                                public void run() {
                                    edit_phone.setError("중복된 값입니다.");
                                }
                            });
                        }else{
                            handler_count_down.post(new Runnable() {
                                @Override
                                public void run() {
                                    //시간초 카운트다운 쓰레드 뷰 관련 실행
                                    timeCountStart();
                                }
                            });

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("duplicate_phone", edit_phone.getText().toString());
                return params;
            }
        };
        request.setShouldCache(false);
        requestQueue.add(request);
    }


    public void timeCountStart(){
        //2-2. 카운트다운 쓰레드 시작 및 관련 뷰 설정
        thread_count_down.start();

        layout_confirm.setVisibility(View.VISIBLE);
        btn_get_auth.setEnabled(false);

        //3. 서버에 6자리 난수 Request
        Map<String,String> params = new HashMap<String,String>();
        params.put("edit_phone_number", edit_phone.getText().toString());
        singleton_http.putParams(params);
        singleton_http.makeRequest(authURL);
    }


    /**
     * 해당 클래스의 역할은 1. 호출한 Activity의 count를 센다.
     * 2. 카운트가 끝나면 초기화한다
     *
     * 로직 설명
     * 1. handler_count_down객체는 호출한 Activity의 시간초인 UI에 영향을 주기 위해서 필요하고,
     * 2. 시간이 초과되었을 때 해당 클래스를 호출한 Activity에 영향을 주기 위해 context선언이 필요하다.
     */
    class BackgroundThread extends Thread {
        //호출한 Activity값 관련 초기화를 위한 객체
        RegisterAuthSms context;
        Handler handler_count_down;

        //fixed_time_count는 제한시간,
        // count는 1초마다 감소하는 시간, count_for_ui는 사용자 UI에 보여주기 위한 변수
        final int count_fixed = 20;
        int count = count_fixed;
        int count_for_ui;
        String text_min, text_sec;



        public BackgroundThread(Handler handler_count_down, RegisterAuthSms context) {
            this.handler_count_down = handler_count_down;
            this.context = context;
        }

        public void run() {
            //시간이 초과되지 않거나 호출되지 않을 때
            for (int i = 0; i < count_fixed && !isInterrupted(); i++) {
                //1초경과후에
                try {
                    Thread.sleep(1000);
                } catch(Exception e) {
                    interrupt();
                }
                count_for_ui = count--;
                int min = count_for_ui / 60;
                int  sec = count_for_ui % 60;
                text_min = (min < 10 ? "0"+min : min+"" );
                text_sec = (sec < 10 ? "0"+sec : sec+"" );
                Log.d(TAG, "run: "+count_for_ui);
                handler_count_down.post(new Runnable() {
                    public void run() {
                        text_auth_time.setText(text_min + ":" +  text_sec );
                    }
                });
                //0초가 되면 화면을 초기화시켜야 한다.
                if(count == 0){
                    handler_count_down.post(new Runnable() {
                        public void run() {
                            resetActivity();
                        }
                    });
                }
            }
        }

        public void resetActivity(){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("경고");
            builder.setMessage("시간이 초과되었습니다.");
            builder.setPositiveButton("확인",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            text_auth_time.setText("");
                            edit_phone.setText("");
                            edit_auth_confirm.setText("");

                            context.initCountThread();
                            context.layout_confirm.setVisibility(View.INVISIBLE);
                            btn_get_auth.setEnabled(true);
                        }
                    });
            builder.show();
        }
    }
}
