package com.techtown.ainglish.Adapter;

import android.app.Activity;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.techtown.ainglish.JSON.TeacherInfoJSON;
import com.techtown.ainglish.R;

import java.util.ArrayList;

public class TeacherUserAdapter extends RecyclerView.Adapter<TeacherUserAdapter.ViewHolderTeacherUser>
        implements OnTeacherUserItemClickListener{

    ArrayList<TeacherInfoJSON> items = new ArrayList<TeacherInfoJSON>();

    OnTeacherUserItemClickListener listener;

    Activity context;

    //반드시 구현되어야 하는 생성자.
    //context가 있어야 glide가 돌아간다.
    public TeacherUserAdapter(Activity context) {
        this.context = context;
    }



    @NonNull
    @Override
    public ViewHolderTeacherUser onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.recycler_teacher_part, viewGroup, false);

        return new ViewHolderTeacherUser(itemView, this, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderTeacherUser viewHolder, int position) {
        TeacherInfoJSON item = items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(TeacherInfoJSON item) {
        items.add(item);
    }

    public void setItems(ArrayList<TeacherInfoJSON> items) {
        this.items = items;
    }

    public TeacherInfoJSON getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, TeacherInfoJSON item) {
        items.set(position, item);
    }

    public void setOnItemClickListener(OnTeacherUserItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(ViewHolderTeacherUser holder, View view, int position) {
        if (listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }




    static public class ViewHolderTeacherUser extends RecyclerView.ViewHolder {

        Activity context;

        ImageView img_teacher;
        TextView text_teacher_nickname;
        TextView text_is_login;


        Button btn_chatting_inside;


        public ViewHolderTeacherUser(View itemView, final OnTeacherUserItemClickListener listener, Activity context) {
            super(itemView);

            this.context = context;

            img_teacher = itemView.findViewById(R.id.img_teacher);
            text_teacher_nickname = itemView.findViewById(R.id.text_teacher_nickname);
            text_is_login = itemView.findViewById(R.id.text_is_login);
            btn_chatting_inside = itemView.findViewById(R.id.btn_chatting_inside);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    if (listener != null) {
                        listener.onItemClick(ViewHolderTeacherUser.this, view, position);
                    }
                }
            });
        }

        public void setItem(TeacherInfoJSON item) {

            Glide.with(context)
                    .load(item.getTeacher_image())
                    .into(img_teacher);

            text_teacher_nickname.setText(item.getTeacher_nickname());
            if(item.getTeacher_login() != null && item.getTeacher_login().equals("YES")){
                text_is_login.setText("현재 접속 중입니다.");
            }else{
                text_is_login.setText("현재 부재중입니다.");
            }
        }

    }

}
