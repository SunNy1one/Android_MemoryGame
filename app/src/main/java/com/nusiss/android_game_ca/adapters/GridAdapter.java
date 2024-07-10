package com.nusiss.android_game_ca.adapters;

import static java.lang.Math.min;

import android.app.Activity;
import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.nusiss.android_game_ca.R;
import java.util.List;


public class GridAdapter extends BaseAdapter {

    private Activity context;
    private List<String> urls;
    private Handler handler = new Handler(Looper.getMainLooper());

    public GridAdapter(Activity context, List<String> urls){
        super();
        this.context = context;
        this.urls = urls;
    }

    @Override
    public int getCount() {
        return urls.size() ;
    }

    @Override
    public Object getItem(int i) {
        return urls.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @androidx.annotation.NonNull
    public View getView(int pos, View view, @NonNull ViewGroup parent){
        if(view == null && context != null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.image, parent, false);
        }
        ImageView imgView = (ImageView) view;

        String imageUrl = urls.get(pos);
        new Thread(() -> {
            try {
              Bitmap bitmap = BitmapFactory.decodeFile(imageUrl);
              handler.post(() -> {
                  imgView.setImageBitmap(bitmap);
              });

            } catch (Exception e){
                e.printStackTrace();
            }
        }).start();

        return imgView;
    }

    public void setUrls(List<String> urls){
        this.urls = urls;
    }
}
