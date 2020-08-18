package com.techtown.ainglish.Adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.techtown.ainglish.JSON.ScoreJSON;
import com.techtown.ainglish.R;

import java.util.ArrayList;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ViewHolder>{


    ArrayList<ScoreJSON> items = new ArrayList<ScoreJSON>();


    //반드시 구현되어야 하는 생성자.
    //context가 있어야 glide가 돌아간다.
    public ScoreAdapter() {

    }


    @NonNull
    @Override
    public ScoreAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.recycler_drawer_score, viewGroup, false);

        return new ScoreAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreAdapter.ViewHolder viewHolder, int position) {
        ScoreJSON item = items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(ScoreJSON item) {
        items.add(item);
    }

    public void setItems(ArrayList<ScoreJSON> items) {
        this.items = items;
    }

    public ScoreJSON getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, ScoreJSON item) {
        items.set(position, item);
    }



    public class ViewHolder extends RecyclerView.ViewHolder {


        TextView text_day;
        TextView text_score;


        public ViewHolder(View itemView) {
            super(itemView);

            text_day = itemView.findViewById(R.id.text_day);
            text_score = itemView.findViewById(R.id.text_score);
        }

        public void setItem(ScoreJSON item) {
            text_day.setText("Day " + item.getUser_study_day());
            text_score.setText(item.getUser_score() + "/3");

        }

    }

}
