package struct;

import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * El Arbol KDB contiene todos los nodos regionales o de puntos.
 *  En el se maneja la insercion y la reorganizacion de regiones en caso del
 * corte. El Arbol KDB es un arbol general n-aria y que sus regiones son
 * separadas por X o Y de acuerdo los diferentes criterios. La estructura del
 * arbol esta hecha por 2 nodos diferentes: de regiones o de puntos.
 * Cada pagina de region (nodos intermedios) puede contener varias paginas
 * de puntos(hojas) o de regiones.
 * @author: Chun-Hau Lai
 */
public class KDBTree {
  private Page root;

  /**
   * Inicializa la raiz dado un area determinada( el area se representa con
   * el rincon superior derecho e inferior izquierdo del MBR)
   * @param rightUp struct.Coordinate
   * @param leftDown struct.Coordinate
   */
  public KDBTree(Coordinate rightUp, Coordinate leftDown) {
    root = new Point("Pagina", rightUp, leftDown);
  }

  /**
   * Retornar la cantidad total de los bloques existentes en la estructura
   * Creation date: (19/11/2004 16:36:07)
   * @return int
   */
  public int calculaBloquesTotal() {
    Stadistic s = root.calculateUtilization();

    return s.getCountBlocks();
  }

  /**
   * Construir el arbol KDB en forma Visual
   * @return javax.swing.JTree
   */
  public JTree constructJTreeForKDB() {
    String valorxy1, valorxy2;
    DefaultMutableTreeNode node;

    valorxy1 =
        String.valueOf(root.getZone().getLeftDown().getX())
        + ","
        + String.valueOf(root.getZone().getLeftDown().getY());
    valorxy2 =
        String.valueOf(root.getZone().getRightUp().getX())
        + ","
        + String.valueOf(root.getZone().getRightUp().getY());
    node = new DefaultMutableTreeNode(valorxy1 + " : " + valorxy2);

    root.addNodes(node);
    JTree tree = new JTree(node);

    return tree;
  }

  /**
   * Retornar los elementos abarcados por una region determinada en
   * el instante t1
   * @return javax.swing.JTable
   * @param zone struct.Zone
   * @param t1 int
   * @param t2 int
   */
  public JTable event(Zone zone, int t) {
    Page n;
    LinkedList lista = new LinkedList(),
        zonas = new LinkedList();

    DObject d;
    /*	Hashtable agregaron = new Hashtable(),
               eliminaron = new Hashtable();
     */
    HashSet agregaron = new HashSet(),
        eliminaron = new HashSet();
    Vector temporal;

    Zone z;
    lista.add(root);
    zonas.add(root.getZone());

    //Obtener los elementos abarcados por la region
    do {
      n = (Page) lista.removeFirst();
      z = (Zone) zonas.removeFirst();
      if (n.intersect(zone)) { //Pretunta si se intersecta a la region
        //Agregar los nodos hijos y las zonas de los nodos hijos a la lista
        //respectivamente

        if (n instanceof Region) {
          lista.addAll(n.getElements());
          zonas.addAll( ( (Region) n).getZonas());
        }

        //Aumenta el acceso cada vez que accesan a la region
        Stadistic.countAccessBlocks++;
        //Si es una hoja, entonces retorna los eventos ocurridos
        if (n instanceof Point) {
          temporal = ( (Point) n).evento(zone, t);
          for (int i = 0; i < temporal.size(); i++) {
            d = (DObject) temporal.elementAt(i);
           // si el id del objeto es igual al negativo,significa que salio de
           //la region donde pertenece si no, no procesa.
            if (d.getID() < 0) {
              // si el objeto esta dentro de la region en consulta y
              //el objeto que se elimino dentro de la misma region,
              // significa que no se ingreso ni se ingreso
              if (agregaron.contains(String.valueOf(Math.abs(d.getID())))) {
                agregaron.remove(String.valueOf(Math.abs(d.getID())));
              }
              else {
                eliminaron.add(String.valueOf(Math.abs(d.getID())));
              }
            }
            else {
              // si el objeto se elimino o de la region en consulta y el objeto
              //que se ingreso dentro de la misma region, significa que no
              //se ingreso ni se ingreso
              if (eliminaron.contains(String.valueOf(d.getID()))) {
                eliminaron.remove(String.valueOf(d.getID()));
              }
              else {
                agregaron.add(String.valueOf(d.getID()));
              }
            }
          }

        }

      }
    }
    while (!lista.isEmpty());
    //--------Construccion de la tabla
    JTable table;
    String columna[] = new String[] {
        "Insertados", "Eliminados"}
        ,
        datos[][] = new String[1][2];

    //pasar los elementos al arreglo para la construccion de la tabla

    datos[0][0] = String.valueOf(agregaron.size());
    datos[0][1] = String.valueOf(eliminaron.size());

    table = new JTable(datos, columna);
    return table;
  }

