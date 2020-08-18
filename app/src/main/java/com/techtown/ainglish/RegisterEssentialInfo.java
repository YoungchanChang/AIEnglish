package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 필수정보입력 Activity
 * 이전 Activity - RegisterAuthSms
 * 이후 Activity - RegisterSpecificInfo
 * 
 */

// TODO: 2020-06-10 아이디 autoFocus벗어났을 때 중복되는 것이 없는지 확인 

public class RegisterEssentialInfo extends AppCompatActivity {

    private static final String TAG = "EssentialInfoLog";

    Toolbar toolbar_main;

    //이메일 관련 객체들
    EditText edit_email;
    Button btn_email;

    //패스워드 관련 객체들
    EditText edit_pwd;
    Button btn_pwd;

    //패스워드 확인 관련 객체들
    EditText edit_pwd_confirm;
    Button btn_pwd_confirm;

    //다음 액티비티를 위한 객체
    Button btn_essential_next;
    MutableLiveData<btnNextEssential> next_result = new MutableLiveData<>();;

    //다이얼로그같은 중복되는 뷰들의 처리를 위한 객체
    SingletonView singleton_view = new SingletonView(this);

    //HTTPS통신을 위한 객체
    static RequestQueue requestQueue;
    Handler handler_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_essential_info);
        initView();
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }
        handler_email = new Handler();
    }

    public void initView(){
        toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        edit_email = findViewById(R.id.edit_email);
        btn_email = findViewById(R.id.btn_email);

        edit_pwd = findViewById(R.id.edit_pwd);
        btn_pwd = findViewById(R.id.btn_pwd);

        edit_pwd_confirm = findViewById(R.id.edit_pwd_confirm);
        btn_pwd_confirm = findViewById(R.id.btn_pwd_confirm);

        btn_essential_next = findViewById(R.id.btn_essential_next);
        btnListener();
    }


    public void btnListener(){
        /**
         * 사용자가 이메일 입력창에서 벗어났을 때 정상적인 메일인지 확인하는 메소드
         */
        edit_email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus == false){
                    String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
                    if(!edit_email.getText().toString().matches(emailPattern)){
                        edit_email.setError("정상적인 메일 주소가 아닙니다.");
                        btn_email.setSelected(false);
                        setNextBtn();
                    }else{
                        //서버에 중복된 이메일이 없는지 확인한다.
                        requestEmail();
                    }
                }else{
                    btn_email.setSelected(false);
                    setNextBtn();
                }
            }
        });



        /**
         * 사용자가 패스워드 입력창에서 벗어났을 때 정상적인 패스워드 확인하는 메소드
         */
        edit_pwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus == false){
                    if(!edit_pwd.getText().toString().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$")){
                        edit_pwd.setError("비밀번호 형식이 잘못되었습니다.");
                        btn_pwd.setSelected(false);
                        setNextBtn();
                    }else{
                        btn_pwd.setSelected(true);
                        setNextBtn();
                    }
                }else{
                    btn_pwd.setSelected(false);
                    setNextBtn();
                }
            }
        });

        /**
         * 사용자가 패스워드 확인 입력창에서 벗어났을 때 정상적인 패스워드 확인하는 메소드
         */
        edit_pwd_confirm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus == false){
                    if(!edit_pwd.getText().toString().equals(edit_pwd_confirm.getText().toString())){
                        edit_pwd_confirm.setError("패스워드가 일치하지 않습니다.");
                        btn_pwd_confirm.setSelected(false);
                        setNextBtn();
                    }else{
                        btn_pwd_confirm.setSelected(true);
                        setNextBtn();
                    }
                }
            }
        });


        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!edit_pwd.getText().toString().equals(edit_pwd_confirm.getText().toString())){
                    btn_pwd_confirm.setSelected(false);
                    setNextBtn();
                }else{
                    btn_pwd_confirm.setSelected(true);
                    btn_essential_next.setEnabled(true);
                    setNextBtn();
                }
            }
        };
        edit_pwd_confirm.addTextChangedListener(afterTextChangedListener);

        btn_essential_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //인텐트에 값 넣어야한다. email과 password
                Intent intent = getIntent();
                Intent goSpecificInfo = new Intent(getApplicationContext(), RegisterSpecificInfo.class);
                goSpecificInfo.putExtra("user_phone", intent.getStringExtra("user_phone"));
                goSpecificInfo.putExtra("user_email", edit_email.getText().toString());
                goSpecificInfo.putExtra("user_password", edit_pwd.getText().toString());
                startActivity(goSpecificInfo);
                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
            }
        });


        //btn_next를 조건에 맞추어서 활성화시키는 함수
        next_result.observe(this, new Observer<btnNextEssential>() {
            @Override
            public void onChanged(btnNextEssential next_result) {
                if(next_result.btn_email
                        && next_result.btn_pwd && next_result.btn_pwd_confirm){
                    btn_essential_next.setEnabled(true);
                }else{
                    btn_essential_next.setEnabled(false);
                }
            }
        });
    }




    public void requestEmail(){
        //뷰관련 처리해야 할 사항들이 많으므로 싱글톤에서 처리하지 않음.
        String myDomain = "youngchanserver.tk";
        String uri = "/MySQL/USER_email_Read.php";
        String url = "https://"+myDomain+ uri;

        Log.d(TAG, "email검증 요청이 감");
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: email검증" + response);
                        //중복되면 duplicate를 반환한다. 아닌 경우에는 Selected를 true로 한다.
                        if(response.equals("duplicate")){
                            handler_email.post(new Runnable() {
                                @Override
                                public void run() {
                                    edit_email.setError("중복된 값입니다.");
                                    btn_email.setSelected(false);
                                }
                            });
                        }else{
                            handler_email.post(new Runnable() {
                                @Override
                                public void run() {
                                    btn_email.setSelected(true);
                                    setNextBtn();
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
                params.put("duplicate_email", edit_email.getText().toString());
                return params;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
    }


    /**
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
     * 체크되는 버튼 여부에 따라서 버튼 활성화시킬것인지 물어보게 한다.
     * 계속 중복되게 됨으로 함수처리함.
     */
    public void setNextBtn(){
        next_result.setValue(new btnNextEssential(btn_pwd.isSelected(),
                btn_email.isSelected(), btn_pwd_confirm.isSelected()));
    }

    /**
     * 다음 버튼 활성화를 위한 Class,
     * 관련 객체 - MutableLiveData에서 쓰인다.
     * 관련 메소드 - setNextBtn(); next_result.observe(this, new Observer<btnNextEssential>();
     */
    class btnNextEssential{
        Boolean btn_email;
        Boolean btn_pwd;
        Boolean btn_pwd_confirm;

        public btnNextEssential(Boolean btn_email,
                                Boolean btn_pwd,Boolean btn_pwd_confirm) {
            this.btn_email = btn_email;
            this.btn_pwd = btn_pwd;
            this.btn_pwd_confirm = btn_pwd_confirm;
        }
    }

}

