package com.techtown.ainglish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

/**
 * 화면이 시작되면 가장 먼저 시작되는 페이지로, 해당 페이지이후
 * @LoginLobby 로 넘어간다.
 *
 */
public class LoginSplash extends AppCompatActivity implements AutoPermissionsListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_splash);
        AutoPermissions.Companion.loadAllPermissions(this, 101);

        //핸들러로 일정시간이 지난 후에 Login.class로 넘어가게 한다.
        Handler splashHandle = new Handler();
        splashHandle.postDelayed(new splashHandler(), 3000);
    }

    private class splashHandler implements Runnable{
        public void run(){

            startActivity(new Intent(getApplication(), LoginLobby.class));

            // 스플래쉬가 끝난 후에는 Activity stack에서 제거, 오른쪽에서 왼쪽으로 나오는 효과 적용
            LoginSplash.this.finish();
            overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
        }
    }

    @Override
    public void onBackPressed() {
        //초반 플래시 화면에서 넘어갈때 뒤로가기 버튼 못누르게 함
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int requestCode, String[] permissions) {
        //Toast.makeText(this, "permissions denied : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGranted(int requestCode, String[] permissions) {
        //Toast.makeText(this, "permissions granted : " + permissions.length, Toast.LENGTH_LONG).show();
    }

}
