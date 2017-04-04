package com.jmk.wallchanger;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

/**
 * Created by juanmartin on 13/10/2016.
 */

public class CustomSwipeAdapter extends PagerAdapter {


    private Imagen[] imagenes;

    private Context context;

    private LayoutInflater layoutInflater;

    private int width, height;

    private int posicionSeleccionada = -1;

    private ImageView imagenSeleccionada = null;

    public CustomSwipeAdapter(Context context, Imagen[] imagenes, int width, int height){

        this.context = context;
        this.imagenes = imagenes;
        this.width = width;
        this.height = height * 1 / 2;

    }


    @Override
    public int getCount() {
        return imagenes.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == (LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutInflater.inflate(R.layout.swipe_layout,container,false);
        final ImageView imageView = (ImageView) item_view.findViewById(R.id.image_view);
        TextView textView = (TextView) item_view.findViewById(R.id.image_count);

        File file = new File(imagenes[position].rutaCompletaOriginal);
        Log.v("Aplicacion: ",file.getAbsolutePath());
        Uri uri = Uri.fromFile(file);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(bitmap,
                    width, height);

            imageView.setImageBitmap(ThumbImage);

            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if(imagenSeleccionada != null) {
                        deseleccionar();
                    }

                    imagenSeleccionada = imageView;
                    MainActivity.seleccionarImagen(imagenes[position].posicion, context);
                    imageView.setAlpha((float)0.5);

                    return false;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Log.v("Aplicacion","No esta la imagen");
        }

        textView.setText(imagenes[position].posicion + "/" + getCount());

        container.addView(item_view);

        return item_view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((LinearLayout) object);
    }

    public void deseleccionar(){
        if(imagenSeleccionada != null) {
            imagenSeleccionada.setAlpha((float) 1.0);
        }

        imagenSeleccionada = null;

    }


}
