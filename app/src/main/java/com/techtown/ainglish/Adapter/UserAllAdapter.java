package com.techtown.ainglish.Adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.techtown.ainglish.JSON.ToServerJSON;
import com.techtown.ainglish.R;

import java.util.ArrayList;

public class UserAllAdapter extends RecyclerView.Adapter<UserAllAdapter.ViewHolderUserAll>
        implements OnUserAllItemClickListener{

    private static final String TAG = "UserAllAdapterLog";

    ArrayList<ToServerJSON> items = new ArrayList<ToServerJSON>();

    OnUserAllItemClickListener listener;

    Activity context;

    public UserAllAdapter(Activity context) {
        this.context = context;
    }

    @NonNull
    @Override
    public UserAllAdapter.ViewHolderUserAll onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.recycler_user_all, viewGroup, false);

        return new UserAllAdapter.ViewHolderUserAll(itemView, this, context);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAllAdapter.ViewHolderUserAll viewHolder, int position) {
        ToServerJSON item = items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(ToServerJSON item) {
        items.add(item);
    }

    public void setItems(ArrayList<ToServerJSON> items) {
        this.items = items;
    }

    public ToServerJSON getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, ToServerJSON item) {
        items.set(position, item);
    }

    public void setOnItemClickListener(OnUserAllItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(UserAllAdapter.ViewHolderUserAll holder, View view, int position) {

        if (listener != null) {
            listener.onItemClick(holder, view, position);
        }

    }




    static public class ViewHolderUserAll extends RecyclerView.ViewHolder {

        Activity context;

        ImageView img_user;
        TextView text_user_nickname;
        TextView text_user_email;
        TextView text_user_study_day;


        Button btn_go_inside;


        public ViewHolderUserAll(View itemView, final OnUserAllItemClickListener listener, Activity context) {
            super(itemView);

            this.context = context;

            img_user = itemView.findViewById(R.id.img_user);
            text_user_nickname = itemView.findViewById(R.id.text_user_nickname);
            text_user_email= itemView.findViewById(R.id.text_user_email);
            text_user_study_day = itemView.findViewById(R.id.text_user_study_day);
            btn_go_inside = itemView.findViewById(R.id.btn_go_inside);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    if (listener != null) {
                        listener.onItemClick(UserAllAdapter.ViewHolderUserAll.this, view, position);
                    }
                }
            });
        }

        public void setItem(ToServerJSON item) {

            Glide.with(context)
                    .load(item.getServer_image())
                    .into(img_user);

            text_user_nickname.setText("닉네임 : " + item.getServer_nickname());
            Log.d(TAG, "setItem: " + item.getServer_nickname());
            text_user_email.setText("이메일 : " + item.getServer_email());
            text_user_study_day.setText("학습일차 : " + item.getServer_study_day()+"day");

        }

    }

}