  /**
   * Consulta Time Slice que retorna el ultimo area de reconstruccion
   * con el objetivo de ayudar a generar los cambios
   * @param zone struct.Zone
   * @param time int
   */
  public Vector getlastRefresh(Zone zone, int time) {

    Page n;
    LinkedList lista = new LinkedList();
    lista.add(root);
    Vector v = new Vector(), x;
    DObject d;
    //Obtener los elementos abarcados por la region
    do {
      n = (Page) lista.removeFirst();
      if (n.intersect(zone)) { //Pretunta si se intersecta a la region
        //Aumenta el acceso cada vez que accesan a la region
        Stadistic.countAccessBlocks++;
        //Si es una hoja, entonces retorna los elementos
        if (n instanceof Point) {
          x = ( (Point) n).timeSlice(time);
          for (int i = 0; i < x.size(); i++) {
            d = (DObject) x.elementAt(i);
            if (zone.isIncluded(d)) {
              v.addElement(d);
            }
          }
        }
        else {
          //sino agregue las regiones abarcadas por el nodo padre a la lista
          lista.addAll(n.getElements());
        }
      }
    }
    while (!lista.isEmpty());

    return v;
  }

  /**
   * Retorna el panel que contenga la tabla de estadistica,
   * el nivel del arbol, cantidad total de los elementos existentes.
   * @return javax.swing.JPanel
   */
  private JPanel getPanelStadistic() {
    JTable table = null;

    JPanel panel = new JPanel();

    Vector sta = root.calculateStadistic();
    String headers[] = new String[] {
        "Nivel", "Cantidad Elementos"}
        ,
        data[][] = new String[sta.size() + 1][2];
    Stadistic s;
    data[0][0] = "Nivel";
    data[0][1] = "Cantidad";
    //Llenar los datos
    for (int i = 1; i <= sta.size(); i++) {
      s = (Stadistic) sta.elementAt(i - 1);
      data[i][0] = String.valueOf(s.getNivel());
      data[i][1] = String.valueOf(s.getCount());
    }
    //creacion de tablas y el panel de rollo
    table = new JTable(data, headers);
    // scroll = new JScrollPane(table);

    panel.setLayout(new java.awt.GridLayout(1, 1));
    panel.setBorder(BorderFactory.createTitledBorder("La estadistica general"));
    panel.add(table);

    return panel;
  }

  /**
   * Retorna el panel que contenga la informacion con respecto a
   * la utilizacion total que hay en el arbol KDB:
   * Cantidad total de punteros, bloques, puntos, cortes
   * @return javax.swing.JPanel
   */
  private JPanel getPanelUtilization() {
    Stadistic s = root.calculateUtilization();

    JPanel panel = new JPanel(new java.awt.GridLayout(5, 2));
    String titulos[] =
        new String[] {
        "Bloques          :",
        "Punteros         :",
        "Cortes           :",
        "Puntos           :"
        /*"Utilizacion Total:"*/
    };
    int data[] =
        new int[] {
        s.getCountBlocks(),
        s.getCountPointers(),
        s.getCountCuts(),
        s.getCountPoints()
        /*s.getUtilization()*/
    };

    for (int i = 0; i < 4; i++) {
      panel.add(new JLabel(titulos[i]));
      panel.add(new JLabel(String.valueOf(data[i]), JLabel.RIGHT));
    }

    return panel;
  }

