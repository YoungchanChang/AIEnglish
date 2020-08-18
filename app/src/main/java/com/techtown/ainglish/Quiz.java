package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.techtown.ainglish.JSON.ToServerJSON;
import com.techtown.ainglish.singleton.SingletonNewHttp;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 * 단어 -> Contain으로 확인!!!
 * 퀴즈의 전체적인 Logic
 * 0. 유저 정보 셋팅. 몇 일차인지에 따라서 URL을 달리 가져온다.
 * onCreate() - getShared();
 *
 * 1. 사요자 말하기 시작 -> 말하기 끝
 * playAudio() -> recog_listen[onResults()]
 *
 * 2. 사진등장 + 시간 초 등장
 * 3. 음성을 맞게 이야기 하면 내가 말한 문장 등장 + 단어 포함
 * 4. 단어가 포함되어 있는 지 확인
 *
 * 1. 시작하겠습니다 음성이 끝났을 때
 * 2. BackgroundThread로 시간초 다운되는 쓰레드
 * TODO
 * InitListener()에서 음성이 끝났을 때,
 * 이미지 뷰 보여짐 + 카운트다운 + openCV로 물체 인식하여서 배열로 저장해놓고 있음.
 *
 * 카운트 다운 시작하면서 내 말하기 open.
 * 카운트 다운 끝나면 내 말하기 끝내고 내가 말한 문장 보여주기.
 *
 */

//받아오는 정보, 몇일차인지. 끝났을 때 시작한다.
//3 onClick()에 마지막 처리
//2 onResults()에 음성인식 후 처리
public class Quiz extends AppCompatActivity  implements  View.OnClickListener{

    private static final String TAG = "QuizLog";

    int answer_count = 0;

    String day = "1";
    String quiz_seq = "1";

    //퀴즈 URL이 들어 있는 주소
    String AUDIO_URL = "https://youngchanserver.tk/MySQL/quiz_audio/day0"+day+"_"+quiz_seq+".mp3";
    String IMAGE_URL = "https://youngchanserver.tk/MySQL/quiz_image/day0"+day+"_000"+quiz_seq+".jpg";

    //내가 말하는 음성의 시작과 끝나는 대기
    MediaPlayer mediaPlayer;
    MediaPlayer.OnCompletionListener media_listener;

    //눈 앞에 보여지는 화면
    ImageView img_from_server;

    TextView text_quiz_seq; //몇 번째 퀴즈인지 보여주는 텍스트 (1/3 인지, 2/3인지)

    TextView text_my_answer; //내가 말한 문장을 보여주는 뷰
    TextView text_needed_word; //문제를 맞추기 위한 단어를 보여주는 뷰

    TextView text_time_count; //시간초가 실제로 내려가는 것을 보여주는 뷰

    TextView text_answer; //맞았습니다. 틀렸습니다를 보여주는 뷰

    Button btn_next_quiz; //다음 버튼을 보여주는 뷰

    //문제가 전부 끝났을 때 등장하는 뷰
    TextView text_end;
    Button btn_home;


    //openCV관련 라이브러리 처리
    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    private Net net_dnn;
    private static final String[] classNames = {"background",
            "airplane", "bicycle", "bird", "boat",
            "bottle", "bus", "car", "cat", "chair",
            "cow", "diningtable", "dog", "horse",
            "motorbike", "person", "pottedplant",
            "sheep", "sofa", "train", "tvmonitor"};


    //내가 말한 문장과 정답
    ArrayList<String> result_I_say = new ArrayList<>();
    String result_answer[];


    TextView text_core_word;
    TextView text_statement;
    ToServerJSON shared_user_info;


    //핸들러를 쓰는 이유? 문제전부가 끝났을 때 버튼 등장 처리
    Handler handler = new Handler();


    //인식된 텍스트가 위치하는 곳

    final int PERMISSION = 1;
    private Intent recognize_intent;
    private SpeechRecognizer speech_recognized;

    //음성을 말했을 때의 반응처리
    private RecognitionListener recog_listen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        //권한 먼저 설정
        if ( Build.VERSION.SDK_INT >= 23 ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }

        //SharedPref에서 유저 정보 가져오기
        getShared();
        day = shared_user_info.getServer_study_day();
        Log.d(TAG, "onCreate:MyDay" + day + "일차");
        quiz_seq = "1"; //퀴즈의 시작은 1부터이다.

