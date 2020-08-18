package com.techtown.ainglish;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.google.gson.Gson;
import com.techtown.ainglish.Adapter.ChatAdapter;
import com.techtown.ainglish.JSON.ChatAllMessageJSON;
import com.techtown.ainglish.JSON.ChatMessageJSON;
import com.techtown.ainglish.JSON.TeacherInfoJSON;
import com.techtown.ainglish.activity.AcitivityBlank;
import com.techtown.ainglish.customView.PermissionUtils;
import com.techtown.ainglish.singleton.SingletonHttp;
import com.techtown.ainglish.singleton.SingletonNewHttp;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/*
* 소켓의 역할은 Process간의 data교환이다.
* => 보내고, 받는 Input/Output Stream을 정의해야한다.
*
* * 안드로이드에서 네트워크 관련 동작은 항상 스레드에서 처리하였다.
* 서버와 통신시 Json형태로 데이터를 주고받았다.


1. 서버에서 접속되는 로직
1-1. ConnectionThread 에서 처음 서버와 Socket연결한다.
 Socket에 처음 연결시 user_id를 보낸다. 서버는 user_id를 바탕으로 소켓을 생성한다.

1-2. MessageFromServer는 서버에서 들어오는 데이터다.
 서버에서 들어오는 데이터는 json형태의 메시지로, 아답터에 바로 add한 뒤에 notify하였다.

1-3. MessageToServer은 내가 서버에게 보내는 데이터이다.
  json형태로 보냈으며, 보내기 전에 @user_id있는지 @message인지 정의했다.



2. 채팅데이터 Adapter의 메소드 로직
2-1 getItemViewType(int position)
2-2 onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
2-3 onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position)

 */
public class Chatting extends AppCompatActivity {

    private static final String TAG = "AChatTestLog";

    // 서버 접속 여부를 판별하기 위한 변수
    boolean isConnect = false;

    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;



    //내가 보낼 메시지내용과 보내는 버튼
    EditText edit_message_mine;
    Button btn_send_stream;
    ImageButton btn_send_img;


    //채팅목록창을 위한 리싸이클러뷰이다.
    RecyclerView recycler_chat;
    ChatAdapter chat_adapter;

    ConnectionThread conn_thread;

    // 서버와 연결되어있는 소켓 객체
    Socket member_socket;
    String ip_address = "192.168.0.21";
    int port_number = 30000;

    //서버와 통신하기 위한 객체
    Gson gson = new Gson();




