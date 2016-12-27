package struct;

import java.util.*;
import struct.*;

/**
 * Bloques que almacenara las claves del tiempo de cada reconstruccion
 * y la cantidad de puntos del area de reconstruccion y el puntero al
 * area.
 * @author: Lai Chun-Hau
 */
public class NodeTime {
  public static int umbralBlock = 4;

  //Nodo del ultimo area de reconstruccion en el bloque de cambios
  private NodeChange lastAreaRefresh;

  //El ultimo separador de tiempo,
  //se actualiza amedida que inserte cambios
  TimeSeparator lastSeparator;
  //El ultimo tiempo en que construyo el
  //ultimo area de reconstruccion
  private int lastRefreshTime, lastCountRefresh;

  //puntero que guarda los bloques de cambios
  public Vector pointers;

  //times es un matriz de n * 2, en donde
  //la primera columna indica el tiempo de reconsutrccion,
  //mientras que el segundo,
  // la cantidad total de los objetos existentes en el area.
  private int times[][];
  private int size_total;

  /**
   * Crear un nuevo bloque de tiempo, la diferencia entre este y el
   * constructor anterior es que el anterior se construcye en tiempo 0 con
   * los puntos existentes en la hoja de KDB, mientras que este se crea
   * a medida que se llene el bloque de tiempos.
   * Se recibe 3 parametros que son el timepo que ocurre el cambio, el cambio y
   * el ultimo area de reconstruccion para que pueda seguir metiendo los cambios
   * @param time int
   * @param o struct.DObject
   * @param lastRefresh changes.NodeChange
   */
  public NodeTime(int time, DObject o, NodeChange lastRefresh,
                  int lastTimeRefresh, int lastCountRefresh) {
    pointers = new Vector();
    // 1024 = 85 * (4:tiempo + 4:contador + 4:puntero) + 4:bid
    //En un bloque de tiempo, puede existir a lo mas 85 claves de tiempos
    times = new int[85][2];
    times[0][0] = time;
    times[0][1] = -1; //-1 indica que aun no se ha reconstruido
    //Inicializar el ultimo registro del bloque anterior
    lastAreaRefresh = lastRefresh;
    lastRefreshTime = lastTimeRefresh;
    this.lastCountRefresh = lastCountRefresh;

    //inicializar el ultimo tiempo que aparece en el bloque
    lastSeparator = new TimeSeparator(time, 1);

    NodeChange tmp = this.getLastNode(lastAreaRefresh);
    //Agreque el separador en el ultimo bloque de cambios
    tmp.addTimeSeparator(lastSeparator);
    tmp.addChengeParcial(o);

    //Agregue el cambio en el ultimo bloque de cambios y luego
    // subirlo a ser puntero de tiempo time
    pointers.addElement(tmp);

    //Inicializar bytes total existente en el
    //bloque de cambios recien construidos
    // es 24 por :el timepo, el contador, oid, x e y y bid
    size_total = 24;
  }

  /**
   * crear el bloque de tiempos con los puntos existentes en la hoja
   * del arbol KDB en tiempo 0
   * @param points java.util.Vector
   */
  public NodeTime(Vector points) {
    pointers = new Vector();
    // 1024 = 85 * (4+4+8) tiempo + contador + puntero
    times = new int[85][2];
    times[0][0] = 0;
    times[0][1] = points.size();

    lastAreaRefresh = new NodeChange(points);
    lastRefreshTime = 0;
    lastCountRefresh = points.size();

    pointers.addElement(lastAreaRefresh);
    //En el primer bloque de cambios, ya existen el primer area de
    //reconstruccion, por eso se calcula el tamanio ocupado con
    //todos los puntos y el bid
    size_total = points.size() * 12 + 4;
  }

  /**
   * Construir el puntero nuevo que corresponde al tiempo nuevo
   * inicializando el tamanio umbral en 4 bytes por la clave del ultimo tiempo
   * insertado
   * @param time int
   */
  private void construcPointerChange(int time) {
    times[pointers.size()][0] = time;
    times[pointers.size()][1] = -1;

    //Retorna el ultimo nodo del area de reconstruccion

    NodeChange n = ( (NodeChange) pointers.elementAt(pointers.size() - 1));

    n = getLastNode(n);
    lastSeparator = new TimeSeparator(time, 0);

    n.addTimeSeparator(lastSeparator);
    n = this.getLastNode(n);
    // Agregue el ultimo bloque que almacena el cambio al set de conjuntos
        pointers.addElement(n);
    //time + contador del cambio + puntero
    size_total = 8;

  }

