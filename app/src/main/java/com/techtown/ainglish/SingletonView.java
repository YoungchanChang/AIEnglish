package com.techtown.ainglish;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;


/**
 * 뒤로가기를 묻는 Dialogue는 여러번 중복됨으로 싱글톤으로 따로 묶었다.
 * 진행창이 뜨는 프로그래스바도 넣었다.
 */

public class SingletonView {

    Activity context;


    public SingletonView(Activity context) {
        this.context = context;
    }



    void dialogueBack()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("뒤로가기 클릭 경고");
        builder.setMessage("뒤로가기 시 작성된 모든 내용은\n저장되지 않습니다. 계속 하시겠습니까?");

        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        context.finish();
                        context.overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }



}
