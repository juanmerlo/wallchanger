package com.jmk.wallchanger;

/**
 * Created by juanmartin on 17/10/2016.
 */

public class Imagen {

    String rutaCompletaOriginal, rutaCompleta;
    int posicion,id;

    public Imagen(int id, String rutaCompletaOriginal, String rutaCompleta, int posicion){

        this.id = id;
        this.rutaCompletaOriginal = rutaCompletaOriginal;
        this.rutaCompleta = rutaCompleta;
        this.posicion = posicion;

    }

}
