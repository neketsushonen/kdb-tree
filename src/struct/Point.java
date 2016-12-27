package struct;

import java.util.*;

import javax.swing.tree.*;

/**
 * En esta clase se almacenara todos los objetos (puntos) que sera observado
 * por SABD.
 * Dado el tamanio determinado de un bloque en el disco, solamente se puede
 * almacenar
 * una cantidad determinada de puntos en esta clase. Se implementa el metodo
 * insertar segun las caracteristicas de una pagina Point.
 * @author: Chun-Hau Lai
 */
public class Point
    extends Page {
  //El tiempo es para guardar el indice de tiempo
  // y puntero es para guardar el puntero al bloque que almacena
  //los bloques de tiempo
  private Vector tiempo, punteros;

  //El skiplist que contenga los cambios para la version que implementa skiplist
  private Change cambio;

  /**
   * Instanciar los atributos de una pagina de puntos con su id, el punto
   * derecho superior junto con el de izquierdo inferior de MBR
   * @param id java.lang.String
   * @param rUp struct.Coordinate
   * @param lDown struct.Coordinate
   */
  public Point(String id, Coordinate rUp, Coordinate lDown) {
    super(id, lDown, rUp);
    tiempo = null;
    punteros = null;
  }

  /**
   * Construir la lista del JTree empezando desde el primier nivel hasta
   * las hojas recursivamente
   * @param pnode javax.swing.tree.DefaultMutableTreeNode
   * @param n struct.Page
   */
  public void addNodes(javax.swing.tree.DefaultMutableTreeNode pnode) {
    javax.swing.tree.DefaultMutableTreeNode node;
    String nombre, valorxy;

    if (!Util.ylList) {
      if (punteros == null) {
        Enumeration e = getElements().elements();
        while (e.hasMoreElements()) {
          DObject elem = (DObject) e.nextElement();
          nombre = "P" + String.valueOf(elem.getID());
          valorxy = String.valueOf(elem.getX()) + "," +
              String.valueOf(elem.getY());
          node = new DefaultMutableTreeNode(nombre + "->" + valorxy);
          pnode.add(node);
        }
      }
      else {
        DObject d;
        int count = 0;
        Vector v;
        Object o;
        TimeSeparator s;
        DefaultMutableTreeNode r, c;
        NodeTime x;
        NodeChange n;
        int tiempoExistente[];
        //Mostrar todas las claves existentes del tiempo en kdb
        for (int i = 0; i < tiempo.size(); i++) {
          int t = Integer.parseInt(tiempo.elementAt(i).toString());
          node = new DefaultMutableTreeNode("indice Bloque t:" + t);
          x = (NodeTime) punteros.elementAt(i);
          tiempoExistente = x.getTimesExist();
          for (int j = 0; j < tiempoExistente.length; j++) {
            node.add(new DefaultMutableTreeNode("t" +
                                                String.valueOf(tiempoExistente[
                j])));
          }
          pnode.add(node);
        }

        x = (NodeTime) punteros.elementAt(0);
        //sacar el primer bloque
        n = (NodeChange) x.pointers.elementAt(0);

        while (n != null) {
          node = new DefaultMutableTreeNode("Bloque " + (count++));
          v = n.getRefreshPoints();
          r = new DefaultMutableTreeNode("Reconstruccion ");
          for (int i = 0; i < v.size(); i++) {
            d = (DObject) v.elementAt(i);

            r.add(
                new DefaultMutableTreeNode(
                String.valueOf(d.getID())
                + "->"
                + String.valueOf(d.getX())
                + ","
                + String.valueOf(d.getY())));
          }
          v = n.getChanges();
          c = new DefaultMutableTreeNode("Cambios");
          for (int i = 0; i < v.size(); i++) {
            o = v.elementAt(i);
            if (o instanceof TimeSeparator) {
              s = (TimeSeparator) o;
              c.add(
               new DefaultMutableTreeNode(
                "t" + String.valueOf(s.time) + "->" + String.valueOf(s.count)));
            }
            else {
              d = (DObject) o;
              c.add(
                  new DefaultMutableTreeNode(
                  String.valueOf(d.getID())
                  + "->"
                  + String.valueOf(d.getX())
                  + ","
                  + String.valueOf(d.getY())));
            }
          }
          node.add(r);
          node.add(c);
          pnode.add(node);
          n = n.getNext();
        }
      }
    }
    else {
      DObject d;
      int count = 0;
      Vector v;
      DefaultMutableTreeNode r = null, c;
      if (cambio == null) {
        Enumeration e = getElements().elements();
        while (e.hasMoreElements()) {
          DObject elem = (DObject) e.nextElement();
          nombre = "P" + String.valueOf(elem.getID());
          valorxy = String.valueOf(elem.getX()) + "," +
              String.valueOf(elem.getY());
          node = new DefaultMutableTreeNode(nombre + "->" + valorxy);
          pnode.add(node);
        }
      }
      else {
        Enumeration e = getElements().elements();
        BlockNode tmp = cambio.skip.indexNode;
        while (tmp != null) {
          r = new DefaultMutableTreeNode("Block " + String.valueOf(count++));
          //aqui no entro en el ciclo...
          //problema
          Vector ver = cambio.skip.indexNode.dObjects;
          for (int i = 0; i < ver.size(); i++) {
            System.out.println("apa");
            d = (DObject) ver.elementAt(i);
            c =
                new DefaultMutableTreeNode(
                String.valueOf(d.getID())
                + "->"
                + String.valueOf(d.getX())
                + ","
                + String.valueOf(d.getY()));
            r.add(c);
          }
          tmp = tmp.ptrNext;
          pnode.add(r);
        }
      }

    }
  }

  /**
   * Obtener la cantidad de total de elementos existentes en el nivel 1
   * @return java.util.Vector
   */
  public Vector calculateStadistic() {
    Vector v = new Vector(1);
    Stadistic s = null;
    int countBloks = 0;
    if (!Util.ylList) {
      if (punteros != null) {
        //Si los puntos ha sido montado en la lista de cambios,
        // entonces se cuenta los bloques de cambios
        countBloks += punteros.size();
        NodeTime nodetime = (NodeTime) punteros.elementAt(0);
        NodeChange nodechange = (NodeChange) nodetime.pointers.elementAt(0);
        while (nodechange != null) {
          countBloks++;
          nodechange = nodechange.getNext();
        }
        s = new Stadistic(1, countBloks);
      }
      else { // si no se cuenta los puntos existentes en las hojas no mas
        s = new Stadistic(1, getElements().size());
      }
    }
    else {
      if (cambio == null) {
        s = new Stadistic(1, getElements().size());
      }
      else {
        BlockNode x = cambio.skip.indexNode;
        BlockPhoto y = null;

        //contar la cantidad total de los bloques que almacena bitagora
        while (x != null) {
          countBloks++;

          if (x.ptrPhoto != null) {
            y = x.ptrPhoto;
          }
          x = x.ptrNext;
        }
        //contar la cantidad total de los bloques de reconstruccion
        while (y != null) {
          countBloks++;
          y = y.ptrNext;
        }

        //countBloks = cambio.n_blocks;
        s = new Stadistic(1, countBloks);

      }

    }
    v.addElement(s);
    return v;
  }

  /**
   * Calcular la utilizacion de la pagina de puntos asignandole los valores
   * de la cantidad bloques y puntos.
   * @return struct.Stadistic
   */
  public Stadistic calculateUtilization() {
    Stadistic s = new Stadistic();
    s.setCountBlocksAndPoints(1, getElements().size());
    return s;
  }

  /**
   * Metodo que chequen si la ubicacion del nuevo elemento ya este ocupado
   * por otro elemento que se encuentra en la misma ubicacion.
   * @return boolean
   * @param elem struct.Element
   */
  private boolean contains(DObject elem) {
    for (int i = 0; i < getElements().size(); i++) {
      DObject n = (DObject) getElements().elementAt(i);
      if (n.getX() == elem.getX()
          &&
          n.getY() == elem.getY()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Retornar la consulta evento
   * @return java.util.Vector
   * @param time int
   */
  public Vector evento(Zone zona, int time) {
    if (!Util.ylList) {
      //procesar la bitacora basado en Lista Variante
      int t = 0, i;
      NodeTime x = null;

      for (i = 0; i < punteros.size(); i++) {
        t = Integer.parseInt( ( (String) tiempo.elementAt(i)));
        if (time >= t) {
          //procese la region correspondiente y actualiza los cambios
          x = (NodeTime) punteros.elementAt(i);
        }
        else {
          break;
        }

      }
      //Si i sobrepase al umbral del bloque de kdb,
      // entonces se calcula la cantidad de accesos a los bloques siguientes
      if (i > 85) {
        Stadistic.countAccessBlocks += (int) (i / 84);

      }
      if (x == null) {
        System.out.println(
            "no encontro el el puntero adecuado para el tiempo...");
      }
      return x.event(zona, time);
    }
    else {
      //procesar la bitacora basado en Skip_List
      return cambio.queryEvent(zona, time);
    }
  }

  /**
   * Este metodo calculara el punto mediano de todos los puntos existentes
   * en la pagina.
   * Primero se ordena de acuerdo al eje X, y luego se calcula el mediano X.
   * Segundo se ordena segun el eje Y y obtiene el mediano Y.
   * y finalmente se obtiene la informacion del corte ejercido horizontalmente o
   * verticalmente.
   * Vertical: se representa con el valor 0.
   * Horizontal: se representa con el valor 1
   * @return struct.Coordinate
   */
  public Coordinate getAverage() {
    //Se guardara la informacion de el valor de la coordenada X de todos los puntos en la panina en el arreglo x[]
    //y el valor Y en el y[]
    int i = 0, countX = 0, countY = 0, average_x, average_y,
        sizeOfelemnt = elements.size();
    //System.out.println(sizeOfelemnt);

    //Si no haya ningun elemento en la pagina de Points,
    //se retornara el punto promedio de la pagina

    if (sizeOfelemnt == 0) {
      return new Coordinate( (int) (getZone().getRightUp().getX() +
                                    getZone().getLeftDown().getX()) / 2,
                            (int) (getZone().getRightUp().getY() +
                                   getZone().getLeftDown().getY()) / 2);
    }
    else if (sizeOfelemnt == 1) {
      DObject n = (DObject) getElements().elementAt(0);
      return new Coordinate(n.getX(), n.getY());
    }

    int x[] = new int[sizeOfelemnt],
        y[] = new int[sizeOfelemnt];

    DObject n;
    Coordinate tmp;

    Enumeration elems = elements.elements();
    while (elems.hasMoreElements()) {
      //obtiene la ubicacion del elemento elem en la pagina.
      n = (DObject) elems.nextElement();
      x[i] = n.getX();
      y[i] = n.getY();
      i++;
    }

    //Ordena el arreglo x e y respectivamente a traves del objeto Arrays.
    Arrays.sort(x);
    Arrays.sort(y);
    average_x = x[ (int) (sizeOfelemnt / 2)];
    average_y = y[ (int) (sizeOfelemnt / 2)]; //}

    //calcular cuantas veces se ha repetido el valor del mediano X
    //con el fin de decidir el corte.
    for (i = 0; i < x.length; i++) {
      if (x[i] == average_x) {
        countX++;
      }
      if (y[i] == average_y) {
        countY++;
      }
    }

    Coordinate corte;
    // Si promedio X se ha repetido menos que promedio Y, implica que se
    //puede cortar verticlmente.
    if (countX < countY) {
      //average_x = (x[(int)(sizeOfelemnt/2 - 1)] + average_x) /2
      corte = new Coordinate(average_x, average_y, (byte) 0);
    }
    else if (countX > countY) {
      //average_y = (y[(int)(sizeOfelemnt/2 - 1)] + average_y) /2
      corte = new Coordinate(average_x, average_y, (byte) 1);
    }
    else {

      if ( (corteeje = (corteeje + 1) % 2) == 1) {
        average_x = (x[ (int) (sizeOfelemnt / 2 - 1)] + average_x) / 2;
        corte = new Coordinate(average_x, average_y, (byte) 0);
      }
      else {
        average_y = (y[ (int) (sizeOfelemnt / 2 - 1)] + average_y) / 2;
        corte = new Coordinate(average_x, average_y, (byte) 1);
      }
    }

    return corte;

  }

  /**
   * Retorna la skip list de Change
   * @return struct.Change
   */
  public Change getChange() {
    return cambio;
  }

  /**
   * Retornar el puntero de los nodos tiempo
   * @return java.util.Vector
   */
  public Vector getPunterosANodeTime() {
    return punteros;
  }

  /**
   * Insertar el cambio de acuerdo el tiempo
   * @param time int
   * @param d struct.DObject
   */
  public void insert(int time, DObject d) {
    if (Util.ylList) {
      //procesar la bitacora basado en Skip List
      cambio.addObj(d);
    }
    else {
      //procesar la bitacora basado en Lista Variante

      //retornar el ultimo puntero;
      NodeTime n = (NodeTime) punteros.elementAt(punteros.size() - 1);
      // si nodo de tiempo n no esta en estado de over flow, se actualiza
      //el puntero
      if (n.insetChange(time, d)) {
        punteros.setElementAt(n, punteros.size() - 1);
      }
      else {
        //si esta en estado de saturacion,
        //se establece una clave de tiempo que apunta al segundo bloque nuevo
        // de tiempo
        tiempo.addElement(String.valueOf(time));
        n =
            new NodeTime(
            time,
            d,
            n.getLastAreaRefresh(),
            n.getLastTimeRefresh(),
            n.getLastCountRefresh());
        //luego agregar el cambio a este.
        n.insetChange(time, d);
        punteros.addElement(n);
      }
    }
  }

  /**
   * Insertar un elemento en la pagina de puntos.
   * Se inserta primero, si ocurre overflow, se realiza el corte buscando
   * primero el eje central,
   * luego aplicar el corte. Luego, se ajusta el tamanio de las paginas d
   * e puntos.
   * Asi se obtiene dos paginas de puntos que contenga en si la mitad aproximada
   * de informacion, y se enlaza a una pagina regional.
   * @return struct.Page
   * @param elem org.omg.CORBA.Object
   * @exception java.lang.CloneNotSupportedException The exception description.
   */
  public Page insert(Object elem) throws java.lang.CloneNotSupportedException {
    //Verifica si el objeto esta abarcado por la zona
    if (!isIncluded( (DObject) elem)) {
      return null;
    }
    // si la posicion a la cual va a estar el objeto ya existe un objeto
    // no insertarlo.
    if (contains( (DObject) elem)) {
      return null;
    }
    //agregar el objeto al nodo
    getElements().add(elem);

    if (getElements().size() < Page.SIZEPOINT) {
      setStatus(Page.NOTFULL);
      return (Page)this.clone();
    }
    else if (getElements().size() == Page.SIZEPOINT) {
      this.setStatus(Page.FULL);
      return (Page)this.clone();
    }
    else {
      // cuando llega al over flow, se realiza el corte
      return (Page)this.split();
    }

  }

  /**
   * Retornar la consulta de interval
   * @return java.util.Vector
   * @param t1 int
   * @param t2 int
   */
  public Vector interval(int t1, int t2) {
    if (!Util.ylList) {
      //procesar la bitacora basado en Lista Variante
      int t = 0, i;
      NodeTime x = null;

      for (i = 0; i < punteros.size(); i++) {
        t = Integer.parseInt( ( (String) tiempo.elementAt(i)));
        if (t1 >= t) {
          //procese la region correspondiente y actualiza los cambios
          x = (NodeTime) punteros.elementAt(i);
        }
        else {
          break;
        }

      }
      //Si i sobrepase al umbral del bloque de kdb,
      // entonces se calcula la cantidad de accesos a los bloques siguientes
      if (i > 85) {
        Stadistic.countAccessBlocks += (int) (i / 84);

      }
      if (x == null) {
        System.out.println(
            "no encontro el el puntero adecuado para el tiempo...");

      }
      return x.interval(t1, t2);
    }
    else {
      //procesar la bitacora basado en Skip-List
      return cambio.queryInterval(t1, t2);
    }
  }

  /**
   * Montar los puntos existentes en las hojas de tiempo 0 enla lista
   * del cambio en caso de la version lista simple
   * Referenciar los puntos existentes en las hojas de tiempo 0 en skiplist en
   * aso de la version skiplist para poder
   * // realizar la actualizacion facilmente
   */
  public void mountPointAtList() {
    if (!Util.ylList) {
      tiempo = new Vector();
      punteros = new Vector();
      tiempo.addElement("0");
      punteros.addElement(new NodeTime(getElements()));
    }
    else {
      cambio = new Change(getElements());

    }
  }

  /**
   * Imprimir el contenido del Arbol conpleto por nivel.
   */
  public void printAll(String salto) {
    try {

      g.setColor(java.awt.Color.blue);

   int length = getZone().getRightUp().getX() - getZone().getLeftDown().getX(),
       width = getZone().getRightUp().getY() - getZone().getLeftDown().getY();
      g.fillRect(
          getZone().getLeftDown().getX() / 100,
          getZone().getLeftDown().getY() / 100,
          length / 100,
          width / 100);
      g.clearRect(
          getZone().getLeftDown().getX() / 100 + 5,
          getZone().getLeftDown().getY() / 100 + 5,
          length / 100 - 10,
          width / 100 - 10);

      Thread.sleep(timeSlice);

      g.clearRect(
          getZone().getLeftDown().getX() / 100,
          getZone().getLeftDown().getY() / 100,
          length / 100,
          width / 100);
      g.drawRect(
          getZone().getLeftDown().getX() / 100,
          getZone().getLeftDown().getY() / 100,
          length / 100,
          width / 100);
      g.setColor(java.awt.Color.red);
      for (int i = 0; i < getElements().size(); i++) {
        DObject e = (DObject) getElements().elementAt(i);
        //g.drawString( String.valueOf(e.getID()),e.getSite().getX(),e.getSite().getY() );
        g.drawOval(e.getX() / 100, e.getY() / 100, 10, 10);
        // System.out.print(e.getSite().getX() + ":" + e.getSite().getY() + "  ");
      }
    }
    catch (InterruptedException e) {
    }

  }

  /**
   * Redefine el area abarcada por una zona en 4 maneras
   * 1: Cambie el valor del eje X del punto superior derecho.
   * 2: Cambie el valor del eje X del punto inferior izquierdo.
   * 3: Cambie el valor del eje Y del punto inferior izquierdo.
   * 4: Cambie el valor del eje Y del punto superior derecho.
   * @return struct.Page
   * @param criterio int
   * @param valorEje int
   */
  public Page setMBR(int criterio, int valorEje) {
    Page tmp = null;
    switch (criterio) {
      case 1:
        tmp = new Point("Page ",
                        new Coordinate(valorEje, zone.getRightUp().getY()),
                        zone.getLeftDown()
                        );
        break;
      case 2:
        tmp = new Point("Page ",
                        zone.getRightUp(),
                        new Coordinate(valorEje, zone.getLeftDown().getY())
                        );

        break;
      case 3:
        tmp = new Point("Page ",
                        getZone().getRightUp(),
                        new Coordinate(getZone().getLeftDown().getX(), valorEje)
                        );
        break;
      case 4:
        tmp = new Point("Page ",
                        new Coordinate(getZone().getRightUp().getX(), valorEje),
                        getZone().getLeftDown()
                        );

        break;

    }
    return tmp;
  }

  /**
   * Dividir pagina de puntos n de acuerdo al eje,Si el corte es ejercido en direccion vertical, el corte es 0, en caso contrario, 1.
   * crear una nueva pagina de region r.
   * Agregue set1 y/o set2 en la region r y retorna la region r
   * @return struct.Page
   * @param criterio byte
   * @exception java.lang.CloneNotSupportedException The exception description.
   */
  public Page split() throws java.lang.CloneNotSupportedException {
    //Declaracion de las variables.
    Coordinate corte = getAverage(); //Obtener el eje adecuado para cortar
    Enumeration sets = elements.elements();
    Page set1, set2;
    Region father = new Region("Page", zone.getRightUp(), zone.getLeftDown());
    DObject o;

    if (corte.getEje() == (byte) 0) {
    //Inicializar la pagina set1 con su area de MBR respectivo
    //cambiando el valor del eje X del punto superior derecho
      set1 = setMBR(1, corte.getX());
      //Inicializar la pagina set1 con su area de MBR respectivo cambiando
     // el valor del eje X del punto inferior izquierdo
      set2 = setMBR(2, corte.getX());
      //Recolcta los puntos en set1 o set2 de acuerdo al eje seleccionado
      while (sets.hasMoreElements()) {
        o = (DObject) sets.nextElement();
        if (o.getX() <= corte.getX()) {
          set1 = set1.insert(o);
        }
        else {
          set2 = set2.insert(o);
        }
      }
      //agregar el corte al nodo padre
      father.addCut(new Corte(corte.getX(), zone.getRightUp().getY(),
                              corte.getX(), zone.getLeftDown().getY()));
    }
    else {
      //Inicializar la pagina set1 con su area de MBR respectivo
      //cambiando el valor del eje Y del punto superior derecho
      set1 = setMBR(3, corte.getY());
      //Inicializar la pagina set1 con su area de MBR respectivo
      //cambiando el valor del eje Y del punto inferior izquierdo
      set2 = setMBR(4, corte.getY());
      //Recolcta los puntos en set1 o set2 de acuerdo al eje seleccionado
      while (sets.hasMoreElements()) {
        o = (DObject) sets.nextElement();
        if (o.getY() > corte.getY()) {
          set1 = set1.insert(o);
        }
        else {
          set2 = set2.insert(o);
        }
      }
      //agregar el corte al nodo padre
      father.addCut(new Corte(zone.getLeftDown().getX(), corte.getY(),
                              zone.getRightUp().getX(), corte.getY()));
    }

   //Asociar los conjuntos set1 y set2 a un nodo raiz y redefine el area del MBR

    father.getElements().addElement(set1);
    father.getElements().addElement(set2);
    father.getZonas().addElement(set1.getZone());
    father.getZonas().addElement(set2.getZone());

    father.setStatus(Page.CUTED);

    return father;
  }

  /**
   * Retornar la consulta TimeSlice
   * @return java.util.Vector
   * @param time int
   */
  public Vector timeSlice(int time) {

    if (!Util.ylList) {
      //procesar la bitacora basado en Lista Variante
      int t = 0, i;
      NodeTime x = null;

      for (i = 0; i < punteros.size(); i++) {
        t = Integer.parseInt( ( (String) tiempo.elementAt(i)));

        if (time >= t) {
          //procese la region correspondiente y actualiza los cambios
          x = (NodeTime) punteros.elementAt(i);
        }
        else {
          break;
        }

      }
      //Si i sobrepase al umbral del bloque de kdb,
      // entonces se calcula la cantidad de accesos a los bloques siguientes
      if (i > 85) {
        Stadistic.countAccessBlocks += (int) (i / 84);

      }
      if (x == null) {
        System.out.println(
            "no encontro el el puntero adecuado para el tiempo...");

      }
      return x.timeSlice(time);
    }
    else {
      //procesar la bitacora basado en Skip-List
      return cambio.queryTimeSlice(time);
    }
  }
}
