package struct;

import java.util.*;

/**
 * En esta clase se almacenara todas las regiones que sera observado por SABD.
 * Dado el tamanio determinado de un bloque en el disco, solamente se puede
 * almacenaruna cantidad determinada de regiones (punteros) en esta clase.
 * Se implementa el metodo
 * insertar segun las caracteristicas de una pagina Region.
 * @author: Chun-Hau Lai
 */
public class Region
    extends Page {
  private java.util.Vector cortes, zonas;

  /**
   * Instanciar los atributos de una pagina regional con su id, el punto
   * derecho superior junto con el de izquierdo inferior de MBR
   * @param id java.lang.String
   * @param rUp struct.Coordinate
   * @param lDown struct.Coordinate
   */
  public Region(String id, Coordinate rUp, Coordinate lDown) {
    super(id, lDown, rUp);
    cortes = new Vector();
    zonas = new Vector();
  }

  /**
   * Agregar la linea de corte a la region
   * @param cut struct.Corte
   */
  public void addCut(Corte cut) {
    if (cut == null) {
      System.out.println("Es nulo");
    }
    cortes.addElement(cut);
  }

  /**
   * Insert the method's description here.
   * Creation date: (2004/8/23 ?? 05:49:00)
   * @param pnode javax.swing.tree.DefaultMutableTreeNode
   * @param n struct.Page
   */
  public void addNodes(javax.swing.tree.DefaultMutableTreeNode pnode) {
    Enumeration e = getElements().elements();

    String valorxy1, valorxy2;

    while (e.hasMoreElements()) {
      Page elem = (Page) e.nextElement();

      valorxy1 = String.valueOf(elem.getZone().getLeftDown().getX()) + "," +
          String.valueOf(elem.getZone().getLeftDown().getY());
      valorxy2 = String.valueOf(elem.getZone().getRightUp().getX()) + "," +
          String.valueOf(elem.getZone().getRightUp().getY());
      javax.swing.tree.DefaultMutableTreeNode node = new javax.swing.tree.
          DefaultMutableTreeNode(valorxy1 + ":" + valorxy2);
      pnode.add(node);
      elem.addNodes(node);
    }
  }

  /**
   * Calcular la cantidad total de los elementos totales de cada nivel.
   * y almacenar la informacion de cada nivel en el vector.
   * @return java.util.Vector
   */
  public Vector calculateStadistic() {
    Vector v = new Vector();
    Stadistic tmp;

    //	Enumeration elems = getElements().elements();
    LinkedList lists = new LinkedList();

    lists.add(this);
    int nivel = 0, count_total = 0, sum_avance = 0, countBloks = 0;
    Page n;
    Object o;

    while (!lists.isEmpty()) {
      if (sum_avance == 0) {
        count_total = lists.size();
        //Tomar la cantidad total de los elementos en un nivel

      }
      sum_avance++; //puntero al elemento indicado en posiscion i;

      o = lists.removeFirst();
      if (o instanceof Page) {
        n = (Page) o;
        if (!Util.ylList) {
          if (o instanceof Point) {
            Vector punteros = ( (Point) o).getPunterosANodeTime();
            if (punteros != null) {
              //Si los puntos ha sido montado en la lista de cambios,
              // entonces se cuenta los bloques de cambios
              countBloks += punteros.size();
              NodeTime nodetime = (NodeTime) punteros.elementAt(0);
              NodeChange nodechange = (NodeChange) nodetime.pointers.elementAt(
                  0);
              while (nodechange != null) {
                countBloks++;
                nodechange = nodechange.getNext();
              }
            }
            else { // si no se cuenta los puntos existentes en las hojas no mas
              countBloks += getElements().size();
            }
          }
        }
        else {
          if (o instanceof Point) {
            Change cambio = ( (Point) o).getChange();

            if (cambio != null) {

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

              //
              //countBloks += cambio.n_blocks;
            }
            else {
              countBloks += getElements().size();
            }
          }
        }
        //Si se ha llegado al ultimo elemento de un nivel,
        //se registra en la variable
        //Stadistica el nivel
        if (sum_avance == count_total) {
          tmp = new Stadistic(++nivel, count_total);
          sum_avance = 0;
          v.addElement(tmp);
        }
        lists.addAll(n.getElements());
      }
      else {
        //Aqui cuando se ha llegado al ultimo elemento de las hojas,
       // se le registra la cantidad total de los bloques de bitagora
      // existentes dentro de las hojas
        tmp = new Stadistic(++nivel, countBloks);
        v.addElement(tmp);
        break;
      }
      //Si puntero llega a ser igual que la cantidad total de elementos,
      // signifique que el siguiente elemento pertenece a otro nivel

    }

    return v;
  }

  /**
   * Calcular la utilizacion sacando todos los elementos del arbol
   * @return struct.Stadistic
   */
  public Stadistic calculateUtilization() {
    Stadistic s = new Stadistic();
    LinkedList list = new LinkedList();
    Page n;
    int countBlocks = 1, //primer bloque leido
        countPoints = 0;

    list.addAll(getElements());

    while (!list.isEmpty()) {
      n = (Page) list.removeFirst();
      countBlocks++;
      if (n instanceof Region) {
        list.addAll(n.getElements());
      }
      else {
        if (!Util.ylList) {
          Vector punteros = ( (Point) n).getPunterosANodeTime();
          if (punteros != null) {
            //Si los puntos ha sido montado en la lista de cambios,
            // entonces se cuenta los bloques de cambios
            countBlocks += punteros.size();
            NodeTime nodetime = (NodeTime) punteros.elementAt(0);
            NodeChange nodechange = (NodeChange) nodetime.pointers.elementAt(0);
            while (nodechange != null) {
              countBlocks++;
              nodechange = nodechange.getNext();
            }
          }
          else { // si no se cuenta los puntos existentes en las hojas no mas
            countBlocks += getElements().size();
          }
        }
        else {
          Change cambio = ( (Point) n).getChange();

          if (cambio != null) {
            countBlocks += cambio.n_blocks;
          }
          else {
            countBlocks += getElements().size();
            //yerko part..
          }
        }
        countPoints += n.getElements().size();
      }
    }
    Stadistic.countAccessBlocks += countBlocks;
    s.setCountBlocksAndPoints(countBlocks, countPoints);

    return s;
  }

  /**
   * Este metodo no hace nada.
   * @return struct.Coordinate
   */
  public Coordinate getAverage() {
    return null;
  }

  /**
   * Buscar la region correspondiente e insertar el cambio
   * @param time int
   * @param d struct.DObject
   */
  public void insert(int time, DObject d) {
    Page n;
    Zone z;

    for (int i = 0; i < zonas.size(); i++) {
      z = (Zone) zonas.elementAt(i);
     //buscar la zona correspondiente y luego insertar el cambio correspondiente
      if (z.isIncluded(d)) {
        n = (Page) getElements().elementAt(i);
        n.insert(time, d);
      }
    }

  }

  /**
   * Insertar un elemento en la pagina de regiones.
   * Ante de insertar, se verificara todas las regiones
   * existentes con la ubicacion del elemento nuevo.
   *  Si el area de cierta region cubre la ubicacion del elemento,
   * se insertara en esta region. Si la region contiene varias regiones mas,
   * se llamara recursivamente el metodo insert(Element e)
   * hasta encontrarse con una pagina de puntos para realizar la operacion final
   * de insercion.
   * Si ocurre overflow, se realiza el corte buscando primero el eje central,
   * luego aplicar el corte. Luego, se ajusta el tamanio de las paginas de
   *  puntos. Asi se obtiene dos paginas de sub-regiones que contenga en si
   * la mitad aproximada de las regiones existentes originalmente
   * y se enlaza a una pagina regional.
   * @return struct.Page
   * @param elem org.omg.CORBA.Object
   * @exception java.lang.CloneNotSupportedException The exception description.
   */
  public Page insert(Object elem) throws java.lang.CloneNotSupportedException {

    Page n = null, tmp = null;

  //Chequear la ubicacion correcta a la cual deberia quedarse el elemento nuevo.
    for (int i = 0; i < getElements().size(); i++) {
      n = (Page) getElements().elementAt(i);
      if (n.isIncluded( (DObject) elem)) {
        n = n.insert(elem);
        if (n == null) {
          break;
        }
        else {
          if (n.getStatus() == Page.CUTED) {
            getElements().addAll(n.getElements());
            getElements().remove(i);
            getZonas().addAll( ( (Region) n).getZonas());
            getZonas().remove(i);
            this.addCut( ( (Region) n).returnFirstCut());
            if (getElements().size() > Page.SIZEREGION) {
              tmp = split();
              tmp.setStatus(Page.CUTED);
            }
            else {
              tmp = (Region)this.clone();
              tmp.setStatus(Page.NOTFULL);
            }
          }
          else {
            tmp = (Region)this.clone();
          }
          break;
        }
      }
    }
    return tmp;
  }

  /**
   * Montar todos los puntos en la hoja en la lista
   */
  public void mountPointAtList() {
    Page n;
    for (int i = 0; i < getElements().size(); i++) {
      n = (Page) getElements().elementAt(i);
      n.mountPointAtList();
    }
  }

  /**
   * Imprimir el contenido del Arbol conpleto por nivel.
   */
  public void printAll(String salto) {
    try {

      Page n;
      g.setColor(java.awt.Color.blue);
      //Enumeration sets = getElements().elements();
      Enumeration lists = getElements().elements();

    int length = getZone().getRightUp().getX() - getZone().getLeftDown().getX(),
        width = getZone().getRightUp().getY() - getZone().getLeftDown().getY();
      g.fillRect(
          getZone().getLeftDown().getX() / 100,
          getZone().getLeftDown().getY() / 100,
          length / 100,
          width / 100);
      g.clearRect(
          getZone().getLeftDown().getX() / 100 + 10,
          getZone().getLeftDown().getY() / 100 + 10,
          length / 100 - 20,
          width / 100 - 20);


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

      while (lists.hasMoreElements()) {
        n = (Page) lists.nextElement();
        n.printAll(salto + "    ");

      }
    }
    catch (InterruptedException e) {
    }

  }

  /**
   * Retorna el primer corte que se realiza en una pagina regional
   * @return struct.Corte
   */
  public Corte returnFirstCut() {
    return (Corte) cortes.elementAt(0);
  }

  /**
   * Redefine el area abarcada por una zona en 4 maneras
   * 1: Cambie el valor del eje X del punto superior derecho.
   * 2: Cambie el valor del eje X del punto inferior izquierdo.
   * 3: Cambie el valor del eje Y del punto superior derecho.
   * 4: Cambie el valor del eje Y del punto inferior derecho.
   * @return struct.Page
   * @param criterio int
   * @param valorEje int
   */
  public Page setMBR(int criterio, int valorEje) {
    Region tmp = null;
    switch (criterio) {
      case 1:
        tmp = new Region("Page ",
                         new Coordinate(valorEje, zone.getRightUp().getY()),
                         zone.getLeftDown()
                         );
        break;
      case 2:
        tmp = new Region("Page ",
                         zone.getRightUp(),
                         new Coordinate(valorEje, zone.getLeftDown().getY())
                         );

        break;
      case 3:
        tmp = new Region("Page ",
                         getZone().getRightUp(),
                         new Coordinate(getZone().getLeftDown().getX(),
                                        valorEje)
                         );
        break;
      case 4:
       tmp = new Region("Page ",
                        new Coordinate(getZone().getRightUp().getX(), valorEje),
                        getZone().getLeftDown()
                        );

        break;

    }
    return tmp;
  }

  /**
   * Dividir pagina de regiones n de acuerdo al primer corte existente
   * Por cada elemento elem de la region n hacer:
   *		Si elem esta abarcado y/o intersectadas por eje entonces
   *			elem = elem.dividir(eje)
   *			agregar elem en set1
   *		Fin-Si
   *		Si elem esta abarcado y/o intersectadas por eje entonces
   *			elem = elem.dividir(eje)
   *			agregar elem en set2
   *		Fin-Si
   * Si la cantidad de las regiones abarcadas por las regiones set1 y/o set2
   * son superiores al limite preestablecido, se vuelven a dividir las paginas
   * aparte de acuerdo al criterio que corresponda.
   *	Crear una nueva pagina de region r.
   *	Agregue set1 y/o set2 en la region r
   *	Retorna r
   * @return struct.Page
   * @param criterio byte
   * @exception java.lang.CloneNotSupportedException The exception description.
   */
  public Page split() throws java.lang.CloneNotSupportedException {
    Region set1, set2,
        //crear un nodo padre que contendra los dos nodos cortados
     root = new Region("page", getZone().getRightUp(), getZone().getLeftDown());
    //obtener el primer corte
    Corte corte = this.returnFirstCut(), corteTmp;
    cortes.remove(corte);
    //obtener todas las subregiones  y los cortes existntes.
    Enumeration paginas = getElements().elements(),
        corteExistente = cortes.elements();
    Page tmp;

    if (corte.getEje() == (byte) 0) {

      //crear los dos conjuntos de acuerdo al eje de corte
      set1 = (Region) setMBR(1, corte.getPoint1().getX());
      set2 = (Region) setMBR(2, corte.getPoint1().getX());

      while (paginas.hasMoreElements()) {
        tmp = (Page) paginas.nextElement();
        //chequear a cual conjunto debe ir una subregion dividida
        if (tmp.getZone().getLeftDown().getX() < corte.getPoint1().getX()) {
          set1.getElements().addElement(tmp);
          set1.getZonas().addElement(tmp.getZone());
        }
        else {
          set2.getElements().addElement(tmp);
          set2.getZonas().addElement(tmp.getZone());
        }
      }

      while (corteExistente.hasMoreElements()) {
        corteTmp = (Corte) corteExistente.nextElement();
        if (corteTmp.getPoint1().getX() < corte.getPoint1().getX()) {
          set1.addCut(corteTmp);
        }
        else {
          set2.addCut(corteTmp);
        }
      }

    }
    else {
      //crear los dos conjuntos de acuerdo al eje de corte
      set1 = (Region) setMBR(3, corte.getPoint1().getY());
      set2 = (Region) setMBR(4, corte.getPoint1().getY());
      while (paginas.hasMoreElements()) {
        tmp = (Page) paginas.nextElement();
        //chequear a cual conjunto debe ir una subregion dividida
        if (tmp.getZone().getRightUp().getY() > corte.getPoint1().getY()) {
          set1.getElements().addElement(tmp);
          set1.getZonas().addElement(tmp.getZone());
        }
        else {
          set2.getElements().addElement(tmp);
          set2.getZonas().addElement(tmp.getZone());
        }
      }
      while (corteExistente.hasMoreElements()) {
        corteTmp = (Corte) corteExistente.nextElement();
        if (corteTmp.getPoint1().getY() > corte.getPoint1().getY()) {
          set1.addCut(corteTmp);
        }
        else {
          set2.addCut(corteTmp);
        }
      }
    }
    //agregar los dos regiones nuevas al padre.
    root.getElements().addElement(set1);
    root.getElements().addElement(set2);
    root.getZonas().addElement(set1.getZone());
    root.getZonas().addElement(set2.getZone());
    root.addCut(corte);
    root.setStatus(Page.CUTED);

    return root;

  }

  /**
   * getZonas
   *
   * @return Vector
   */
  public Vector getZonas() {
    return this.zonas;
  }
}
