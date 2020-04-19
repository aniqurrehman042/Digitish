package com.example.test_04.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

import javax.annotation.Nullable;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    Bitmap bmImage;
    ImageView imageView;

    public DownloadImageTask(Bitmap bmImage, @Nullable ImageView imageView) {
        this.imageView = imageView;
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage = result;
        if (imageView != null)
            imageView.setImageBitmap(result);
    }
}
