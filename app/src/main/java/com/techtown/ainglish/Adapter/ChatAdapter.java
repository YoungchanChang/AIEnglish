package com.techtown.ainglish.Adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.techtown.ainglish.Chatting;
import com.techtown.ainglish.JSON.ChatMessageJSON;
import com.techtown.ainglish.R;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final String TAG = "ChatAdapterLog";

    //실제로 데이터가 들어가 있는 ArrayList이다.
    //setItems로 외부의 ArrayList와 연동할 수 있따.
    ArrayList<ChatMessageJSON> items = new ArrayList<ChatMessageJSON>();

    //이걸 설정하는 이유? Glide(activity)에 쓰기 위해서
    Chatting activity;

    public ChatAdapter(Chatting activity) {
        this.activity = activity;
    }

    private static final int VIEW_TYPE_ME_MSG = 1;
    private static final int VIEW_TYPE_OTHER_MSG = 2;
    private static final int VIEW_TYPE_ME_IMG = 3;
    private static final int VIEW_TYPE_OTHER_IMG = 4;

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType: " + Chatting.user_info + "확인값 : " + items.get(position).getUser_info());
        //보낸 메시지가 본인이 맞는지 확인
        //1. 메시지가 이미지인지 확인
        if (TextUtils.equals(items.get(position).getUser_info(),
                Chatting.user_info) && TextUtils.equals(items.get(position).getPosition(),
                Chatting.position)) {
            if(items.get(position).getIs_picture() != null && items.get(position).getIs_picture().equals("TRUE")){
                return VIEW_TYPE_ME_IMG;
            }
            return VIEW_TYPE_ME_MSG;
        }else{
            if(items.get(position).getIs_picture() != null && items.get(position).getIs_picture().equals("TRUE")){
                return VIEW_TYPE_OTHER_IMG;
            }
            return VIEW_TYPE_OTHER_MSG;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        Log.d(TAG, "onCreateViewHolder: ");

        View itemView = null;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType){
            case VIEW_TYPE_ME_MSG:
                itemView = inflater.inflate(R.layout.chat_message_text_me, viewGroup, false);
                viewHolder = new MyMsgViewHolder(itemView);
                break;
            case VIEW_TYPE_OTHER_MSG:
                itemView = inflater.inflate(R.layout.chat_message_text_other, viewGroup, false);
                viewHolder = new OtherMsgViewHolder(itemView);
                break;

                //TODO
            case VIEW_TYPE_ME_IMG:
                itemView = inflater.inflate(R.layout.chat_message_img_me, viewGroup, false);
                viewHolder = new MyImgViewHolder(itemView);
                break;
            case VIEW_TYPE_OTHER_IMG:
                itemView = inflater.inflate(R.layout.chat_message_img_other, viewGroup, false);
                viewHolder = new OtherImgViewHolder(itemView);
                break;
        }
        return viewHolder;
    }

    /**
     * NULL값이 떴던 이유? NULL을반환하니깐
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ChatMessageJSON item = items.get(position);
        //본인이 보낸 메시지인지 남이 보낸 메시지인지 확인하는 과정

        Log.d(TAG, "onBindViewHolder: " + Chatting.user_info + "확인값 : " + items.get(position).getUser_info());
        if (TextUtils.equals(items.get(position).getUser_info(),
                Chatting.user_info) && TextUtils.equals(items.get(position).getPosition(),
                Chatting.position)) {
            if(items.get(position).getIs_picture() != null &&
                    items.get(position).getIs_picture().equals("TRUE")){
                MyImgViewHolder my_img_view_holder = (MyImgViewHolder)viewHolder;
                my_img_view_holder.setItem(item);
                return;
            }
            MyMsgViewHolder otherChatViewHolder = (MyMsgViewHolder)viewHolder;
            otherChatViewHolder.setItem(item);
        } else {
            if(items.get(position).getIs_picture() != null &&
                    items.get(position).getIs_picture().equals("TRUE")){
                OtherImgViewHolder other_img_view_holder = (OtherImgViewHolder)viewHolder;
                other_img_view_holder.setItem(item);
                return;
            }
            OtherMsgViewHolder otherChatViewHolder = (OtherMsgViewHolder)viewHolder;
            otherChatViewHolder.setItem(item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * 기본적으로 set으로 item객체를 설정한다. add로 추가하고, get으로 특정 포지션을 가져온다.
     * setItem은 데이터 바꿀 때 사용된다.
     */
    public void setItems(ArrayList<ChatMessageJSON> items) {
        this.items = items;
    }

    public void addItem(ChatMessageJSON item) {
        Log.d(TAG, "addItem: 아이템 추가됨" + item.getUser_info() + "유저 메시지" + item.getUser_message());
        items.add(item);
    }

    public ChatMessageJSON getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, ChatMessageJSON item) {
        items.set(position, item);
    }



    /**
     * 뷰홀더 클래스 묶음.
     * 여기서부터는 뷰홀더가 있다. 데이터와 뷰를 묶어준다.
     *
     * 1. MyMsgViewHolder
     * 2. MyImgViewHolder
     * */

    class MyMsgViewHolder extends RecyclerView.ViewHolder {

        TextView text_chat_me;
        TextView text_time_me;

        public MyMsgViewHolder(View itemView) {
            super(itemView);
            text_chat_me = itemView.findViewById(R.id.text_chat_me);
            text_time_me = itemView.findViewById(R.id.text_time_me);
        }

        public void setItem(ChatMessageJSON item) {
            text_chat_me.setText(item.getUser_message());
            text_time_me.setText(item.getUser_chat_time());
        }
    }

    class OtherMsgViewHolder extends RecyclerView.ViewHolder {

        ImageView image_profile_other;
        TextView text_chat_other;
        TextView text_time_other;
        TextView text_nickname_other;

        public OtherMsgViewHolder(View itemView) {
            super(itemView);
            image_profile_other = itemView.findViewById(R.id.image_profile_other);
            text_chat_other = itemView.findViewById(R.id.text_chat_other);
            text_time_other = itemView.findViewById(R.id.text_time_other);
            text_nickname_other = itemView.findViewById(R.id.text_nickname_other);

        }

        public void setItem(ChatMessageJSON item) {

            Glide.with(activity)
                    .load(item.getUser_profile())
                    .into(image_profile_other);

            text_chat_other.setText(item.getUser_message());
            text_time_other.setText(item.getUser_chat_time());
            text_nickname_other.setText(item.getUser_nickname());
        }
    }


    class MyImgViewHolder extends RecyclerView.ViewHolder {

        TextView text_time_me;

        ImageView img_send_me;

        public MyImgViewHolder(View itemView) {
            super(itemView);

            text_time_me = itemView.findViewById(R.id.text_time_me);
            img_send_me = itemView.findViewById(R.id.img_send_me);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                }
            });
        }

        public void setItem(ChatMessageJSON item) {
            text_time_me.setText(item.getUser_chat_time());
            Glide.with(activity)
                    .load(item.getUser_message())
                    .into(img_send_me);
        }

    }

    class OtherImgViewHolder extends RecyclerView.ViewHolder {

        ImageView img_other_profile;
        ImageView img_other_message;
        TextView text_other_nickname;
        TextView text_other_time;

        public OtherImgViewHolder(View itemView) {
            super(itemView);

            img_other_profile = itemView.findViewById(R.id.img_other_profile);
            img_other_message = itemView.findViewById(R.id.img_other_message);

            text_other_nickname = itemView.findViewById(R.id.text_other_nickname);
            text_other_time = itemView.findViewById(R.id.text_other_time);

        }

        public void setItem(ChatMessageJSON item) {

            Glide.with(activity)
                    .load(item.getUser_profile())
                    .into(img_other_profile);
            Glide.with(activity)
                    .load(item.getUser_message())
                    .into(img_other_message);
            text_other_time.setText(item.getUser_chat_time());
            text_other_nickname.setText(item.getUser_nickname());

        }


    }

}






