package com.jmk.wallchanger;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;


public class CropActivity extends AppCompatActivity{

    CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        cropImageView = (CropImageView) findViewById(R.id.cropImageView);

        File file = new File(getIntent().getStringExtra("path"));

        Uri uri = Uri.fromFile(file);

        cropImageView.setImageUriAsync(uri);

        cropImageView.setAspectRatio(16, 9);
        cropImageView.setFixedAspectRatio(true);
        cropImageView.setGuidelines(CropImageView.Guidelines.ON_TOUCH);
        cropImageView.setCropShape(CropImageView.CropShape.RECTANGLE);
        cropImageView.setScaleType(CropImageView.ScaleType.FIT_CENTER);
        cropImageView.setAutoZoomEnabled(true);
        cropImageView.setShowProgressBar(true);
        cropImageView.setCropRect(new Rect(0, 0, 800, 500));
        cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {

                Intent localIntent = new Intent("cargar_imagenes");

                // Emitir el intent a la actividad
                LocalBroadcastManager.getInstance(CropActivity.this).sendBroadcast(localIntent);


                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crop_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_action_crop) {

            addImage();

            return true;
        } else if (item.getItemId() == R.id.main_action_rotate) {
            cropImageView.rotateImage(90);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addImage(){

        Handler_sqlite helper = new Handler_sqlite(this);
        int posicion = helper.dameConteoRegistros() + 1;
        String nombreArchivo = cropImageView.getCroppedImage().getHeight() + "x" + cropImageView.getCroppedImage().getWidth() + "_" + cropImageView.getImageUri().getLastPathSegment();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/WallChangerTemp/" + nombreArchivo);
        Uri uri = Uri.fromFile(file);
        cropImageView.saveCroppedImageAsync(uri);

        String rutaCompletaOriginal = uri.getPath();

        String rutaCompleta = "";
        helper.insertarRegistro(rutaCompletaOriginal,rutaCompleta, posicion);
        helper.close();

    }




}