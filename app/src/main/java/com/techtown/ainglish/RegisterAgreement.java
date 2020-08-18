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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 회원가입 관련 Activity
 * 이전 Activity - LoginLobby
 * 이후 Activity - RegisterAuthSms
 */

public class RegisterAgreement extends AppCompatActivity {

    Toolbar toolbar_main;

    //동의와 관련된 객체
    Button btn_agreement_all;
    Button btn_agreement_infogather;
    TextView text_agreement_infogather;
    Button btn_agreement_use;
    TextView text_agreement_use;
    Button btn_agreement_guideline;
    TextView text_agreement_guideline;

    //다음 Activity활성화 버튼관련 객체
    Button btn_next;
    MutableLiveData<btnNextAgreement> next_result = new MutableLiveData<>();

    //다이얼로그같은 중복되는 뷰들의 처리를 위한 객체
   SingletonView singleton_view = new SingletonView(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_agreement);

        initView();
    }

    /**
     * 초기화하는 View 모음
     */
    public void initView(){
        //기본 ToolBar초기화
        toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 동의여부 관련 버튼들. 버튼 클릭여부에 따라 색상이 변화된다.
        btn_agreement_all = findViewById(R.id.btn_agreement_all);
        btn_agreement_guideline = findViewById(R.id.btn_auth_confirm);
        text_agreement_guideline = findViewById(R.id.text_agreement_guideline);
        btn_agreement_infogather = findViewById(R.id.btn_agreement_infogather);
        text_agreement_infogather = findViewById(R.id.text_agreement_infogather);
        btn_agreement_use = findViewById(R.id.btn_agreement_use);
        text_agreement_use = findViewById(R.id.text_agreement_use);

        btn_next = findViewById(R.id.btn_next);

        //버튼 기능관련 함수
        btnListener();
    }

    /**
     * 버튼, 텍스트는 리스너가 많으므로 따로 함수화하여 관리
     */
    public void btnListener(){
        btn_agreement_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btn_agreement_all.isSelected()){
                    btn_agreement_all.setSelected(false);
                    btn_agreement_guideline.setSelected(false);
                    btn_agreement_infogather.setSelected(false);
                    btn_agreement_use.setSelected(false);
                    setNextBtn();
                }else{
                    btn_agreement_all.setSelected(true);
                    btn_agreement_guideline.setSelected(true);
                    btn_agreement_infogather.setSelected(true);
                    btn_agreement_use.setSelected(true);
                    setNextBtn();
                }
            }
        });

        btn_agreement_guideline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_agreement_guideline.isSelected()){
                    btn_agreement_all.setSelected(false);
                    btn_agreement_guideline.setSelected(false);
                    setNextBtn();
                }else{
                    btn_agreement_guideline.setSelected(true);
                    setNextBtn();
                }
            }
        });

        text_agreement_guideline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go_content = new Intent(getApplicationContext(), RegisterAgreementContent.class);
                startActivity(go_content);
                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
            }
        });

        btn_agreement_infogather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_agreement_infogather.isSelected()){
                    btn_agreement_all.setSelected(false);
                    btn_agreement_infogather.setSelected(false);
                    setNextBtn();
                }else{
                    btn_agreement_infogather.setSelected(true);
                    setNextBtn();
                }
            }
        });

        text_agreement_infogather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go_content = new Intent(getApplicationContext(), RegisterAgreementContent.class);
                startActivity(go_content);
                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
            }
        });

        btn_agreement_use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_agreement_use.isSelected()){
                    btn_agreement_all.setSelected(false);
                    btn_agreement_use.setSelected(false);
                    setNextBtn();
                }else{
                    btn_agreement_use.setSelected(true);
                    setNextBtn();
                }
            }
        });

        text_agreement_use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go_content = new Intent(getApplicationContext(), RegisterAgreementContent.class);
                startActivity(go_content);
                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
            }
        });


        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goSms = new Intent(getApplicationContext(), RegisterAuthSms.class);
                startActivity(goSms);
                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
            }
        });

        //btn_next를 조건에 맞추어서 활성화시키는 함수
        next_result.observe(this, new Observer<btnNextAgreement>() {
            @Override
            public void onChanged(btnNextAgreement next_result) {
                if(next_result.btn_agreement_infogather
                        && next_result.btn_agreement_use && next_result.btn_agreement_guideline){
                    btn_agreement_all.setSelected(true);
                    btn_next.setEnabled(true);
                }else{
                    btn_agreement_all.setSelected(false);
                    btn_next.setEnabled(false);
                }
            }
        });
    }

    /**
     * 체크되는 버튼 여부에 따라서 버튼 활성화시킬것인지 물어보게 한다.
     * 계속 중복되게 됨으로 함수처리함.
     */
    public void setNextBtn(){
        next_result.setValue(new btnNextAgreement(btn_agreement_use.isSelected(),
                btn_agreement_infogather.isSelected(), btn_agreement_guideline.isSelected()));
    }


    /**
     * 1. 상단 Toolbar에서 뒤로가기 버튼을 눌렀을 때 2. BackPress됬을 때를 위한 메소드
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:  singleton_view.dialogueBack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        singleton_view.dialogueBack();
    }


    /**
     * 다음 버튼 활성화를 위한 Class, MutableLiveData에서 쓰인다.
     */
    class btnNextAgreement{
        Boolean btn_agreement_infogather;
        Boolean btn_agreement_use;
        Boolean btn_agreement_guideline;

        public btnNextAgreement(Boolean btn_agreement_infogather,
                                Boolean btn_agreement_use,Boolean btn_agreement_guideline) {
            this.btn_agreement_infogather = btn_agreement_infogather;
            this.btn_agreement_use = btn_agreement_use;
            this.btn_agreement_guideline = btn_agreement_guideline;
        }
    }


}

