package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dennyy.oldschoolcompanion.R;

import java.util.ArrayList;


public class ImageSliderAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<String> imageList;
    private LayoutInflater inflater;

    public ImageSliderAdapter(Context context, ArrayList<String> list) {
        mContext = context;
        imageList = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        final ViewGroup imageLayout = (ViewGroup) inflater.inflate(R.layout.tt_img_slider, collection, false);
        Glide.with(mContext).load(imageList.get(position)).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                imageLayout.findViewById(R.id.tt_img_load_fail).setVisibility(View.VISIBLE);
                imageLayout.findViewById(R.id.tt_img_loading).setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                imageLayout.findViewById(R.id.tt_img_load_fail).setVisibility(View.GONE);
                imageLayout.findViewById(R.id.tt_img_loading).setVisibility(View.GONE);
                return false;
            }
        }).into((ImageView) imageLayout.findViewById(R.id.imageView));
        collection.addView(imageLayout);
        return imageLayout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}
