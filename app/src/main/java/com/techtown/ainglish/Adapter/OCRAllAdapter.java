package com.techtown.ainglish.Adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.techtown.ainglish.JSON.OCRDataJSON;
import com.techtown.ainglish.R;

import java.util.ArrayList;

public class OCRAllAdapter extends RecyclerView.Adapter<OCRAllAdapter.ViewHolder>
        implements OnOCRAllItemListener{


    ArrayList<OCRDataJSON> items = new ArrayList<OCRDataJSON>();

    OnOCRAllItemListener listener;

    Activity context;

    //반드시 구현되어야 하는 생성자.
    //context가 있어야 glide가 돌아간다.
    public OCRAllAdapter(Activity context) {
        this.context = context;
    }



    @NonNull
    @Override
    public OCRAllAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.recycler_ocr_all, viewGroup, false);

        return new OCRAllAdapter.ViewHolder(itemView, this, context);
    }

    @Override
    public void onBindViewHolder(@NonNull OCRAllAdapter.ViewHolder viewHolder, int position) {
        OCRDataJSON item = items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(OCRDataJSON item) {
        items.add(item);
    }

    public void setItems(ArrayList<OCRDataJSON> items) {
        this.items = items;
    }

    public OCRDataJSON getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, OCRDataJSON item) {
        items.set(position, item);
    }

    public void setOnItemClickListener(OnOCRAllItemListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(OCRAllAdapter.ViewHolder holder, View view, int position) {
        if (listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }




    public class ViewHolder extends RecyclerView.ViewHolder {

        Activity context;

        ImageView img_ocr;
        TextView text_create_time;

        Button btn_content_inside;

        public ViewHolder(View itemView, final OnOCRAllItemListener listener, Activity context) {
            super(itemView);

            this.context = context;

            img_ocr= itemView.findViewById(R.id.img_ocr);
            text_create_time= itemView.findViewById(R.id.text_create_time);
            btn_content_inside = itemView.findViewById(R.id.btn_content_inside);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    if (listener != null) {
                        listener.onItemClick(OCRAllAdapter.ViewHolder.this, view, position);
                    }
                }
            });
        }

        public void setItem(OCRDataJSON item) {

            Glide.with(context)
                    .load(item.getImg_path())
                    .into(img_ocr);

            text_create_time.setText("만든 시간 : " + item.getOcr_create_time());




        }

    }

}