  /**
   * Retornar la cantidad de puntos aparecidos y tambien la cantidad de puntos
   * eliminados en el evento
   * @return java.lang.int[][]
   * @param t int
   */
  public Vector event(Zone z, int t) {
    //inicializar la variable estadistica en cero
    // la primera posicion es la cantidad aparecido,
    // mientras que el segundo el lo desaparecido

    NodeChange tmp = lastAreaRefresh;

    Vector v, datos = new Vector();
    DObject d;
    Object o;
    TimeSeparator s;
    //lastTime: variable que registra el tiempo en que ocurrio el ultimo area
    //de reconstruccion anterior al tiempo t, mientras que lastCount es
    // la cantidad de puntos reconstruidos en aquel tiempo lastTime
    int lasttime = lastRefreshTime, lastCount = lastCountRefresh;

    boolean flag = false; //es verdadero cuando no hay que procesar mas cambios

    int cant = 0, timeTmp = 0;

    //Acceso a un bloque del tiempo, se debe contabilizar
    Stadistic.countAccessBlocks++;
    if (t == 0) {
      return datos;
    }
    tmp = (NodeChange) pointers.elementAt(0);
    //Encontrar la reconstruccion anterior al tiempo t
    for (int i = 0; i < pointers.size(); i++) {
      if (times[i][0] < t) {
        lasttime = times[i][0];
        tmp = (NodeChange) pointers.elementAt(i);
      }
      else {
        break;
      }
    }

    while (tmp.getChanges().size() == 0) {
      //Acceso al bloque de cambios
      Stadistic.countAccessBlocks++;
      //cuando el bloque de cambios esta lleno por varios objetos en el
      //area de reconstruccion y lo cual implica no haber ningun cambio en el
      //bloque,sigue procesando
      //hasta encontrarse con el bloque que contiene los cambios

      tmp = tmp.getNext();
    }

    //Procesar los cambios posteriores
    while (tmp != null) {
      v = tmp.getChanges();
      //Si no hay cambios, se sale del ciclo
      if (v.size() == 0) {
        break;
      }
      for (int i = 0; i < v.size(); i++) {
        o = v.elementAt(i);
        if (o instanceof TimeSeparator) {
          //chequear el tiempo, si los cambios llevan el tiempo
          // menor al lastTime, entonces se salta
          s = (TimeSeparator) o;
          if (s.time == t) { //el tiempo debe ser = al t que esta buscando
            cant = s.count;
          }
          else
          if (s.time < t) {
            continue;
          }
          else {
            //si s.time es mayor al t, entonces signifique que
            //no hay cambios en el bloque
            flag = true;
            break;
          }
        }
        else
        if (cant != 0) {
          //disminuir el contador, lo que implica saber hasta
          //donde parar de procesar,cuando llega a ser igual 0
          //significa que no hay mas cambios en este tiempo.
          cant--;
          d = (DObject) o;
          if (z.isIncluded(d)) {
            datos.addElement(d);

          }
        }
      }
      if (!flag) {
        //por cada bloque de cambios que accesa, se aumenta el contador
        Stadistic.countAccessBlocks++;
        tmp = tmp.getNext();
        if (tmp == null) {
          Stadistic.countAccessBlocks--;
        }
      }
      else {
        tmp = null;
      }
    }
    return datos;
  }

  /**
   * Retornar el puntero que apunte al ultimo area de reconstruccion
   * @return changes.NodeChange
   */
  public NodeChange getLastAreaRefresh() {
    return lastAreaRefresh;
  }

  /**
   * Retornar la cantidad total existente en el area de reconstruccion
   * @return int
   */
  public int getLastCountRefresh() {
    return lastCountRefresh;
  }

  /**
   * Retornar el ultimo nodo
   * @return changes.NodeChange
   * @param node changes.NodeChange
   */
  private NodeChange getLastNode(NodeChange node) {
    NodeChange tmp = node;
    while (tmp.getNext() != null) {
      tmp = tmp.getNext();
    }
    return tmp;
  }

  /**
   * Retornar el ultimo cambio que reconstruyo el area
   * @return int
   */
  public int getLastTimeRefresh() {
    return lastRefreshTime;
  }

  /**
   * retorna las claves del tiempo existentes en el bloque de tiempo
   * @return int[]
   */
  public int[] getTimesExist() {
    int n[] = new int[pointers.size()];
    for (int i = 0; i < n.length; i++) {
      n[i] = times[i][0];
    }
    return n;
  }