  /**
   * Construir el Panel que contenga una tabla de la estadistica
   * (el nivel, cantidad de elementos)
   * y un panel que contenga la informacion general de la utilizacion,
   * es decir, cantidad total de bloques cantidad total de punteros, cantaidad
   * total de puntos y la utilizacion total en el disco.
   * @return javax.swing.JPanel
   */
  public JPanel getPanelUtilizationAndStadistic() {
    JPanel pGeneral = new JPanel(),
        pStatistic = getPanelStadistic(),
        pUtilization = getPanelUtilization();
    pGeneral.setSize(500, 500);
    pGeneral.setLayout(new java.awt.GridLayout(2, 1));
    pGeneral.add(pStatistic);
    pGeneral.add(pUtilization);
    // pGeneral.setSize(200,200);

    return pGeneral;
  }

  /**
   * Retornar el area total del MBR que abarca cualquier punto
   * @return struct.Zone
   */
  public Zone getZone() {
    return root.getZone();
  }

  /**
   * Insertar el cambio en el tiempo t
   * @param time int
   * @param d struct.DObject
   */
  public void insert(int time, DObject d) {
    root.insert(time, d);
  }

  /**
   * Insertar un elemento elem en el arbol. La metologia que se utiliza es
   *  consultar la posible region a estar y luego insertarlo en ella. En caso de
   *  overflow, se divide la pagina hasta lograr una distribucion equilibrada de
   *  regiones. Se retorna verdadero cuando se haya insertado con exito,
   *  en caso contrario, falso (cuando se repite o la ubicacion no esta dentro
   *  de la region.)
   * @return boolean
   * @param elem javax.swing.text.Element
   */
  public boolean insert(DObject elem) {
    try {
      Page tmp = root.insert(elem);
      if (tmp == null) {
        return false;
      }
      else {
        root = tmp;
        return true;
      }
    }
    catch (CloneNotSupportedException e) {
      return false;
    }
  }

  /**
   * Retornar los elementos abarcados por una region determinada en
   * el intervalo del tiempo t1 y t2
   * @return javax.swing.JTable
   * @param zone struct.Zone
   * @param t1 int
   * @param t2 int
   */
  public JTable interval(Zone zone, int t1, int t2) {
    Page n;
    LinkedList lista = new LinkedList(),
        zonas = new LinkedList();
    Vector x;
    TreeMap temp;
    TreeMap v = new TreeMap();
    DObject d;
    Zone z;
    lista.add(root);
    zonas.add(root.getZone());

    //Obtener los elementos abarcados por la region
    do {
      n = (Page) lista.removeFirst();
      z = (Zone) zonas.removeFirst();
      if (n.intersect(zone)) { //Pretunta si se intersecta a la region
        //Agregar los nodos hijos y las zonas de los nodos hijos a la lista
        //respectivamente

        if (n instanceof Region) {
          lista.addAll(n.getElements());
          zonas.addAll( ( (Region) n).getZonas());
        }

        Stadistic.countAccessBlocks++;
        //Aumenta el acceso cada vez que accesan a la region
        //Si es una hoja, entonces retorna los elementos
        if (n instanceof Point) {

          x = ( (Point) n).interval(t1, t2);
          temp = new TreeMap();

          for (int i = 0; i < x.size(); i++) {
            d = (DObject) x.elementAt(i);
            if (zone.isIncluded(d)) {
              if (v.containsKey(String.valueOf(d.time))) {
                temp = (TreeMap) v.get(String.valueOf(d.time));
                temp.put(String.valueOf(d.getID()), d);
              }
              else {
                temp = new TreeMap();
                temp.put(String.valueOf(d.getID()), d);
                v.put(String.valueOf(d.time), temp);
              }
            }
          }
        }
      }
    }
    while (!lista.isEmpty());

    //--------Construccion de la tabla
    JTable table;
    String columna[] = new String[] {
        "T", "ID", "X", "Y"}
        ,
        datos[][];

    //pasar los elementos al arreglo para la construccion de la tabla
    //pasar el conjunto de cada tiempo
    Vector tiempo = new Vector(v.values());
    int size = 0;
    for (int i = 0; i < tiempo.size(); i++) {
      temp = (TreeMap) tiempo.elementAt(i);
      Vector aux = new Vector(temp.values());
      size += aux.size();
    }
    datos = new String[size][4];

    for (int i = 0, count = 0; i < tiempo.size(); i++) {
      temp = (TreeMap) tiempo.elementAt(i);
      Vector aux = new Vector(temp.values());

      for (int j = 0; j < aux.size(); j++, count++) {

        d = (DObject) aux.elementAt(j);
        datos[count][0] = String.valueOf(d.time);
        datos[count][1] = String.valueOf(d.getID());
        datos[count][2] = String.valueOf(d.getX());
        datos[count][3] = String.valueOf(d.getY());
        //i++;
      }
    }
    table = new JTable(datos, columna);
    return table;
  }

