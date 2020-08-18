package com.techtown.ainglish.singleton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.techtown.ainglish.MainHome;
import com.techtown.ainglish.R;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class SingletonHttp {
    private static final String TAG = "SingletonHttpLog";

    static RequestQueue requestQueue;
    Map<String,String> params = new HashMap<String,String>();
    Activity context;

    String http_result = "";


    Button button;

    String Where = "";

    //String my_domain = "youngchanserver.tk";
    String my_domain = "54.180.150.113";


    public SingletonHttp(Activity context) {
        this.context = context;
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
    }

    public String getHttpResult(){
        return http_result;
    }

    //파라미터가 있다면 파라미터를 넣으면 makeRequest()에서 보내게 된다.
    public void putParams(Map<String, String> params){
        this.params = params;
    }


    public void setButton(Button button){
        this.button = button;
    }
    public void setWhere(String Where){
        this.Where = Where;
    }



    /**
     * 서버에서 sms번호를 갖고오기 위한 메소드
     * onResponse에 result에 값을 갖고 있다.
     */
    public void makeRequest(String uri) {

        Log.d(TAG, "makeRequest: 시작점");
        String my_domain = "youngchanserver.tk";
        String url = "https://"+my_domain+ uri;



        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        http_result = response;
                        Log.d(TAG, "httpRequest: return값 test01" + response);

                        if(Where.equals("Specific") && http_result.equals("user_info_create_success")){

                            setIntent();
                            dismissProgressDialogue();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //dismissProgressDialogue();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
    }




    /**
     * 채팅을 위한 메소드. 반환 값은 JSON형태이다.
     * 이미지가 돌아오면 ~하게 처리
     * @param uri
     */
    public void makeRequestForChat(String uri) {

        Log.d(TAG, "makeRequest: 시작점");


        String url = "https://"+my_domain+ uri;

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        http_result = response;
                        Log.d(TAG, "httpRequest: 돌아오는 값 :" + response);


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "httpRequest: 에러값 :" + error);

                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            Log.d(TAG, "onErrorResponse: " + jsonError);
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
    }





    ProgressDialog progressDialog;

    /**
     * 파일이 업로드 되는 동안 다이얼로그 창을 띄우고, 없애기 위한 메소드.
     */
    public void showProgressDialogue(){

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Uploading, please wait...");
        progressDialog.show();
    }
    public void dismissProgressDialogue(){
        progressDialog.dismiss();
    }

    public void setIntent(){
        Intent goHome = new Intent(context, MainHome.class);
        //회원가입이 완료되면 이전에 있던 1-4까지 ACTIVITY는 사라져야한다.
        goHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(goHome);
        context.finish();
        context.overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
    }
}