//class Message {
//
//
//    private String uid;
//    private String message;
//    String profilePic;
//    String nickName;
//    //이미지가 1이면 msg를 image로 해석
//    boolean image;
//    public long timestamp;
//
//    public Message(){}
//
//    public Message(String uid, String message, String profilePic, String nickName, boolean image, long timestamp) {
//        this.uid = uid;
//        this.message = message;
//        this.profilePic = profilePic;
//        this.nickName = nickName;
//        this.image = image;
//        this.timestamp = timestamp;
//    }
//}

/**
 * 채팅데이터 Adapter의 메소드 로직
 * 1번. getItemViewType(int position)
 * ArrayList<Messsage> items에 데이터가 존재한다.
 * position은 ArrayList의 데이터 순번이다.
 * items.get(position).get변수()로 데이터의 상태에 따라서 값을 달리 돌려준다.
 *
 * 2번. onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
 * 받은 데이터 상태에 따라서 (1)Layout과 (2)뷰홀더를 다르게 반환시킨다.
 * 아직 뷰홀더에는 Data가 없다.
 *
 * 3번. onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position)
 * ArrayList<Messsage> items에 있는 실제 데이터와 뷰를 엮는다.
 * getItemViewType(int position)와 유사하지만, 실제 데이터를 엮는다는 점에서 다르다.
 *
 */
