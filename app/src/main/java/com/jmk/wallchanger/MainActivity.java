package com.jmk.wallchanger;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class MainActivity extends AppCompatActivity implements Serializable{

    ViewPager viewPager;
    CustomSwipeAdapter adapter;

    EditText tiempo;
    ToggleButton iniciarServicio;
    Button agregarImagen;
    ImageButton flecha_izq, flecha_der;
    TextView sinImagenText;

    int tiempo_defecto = 60;
    Intent servicio;
    File raiz;
    Imagen[] imagenes;
    int IMAGE_REQUEST = 1;
    int width, height = 0;
    boolean sin_imagenes = true;
    Menu menu;
    int posicionABorrar = -1;
    //CustomSwipeAdapter contextCSA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializar();
        inicializarFiltros();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.setGroupVisible(R.id.grupo1,false);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.delete){

            Log.v("Aplicacion:","Delete");
            borrarImagenBD(posicionABorrar);
            borrarImagenArchivo(posicionABorrar - 1 );
            inicializarImagenes();
            menu.setGroupVisible(R.id.grupo1,false);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if(posicionABorrar != -1){
            menu.setGroupVisible(R.id.grupo1,false);
            posicionABorrar = -1;
            ((CustomSwipeAdapter) viewPager.getAdapter()).deseleccionar();
        }else{
            super.onBackPressed();
        }
    }

    private void inicializar(){

        inicializarDimensiones();

        tiempo = (EditText) findViewById(R.id.tiempo);
        tiempo.setHint(String.valueOf(tiempo_defecto));

        iniciarServicio = (ToggleButton) findViewById(R.id.iniciarServicio);
        iniciarServicio.setOnClickListener(new BotonServicio());


        flecha_izq = (ImageButton) findViewById(R.id.flecha_izq);
        flecha_der = (ImageButton) findViewById(R.id.flecha_der);

        sinImagenText = (TextView) findViewById(R.id.sinImagenText);

        inicializarImagenes();

        agregarImagen = (Button) findViewById(R.id.agregarImagen);
        agregarImagen.setOnClickListener(new AgregarImagenListener());


        if(isMyServiceRunning(ChangeWallpaperService.class)){

            Log.v("Servicio: ", "Activo");
            iniciarServicio.setChecked(true);
            servicio = new Intent(getApplicationContext(), ChangeWallpaperService.class);

        }else{

            Log.v("Servicio: ", "Inactivo");

        };

        raiz = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/WallChangerTemp");



        Log.v("Aplicacion: ",raiz.getAbsolutePath());

        if(raiz.mkdir()){
            Log.v("Creación de carpeta: ","Completa");
        }else{
            Log.v("Creación de carpeta: ","Ya existe");
        }

        SharedPreferences prefe = getSharedPreferences("data",Context.MODE_PRIVATE);
        int segundos = prefe.getInt("tiempo",60);
        tiempo.setHint("" + segundos);

    }

    private void inicializarDimensiones(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

    }

    private void copyFile(String inputPath, String outputPath) {

        InputStream in = null;
        OutputStream out = null;

        try {

            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath + ".jpg");

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    static public void seleccionarImagen(int posicion, Context context){

        MainActivity main = (MainActivity) context;

        main.menu.setGroupVisible(R.id.grupo1,true);

        main.posicionABorrar = posicion;

        Log.v("Aplicacion:", "Posicion: " + posicion);

    }

    private void borrarImagenBD(int posicion){

        Handler_sqlite helper = new Handler_sqlite(this);
        helper.abrir();
        helper.borrarRegistro(posicion);

        for(int i = posicion + 1;i<=helper.dameConteoRegistros()+1;i++){

            helper.disminuirPosicion(i);
            Log.v("Aplicacion:", "Update:" + i);

        }

        helper.close();

    }

    private void borrarImagenArchivo(int posicion){

        File file = new File(imagenes[posicion].rutaCompletaOriginal);
        file.delete();

        posicionABorrar = -1;

    }

    public void inicializarImagenes(){
        Handler_sqlite helper = new Handler_sqlite(this);
        helper.abrir();
        imagenes = null;
        imagenes = helper.dameImagenes();
        int conteo = helper.dameConteoRegistros();
        helper.close();

        if(conteo > 1) {
            sinImagenText.setText("");
            sin_imagenes = false;
            iniciarServicio.setEnabled(true);
            flecha_izq.setEnabled(true);
            flecha_izq.setAlpha((float)1.0);
            flecha_der.setEnabled(true);
            flecha_der.setAlpha((float)1.0);
        }else{
            sinImagenText.setText("Add two images to start");
            sin_imagenes = true;
            if(isMyServiceRunning(ChangeWallpaperService.class)){
                iniciarServicio.setEnabled(true);

            }else{
                iniciarServicio.setEnabled(false);
            }
            flecha_izq.setEnabled(false);
            flecha_izq.setAlpha((float)0.5);
            flecha_der.setEnabled(false);
            flecha_der.setAlpha((float)0.5);
        }

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        adapter = new CustomSwipeAdapter(this, imagenes,width,height);
        viewPager.setAdapter(adapter);

    }

    private String[] filtrarPaths(){

        String[] paths = new String[imagenes.length];
        for(int i =0;i<imagenes.length;i++){

            paths[i] = imagenes[i].rutaCompletaOriginal;
        }

        return paths;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK){

            Intent cropActivity = new Intent(this,CropActivity.class);
            cropActivity.putExtra("path",getRealPathFromURI(this,data.getData()));
            startActivity(cropActivity);

        }
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private class BotonServicio implements View.OnClickListener{

        @Override
        public void onClick(View view) {

            SharedPreferences preferencias=getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=preferencias.edit();

            if(iniciarServicio.isChecked()){
                servicio = new Intent(getApplicationContext(), ChangeWallpaperService.class);

                if(!tiempo.getText().toString().equals("")){
                    editor.putInt("tiempo", Integer.parseInt(tiempo.getText().toString()));
                }

                editor.putBoolean("serviceOn", true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.SET_WALLPAPER},1);
                }

                startService(servicio); //Iniciar servicio

            }else {
                editor.putBoolean("serviceOn", false);

                if(imagenes.length < 2){
                    iniciarServicio.setEnabled(false);
                }
                stopService(servicio); //Detener servicio
            }

            editor.commit();

        }
    }

    private class AgregarImagenListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);

            }

            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");

            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

            startActivityForResult(chooserIntent,IMAGE_REQUEST);

        }
    }

    //Inicializar Filtros
    public void inicializarFiltros(){
        // Filtro de acciones que serán alertadas
        IntentFilter filter = new IntentFilter("cargar_imagenes");
        // Crear un nuevo ResponseReceiver
        ResponseReceiver receiver = new ResponseReceiver();
        // Registrar el receiver y su filtro
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);
    }

    // Broadcast receiver que recibe las emisiones desde los servicios
    private class ResponseReceiver extends BroadcastReceiver {

        // Sin instancias
        private ResponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent){
            switch (intent.getAction()){
                case "cargar_imagenes":

                    inicializarImagenes();
                    break;

            }
        }


    }


}