  /**
   * Insertar el cambio en el tiempo correspondiente
   * Si el bloque este lleno entonces no insertarlo en este bloque
   * sino si la ultima vez que inserto el cambio produce la reconstruccion del
   * area entonces se crea una nueva clave del tiempo y un puntero nuevo que
   * inserte el separador del tiempo y el cambio,
   * sino si ya sobrepase el umbral de los cambios, se cheque si el nuevo
   * cambio que entre pertenece el tiempo anterior o no, si pertenece,
   * entonces no se reconstruye el area sino que se sigue insertando en la lista
   * de los cambios, si no pertences, se construye el area
   * @return boolean
   * @param time int
   * @param o struct.DObject
   */
  public boolean insetChange(int time, DObject o) {
    int cant = pointers.size();
    //Chequear si se lleno el bloque o no
    if (cant == 85 && times[cant - 1][1] != -1) {
      return false;
    }
    else
    if (size_total + 12 > 1024 * umbralBlock) {
      if (lastSeparator.time != time) {
        //  reconstruye el area y crea un nuevo puntero
        this.refreshPoints();
        this.construcPointerChange(time);
      }
      if (cant == 85) {
        return false;
      }
    }
    else
    if (times[cant - 1][1] != -1) {
      //construir un nuevo puntero y una nueva clave del tiempo
      this.construcPointerChange(time);
      // cheque si se hayan sobrepasado el umbral o no
    }
    //cant es la cantidad de elementos total,
    //se actualiza a medida que invoque el metodo
    //this.construcPointerChange(time);

    NodeChange n;
    cant = pointers.size();
    //si viene un cambio de un tiempo nuevo,
    // se crea un nuevo separador de tiempo
    if (time != lastSeparator.time) {
      lastSeparator = new TimeSeparator(time, 0);
      //obtener el ultimo bloque de cambio
      n = (NodeChange) pointers.elementAt(cant - 1);
      //agregar el separador a este bloque
      n.addTimeSeparator(lastSeparator);
      // si al agregar este separador provoca una saturacion en el bloque
      // y por consiguiente crea un nuevo bloque,
      //hay que referenciar el puntero al
      // ultimo bloque
      n = getLastNode(n);
      pointers.setElementAt(n, cant - 1);
      size_total += 8;
    }

    times[cant - 1][0] = time;

    lastSeparator.count++;
    //obtener el ultimo bloque de cambios
    n = (NodeChange) pointers.elementAt(cant - 1);
    //agregar el cambio al bloque
    n.addChengeParcial(o);
    n = getLastNode(n);
    pointers.setElementAt(n, cant - 1);
    //aumentar el tamanio del bloque
    size_total += 12;

    return true;
  }