    //사진, 갤러리에서 오는 이미지data를 bitmap으로 변환시키기 위한 객체.
    // 서버로 String으로 변환시켜 보낸다. encodedImage는 String형태이다.
    //사진 파일 이름을 구분하지 않는 이유는 서버에서 구분할 것이기 때문이다.
    File camera_file;
    public static final String FILE_NAME = "send_image.jpg";
    Bitmap bitmap_profile;
    String encodedImage;
    //Gallery에서 가져오는 이미지를 담는 Stream
    InputStream profile_img_data;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_chat_test);

        //chatRoom들어올때 처리
        Intent intent = getIntent();
        teacher_info = intent.getStringExtra("teacher_info");
        user_info = intent.getStringExtra("user_info");
        position = intent.getStringExtra("position");
        user_profile = intent.getStringExtra("user_profile");
        user_nickname = intent.getStringExtra("user_nickname");
        //먼저 리스너 아답터에서 데이터를 받아온다.
        //데이터 저장 성공시에 서버에 전달한다.
        chat_load_listener = new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponseAllChatData" + response);
                //아답터에 데이터 넣기
                ChatAllMessageJSON chat_all_data = gson.fromJson(response, ChatAllMessageJSON.class);
                chat_adapter.setItems(chat_all_data.chat_all_json);

                //응답이 오면 UI반영 변경!!!!!!!
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chat_adapter.notifyDataSetChanged();
                        recycler_chat.scrollToPosition(recycler_chat.getAdapter().getItemCount() -1);
                    }
                });
            }
        };

        //학생, 선생과의 데이터가 기준이다.
        useChatLoadRequest(teacher_info, user_info);

        //리스너 그룹 설정창이다. initView()보다 먼저 설정해야한다.
        buttonListener();

        initView();

        //서버에 접속시도하는 쓰레드 객체
        conn_thread = new ConnectionThread();
        conn_thread.start();

    }


    public void initView(){
        edit_message_mine = findViewById(R.id.edit_message_mine);

        btn_send_stream = findViewById(R.id.btn_send_stream);
        btn_send_stream.setOnClickListener(btn_send_stream_listener);

        btn_send_img = findViewById(R.id.btn_send_img);
        btn_send_img.setOnClickListener(btn_send_img_listener);

        //리싸이클러뷰관련 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // 1.레이아웃 설정
        recycler_chat = findViewById(R.id.recycler_chat);
        recycler_chat.setLayoutManager(layoutManager);

        // 2.아답터 결합
        chat_adapter = new ChatAdapter(this);
        recycler_chat.setAdapter(chat_adapter);

    }



    View.OnClickListener btn_send_stream_listener;
    View.OnClickListener btn_send_img_listener;
    public void buttonListener(){
        btn_send_stream_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSendMessage(v);
            }
        };

        btn_send_img_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogueGetProfile();
            }
        };
    }


    /**
     * 처음 서버와 연결하는 객체. 서버통신이라 쓰레드로 구성
     * 연결 및 관련 객체 생성
     */
    class ConnectionThread extends Thread {

        // OutoutStream객체는 socket객체와 연결되어서 실제 출력을 진행한다.
        // DataOutputStream보조객체는 OutputStream을 위해 버퍼, 기능만 제공한다.
        OutputStream output_stream;
        DataOutputStream data_output_stream;

        MessageFromServer message_from_server;

        @Override
        public void run() {
            try {
                //서버와 클라이언트의 연결 고리가 되는 메소드
                member_socket = new Socket(ip_address, port_number);

                //맨 처음 보내는 데이터 => 사용자 식별용
                output_stream = member_socket.getOutputStream();
                data_output_stream = new DataOutputStream(output_stream);

                //sendMessage()에 기본 format 정의해놨다.
                String string_from_json = sendMessage();

                // 서버에 실제로 write
                data_output_stream.writeUTF(string_from_json);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        edit_message_mine.setText("");
                        edit_message_mine.setHint("메세지 입력");
                        btn_send_stream.setText("전송");

                        // 접속 상태를 true로 셋팅한다.
                        isConnect=true;

                        //MessageFromServer로 서버에서 지속적으로 StreamData를 받는 객체 생성한다.
                        message_from_server = new MessageFromServer(member_socket);
                        message_from_server.start();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //접속시 처음 보내는 메시지
    public String sendMessage(){
        //처음 접속할 때 보내야 할 식별정도
        ChatMessageJSON json_format_message = new ChatMessageJSON();
        //누가 접속했는지 식별하기 위한 것
        json_format_message.setUser_info(user_info);
        json_format_message.setTeacher_info(teacher_info);
        json_format_message.setPosition(position);

        return gson.toJson(json_format_message);
    }

    //is_picture에 TRUE이면 메시지가 사진의 URL을 담고 있고, FALSE이면 메시지이다.
    public String sendMessage(String message_to_server, String is_picture){
        //서버에 보낼 때는 Json형태로 보낸다.
        ChatMessageJSON json_format_message = new ChatMessageJSON();

        json_format_message.setUser_info(user_info);
        json_format_message.setTeacher_info(teacher_info);
        json_format_message.setPosition(position);

        json_format_message.setUser_nickname(position);
        json_format_message.setUser_nickname(user_nickname);
        //사용자 프로필이미지
        json_format_message.setUser_profile(user_profile);

        json_format_message.setIs_picture(is_picture);
        json_format_message.setUser_message(message_to_server);


        //보내는 현재 시간을 구한다.
        SimpleDateFormat date_format = new SimpleDateFormat("hh:mm a");
        json_format_message.setUser_chat_time(date_format.format(System.currentTimeMillis()));

        return gson.toJson(json_format_message);
    }

    /**
     * 버튼을 클릭했을 때 메시지가 전달되는 메소드
     * 관련 객체 MessageToServer
     */
    public void btnSendMessage(View v) {
        //접속되었을 때 처리하는 부분
        if(isConnect){
            // 입력한 문자열을 가져온다.
            Log.d(TAG, "btnSendMessage: 버튼 클릭됨");
            String message_to_server=edit_message_mine.getText().toString();


            //message_to_server가 핵심 내용, is_picture은 FALSE이다.
            String json_to_server = sendMessage(message_to_server, "FALSE");

            //데이터 저장 성공시에 서버에 전달한다.
            chat_save_listener = new Response.Listener<String>(){
                @Override
                public void onResponse(String response) {
                    //서버에게 보낼 객체를 생성하였다.
                    // 네트워크통신은 thread로 돌렸기 때문에 매번 새로 생성한다.
                    MessageToServer send_message = new MessageToServer(member_socket, message_to_server, "FALSE");
                    send_message.start();

                }
            };

            useChatSaveRequest(json_to_server);
        }
    }



    /**
     * 서버로부터 지속적으로 데이터를 수신받아서 화면에 출력해주는 쓰레드
     *
     */
    class MessageFromServer extends Thread {
        //서버에서 정보를 받기 위한 객체
        Socket socket;
        InputStream input_stream;
        DataInputStream data_input_stream;

        public MessageFromServer(Socket socket) {
            try {
                this.socket = socket;
                input_stream = socket.getInputStream();
                data_input_stream = new DataInputStream(input_stream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            try{
                while (isConnect){
                    // 서버로부터 데이터를 수신받는 것을 처리한다.
                    final String message_from_server = data_input_stream.readUTF();
                    Log.d(TAG, "MessageFromServer run: " + message_from_server);

                    //json형태의 데이터는 메시지로 아답터의 데이터셋에 바로 추가한다.
                    //아답터에서 받은 정보를 바탕으로 메시지인지, 내가 보낸 메시지인지 파악한다.
                    ChatMessageJSON json_message = gson.fromJson(message_from_server, ChatMessageJSON.class);
                    chat_adapter.addItem(json_message);
                    Log.d(TAG, "MessageFromServer addItem 데이터추가됨: " + message_from_server);


                    //아답터 변경이 화면에 반영되어야 한다.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chat_adapter.notifyDataSetChanged();
                            recycler_chat.scrollToPosition(recycler_chat.getAdapter().getItemCount() -1);
                            Log.d(TAG, "MessageFromServer addItem 갱신됨: " + message_from_server);
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    /**
     * 데이터를 보낸다 - 관련 메소드 sendMessage();
     * MessageFromServer에서 데이터를 받는다.
     *
     */

    class MessageToServer extends Thread{

        Socket socket;
        OutputStream output_stream;
        DataOutputStream data_output_stream;
        String message_to_server;
        String is_picture;

        //메시지를 보낼 때마다 매번 새로 생성한다.
        //사진인지 아닌지 식별해서 보낸다.
        public MessageToServer(Socket socket, String message_to_server, String is_picture){
            try{
                this.socket=socket;
                this.message_to_server = message_to_server;
                this.is_picture = is_picture;
                output_stream = socket.getOutputStream();
                data_output_stream=new DataOutputStream(output_stream);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

                Log.d(TAG, "sendMessageRun: " + message_to_server);

                //서버네 json형태로 보낸다.
                String json_to_server = sendMessage(message_to_server, is_picture);

                try {
                    data_output_stream.writeUTF(json_to_server);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        edit_message_mine.setText("");
                    }
                });
        }

    }

    @Override
    protected void onDestroy() {
        try{
            //conn_thread.message_from_server.data_input_stream = null;
            member_socket.close();
            isConnect=false;
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }


    /**
     * 이미지를 보내는 Logic
     * 1. dialogueGetProfile() - 앨범에서 혹은 사진에서 이미지를 가져오면 uri값이 반환된다.
     * 1-1) getCameraFile(), startCamera()
     * 1-2) startGalleryChooser()
     * 2. onActivityResult() - URI를 Bitmap으로 변환
     * 3. sendStringImg() -  bitmap을 string형태로 변환
     * 4. useSingletonHttp() - 이미지 저장 DB서버에게 보낸다. 성공시에 채팅 서버에 메시지 보낸다.
     * MessageFromServer에서 데이터를 받는다.
     */
    //1.앨번에서 이미지를 가져오는 로직
    void dialogueGetProfile()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("사진 고르기 선택");
        builder.setMessage("어디서 사진을 가져오시겠습니까?");

        builder.setPositiveButton("카메라",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startCamera();
                    }
                });
        builder.setNegativeButton("앨범",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startGalleryChooser();
                    }
                });
        builder.show();
    }


    public File getCameraFile() {
        File storageDir = getApplicationContext().getFilesDir();
        return new File(storageDir, FILE_NAME);
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (camera_file == null) {
                camera_file = getCameraFile();
            }
            Uri photoUri = FileProvider.getUriForFile(this, "org.techtown.english.fileprovider", camera_file);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);

        }
    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }


    //2. 반환되는 URI를 string으로 변환시키는 로직
    // sendStringImg
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult : 사진 데이터 들어옴");
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {

            Uri fileUri = data.getData();
            ContentResolver resolver = getContentResolver();

            try {
                profile_img_data = resolver.openInputStream(fileUri);
                //uri를 바탕으로 bitmap으로 변환한다.
                bitmap_profile = BitmapFactory.decodeStream(profile_img_data);
                Log.d(TAG, "onActivityResult: Gallery 이미지 확인" + bitmap_profile);
                sendStringImg(bitmap_profile);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            bitmap_profile = BitmapFactory.decodeFile(camera_file.getAbsolutePath());
            sendStringImg(bitmap_profile);
        }
    }

    public void sendStringImg(Bitmap bitmap_profile){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap_profile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.d(TAG, "onActivityResult: CAMERA 이미지 String 확인 :" + encodedImage);

        //DB서버에 이미지를 보낸다. return값은 저장경로
        useSingletonHttp(encodedImage);
    }

    //저장경로를 메시지로 보낸다. 그러면 glide에서 해당 경로로 이미지 반환
    public void useSingletonHttp(String encodedImage){

        Response.Listener<String> response_listener =  new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {


                //이미지가 저장된 경로를 반환한다.
                Log.d(TAG, "onResponseImg" + response);

                String picture_path = response;
                //이미지가 저장되었다는 메시지가 온다.
                String json_to_server = sendMessage(response, "TRUE");

                //데이터 저장 성공시에 서버에 전달한다.
                chat_save_listener = new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {
                        MessageToServer send_message = new MessageToServer(member_socket, picture_path, "TRUE");
                        send_message.start();
                    }
                };

                useChatSaveRequest(json_to_server);



            }
        };

        //서버에 보낼 파라미터를 설정한다.
        Map<String,String> params = new HashMap<String,String>();
        params.put("send_chat_img", encodedImage);
        SingletonNewHttp.getInstance().putParams(params);
        //return값은 저장경로.
        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.CHATTING_SEND_IMG);
        //성공시 처리한다.
        SingletonNewHttp.getInstance().setHttpProperty(Chatting.this, response_listener);
        SingletonNewHttp.getInstance().makeRequest();

    }


    //채팅 서버에 저장한다 => 그 이후에 보낸다.
    //처음 시작할 때 서버에서 가져와서 아답터로 붙인다.
    Response.Listener<String> chat_save_listener;
    public void useChatSaveRequest(String chat_data){
        //서버에 보낼 파라미터를 설정한다.
        Map<String,String> params = new HashMap<String,String>();
        params.put("chat_data", chat_data);
        SingletonNewHttp.getInstance().putParams(params);
        //서버에 보낼 목적지 URI를 설정한다.
        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.CHATTING_MESSAGE_SAVE);
        //성공시 처리한다.
        SingletonNewHttp.getInstance().setHttpProperty(Chatting.this, chat_save_listener);

        SingletonNewHttp.getInstance().makeRequest();
    }

    //채팅 서버에 저장한다 => 그 이후에 보낸다.
    //처음 시작할 때 서버에서 가져와서 아답터로 붙인다.
    Response.Listener<String> chat_load_listener;
    public void useChatLoadRequest(String teacher_info, String user_info){
        //서버에 보낼 파라미터를 설정한다.
        Map<String,String> params = new HashMap<String,String>();
        params.put("teacher_info", teacher_info);
        params.put("user_info", user_info);
        SingletonNewHttp.getInstance().putParams(params);
        //서버에 보낼 목적지 URI를 설정한다.
        SingletonNewHttp.getInstance().putURI(SingletonNewHttp.CHATTING_MESSAGE_LOAD);
        //성공시 처리한다.
        SingletonNewHttp.getInstance().setHttpProperty(Chatting.this, chat_load_listener);

        SingletonNewHttp.getInstance().makeRequest();
    }

}
