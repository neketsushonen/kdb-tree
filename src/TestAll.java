import java.util.*;
import struct.*;
import java.io.*;
/**
 * Insert the type's description here.
 * Creation date: (19/11/2004 16:57:55)
 * @author:
 */
class TestAll {
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {

	if (args.length!=2){
		System.out.println("java TestAll 1|2 ArchivoDeSalida");
		System.exit(0);
	}

        Page.SIZEREGION = 50;
	if (args[0].equals("1")){
		Util.ylList = true; //Version SkipList
                Page.SIZEPOINT = 84;
	}else{
          Util.ylList = false; //Version List Simple
          Page.SIZEPOINT = 85;
        }
    System.out.println("Abre el Archivo de respuesta con Excel para tenerlo mas limpio");

    try {

        FileWriter fout = new FileWriter(args[1]);
        PrintWriter out = new PrintWriter(fout);

        int pointCount = 0;
        KDBTree kdbTree;
        String nombreArchivoPruebas[][] =
            new String[][] {
            {
		"prueba_3000_1", "3000", "1" }, {
               "prueba_3000_3", "3000", "3" }, {
               "prueba_3000_5", "3000", "5" }, {
               "prueba_3000_7", "3000", "7" }, {
               "prueba_3000_9", "3000", "9" }, {
		"prueba_3000_11", "3000", "11" }, {
		"prueba_3000_13", "3000", "13" }, {
		"prueba_3000_15", "3000", "15" }, {
		"prueba_3000_20", "3000", "20" }, {
		"prueba_3000_30", "3000", "30" }, {
		"prueba_3000_50", "3000", "50" }, {

		"prueba_10000_1", "10000", "1" }, {
               "prueba_10000_3", "10000", "3" }, {
               "prueba_10000_5", "10000", "5" }, {
               "prueba_10000_7", "10000", "7" }, {
               "prueba_10000_9", "10000", "9" }, {
		"prueba_10000_11", "10000", "11" }, {
		"prueba_10000_13", "10000", "13" }, {
		"prueba_10000_15", "10000", "15" }, {
		"prueba_10000_20", "10000", "20" }, {
		"prueba_10000_30", "10000", "30" }, {
		"prueba_10000_50", "10000", "50" }, {

		"prueba_50000_1", "50000", "1" }, {
               "prueba_50000_3", "50000", "3" }, {
               "prueba_50000_5", "50000", "5" }, {
               "prueba_50000_7", "50000", "7" }, {
               "prueba_50000_9", "50000", "9" }, {
		"prueba_50000_11", "50000", "11" }, {
		"prueba_50000_13", "50000", "13" }, {
		"prueba_50000_15", "50000", "15" }, {
		"prueba_50000_20", "50000", "20" }, {
		"prueba_50000_30", "50000", "30" }, {
		"prueba_50000_50", "50000", "50" }


        };

        String nombreArchivoConsulta[][] =
            new String[][] {
            {   "timeslice_5", "TimeSlice", "5" }, {
                "timeslice_10", "TimeSlice", "10" }, {
                 "timeslice_20", "TimeSlice", "20" }, {
                "interval_5_5", "Intelval5", "5" }, {
                "interval_5_10", "Intelval5", "10" }, {
                "interval_5_20", "Intelval5", "20" }, {
                "interval_10_5", "Intelval10", "5" }, {
                "interval_10_10", "Intelval10", "10" }, {
                "interval_10_20", "Intelval20", "20" }, {
                "event_5", "Event", "5" }, {
                "event_10", "Event", "10" },{
                "event_20", "Event", "20" }
        };

        int dis[] = new int[] {4,8,12 };

        FileReader f;
        BufferedReader in;
        StringTokenizer str;
        String item;
        int x1, y1, x2, y2, t1, t2, aux = 0, oid, t;
        DObject d, e, f1;

        double promedio = 0;
        //posicion 1: Tamanio de D
        //posicion 2: Numeros de Objetos
        //posicion 3: % de movimiento
        //posicion 4: Bloques Usados
        //posicion 5: Tipo Consulta
        //posicion 6: % del area de consulta
        //posicion 7: Bloques leidos

        out.write(
            "Tamanio D\tN Objetos\t%Movimiento\tBloques Usados\tTipo Consulta\t%Area consulta\tBloque leido\n");

        Zone z;
        boolean flag = false;
        Vector v = null;
        Hashtable todos = new Hashtable();
        int totalBloques=0;

        for (int k = 0; k < dis.length; k++) {
            if (Util.ylList)
                Util.D = dis[k];
            else
                NodeTime.umbralBlock = dis[k];

                for (int l = 0; l < nombreArchivoPruebas.length; l++) {
                    f = new FileReader(nombreArchivoPruebas[l][0]);
                    in = new BufferedReader(f);
                    aux = 0;
                    flag = false;
                    kdbTree = new KDBTree(new Coordinate(100000, 100000), new Coordinate(0, 0));
                    z = new Zone(new Coordinate(100000, 100000), new Coordinate(0, 0));
                    v = null;
                    //Por cada archivo que leo, actualizo el arbol kdb
                    while ((item = in.readLine()) != null) {
                        str = new StringTokenizer(item);
                        oid = (int) (Float.parseFloat(str.nextToken()));
                        t = (int) (Float.parseFloat(str.nextToken()) * 100);
                        x1 = (int) (Float.parseFloat(str.nextToken()) * 100000);
                        y1 = (int) (Float.parseFloat(str.nextToken()) * 100000);
                        //Cuando comienza el cambio, se monta el bloque del tiempo cero en
                        // la lista de cambios
                        if (aux + 1 != oid && !flag) {
                            kdbTree.mountPointAtList();
                            v = kdbTree.getlastRefresh(z, 500);
                          for (int i = 0; i < v.size(); i++) {
                                 d = (DObject) v.elementAt(i);
                                 todos.put(String.valueOf(d.getID()),d);
                             }

                         }

                         if (aux + 1 == oid) {
                             d = new DObject(oid, x1, y1, t, 'i');
                             kdbTree.insert(d);
                             pointCount++; //sumador de cantidad de puntos
                             aux++;

                         } else {
                             flag = true;
                             d = null;
                             d = (DObject)todos.get(String.valueOf(oid));

                             //insertar el cambio en la posicion original
                             d = new DObject(oid * -1, d.getX(), d.getY(), t, '3');
                             kdbTree.insert(t, d);
                             e = new DObject(oid, x1, y1, t, 'i');
                             f1 = new DObject(oid, x1, y1, t, 'i');
                            todos.remove(String.valueOf(oid));
                            todos.put(String.valueOf(oid),e);
                            //v.setElementAt(e, i);
                            kdbTree.insert(t, f1); //insertar el cambio en la nueva posisicon
                        }
                    } //fin mientras
                      //obtener la cantidad total de bloques utilizados
                    totalBloques = kdbTree.calculaBloquesTotal();
		for (int j = 0; j < nombreArchivoConsulta.length; j++) {
                    promedio = 0;

                    f = new FileReader(nombreArchivoConsulta[j][0]);
                    in = new BufferedReader(f);
                    while ((item = in.readLine()) != null) {
                        Stadistic.countAccessBlocks = 0;
                        str = new StringTokenizer(item);
                        x1 = (int) (Float.parseFloat(str.nextToken()) * 100000);
                        y1 = (int) (Float.parseFloat(str.nextToken()) * 100000);
                        x2 = (int) (Float.parseFloat(str.nextToken()) * 100000);
                        y2 = (int) (Float.parseFloat(str.nextToken()) * 100000);
                        t1 = (int) (Float.parseFloat(str.nextToken()) * 100);
                        z = new Zone(new Coordinate(x2, y2), new Coordinate(x1, y1));
                        if (!str.hasMoreTokens())
                            kdbTree.timeSlice(z, t1);
                       else {
                            t2 = (int) (Float.parseFloat(str.nextToken()) * 100);
                            if (t2 < 0) //si es un negativo el ultimo token, se realiza la consulta event
                                kdbTree.event(z, t1);
                            else {
                                if (t1 < t2) //si chequee el tiempo 1 y 2
                                    kdbTree.interval(z, t1, t2);
                                else
                                    kdbTree.interval(z, t2, t1);
                            }
                        }
                        promedio += Stadistic.countAccessBlocks;
                    } //fin while
                    promedio = promedio / 100; //por cada consulta genero eso

                    out.write(
                        dis[k]
                            + "\t"
                            + nombreArchivoPruebas[l][1]
                            + "\t"
                            + nombreArchivoPruebas[l][2]
                            + "\t"
                            + totalBloques
                            + "\t"
                            + nombreArchivoConsulta[j][1]
                            + "\t"
                            + nombreArchivoConsulta[j][2]
                            + "\t"
                            + promedio
                            + "\n");
                    System.out.println(
                        dis[k]
                            + "\t"
                            + nombreArchivoPruebas[l][1]
                            + "\t"
                            + nombreArchivoPruebas[l][2]
                            + "\t"
                            + kdbTree.calculaBloquesTotal()
                            + "\t"
                            + nombreArchivoConsulta[j][1]
                            + "\t"
                            + nombreArchivoConsulta[j][2]
                            + "\t"
                            + promedio);

                    out.flush();

                } // fin tercer for
            } // fin segundo for
        } //fin primer for

    } catch (IOException ex) {
        System.out.println("ex..");

    }
}
}