  /**
   * Procesar los cambios existentes desde tiempo 1 hasta tiempo 2;
   * @return java.util.Vector
   * @param t2 int
   */
  public Vector interval(int ti, int tf) {
    NodeChange tmp = lastAreaRefresh;
    Hashtable hs = new Hashtable();
    Vector v, finish = new Vector();
    DObject d, tempd;
    Object o;
    TimeSeparator s;
    //lastTime: variable que registra el tiempo en que ocurrio el ultimo area
    //de reconstruccion anterior al tiempo t, mientras que lastCount es
    // la cantidad de puntos reconstruidos en aquel tiempo lastTime
    int lasttime = lastRefreshTime, lastCount = lastCountRefresh;
//es verdadero cuando no hay que procesar mas cambios
    boolean flag = false,
        flag2 = false;
    //es verdadero cuando haya cambios en entre el intervalo de senalado

    int cant = 0, timeTmp = 0, anterior = ti;

    //Acceso a un bloque del tiempo, se debe contabilizar
    Stadistic.countAccessBlocks++;

    for (int i = 0; i < pointers.size(); i++) {
      if (times[i][0] <= ti && times[i][1] != -1) {
        lasttime = times[i][0];
        lastCount = times[i][1];
        tmp = (NodeChange) pointers.elementAt(i);
      }
      else {
        break;
      }
    }

    while (lastCount != cant) {
      //Acceso al bloque de cambios
      Stadistic.countAccessBlocks++;
      v = tmp.getRefreshPoints();
      cant += v.size();
      for (int i = 0; i < v.size(); i++) {
        d = (DObject) v.elementAt(i);
        hs.put(String.valueOf(d.getID()), d);
      }
      //si faltan puntos en el area re reconstruccion, se
      // sigue procesando
      if (lastCount > cant) {
        tmp = tmp.getNext();

      }
    }
    NodeChange ant = tmp;

   //se procesa el cambio y actualizacionpor cada tiempo incluido en el interval
        for (int t = ti; t <= tf; t++) {
      flag = false;
      //Procesar los cambios posteriores
      while (tmp != null) {
        v = tmp.getChanges();
        //Si no hay cambios, se sale del ciclo
        if (v.size() == 0) {
          break;
        }
        for (int i = 0; i < v.size(); i++) {
          o = v.elementAt(i);
          if (o instanceof TimeSeparator) {
            //chequear el tiempo, si los cambios llevan el tiempo
            // menor al lastTime, entonces se salta
            s = (TimeSeparator) o;
            if (s.time <= lasttime) {
              continue;
            }
            else //el tiempo debe ser <= al t que esta buscando
            if (s.time <= t) {
              cant = s.count;
            }
            else {
              //si s.time es mayor al t, entonces signifique que
              //no hay que procesar mas cambios
              flag = true;

              break;
            }
          }
          else
          if (cant != 0) {
            //disminuir el contador, lo que implica saber hasta
            //donde parar de procesar,cuando llega a ser igual 0
            //significa que no hay mas cambios en este tiempo.
            cant--;
            d = (DObject) o;
            //El actualizar consiste en eliminar un elemento o
            //ingresar un elemnto
            if (d.getID() < 0) {
              hs.remove(String.valueOf(Math.abs(d.getID())));
            }
            else {
              if (hs.contains(String.valueOf(d.getID()))) {
                hs.remove(String.valueOf(d.getID()));
              }
              hs.put(String.valueOf(d.getID()), d);
            }
          }
        }
        if (!flag) {

          tmp = tmp.getNext();
          Stadistic.countAccessBlocks++;
          // si el siguinte bloque es igual al null significa que es
          //invalido el acceso por eso se disminuye en uno
          if (tmp == null) {
            Stadistic.countAccessBlocks--;
          }
        }
        else {
          // si no hay mas cambios en este bloque en el instante ti,
          //entonces se sale del ciclo.
          break;
        }
      }
      //agregar los objetos actualizados en tiempo ti al vector general
      //terminal para luego mostrar los resultados
      Enumeration e = hs.elements();
      while (e.hasMoreElements()) {
        d = (DObject) e.nextElement();
        d = new DObject(Math.abs(d.getID()), d.getX(), d.getY(), t, 'i');
        finish.addElement(d);

      }
    }

    return finish;
  }

  /**
   * Reconstruir la foto desde el area de la reconstruccion anterior
   * hasta el ultimo tiempo
   * Creation date: (2004/10/13 �U�� 02:19:29)
   */
  private void refreshPoints() {
    NodeChange tmp = lastAreaRefresh;
    Hashtable hs = new Hashtable();
    Vector v;
    DObject d;
    Object o;

    int cant = 0;
    //almacenar los puntos del ultimo area de reconstrucicon
    // en el hash table para facilitar la actualizacion con los
    // cambios posteriores
    while (cant < lastCountRefresh) {
      v = tmp.getRefreshPoints();
      cant += v.size();
      for (int i = 0; i < v.size(); i++) {
        d = (DObject) v.elementAt(i);
        hs.put(String.valueOf(d.getID()), d);
      }
      if (cant < lastCountRefresh) {
        tmp = tmp.getNext();
      }
    }

    //Procesar los cambios posteriores
    cant = 0;
    while (tmp != null) {
      v = tmp.getChanges();
      for (int i = 0; i < v.size(); i++) {
        o = v.elementAt(i);
        if (o instanceof TimeSeparator) {
          if ( ( (TimeSeparator) o).time > lastRefreshTime) {
            cant = ( (TimeSeparator) o).count;
          }
        }
        else if (cant != 0) {
          //disminuir el contador,
          //lo que implica saber hasta donde parar de procesar,
          // cuando llega a ser igual 0 significa que no
          //hay mas cambios en este tiempo.
          cant--;
          d = (DObject) o;
          //El actualizar consiste en eliminar un elemento
          // o ingresar un elemnto
          if (d.getID() < 0) {
            hs.remove(String.valueOf(Math.abs(d.getID())));
          }
          else {
            if (hs.contains(String.valueOf(d.getID()))) {
              hs.remove(String.valueOf(d.getID()));
            }
            hs.put(String.valueOf(d.getID()), d);
          }
        }
      }
      tmp = tmp.getNext();
    }

    //Agregar los puntos de reconstruccion existente en hs en
    // el bloque nodeChange

    Enumeration e = hs.elements();
    //Empezar desde el ultimo nodo de cambio
    tmp = getLastNode(lastAreaRefresh);

    //NodeChange aux = tmp;

    while (e.hasMoreElements()) {
      d = (DObject) e.nextElement();
      d = new DObject(Math.abs(d.getID()), d.getX(), d.getY());
      tmp.addRefreshPoints(d);
    }
    //Enlazar un puntero al ultimo nodo del area de reconstruccion
    tmp.setNextTimeNode(this.getLastNode(tmp));
    //Actualizar el ultimo nodeo de area de reconstruccion y el tiempo
    lastAreaRefresh = tmp;
    lastRefreshTime = lastSeparator.time;
    lastCountRefresh = times[pointers.size() - 1][1] = hs.size();

    pointers.setElementAt(lastAreaRefresh, pointers.size() - 1);

  }

