package com.techtown.ainglish;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.techtown.ainglish.JSON.OCRDataJSON;
import com.techtown.ainglish.JSON.ToServerJSON;

public class MainTranslateSpecific extends AppCompatActivity {

    TextView text_translated;
    ImageView img_crop_pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_translate_specific);

        img_crop_pic = findViewById(R.id.img_crop_pic);
        text_translated = findViewById(R.id.text_translated);

        Gson gson = new Gson();
        Intent get_intent = getIntent();
        String ocr_info = get_intent.getStringExtra("ocr_info");
        OCRDataJSON shared_user_info = gson.fromJson(ocr_info, OCRDataJSON.class);

        Glide.with(this)
                .load(shared_user_info.getImg_path())
                .into(img_crop_pic);

        text_translated.setText(shared_user_info.getOcr_data());

    }
}
