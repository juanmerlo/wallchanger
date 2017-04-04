package com.jmk.wallchanger;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ChangeWallpaperService extends Service {

    TimerTask timerTask;
    Timer timer = null;
    int tiempo;
    Imagen[] imagenes;
    int posicion = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        SharedPreferences prefe = getSharedPreferences("data", Context.MODE_PRIVATE);
        boolean serviceOn = prefe.getBoolean("serviceOn",false);

        if(!serviceOn){
            stopSelf();
        }else {

            timer = new Timer();

            Handler_sqlite helper = new Handler_sqlite(ChangeWallpaperService.this);
            helper.abrir();
            imagenes = null;
            imagenes = helper.dameImagenes();
            helper.close();

            timerTask = new TimerTask() {
                @Override
                public void run() {

                    Log.v("Aplicacion: ", "Servicio activo");

                    WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());

                    try {
                        if (posicion == imagenes.length) posicion = 0;

                        File file = new File(imagenes[posicion].rutaCompletaOriginal);
                        Uri uri = Uri.fromFile(file);

                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
                        myWallpaperManager.setBitmap(bitmap);
                        posicion++;


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            };

            int tiempo = prefe.getInt("tiempo", 60) * 1000;

            timer.scheduleAtFixedRate(timerTask, 0, tiempo);

        }

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        if(timer != null){
            timer.cancel();
            timer.purge();
        }
        super.onDestroy();

    }

    @Nullable
    @Override

    public IBinder onBind(Intent intent) {
        return null;
    }

}
