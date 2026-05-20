package com.example.babylove.utils;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class ImageStorageHelper {

    
    public static String saveImageToInternalStorage(Context context, Uri uri, String folderName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File directory = new File(context.getFilesDir(), folderName);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = UUID.randomUUID().toString() + ".jpg";
            File file = new File(directory, fileName);

            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
