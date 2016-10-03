package com.app.the.bunker.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    public static final int NO_ERROR = 0;
    public static final int DOWNLOAD_ERROR = 100;

    public static int downloadImage(Context context, String imageURL){

        HttpURLConnection urlConnection;
        InputStream is;
        Bitmap image;

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
                return NO_ERROR;
            } else {
                Log.w(TAG, "HTTP Request is not OK");
                return DOWNLOAD_ERROR;
            }

        } catch (MalformedURLException e){
            Log.w(TAG, "URL malformed");
            e.printStackTrace();
            return DOWNLOAD_ERROR;
        } catch (IOException e){
            Log.w(TAG, "IO Exception");
            e.printStackTrace();
            return DOWNLOAD_ERROR;
        }

    }

    private static void saveImage(Context context, Bitmap image, String imageName) throws IOException {

        FileOutputStream fos = null;

        File directory = context.getDir("images",Context.MODE_PRIVATE);
        File imagePath = new File(directory, imageName);

        try {
            fos = new FileOutputStream(imagePath);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } finally {
            try{
                if (fos != null) fos.close();
            } catch (IOException e){
                e.printStackTrace();
            }
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
        } finally {
            try{
                if (fis != null) fis.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        return image;

    }

    public static String getIconName(String iconPath){
        return iconPath.substring(iconPath.lastIndexOf("/")+1, iconPath.length());
    }

}