  /**
   * Retornar los datos hasta el tiempo t
   * @return java.util.Vector
   * @param t int
   */
  public Vector timeSlice(int t) {
    NodeChange tmp = lastAreaRefresh;
    Hashtable hs = new Hashtable();
    Vector v;
    DObject d;
    Object o;
    TimeSeparator s;
    //lastTime: variable que registra el tiempo en que ocurrio el ultimo area
    //de reconstruccion anterior al tiempo t, mientras que lastCount es
    // la cantidad de puntos reconstruidos en aquel tiempo lastTime
    int lasttime = lastRefreshTime, lastCount = lastCountRefresh;

    boolean flag = false; //es verdadero cuando no hay que procesar mas cambios

    int cant = 0, timeTmp = 0;

    //Acceso a un bloque del tiempo, se debe contabilizar
    Stadistic.countAccessBlocks++;

    for (int i = 0; i < pointers.size(); i++) {
      if (times[i][0] <= t && times[i][1] != -1) {
        lasttime = times[i][0];
        lastCount = times[i][1];
        tmp = (NodeChange) pointers.elementAt(i);
      }
      else {
        break;
      }
    }

    while (lastCount != cant) {
      //Acceso al bloque de cambios
      Stadistic.countAccessBlocks++;
      v = tmp.getRefreshPoints();
      cant += v.size();
      for (int i = 0; i < v.size(); i++) {
        d = (DObject) v.elementAt(i);
        hs.put(String.valueOf(d.getID()), d);
      }
      //si faltan puntos en el area re reconstruccion, se
      // sigue procesando
      if (lastCount > cant) {
        tmp = tmp.getNext();

      }
    }

    //Procesar los cambios posteriores
    while (tmp != null) {
      v = tmp.getChanges();
      //Si no hay cambios, se sale del ciclo
      if (v.size() == 0) {
        break;
      }
      for (int i = 0; i < v.size(); i++) {
        o = v.elementAt(i);
        if (o instanceof TimeSeparator) {
          //chequear el tiempo, si los cambios llevan el tiempo
          // menor al lastTime, entonces se salta
          s = (TimeSeparator) o;
          if (s.time <= lasttime) {
            continue;
          }
          else if (s.time <= t) { //el tiempo debe ser <= al t que esta buscando
            cant = s.count;
          }
          else {
            //si s.time es mayor al t, entonces signifique que
            //no hay que procesar mas cambios
            flag = true;
            break;
          }
        }
        else if (cant != 0) {
    //disminuir el contador, lo que implica saber hasta donde parar de procesar,
  // cuando llega a ser igual 0 significa que no hay mas cambios en este tiempo.
          cant--;
          d = (DObject) o;
          //El actualizar consiste en eliminar un elemento o ingresar un elemnto
          if (d.getID() < 0) {
            hs.remove(String.valueOf(Math.abs(d.getID())));
          }
          else {
            if (hs.contains(String.valueOf(d.getID()))) {
              hs.remove(String.valueOf(d.getID()));
            }
            hs.put(String.valueOf(d.getID()), d);
          }
        }
      }
      if (!flag) {

        tmp = tmp.getNext();
        //Acceso al bloque de cambios
        Stadistic.countAccessBlocks++;
        if (tmp == null) {
          Stadistic.countAccessBlocks--;
        }
      }
      else { // sino hay mas cambios en el tiempo ti, termina de procesar
        tmp = null;
      }
    }
    //agregar los todos los objetos en el area de reconstruccion al vector final
    v = new Vector(hs.size());
    Enumeration e = hs.elements();
    while (e.hasMoreElements()) {
      d = (DObject) e.nextElement();
      d = new DObject(Math.abs(d.getID()), d.getX(), d.getY());
      v.addElement(d);
    }

    return v;
  }
}
