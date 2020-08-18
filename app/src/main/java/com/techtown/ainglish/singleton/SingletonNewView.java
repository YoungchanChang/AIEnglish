package com.techtown.ainglish.singleton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.techtown.ainglish.R;

public class SingletonNewView {
    private static final SingletonNewView ourInstance = new SingletonNewView();

    public static SingletonNewView getInstance() {
        return ourInstance;
    }

    private SingletonNewView() {
    }

    //일반적으로 많이 쓰이는 다이얼로그이다.
    public static void dialogueBack(Activity context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("뒤로가기 클릭 경고");
        builder.setMessage("뒤로가기 시 작성된 모든 내용은\n저장되지 않습니다. 계속 하시겠습니까?");

        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
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

    /**
     * 다이얼로그를 내 마음대로 custom할 때 쓰이는 메소드, "예"를 클릭하는 경우가 많으므로,
     * "예"인 경우만 처리
     * @param context
     * @param title
     * @param message
     * @param dialogueListener
     */
    public void dialogueCustom(Activity context, String title, String message, DialogInterface.OnClickListener dialogueListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("예", dialogueListener);
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }



    ProgressDialog progressDialog;
    /**
     * 파일이 업로드 되는 동안 다이얼로그 창을 띄우고, 없애기 위한 메소드.
     */
    public void showProgressDialogue(Activity context, String dialougeText){
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(dialougeText);
        progressDialog.show();
    }
    public void dismissProgressDialogue(){
        progressDialog.dismiss();
    }
}
