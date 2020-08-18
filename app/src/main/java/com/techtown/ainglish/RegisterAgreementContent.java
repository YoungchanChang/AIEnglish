package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * 사용자 동의약관 상세 페이지
 * 이전 Activity - RegisterAgreemnt
 *
 * 관련 버튼 누르면 finish되서 이전 Activity로 돌아간다.
 * 관련버튼 - 백버튼, 확인버튼, 뒤로가기 버튼기능을 통해 뒤로 감
 *
 */

public class RegisterAgreementContent extends AppCompatActivity {
    Toolbar toolbar_main;
    Button btn_agreement_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_agreement_content);

        initView();
    }

    public void initView(){
        toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //컨텐츠보았을 때 뒤로가기를 누를 수 있게 만드는 버튼
        btn_agreement_content = findViewById(R.id.btn_agreement_content);

        btnListener();
    }


    public void btnListener(){
        btn_agreement_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
            }
        });
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
