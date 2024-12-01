package com.example.netflixplus.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.netflixplus.R;

public class ImageLoader {
    public static void loadMovieThumbnail(Context context, String thumbnailUrl, ImageView imageView) {
        if (context == null || imageView == null) {
            return;
        }

        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            Glide.with(context)
                    .load(thumbnailUrl)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_thumbnail)
                    .error(R.drawable.placeholder_thumbnail)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_thumbnail);
        }
    }

    public static void loadMovieThumbnail(String thumbnailUrl, ImageView imageView) {
        if (imageView == null) {
            return;
        }

        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            Glide.with(imageView.getContext())
                    .load(thumbnailUrl)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_thumbnail)
                    .error(R.drawable.placeholder_thumbnail)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_thumbnail);
        }
    }
}