  /**
   * Montar los puntos existentes en la hoja en la lista
   */
  public void mountPointAtList() {
    root.mountPointAtList();
  }

  /**
   * Imprimir el Arbol KDB por nivel.
   */
  public void printAll() {
    root.printAll("");
  }

  /**
   * Consulta Time Slice
   * @param zone struct.Zone
   * @param time int
   */
  public JTable timeSlice(Zone zone, int time) {

    Page n;
    LinkedList lista = new LinkedList(),
        zonas = new LinkedList();
    Vector x;
    TreeMap v = new TreeMap();
    Zone z;
    lista.add(root);
    zonas.add(root.getZone());

    DObject d;
    //Obtener los elementos abarcados por la region
    do {
      n = (Page) lista.removeFirst();
      z = (Zone) zonas.removeFirst();
      if (n.intersect(zone)) { //Pretunta si se intersecta a la region
        //Agregar los nodos hijos y las zonas de los nodos hijos
        //a la lista respectivamente
        if (n instanceof Region) {
          lista.addAll(n.getElements());
          zonas.addAll( ( (Region) n).getZonas());
        }
        //Aumenta el acceso cada vez que accesan a la region
        Stadistic.countAccessBlocks++;
        //Si es una hoja, entonces retorna los elementos
        if (n instanceof Point) {
          x = ( (Point) n).timeSlice(time);
          for (int i = 0; i < x.size(); i++) {
            d = (DObject) x.elementAt(i);
            if (zone.isIncluded(d)) {
              v.put(String.valueOf(d.getID()), d);
            }
          }
        }
      }
    }
    while (!lista.isEmpty());

    //--------Construccion de la tabla
    JTable table;
    String columna[] = new String[] {
        "ID", "X", "Y"}
        ,
        datos[][] = new String[v.size()][3];

    //pasar los elementos al arreglo para la construccion de la tabla
    Vector aux = new Vector(v.values());
    Hashtable hs = new Hashtable();
//	int i=0;
    //while(e.hasNext()){
    for (int i = 0; i < aux.size(); i++) {
      //d = (DObject)e[i];
      d = (DObject) aux.elementAt(i);
      hs.put(String.valueOf(d.getID()), d);
      datos[i][0] = String.valueOf(d.getID());
      datos[i][1] = String.valueOf(d.getX());
      datos[i][2] = String.valueOf(d.getY());
      //i++;
    }
    /*
     for(int i=1;i<=1000; i++)
      if (!hs.containsKey(String.valueOf(i)))
       System.out.println("El objeto "+i+" no se encuentra");
     */
    table = new JTable(datos, columna);
    return table;
  }
}
