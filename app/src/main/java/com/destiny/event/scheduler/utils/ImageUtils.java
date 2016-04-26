package com.destiny.event.scheduler.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    public static Bitmap downloadImage(Context context, String imageURL){

        HttpURLConnection urlConnection = null;
        InputStream is = null;
        Bitmap image = null;

        try{

            URL url = new URL(imageURL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setAllowUserInteraction(false);
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            int response = urlConnection.getResponseCode();

            if (response == HttpURLConnection.HTTP_OK){
                is = new BufferedInputStream(urlConnection.getInputStream());
                image = BitmapFactory.decodeStream(is);
                String imageName = imageURL.substring(imageURL.lastIndexOf("/")+1, imageURL.length());
                saveImage(context, image, imageName);
            } else {
                Log.w(TAG, "HTTP Request is not OK");
            }

        } catch (MalformedURLException e){
            Log.w(TAG, "URL malformed");
            e.printStackTrace();
        } catch (IOException e){
            Log.w(TAG, "IO Exception");
            e.printStackTrace();
        }

        return image;

    }

    private static void saveImage(Context context, Bitmap image, String imageName) throws IOException {

        FileOutputStream fos = null;

        File directory = context.getDir("images",Context.MODE_PRIVATE);
        File imagePath = new File(directory, imageName);

        try {
            fos = new FileOutputStream(imagePath);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } finally {
            fos.close();
        }

    }

    public static Bitmap loadImage(Context context, String imageName) throws IOException {

        Bitmap image = null;
        FileInputStream fis = null;

        File directory = context.getDir("images",Context.MODE_PRIVATE);
        String path = directory.getAbsolutePath();

        try{
            File f = new File(path, imageName);
            fis = new FileInputStream(f);
            image = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e){
            Log.w(TAG, "Image not found");
            e.printStackTrace();
        } finally {
            if (fis != null) fis.close();
        }

        return image;

    }
}