        //퀴즈에 맞춰서 URL을 새로 셋팅한다.
        //AUDIO_URL = "https://youngchanserver.tk/MySQL/quiz_audio/day0"+day+"_"+quiz_seq+".mp3";
        //IMAGE_URL = "https://youngchanserver.tk/MySQL/quiz_image/day0"+day+"_"+quiz_seq+".jpg";
        AUDIO_URL = "https://youngchanserver.tk/MySQL/quiz_audio/day0"+day+"_"+quiz_seq+".mp3";
        IMAGE_URL = "https://youngchanserver.tk/MySQL/quiz_image/day0"+day+"_000"+quiz_seq+".jpg";

        //리스너를 먼저 설정해야된다.
        InitListener();
        //관련 뷰 초기화
        initView();

        //보여지고 안 보여지고의 처리
        setQuizVisible();

        //실제 문제 시작
        Glide.with(this)
                .load(R.drawable.quiz_1)
                .into(img_from_server);

        playAudio(AUDIO_URL);
    }

    public void getShared(){
        Gson gson = new Gson();
        //SharedPref에서 유저정보를 가져와서 유저에게 전달한다.
        SharedPreferences pref= getSharedPreferences("USER_INFO", Activity.MODE_PRIVATE);
        String info = pref.getString("json_info", "");
        //gosn으로 객체 파싱한다.
        shared_user_info = gson.fromJson(info, ToServerJSON.class);
    }


    public void initView(){
        //뷰 초기화
        text_time_count = findViewById(R.id.text_time_count);
        text_quiz_seq = findViewById(R.id.text_quiz_seq);
        btn_next_quiz = findViewById(R.id.btn_next_quiz);
        btn_next_quiz.setOnClickListener(this);
        text_needed_word = findViewById(R.id.text_needed_word);
        text_core_word = findViewById(R.id.text_core_word);
        text_statement = findViewById(R.id.text_statement);
        img_from_server = findViewById(R.id.img_from_server);
        text_my_answer = findViewById(R.id.text_my_answer);
        text_answer = findViewById(R.id.text_answer);
        text_end = findViewById(R.id.text_end);
        btn_home = findViewById(R.id.btn_home);
        btn_home.setOnClickListener(this);

        //음성인식 객체 초기화
        speech_recognized=SpeechRecognizer.createSpeechRecognizer(this);
        speech_recognized.setRecognitionListener(recog_listen);

        //음성인식 하기 설정
        recognize_intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognize_intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        recognize_intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US");

    }





    //5초 카운트다운 쓰레드. 5초가 끝났을 때, 다음 화면으로 넘어간다.
    class BackgroundThread extends Thread {
        //호출한 Activity값 관련 초기화를 위한 객체
        Quiz context;

        //fixed_time_count는 제한시간,
        // count는 1초마다 감소하는 시간, count_for_ui는 사용자 UI에 보여주기 위한 변수
        final int count_fixed = 5;
        int time_count = count_fixed;

        public BackgroundThread(Quiz context) {
            this.context = context;
        }

        public void run() {
            //glide불러오는 시간
            sleep_one_sec();

            //시간이 초과되지 않거나 호출되지 않을 때
            for (int i = 0; i <= count_fixed && !isInterrupted(); i++) {
                //0일때 5, 1일때 4, 2일때 3, 3일때 2, 4일때 1, 5일때 0이 되면서 else로 빠져나가.

                sleep_one_sec();
                time_count--;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(time_count >=0){
                            text_time_count.setText("남은 시간 :" +  time_count );
                        }
                    }
                });

            }


            sleep_one_sec();
            Log.d(TAG, "CheckTheSize:" + result_I_say.size());
            if(result_I_say.size() != 0){
                setTest();
            }else{
                setNoAnswer();
            }





        }

        public void setTest(){

            //끝나고 나서 정답 확인하는 작업
            for(int i = 0; i < result_I_say.size() ; i++){
                //문장을 space 기준으로 정답 단어들로 쪼갠다.
                result_answer = result_I_say.get(i).split(" ");
                Log.d(TAG, "onResultsForIsay " + result_I_say.get(i));
                Log.d(TAG, "onResultsForIsayLength " + result_I_say.size());
            }

            //어떤 단어들이 나와있는지 확인
            for(int i=0; i< result_answer.length; i++){
                Log.d(TAG, "onResultsForIsay:" + i + "번째" + result_answer[i]);
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //1. 말하기 내가 말한 문장에 정답을 셋팅한다.
                    for(int i = 0; i < result_I_say.size() ; i++){
                        text_my_answer.setText(result_I_say.get(i));
                    }

                    //정답이 들어가 있는 배열
                    //array_opencv_answer, result_answer
                    //이중 배열로 검사한다.

                    //정답의 배열만큼 확인한다.
                    int count_answer = 0;


                    //쪼갠 단어들에 대해서 배열에 들어있는지 확인
                    for(int j=0; j<result_answer.length; j++){
                        if(array_opencv_answer.contains(result_answer[j])){
                            Log.d(TAG, "openCV정답에 포함된 단어 :  " + result_answer[j]);
                            count_answer++;
                        }
                    }
                    Log.d(TAG, "runNumber:" + count_answer);

                    //정답의 수[ex)3개]보다  일치되는 단어가 많다면 정답이다???
                    if(array_opencv_answer.size() <= count_answer){
                        text_answer.setText("정답입니다.");
                        answer_count++;
                    }else{
                        text_answer.setText("틀렸습니다.");
                    }

                    //시간초 멈추기
                    //count_thread.interrupt();
                    //정답이 보여진다.
                    setAnswerVisible();

                    int quiz_plus = Integer.parseInt(quiz_seq);
                    quiz_plus++;
                    quiz_seq = Integer.toString(quiz_plus);
                    Log.d(TAG, "onClick11111: " + quiz_plus);


                    if(quiz_seq.equals("4")){
                        btn_next_quiz.setVisibility(View.INVISIBLE);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideAll();
                                //몇 문제 맞았는지 명시하기.
                                text_end.setText("맞은 문제 수 : " + answer_count);
                                btn_home.setVisibility(View.VISIBLE);

                            }
                        }, 3000);
                        return;

                    }

                }
            });
        }

        public void setNoAnswer(){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    text_answer.setText("틀렸습니다.");


                    //시간초 멈추기
                    //count_thread.interrupt();
                    //정답이 보여진다.
                    setAnswerVisible();

                    int quiz_plus = Integer.parseInt(quiz_seq);
                    quiz_plus++;
                    quiz_seq = Integer.toString(quiz_plus);
                    Log.d(TAG, "onClick11111: " + quiz_plus);


                    if(quiz_seq.equals("4")){
                        btn_next_quiz.setVisibility(View.INVISIBLE);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideAll();
                                //몇 문제 맞았는지 명시하기.
                                text_end.setText("맞은 문제 수 : " + answer_count);
                                btn_home.setVisibility(View.VISIBLE);

                            }
                        }, 3000);
                        return;

                    }

                }
            });
        }

        //1초카운트 다운 쓰레드
        public void sleep_one_sec(){
            try {
                Thread.sleep(1000);
            } catch(Exception e) {
                interrupt();
            }
        }


        public void resetActivity(){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("경고");
            builder.setMessage("시간이 초과되었습니다.");
            builder.setPositiveButton("확인",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            builder.show();
        }
    }


    /**
     * 퀴즈 시작시에 View처리
     * 1. 문제를 보여주는 사진 처리
     * 2.
     */
    public void setQuizVisible(){
        //보여줘야 할 것 : 이미지, 몇초인지.
        img_from_server.setVisibility(View.VISIBLE);
        text_time_count.setVisibility(View.VISIBLE);
        text_time_count.setText("");

        text_core_word.setVisibility(View.INVISIBLE); //필요한 핵심 단어써 있는 뷰
        text_statement.setVisibility(View.INVISIBLE); //내가 말한 문장 써 있는 뷰
        text_my_answer.setVisibility(View.INVISIBLE); // result_I_say 참고
        text_my_answer.setText("");
        btn_next_quiz.setVisibility(View.INVISIBLE);
        text_answer.setVisibility(View.INVISIBLE);
        text_needed_word.setVisibility(View.INVISIBLE);
        text_needed_word.setText("");

        list_answer = new HashSet<>();
        array_opencv_answer = new ArrayList<>();
        result_I_say = new ArrayList<>();
    }

    /**
     * 문제 -> 정답
     * 정답시 보여지지 말아야 할 것 : 이미지, 몇 초인지 카운트다운
     */
    public void setAnswerVisible(){
        //이미지와 텍스트
        img_from_server.setVisibility(View.INVISIBLE);

        text_time_count.setVisibility(View.INVISIBLE);

        text_core_word.setVisibility(View.VISIBLE);
        text_statement.setVisibility(View.VISIBLE);
        text_my_answer.setVisibility(View.VISIBLE);
        btn_next_quiz.setVisibility(View.VISIBLE);
        text_answer.setVisibility(View.VISIBLE);
        text_needed_word.setVisibility(View.VISIBLE);
    }


    public void hideAll(){
        img_from_server.setVisibility(View.INVISIBLE);
        text_time_count.setVisibility(View.INVISIBLE);
        text_core_word.setVisibility(View.INVISIBLE);
        text_statement.setVisibility(View.INVISIBLE);
        text_my_answer.setVisibility(View.INVISIBLE);
        btn_next_quiz.setVisibility(View.INVISIBLE);
        text_answer.setVisibility(View.INVISIBLE);
        text_needed_word.setVisibility(View.INVISIBLE);
    }


    Response.Listener<String> end_response_listener;
    public void useSingletonHttp(){
        end_response_listener =  new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {

                Log.d(TAG, "onResponse: " +response);
                //뷰에 반영한다.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent goHome = new Intent(getApplicationContext(), DrawerScore.class);
                        startActivity(goHome);
                        finish();
                        overridePendingTransition(R.anim.rightin_activity, R.anim.not_move_activity);
                    }
                });
            }
        };

        //1. 회원정보 업데이트 2. 점수 업데이트
        //서버에 보낼 파라미터를 설정한다.
        Map<String,String> params = new HashMap<String,String>();
        params.put("user_id", shared_user_info.getServer_id());
        params.put("user_day", shared_user_info.getServer_study_day());
        params.put("user_score", Integer.toString(answer_count));
        SingletonNewHttp.getInstance().putParams(params);

        //업데이트부터 시킨다.
        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.SCORE_UPDATE);
        //성공시 처리한다.
        SingletonNewHttp.getInstance().setHttpProperty(Quiz.this, end_response_listener);

        SingletonNewHttp.getInstance().makeRequest();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_home:
                useSingletonHttp();
                break;

            case R.id.btn_next_quiz:
                AUDIO_URL = "https://youngchanserver.tk/MySQL/quiz_audio/day0"+day+"_"+quiz_seq+".mp3";
                IMAGE_URL = "https://youngchanserver.tk/MySQL/quiz_image/day0"+day+"_000"+quiz_seq+".jpg";
                Log.d(TAG, "onClickAUDIO_URL : " + AUDIO_URL);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onClickSeq" + quiz_seq);
                        setQuizVisible();
                        if(quiz_seq.equals("2")){
                            Glide.with(Quiz.this)
                                    .load(R.drawable.quiz_2)
                                    .into(img_from_server);
                            text_quiz_seq.setText("2 / 3");
                        }else if (quiz_seq.equals("3")){
                            Glide.with(Quiz.this)
                                    .load(R.drawable.quiz_3)
                                    .into(img_from_server);
                            text_quiz_seq.setText("3 / 3");
                        }
                        playAudio(AUDIO_URL);
                    }
                });

                break;

            default:
                break;
        }
    }



    BackgroundThread count_thread;

    public void InitListener(){

        //음성이 끝났을 때 처리한다.
        media_listener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                //사진 받아오기. openCV로 인식해놓기
                Glide.with(Quiz.this)
                        .asBitmap()
                        .load(IMAGE_URL)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                img_from_server.setImageBitmap(resource);

                                //받아온 resource를 Mat으로 변환
                                bitmapToMat(resource);
                                //openCV로 한다.
                                for(int i=0; i<list_answer.size(); i++){
                                    Log.d(TAG, "onResourceReadyList: " +array_opencv_answer.get(i));
                                }

                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });
                count_thread = new BackgroundThread(Quiz.this);
                count_thread.start();
                speech_recognized.startListening(recognize_intent);


            }
        };

        recog_listen = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