//싯팔 Gson으로 보내야되?
//class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//
//    //실제로 데이터가 들어가 있는 ArrayList이다.
//    //setItems로 외부의 ArrayList와 연동할 수 있따.
//    ArrayList<Message> items = new ArrayList<Message>();
//
//    //이걸 설정하는 이유? Glide(activity)에 쓰기 위해서
//    ChatActivity activity;
//
//    //굳이 이지랄 안 해도 되지않냐?
//
//    public ChatAdapter(ChatActivity activity) {
//        this.activity = activity;
//    }
//
//    private static final int VIEW_TYPE_ME_MSG = 1;
//    private static final int VIEW_TYPE_OTHER_MSG = 2;
//    private static final int VIEW_TYPE_ME_IMG = 3;
//    private static final int VIEW_TYPE_OTHER_IMG = 4;
//    final String TAG = "이미지뷰체크";
//
//
//    @Override
//    public int getItemViewType(int position) {
//        if (TextUtils.equals(items.get(position).getUid(),
//                FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//            if(items.get(position).image == false){
//                return VIEW_TYPE_ME_MSG;
//            }else{
//                return VIEW_TYPE_ME_IMG;
//            }
//        } else {
//            if(items.get(position).image == false){
//                return VIEW_TYPE_OTHER_MSG;
//            }else{
//                return VIEW_TYPE_OTHER_IMG;
//            }
//        }
//    }
//
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
//        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
//
//        View itemView = null;
//        RecyclerView.ViewHolder viewHolder = null;
//        switch (viewType){
//            case VIEW_TYPE_ME_MSG:
//                itemView = inflater.inflate(R.layout.activity_chat_msg_me, viewGroup, false);
//                viewHolder = new MyMsgViewHolder(itemView);
//                break;
//            case VIEW_TYPE_ME_IMG:
//                itemView = inflater.inflate(R.layout.activity_chat_image_me, viewGroup, false);
//                viewHolder = new MyImgViewHolder(itemView);
//                break;
//            case VIEW_TYPE_OTHER_MSG:
//                itemView = inflater.inflate(R.layout.activity_chat_msg_other, viewGroup, false);
//                viewHolder = new OtherMsgViewHolder(itemView);
//                break;
//            case VIEW_TYPE_OTHER_IMG:
//                itemView = inflater.inflate(R.layout.activity_chat_image_other, viewGroup, false);
//                viewHolder = new OtherImgViewHolder(itemView);
//
//                break;
//
//        }
//        return viewHolder;
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
//        Message item = items.get(position);
//        //본인이 보낸 메시지인지 남이 보낸 메시지인지 확인하는 과정
//        if (TextUtils.equals(items.get(position).getUid(),
//                FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//
//            //이미지인지 확인하는 과정
//            if(items.get(position).image == false){
//                MyMsgViewHolder otherChatViewHolder = (MyMsgViewHolder)viewHolder;
//                otherChatViewHolder.setItem(item);
//            }else{
//                MyImgViewHolder otherChatViewHolder = (MyImgViewHolder)viewHolder;
//                otherChatViewHolder.setItem(item);
//            }
//
//        } else {
//            if(items.get(position).image == false){
//                OtherMsgViewHolder otherChatViewHolder = (OtherMsgViewHolder)viewHolder;
//                otherChatViewHolder.setItem(item);
//            }else{
//                OtherImgViewHolder otherChatViewHolder = (OtherImgViewHolder)viewHolder;
//                otherChatViewHolder.setItem(item);
//            }
//        }
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return items.size();
//    }
//
//    public void addItem(Message item) {
//        items.add(item);
//    }
//
//    public void setItems(ArrayList<Message> items) {
//        this.items = items;
//    }
//
//    public Message getItem(int position) {
//        return items.get(position);
//    }
//
//    public void setItem(int position, Message item) {
//        items.set(position, item);
//    }
//
//
//
//
//    /**
//     * 뷰홀더 클래스 묶음.
//     * 여기서부터는 뷰홀더가 있다. 데이터와 뷰를 묶어준다.
//     *
//     * 1. MyMsgViewHolder
//     * 2. MyImgViewHolder
//     * 3. OtherMsgViewHolder
//     * 4. OtherImgViewHolder
//     *
//     * */
//    class MyMsgViewHolder extends RecyclerView.ViewHolder {
//        TextView text_name;
//        TextView text_msg;
//        TextView text_time;
//        ImageView image_profile;
//
//        public MyMsgViewHolder(View itemView) {
//            super(itemView);
//            text_time = itemView.findViewById(R.id.text_time);
//            text_name = itemView.findViewById(R.id.text_name);
//            text_msg = itemView.findViewById(R.id.text_msg);
//            image_profile = itemView.findViewById(R.id.image_profile);
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int position = getAdapterPosition();
//                }
//            });
//        }
//
//        public void setItem(Message item) {
//            text_name.setText(item.getMessage());
//            text_time.setText(item.getUid());
//            SimpleDateFormat sfd = new SimpleDateFormat("hh:mm a");
//            String date=sfd.format(item.getTimestamp());
//            text_msg.setText(date);
//            Glide.with(activity)
//                    .load(item.profilePic)
//                    .into(image_profile);
//        }
//
//    }
//
//    class MyImgViewHolder extends RecyclerView.ViewHolder {
//
//        TextView text_time;
//
//        ImageView image_msg;
//
//        public MyImgViewHolder(View itemView) {
//            super(itemView);
//
//            text_time = itemView.findViewById(R.id.text_time);
//            image_msg = itemView.findViewById(R.id.image_msg);
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int position = getAdapterPosition();
//                }
//            });
//        }
//
//        public void setItem(Message item) {
//            SimpleDateFormat sfd = new SimpleDateFormat("hh:mm a");
//            String date=sfd.format(item.getTimestamp());
//            text_time.setText(date);
//            Glide.with(activity)
//                    .load(item.getMessage())
//                    .into(image_msg);
//        }
//
//    }
//
//    class OtherMsgViewHolder extends RecyclerView.ViewHolder {
//        TextView text_name;
//        TextView text_msg;
//        TextView text_time;
//        ImageView image_profile;
//
//        public OtherMsgViewHolder(View itemView) {
//            super(itemView);
//
//            text_time = itemView.findViewById(R.id.text_time);
//            text_name = itemView.findViewById(R.id.text_name);
//            text_msg = itemView.findViewById(R.id.text_msg);
//            image_profile = itemView.findViewById(R.id.image_profile);
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int position = getAdapterPosition();
//                }
//            });
//        }
//
//        public void setItem(Message item) {
//            text_name.setText(item.getMessage());
//            text_time.setText(item.getUid());
//            SimpleDateFormat sfd = new SimpleDateFormat("hh:mm a");
//            String date=sfd.format(item.getTimestamp());
//            text_msg.setText(date);
//            Glide.with(activity)
//                    .load(item.profilePic)
//                    .into(image_profile);
//        }
//
//    }
//
//    class OtherImgViewHolder extends RecyclerView.ViewHolder {
//        TextView text_time;
//
//        ImageView image_msg;
//
//        public OtherImgViewHolder(View itemView) {
//            super(itemView);
//
//            text_time = itemView.findViewById(R.id.text_time);
//            image_msg = itemView.findViewById(R.id.image_msg);
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int position = getAdapterPosition();
//                }
//            });
//        }
//
//        public void setItem(Message item) {
//            SimpleDateFormat sfd = new SimpleDateFormat("hh:mm a");
//            String date=sfd.format(item.getTimestamp());
//            text_time.setText(date);
//            Glide.with(activity)
//                    .load(item.getMessage())
//                    .into(image_msg);
//        }
//
//
//    }
//}