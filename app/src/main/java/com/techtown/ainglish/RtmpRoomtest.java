package com.techtown.ainglish;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;


import net.ossrs.rtmp.ConnectCheckerRtmp;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class RtmpRoomtest extends AppCompatActivity
        implements ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback, IVLCVout.Callback {

        private static final String TAG = "RtmpRoomLog";
        //내가 보내는 url
        private String my_url_send="rtmp://54.180.150.113/live/";

        //RtmpCamera가 SurfaceViewHolder위에 덧붙여진다.
        private RtmpCamera1 rtmpCamera1;


        //private String[] options = new String[]{":fullscreen"};
//        List<String> options = new ArrayList<>();
        private String my_url_get="rtmp://54.180.150.113/live/";

        //서버에 보낼 메시지의 METADATA들.
        //1.학생과 선생 방의 식별자
        public static String teacher_info;
        public static String user_info;
        //2.학생인지 선생인지는 user_info와 더불어 서버에서  쓰레드 만드는 식별자가 된다.
        public static String position;
        //3.프로필 사진 보내기
        public static String user_profile;
        //4.is_picture=true이면 메시지에 사진데이터이고, false이면 일반 메시지로 해석한다.

        public static String user_nickname;




        //받아오기 위한 라이브러리들
        private String mFilePath;
        private SurfaceView mSurface;
        private SurfaceHolder holder;
        private LibVLC libvlc;
        private MediaPlayer mMediaPlayer = null;
        private int mVideoWidth;
        private int mVideoHeight;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                setContentView(R.layout.activity_rtmp_room);

                //options.add(":fullscreen");


                //Student나 Teacher임에 따라서 시작되는 url이 다르다.
                getIntents();


                Log.d(TAG, "onCreatePosition " + position);
                if(position.equals("teacher")){
                        my_url_get = my_url_get+user_info;;
                        my_url_send = my_url_send + teacher_info;
                        //선생님 id로 stream을 보낸다.
                        Log.d(TAG, "CheckURLGet" + my_url_get);
                        Log.d(TAG, "CheckURLSend" + my_url_send);
                }else if(position.equals("student")){
                        my_url_get = my_url_get+teacher_info;
                        my_url_send = my_url_send + user_info;
                        //학생 id로 스트림을 보낸다.
                        Log.d(TAG, "CheckURLGet" + my_url_get);
                        Log.d(TAG, "CheckURLSend" + my_url_send);
                }

                //화면 전환
                ImageView switchCamera = findViewById(R.id.btn_rotate);
                switchCamera.setOnClickListener(this);

                //내가 보내는 화면이네?
                SurfaceView surface_me = findViewById(R.id.surface_mine);
                rtmpCamera1 = new RtmpCamera1(surface_me, this);
                rtmpCamera1.setReTries(10);


                //surfaceView.getHolder()
                //surfaceView는 Holder가 컨트롤한다.
                /**
                 * camera와 연결시켜준다.
                 * 연관 메소드
                 * surfaceChanged(), surfaceDestroyed(), surfaceCreated()
                 */
                surface_me.getHolder().addCallback(this);


                mFilePath = "rtmp://youngchanserver.tk:1935/live/7";
                Log.d(TAG, "Playing: " + mFilePath);
                mSurface = (SurfaceView) findViewById(R.id.surface_other);
                holder = mSurface.getHolder();





                if (rtmpCamera1.isRecording()
                        || rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
                        rtmpCamera1.startStream(my_url_send);
                }


                //아래 버튼은 동영상 받아오기 위한 버튼

        }

        public void getIntents(){
                //RtmpRoom들어올때 처리
                //포지션에 따라 방값을 달리 준다.
                //선생님이라면 학생의 정보를 get하고,
                //학생이라면 선생님의 정보를 get한다.
                Intent intent = getIntent();
                teacher_info = intent.getStringExtra("teacher_info");
                user_info = intent.getStringExtra("user_info");
                position = intent.getStringExtra("position");
                user_profile = intent.getStringExtra("user_profile");
                user_nickname = intent.getStringExtra("user_nickname");
        }



        @Override
        public void onConnectionSuccessRtmp() {
                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                Toast.makeText(RtmpRoomtest.this, "연결이 성공하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                });
        }


        //RTMP끊겼을 때
        @Override
        public void onConnectionFailedRtmp(final String reason) {
                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                if (rtmpCamera1.reTry(5000, reason)) {
                                        Toast.makeText(RtmpRoomtest.this, "재시도합니다.", Toast.LENGTH_SHORT)
                                                .show();
                                } else {
                                        Toast.makeText(RtmpRoomtest.this, "연결이 실패하였습니다. " + reason, Toast.LENGTH_SHORT)
                                                .show();
                                        rtmpCamera1.stopStream();
                                }
                        }
                });
        }

        //넌 뭔데?
        @Override
        public void onNewBitrateRtmp(long bitrate) {

        }

        //RTMP끊겼을 때
        @Override
        public void onDisconnectRtmp() {
                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                Toast.makeText(RtmpRoomtest.this, "Disconnected", Toast.LENGTH_SHORT).show();
                        }
                });
        }

        @Override
        public void onAuthErrorRtmp() {
                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                Toast.makeText(RtmpRoomtest.this, "Auth error", Toast.LENGTH_SHORT).show();
                        }
                });
        }

        @Override
        public void onAuthSuccessRtmp() {
                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                Toast.makeText(RtmpRoomtest.this, "Auth success", Toast.LENGTH_SHORT).show();
                        }
                });
        }




        //참 좋은 표현이네.
        @Override
        public void onClick(View view) {
                switch (view.getId()) {
                        //start하면 stop Stream으로 바꿔라.

                        //스트리밍중인지.
                        //rtmpCamera1.isStreaming()
                        //rtmpCamera1.stopStream();
                        //

                        case R.id.b_start_stop:

                                //핵심메소드
                                if (rtmpCamera1.isRecording()
                                        || rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
                                        //rtmpCamera1.startStream(etUrl.getText().toString());

                                }

                                //살려두기
                        case R.id.btn_rotate:
                                try {
                                        rtmpCamera1.switchCamera();
                                } catch (CameraOpenException e) {
                                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                break;


                }
        }



        //내 카메라에서 보이는 화면
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                rtmpCamera1.startPreview();
        }

        //내 카메라에서 화면이 없어졌을 때.
        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording()) {
                        rtmpCamera1.stopRecord();

                }
                if (rtmpCamera1.isStreaming()) {
                        rtmpCamera1.stopStream();

                }
                rtmpCamera1.stopPreview();
                //vlcVideoLibrary.stop();
        }






        @Override
        public void onConfigurationChanged(Configuration newConfig) {
                super.onConfigurationChanged(newConfig);
                setSize(mVideoWidth, mVideoHeight);
        }
        @Override
        protected void onResume() {
                super.onResume();
                createPlayer(mFilePath);
        }
        @Override
        protected void onPause() {
                super.onPause();
                releasePlayer();
        }
        @Override
        protected void onDestroy() {
                super.onDestroy();
                releasePlayer();
        }
        /**
         * Used to set size for SurfaceView
         *
         * @param width
         * @param height
         */
        private void setSize(int width, int height) {
                mVideoWidth = width;
                mVideoHeight = height;
                if (mVideoWidth * mVideoHeight <= 1)
                        return;
                if (holder == null || mSurface == null)
                        return;
                int w = getWindow().getDecorView().getWidth();
                int h = getWindow().getDecorView().getHeight();
                boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
                if (w > h && isPortrait || w < h && !isPortrait) {
                        int i = w;
                        w = h;
                        h = i;
                }
                float videoAR = (float) mVideoWidth / (float) mVideoHeight;
                float screenAR = (float) w / (float) h;
                if (screenAR < videoAR)
                        h = (int) (w / videoAR);
                else
                        w = (int) (h * videoAR);
                holder.setFixedSize(mVideoWidth, mVideoHeight);
                ViewGroup.LayoutParams lp = mSurface.getLayoutParams();
                lp.width = w;
                lp.height = h;
                mSurface.setLayoutParams(lp);
                mSurface.invalidate();
        }
        /**
         * Creates MediaPlayer and plays video
         *
         * @param media
         */
        private void createPlayer(String media) {
                releasePlayer();
                try {
                        if (media.length() > 0) {
                                Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                                        0);
                                toast.show();
                        }
                        // Create LibVLC
                        // TODO: make this more robust, and sync with audio demo
                        ArrayList<String> options = new ArrayList<String>();
                        //options.add("--subsdec-encoding <encoding>");
                        options.add("--aout=opensles");
                        options.add("--audio-time-stretch"); // time stretching
                        options.add("-vvv"); // verbosity
                        libvlc = new LibVLC(this, options);
                        holder.setKeepScreenOn(true);
                        // Creating media player
                        mMediaPlayer = new MediaPlayer(libvlc);
                        mMediaPlayer.setEventListener(mPlayerListener);
                        // Seting up video output
                        final IVLCVout vout = mMediaPlayer.getVLCVout();
                        vout.setVideoView(mSurface);
                        //vout.setSubtitlesView(mSurfaceSubtitles);
                        vout.addCallback(this);
                        vout.attachViews();
                        Media m = new Media(libvlc, Uri.parse(media));
                        mMediaPlayer.setMedia(m);
                        mMediaPlayer.play();
                } catch (Exception e) {
                        Toast.makeText(this, "Error in creating player!", Toast
                                .LENGTH_LONG).show();
                }
        }
        private void releasePlayer() {
                if (libvlc == null)
                        return;
                mMediaPlayer.stop();
                final IVLCVout vout = mMediaPlayer.getVLCVout();
                vout.removeCallback(this);
                vout.detachViews();
                holder = null;
                libvlc.release();
                libvlc = null;
                mVideoWidth = 0;
                mVideoHeight = 0;
        }
        /**
         * Registering callbacks
         */
        private MediaPlayer.EventListener mPlayerListener = new RtmpRoomtest.MyPlayerListener(this);
        @Override
        public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
                if (width * height == 0)
                        return;
                // store video size
                mVideoWidth = width;
                mVideoHeight = height;
                setSize(mVideoWidth, mVideoHeight);
        }
        @Override
        public void onSurfacesCreated(IVLCVout vout) {
        }
        @Override
        public void onSurfacesDestroyed(IVLCVout vout) {
        }
        @Override
        public void onHardwareAccelerationError(IVLCVout vlcVout) {
                Log.e(TAG, "Error with hardware acceleration");
                this.releasePlayer();
                Toast.makeText(this, "Error with hardware acceleration", Toast.LENGTH_LONG).show();
        }
        private static class MyPlayerListener implements MediaPlayer.EventListener {
                private WeakReference<RtmpRoomtest> mOwner;
                public MyPlayerListener(RtmpRoomtest owner) {
                        mOwner = new WeakReference<RtmpRoomtest>(owner);
                }
                @Override
                public void onEvent(MediaPlayer.Event event) {
                        RtmpRoomtest player = mOwner.get();
                        switch (event.type) {
                                case MediaPlayer.Event.EndReached:
                                        Log.d(TAG, "MediaPlayerEndReached");
                                        player.releasePlayer();
                                        break;
                                case MediaPlayer.Event.Playing:
                                case MediaPlayer.Event.Paused:
                                case MediaPlayer.Event.Stopped:
                                default:
                                        break;
                        }
                }
        }
}
