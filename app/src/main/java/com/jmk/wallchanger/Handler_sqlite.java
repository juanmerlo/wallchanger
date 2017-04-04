package com.jmk.wallchanger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.provider.BaseColumns._ID;


/**
 * Created by juanmartin on 17/10/2016.
 */

public class Handler_sqlite extends SQLiteOpenHelper {

    String nombre;

    public Handler_sqlite(Context context) {

        super(context, context.getResources().getString(R.string.app_name), null, 1);
        nombre = context.getResources().getString(R.string.database);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String query = "CREATE TABLE "+ nombre + " ("+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                " rutaCompletaOriginal TEXT," +
                " rutaCompleta TEXT," +
                " posicion INTEGER);";

        sqLiteDatabase.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ nombre + ";");
        onCreate(sqLiteDatabase);

    }

    public void insertarRegistro(String rutaCompletaOriginal,
                                 String rutaCompleta,
                                 int posicion
    ){

        ContentValues valores = new ContentValues();
        valores.put("rutaCompletaOriginal",rutaCompletaOriginal);
        valores.put("rutaCompleta",rutaCompleta);
        valores.put("posicion",posicion);

        this.getWritableDatabase().insert(nombre,null,valores);

    }

    public void aumentarPosicion(int posicion){
        String query = "UPDATE FROM " + nombre + " WHERE posicion=" + (posicion + 1) + ";";
        this.getWritableDatabase().execSQL(query);

    }

    public void disminuirPosicion(int posicion){
        int nuevaposicion = posicion -1;

        String query = "UPDATE " + nombre + " SET posicion=" + nuevaposicion + " WHERE posicion=" + posicion + ";";
        this.getWritableDatabase().execSQL(query);

    }

    public void borrarRegistro(int posicion){
        String query = "DELETE FROM " + nombre + " WHERE posicion=" + posicion + ";";
        //String query = "DELETE FROM alarmas WHERE 1=1";

        this.getWritableDatabase().execSQL(query);

    }

    public int dameConteoRegistros(){

        String columnas[] = {_ID,"rutaCompletaOriginal","rutaCompleta","posicion"};
        Cursor c = this.getWritableDatabase().query(nombre,columnas,null,null,null,null,null);

        return c.getCount();
    }

    public Imagen[] dameImagenes(){

        String columnas[] = {_ID,"rutaCompletaOriginal","rutaCompleta","posicion"};
        Cursor c = this.getReadableDatabase().query(nombre,columnas,null,null,null,null,null);

        Imagen[] imagenes = new Imagen[c.getCount()];
        int i = 0;

        int id, rCO, rC, p;
        id = c.getColumnIndex(_ID);
        rCO = c.getColumnIndex("rutaCompletaOriginal");
        rC = c.getColumnIndex("rutaCompleta");
        p = c.getColumnIndex("posicion");

        c.moveToFirst();

        while(!c.isAfterLast()){
            Imagen imagen = new Imagen(c.getInt(id),
                    c.getString(rCO),
                    c.getString(rC),
                    c.getInt(p));

            imagenes[i] = imagen;
            i++;
            c.moveToNext();
        }

        return imagenes;
    }

    public void abrir(){
        this.getWritableDatabase();
    }

    public void cerrar(){
        this.close();
    }

}
