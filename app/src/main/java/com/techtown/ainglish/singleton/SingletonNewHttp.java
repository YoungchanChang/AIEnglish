package com.techtown.ainglish.singleton;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.techtown.ainglish.JSON.KakaoPayJSON;

import java.util.HashMap;
import java.util.Map;

public class SingletonNewHttp {

    private static final String TAG = "SingletonHttpLog";

    Activity context;

    //request_queue와 response_listener을 static으로 선언하지 않은 이유는
    //실수로 입력하지 않았을 경우 null값이 됨으로 메소드로 처리하였다.
    RequestQueue requestQueue;
    Response.Listener<String> response_listener;

    //파라미터값들
    Map<String,String> params = new HashMap<String,String>();

    //경로설정하는 객체들
    String my_domain = "https://youngchanserver.tk/";
    String specific_URI = "";
    public static final String URI_REGISTER = "MySQL/USER_Create.php";
    public static final String URI_LOGIN = "MySQL/USER_Login.php";
    public static final String USER_FIND = "MySQL/USER_Find.php";
    public static final String COIN_UPDATE = "MySQL/COIN_Update.php";
    public static final String TEACHER_FIND = "MySQL/TEACHER_Find.php";
    public static final String TEACHER_LOGIN_FIND = "MySQL/TEACHER_Login_Find.php";
    public static final String TEACHER_USER_ADD = "MySQL/TEACHER_USER_Add.php";
    public static final String TEACHER_USER_ADD_STREAM = "MySQL/TEACHER_USER_Add_stream.php";
    public static final String TEACHER_USER_T_FIND = "MySQL/TEACHER_USER_T_Find.php";
    //선생님으로 로그인했을 때 찾는 역할.
    public static final String TEACHER_USER_T_STREAM_FIND = "MySQL/TEACHER_USER_T_stream_Find.php";
    public static final String TEACHER_USER_U_FIND = "MySQL/TEACHER_USER_U_Find.php";
    public static final String TEACHER_USER_U_STREAM_FIND = "MySQL/TEACHER_USER_U_stream_Find.php";


    //이미지 저장후에 저장경로 반환
    public static final String CHATTING_SEND_IMG = "MySQL/CHATTING_send_img.php";

    //채팅메시지 DB에 저장하기
    public static final String CHATTING_MESSAGE_SAVE = "MySQL/CHATTING_message_save.php";
    public static final String CHATTING_MESSAGE_LOAD = "MySQL/CHATTING_message_load.php";

    //OCR데이터 세이브하기
    public static final String OCR_IMG_SAVE = "MySQL/OCR_img_save.php";
    public static final String OCR_DATA_SAVE = "MySQL/OCR_data_save.php";
    public static final String OCR_DATA_LOAD = "MySQL/OCR_data_load.php";


    //퀴즈 세이브
    public static final String SCORE_UPDATE = "MySQL/SCORE_Update.php";
    public static final String SCORE_FIND = "MySQL/SCORE_Find.php";

    public static final String URI_TEST = "test.php";

    private static final SingletonNewHttp ourInstance = new SingletonNewHttp();

    public static SingletonNewHttp getInstance() {
        return ourInstance;
    }

    private SingletonNewHttp() {
    }

    /**
     * Http통신을 위해 반드시 구현되어야 하는 메소드 2개
     * 1.setHttpPropery(Activity, Listener)에서 현재 엑티비티와 응답시에 어떻게 처리할지 명시
     * 2.parameter 있다면 putParams()에 명시
     * 3.URI을 putURI()에 명시해서 경로 설정
     */
    public void setHttpProperty(Activity context, Response.Listener<String> response_listener){
        this.context = context;
        this.response_listener = response_listener;
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
    }
    public void putParams(Map<String, String> params){
        this.params = params;
    }

    public void putURI(String specific_URI){
        this.specific_URI = specific_URI;
    }

    public void makeRequest() {
        Log.d(TAG, "makeRequest: 시작점");
        String url = my_domain+ specific_URI;
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response_listener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

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



    public void kakaoPayRequest(){
        String url = "https://kapi.kakao.com/v1/payment/ready";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response_listener,
                new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("에러처리", "onErrorResponse: " + error.networkResponse.statusCode);
                Log.d(TAG, "onErrorResponse: " + error.networkResponse.data);

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
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();

                String Authorization = "KakaoAK c97bd3133835d57862b2d8add8a76bca";
                params.put("Authorization", Authorization);
                params.put("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

                return params;
            }

        };

        request.setShouldCache(false);
        requestQueue.add(request);
    }


}