//                // 사용자가 말하기 시작할 준비가되면 호출됩니다.
                //Toast.makeText(getApplicationContext(),"말하기를 시작해주세요.",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {
                // 사용자가 말하기 시작했을 때 호출됩니다.
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // 입력받는 소리의 크기를 알려줍니다.
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // 사용자가 말을 시작하고 인식이 된 단어를 buffer에 담습니다.
            }

            @Override
            public void onEndOfSpeech() {
                // 사용자가 말하기를 중지하면 호출됩니다.
                //Toast.makeText(getApplicationContext(),"말하기가 끝났습니다.",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error) {
                // 네트워크 또는 인식 오류가 발생했을 때 호출됩니다.
                String message;

                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        message = "오디오 에러";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        message = "클라이언트 에러";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        message = "퍼미션 없음";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        message = "네트워크 에러";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        message = "네트웍 타임아웃";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        message = "찾을 수 없음";
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        message = "RECOGNIZER가 바쁨";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        message = "서버가 이상함";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        message = "말하는 시간초과";
                        break;
                    default:
                        message = "알 수 없는 오류임";
                        break;
                }

                //Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();

            }


            //내가 말하기가 끝나서 준비됬을 때 호출된다.
            @Override
            public void onResults(Bundle results) {
                // 인식 결과가 준비되면 호출됩니다.
                // 아래 코드는 음성인식된 결과를 ArrayList로 모아옵니다.
                result_I_say = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // 부분 인식 결과를 사용할 수 있을 때 호출됩니다.
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // 향후 이벤트를 추가하기 위해 예약됩니다.
            }
        };
    }


    private void playAudio(String url) {
        killMediaPlayer();
        try {
            mediaPlayer = new MediaPlayer();
            //음성인식이 끝났을 때 처리
            mediaPlayer.setOnCompletionListener(media_listener);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void killMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //BITMAP을 MAT으로 변환시켜주는 MAT
    /**
     * 사진에서 물체를 인식한 단어들을 추출한다.
     * array_opencv_answer ArrayList에 담겨있고,
     * text_needed_word 에 정답을 이어붙여줬다.
     */
    Mat img_mat;
    HashSet<String> list_answer = new HashSet<>();
    ArrayList<String> array_opencv_answer = new ArrayList<>();
    public void bitmapToMat(Bitmap img_bitmap){
        //2. bitmap -> mat
        img_mat = new Mat();
        Bitmap bmp32 = img_bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, img_mat);

        //이미지를 RGB형태로 재설정
        Imgproc.cvtColor(img_mat, img_mat, Imgproc.COLOR_RGBA2RGB);
        Mat image_resized = new Mat(img_mat.rows(), img_mat.cols(), CV_8UC3, new Scalar(255, 255, 255));

        float resizeRatio = resize(img_mat, image_resized, 160);

        //훈련의 가중치를 저장하는 이진 파일
        String pro = copyFile("MobileNetSSD_deploy.prototxt.txt", this);
        //네트워크 구성을 저장하고 있는 텍스트 파일
        String caff = copyFile("MobileNetSSD_deploy.caffemodel", this);
        net_dnn = Dnn.readNetFromCaffe(pro, caff);

        final int IN_WIDTH = 300;
        final int IN_HEIGHT = 300;
        final double MEAN_VAL = 127.5;
        final double IN_SCALE_FACTOR = 0.007843;

        //MAT으로부터 blob객체 생성 blob객체여야 학습할 수 있다.
        Mat blob = Dnn.blobFromImage(image_resized, IN_SCALE_FACTOR,
                //학습과정에서 사용한 입력 영상, 출력 영상의 크기
                new Size(IN_WIDTH, IN_HEIGHT),
                //입력 영상 각 채널에서 뺄 평균값
                new Scalar(MEAN_VAL, MEAN_VAL, MEAN_VAL), /*swapRB*/false, /*crop*/false);
        net_dnn.setInput(blob);
        //학습된 결과를 Mat객체에 반환
        Mat detections = net_dnn.forward();

        //학습 신뢰도의 임계점 설정
        final double THRESHOLD = 0.5;

        int cols = image_resized.cols();
        int rows = image_resized.rows();
        detections = detections.reshape(1, (int)detections.total() / 7);

        //MAT결과를 분석한다.
        for (int i = 0; i < detections.rows(); ++i) {
            double confidence = detections.get(i, 2)[0];
            if (confidence > THRESHOLD) {
                int classId = (int)detections.get(i, 1)[0];

                String label = classNames[classId] + ": " + String.format("%,4.2f", confidence);;
                Log.d(TAG, "showConfidence:" + label);

                String label_save = classNames[classId];
                //HashArrayList를 통해서 중복을 제거한 값을 보여준다.
                list_answer.add(label_save);

            }
        }

        //Hash에서 나온 값을 배열로 재설정한다.
        Iterator<String> iter = list_answer.iterator();
        while(iter.hasNext()){
            array_opencv_answer.add(iter.next());
        }
        for(int i=0; i< array_opencv_answer.size(); i++){
            Log.d(TAG, "openCV의Answer:" + i + "번째" + array_opencv_answer.get(i));
        }
        //정답이 되는 단어들의 텍스트를 이어서 붙인다.
        for(int i = 0; i < array_opencv_answer.size() ; i++){
            text_needed_word.append(array_opencv_answer.get(i) + " ");
        }
    }



    public float resize(Mat img_src, Mat img_resize, int resize_width){

        float scale = resize_width / (float)img_src.cols() ;
        if (img_src.cols() > resize_width) {
            int new_height = Math.round(img_src.rows() * scale);
            Imgproc.resize(img_src, img_resize, new Size(resize_width, new_height));
        }
        else {
            img_resize = img_src;
        }
        return scale;
    }

    private static String copyFile(String filename, Context context) {

        Mat img1 = imread("lenna.bmp", IMREAD_GRAYSCALE);
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = context.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }

        return pathDir;

    }



}
