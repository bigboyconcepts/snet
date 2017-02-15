package org.skynetsoftware.snet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Predrag Čokulov
 */

public class AndroidInternet extends Internet
{
    protected final Context context;

    public AndroidInternet(Context context)
    {
        if(context == null)
            throw new IllegalArgumentException("Context cannot be null");
        this.context = context.getApplicationContext();
    }

    @Override
    protected InputStream rescaleImageIfNecessary(org.skynetsoftware.snet.Request.UploadFile file)
    {
        InputStream boundsStream = null, fullStream = null, fallbackStream = null;
        try
        {
            boundsStream = createInputStreamFromUploadFile(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(boundsStream, null, options);

            int width = options.outWidth;
            int height = options.outHeight;
            int maxSize = file.getMaxImageSize();
            //if any dimension is larger than maxSize, do resize, to avoid loading to large bitmap in memory
            if (width > maxSize || height > maxSize)
            {
                //first scale it down using inSampleSize power of 2
                int inSampleSize = 1;

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= maxSize && (halfWidth / inSampleSize) >= maxSize)
                {
                    inSampleSize *= 2;
                }

                options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inSampleSize = inSampleSize;

                fullStream = createInputStreamFromUploadFile(file);//get the new inputstream

                Bitmap bitmap = BitmapFactory.decodeStream(fullStream, null, options);

                if (bitmap != null)
                {
                    width = bitmap.getWidth();
                    height = bitmap.getHeight();
                    //if any dimension is larger than maxSize, do resize
                    if (width > maxSize || height > maxSize)
                    {
                        //now scale it to target dimension
                        //check which dimension is larger, if they are equal any will do
                        boolean scaleByWidth = width > height;
                        float aspectRatio = scaleByWidth ? (float)width/(float)height : (float)height/(float)width;

                        //scale bitmap keeping aspect ratio
                        int diff = (scaleByWidth ? width : height) - maxSize;
                        width = scaleByWidth ? maxSize : (int) (width - diff / aspectRatio);
                        height = scaleByWidth ? (int) (height - diff / aspectRatio) : maxSize;
                        Bitmap tmp = Bitmap.createScaledBitmap(bitmap, width, height, false);
                        bitmap.recycle();
                        bitmap = tmp;
                    }
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
                    bitmap.recycle();
                    byte[] bitmapData = bos.toByteArray();
                    return new ByteArrayInputStream(bitmapData);
                }
            }
            //since we used input stream above, create new one here
            fallbackStream = createInputStreamFromUploadFile(file);
            return fallbackStream;
        }
        catch (IOException e)
        {
            return null;
        }
        finally
        {
            close(boundsStream, fullStream, fallbackStream);
        }
    }

    @Override
    protected InputStream createInputStreamFromUploadFile(Request.UploadFile file) throws FileNotFoundException
    {
        if (file.getUri().startsWith("content://"))
        {
            return context.getContentResolver().openInputStream(Uri.parse(file.getUri()));
        }
        else
        {
            return super.createInputStreamFromUploadFile(file);
        }
    }

}