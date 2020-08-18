package com.techtown.ainglish.Adapter;

import android.app.Activity;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.techtown.ainglish.JSON.TeacherInfoJSON;
import com.techtown.ainglish.R;

import java.util.ArrayList;

public class TeacherAllAdapter extends RecyclerView.Adapter<TeacherAllAdapter.ViewHolder>
        implements OnTeacherItemClickListener{

    ArrayList<TeacherInfoJSON> items = new ArrayList<TeacherInfoJSON>();

    OnTeacherItemClickListener listener;

    Activity context;

    //반드시 구현되어야 하는 생성자.
    //context가 있어야 glide가 돌아간다.
    public TeacherAllAdapter(Activity context) {
        this.context = context;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.recycler_teacher_all, viewGroup, false);

        return new ViewHolder(itemView, this, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
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

    public void setOnItemClickListener(OnTeacherItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if (listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }




    public class ViewHolder extends RecyclerView.ViewHolder {

        Activity context;

        ImageView image_profile;
        TextView text_nickname;
        TextView text_brief;
        TextView text_chat;
        TextView text_streaming;
        TextView text_streaming_available;
        TextView text_chat_available;

        public ViewHolder(View itemView, final OnTeacherItemClickListener listener, Activity context) {
            super(itemView);

            this.context = context;

            image_profile = itemView.findViewById(R.id.image_profile);
            text_nickname = itemView.findViewById(R.id.text_nickname);
            text_brief = itemView.findViewById(R.id.text_brief);
            text_chat = itemView.findViewById(R.id.text_chat);
            text_streaming =itemView.findViewById(R.id.text_streaming);
            text_streaming_available = itemView.findViewById(R.id.text_streaming_available);
            text_chat_available = itemView.findViewById(R.id.text_chat_available);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    if (listener != null) {
                        listener.onItemClick(ViewHolder.this, view, position);
                    }
                }
            });
        }

        public void setItem(TeacherInfoJSON item) {

            Glide.with(context)
                    .load(item.getTeacher_image())
                    .into(image_profile);

            text_nickname.setText(item.getTeacher_nickname());
            text_brief.setText(item.getTeacher_brief());

            text_streaming_available.setText(item.getTeacher_streaming());
            if(item.getTeacher_streaming() != null && item.getTeacher_streaming().equals("YES")){
                text_streaming_available.setText("가능");
                text_streaming_available.setTextColor(Color.parseColor("#FFFFFF"));
            }else{
                text_streaming_available.setText("불가능");
            }
            text_chat_available.setText(item.getTeacher_chatting());
            if(item.getTeacher_chatting() != null && item.getTeacher_chatting().equals("YES")){
                text_chat_available.setText("가능");
                text_chat_available.setTextColor(Color.parseColor("#FFFFFF"));
            }else{
                text_chat_available.setText("불가능");
            }

        }

    }

}
