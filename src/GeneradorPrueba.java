import java.io.*;
import java.util.*;
import struct.*;

/**
 * Generador de los datos de pruebas
 * @author:
 */
class GeneradorPrueba {
  public static void main(java.lang.String[] args) throws Exception {

    int cantidadDatos[] = new int[] {
        3000, 10000, 50000};
    int porcentaje_cambios[] = new int[] {
        1, 3, 5, 7, 9, 11, 13, 15, 20, 30, 50};
    FileReader f1;
    BufferedReader in;
    FileWriter f2;
    PrintWriter out;
    StringTokenizer str;
    String item;
    int oid, t, x1, y1, x2, y2, x = 0, y = 0, aux = 0, count = 0;
    DObject d = null, d1 = null, e = null, f = null;
    boolean flag = false;
    Vector puntos = new Vector(), cambios;

    KDBTree kdbTree;

    for (int i = 0; i < cantidadDatos.length; i++) {
      System.out.println("\nCantidad datos:" + cantidadDatos[i]);

      for (int j = 0; j < porcentaje_cambios.length; j++) {
        count = 0;
        puntos = new Vector();
        cambios = new Vector();
        //insertar los datos originales en el kdb
        f1 = new FileReader(String.valueOf(cantidadDatos[i]));
        in = new BufferedReader(f1);

        kdbTree = new KDBTree(new Coordinate(100000, 100000),
                              new Coordinate(0, 0));
        f2 = new FileWriter("prueba_" + String.valueOf(cantidadDatos[i]) + "_" +
                            String.valueOf(porcentaje_cambios[j]));
        out = new PrintWriter(f2);

        while (count != cantidadDatos[i] && (item = in.readLine()) != null) {
          count++;
          str = new StringTokenizer(item);
          oid = (int) (Float.parseFloat(str.nextToken()));
          t = (int) (Float.parseFloat(str.nextToken()) * 100);
          x1 = (int) (Float.parseFloat(str.nextToken()) * 100000);
          y1 = (int) (Float.parseFloat(str.nextToken()) * 100000);
          //escribir en el archivo sobre los datos originales
          out.write(oid + " " + (float) 0 / 100 + " " + (float) x1 / 100000 +
                    " " +
                    (float) y1 / 100000 + "\n");
          out.flush();
          d = new DObject(oid, x1, y1, t, 'i');
          puntos.addElement(new DObject(oid, x1, y1, t, 'i'));
          //insertar los objetos leidos en el kdb
          kdbTree.insert(d);
        }
        System.out.println(" " + porcentaje_cambios[j]);
        in.close();
        f1.close();

        //datos * porcentaje% / 100 tiempos

        int contador_cambios_porcadatiempo = cantidadDatos[i] *
            porcentaje_cambios[j] / 100;

        for (int k = 2; k <= 100; k += 2) {
          cambios = new Vector();
          for (int l = 0; l < contador_cambios_porcadatiempo; l++) {
//por cada tiempo, se saca un cierto cantidad
            //de objetos para cambiar su posiscion
            d = (DObject) puntos.remove( (int) (Math.random() * puntos.size()));
            do {
              d1 = new DObject(d.getID(), (int) (Math.random() * 100001),
                               (int) (Math.random() * 100001), k, 'i'
                               );

            }
            while (!kdbTree.insert(d1));
            //guardar los objetos cambiados en el vector cambios para
            // despues agregarlo al conjunto de puntos totales
            cambios.addElement(d1);
            out.write(String.valueOf(d1.getID()) + " " + (float) k / 100 + " " +
                      (float) d1.getX() / 100000 + " " +
                      (float) d1.getY() / 100000 + " " +
                      (float) d1.getX() / 100000 + " " +
                      (float) d1.getY() / 100000 + "1\n");
            out.flush();
          } //fin for
          //agregar los puntos variados al conjunto original
          //ahora refrescar el kdb con estos conjuntos
          puntos.addAll(cambios);
          kdbTree = new KDBTree(new Coordinate(100000, 100000),
                                new Coordinate(0, 0));
          for (int l = 0; l < puntos.size(); l++) {
            while (!kdbTree.insert( (DObject) puntos.elementAt(l))) {
              ;
            }
          }

        } //fin for

        out.close();
        f2.close();
        puntos = new Vector();

      } //fin segundo for

    } //fin primer for
  }
}
