package struct;

import java.util.Vector;
import java.util.HashMap;
import java.util.Hashtable;

public class Util {
  public static int D = 4; // cada D bloques crear uno con foto
  public static short MAX_LEVEL = 2;

  //ylList es verdadero cuando se selecciona la version de Skiplist
  // es falso por defecto cuando se selecciona la version de Lista simple
  public static boolean ylList = true;

  /*--------------------------------------------------------------------------*/
  private static Vector cloneDObjs(Vector datos) {
    Vector nuevo = new Vector(datos.size());
    for (int i = 0; i < datos.size(); i++) {
      nuevo.addElement( ( (DObject) datos.get(i)).cloneDObj());
    }
    return nuevo;
  }

  /*--------------------------------------------------------------------------*/
  static Vector cloneDObjs(Vector datos, int t) {
    Vector nuevo = new Vector(datos.size());
    DObject dObj;
    for (int i = 0; i < datos.size(); i++) {
      dObj = ( (DObject) datos.get(i)).cloneDObj();
      dObj.time = t;
      nuevo.addElement(dObj);
    }
    return nuevo;
  }

  /*--------------------------------------------------------------------------*/
  static int getPositivo(int n) {
    if (n < 0) {
      return (n * -1);
    }
    else {
      return n;
    }
  }

  /* --------------------------------------------------------------------------*/
  static Vector joinVectores(Vector v1, Vector v2) {
    Vector newv = new Vector(v1.size() + v2.size());
    for (int i = 0; i < v1.size(); i++) {
      newv.addElement(v1.get(i));
    }
    for (int i = 0; i < v2.size(); i++) {
      newv.addElement(v2.get(i));
    }
    return newv;
  }

  /*--------------------------------------------------------------------------*/
  static Vector makeInterval(Vector ult_foto, Vector datos, int tA, int tB) {
    DObject dObj;
    int ntimes = tB - tA + 1;
    Hashtable ht = new Hashtable(ntimes);
    Hashtable ht2 = new Hashtable(ntimes);
    Vector tmp;
    Vector interval = new Vector();
    for (int i = 0; i < ntimes; i++) {
      ht.put(String.valueOf(tA + i), Util.cloneDObjs(ult_foto, (tA + i)));
    }
    boolean agregar;
    for (int i = 0; i < datos.size(); i++) {
      agregar = true;
      dObj = ( (DObject) datos.get(i)).cloneDObj();
      Vector vt = ( (Vector) ht.get(String.valueOf(dObj.time)));
      tmp = Util.cloneDObjs(vt);
      for (int z = 0; z < tmp.size(); z++) {
        if ( ( (DObject) tmp.get(z)).oid == dObj.oid) {
          tmp.remove(z--);
          if (dObj.oid < 0) {
            agregar = false;
          }
          break;
        }
      }
      if (agregar) {
        tmp.addElement(dObj);
      }
      ht.remove(String.valueOf(dObj.time));
      ht.put(String.valueOf(dObj.time), Util.cloneDObjs(tmp));
    }
    for (int i = 0; i < ntimes; i++) {
      tmp = Util.cloneDObjs( ( (Vector) ht.get(String.valueOf(tA + i))));
      interval = Util.joinVectores(interval, Util.reduceRepetidos(tmp));
    }
    return interval;
  }

  /*--------------------------------------------------------------------------*/
  static boolean puedeAddObj2BN(BlockNode blockNode) {
    // tiempo real  -> 4 bytes (int)
    // numero de objetos * 14 bytes c/u
    // cantidad de punteros en torre de Skip * 4 bytes c/u
    // puntero a siguiente bloque  -> 4 bytes (long)
    int tot_bytes = 4 + (blockNode.n_objs * 14) + (blockNode.level * 4) + 4;
    if (blockNode.level > 0) {
      tot_bytes += 2; // numero de punteros en torre de Skip -> 2 bytes (short)
    }
    if (blockNode.ptrPhoto != null) {
      tot_bytes += 4; // puntero a Foto   -> 4 bytes (long)
    }
    return (tot_bytes <= (1024 - 14));
  }

  /*--------------------------------------------------------------------------*/
  static boolean puedeAddObjBlockPhoto(BlockPhoto photo) {
    // numero de marcas( cantidad de Fotos en bloque) * 4 bytes por tiempo c/u
    // numero de objetos * 12 bytes c/u , ya que no tienen tiempo
    // 1 byte para las marcas
    int tot_bytes = (photo.n_marcas * 4) + (photo.dObjects.size() * 12) + 1;
    return (tot_bytes <= (1024 - 12));
  }
  /*--------------------------------------------------------------------------*/
  static Vector reduceNegativos(Vector v) {
    Vector nuevo = new Vector();
    DObject obj;
    for (int i = 0; i < v.size(); i++) {
      obj = (DObject) v.get(i);
      if (obj.oid > 0) {
        nuevo.addElement(obj.cloneDObj());
      }
    }
    return nuevo;
  }

  /*--------------------------------------------------------------------------*/
  static Vector reduceRepetidos(Vector v) {
    java.util.Hashtable hs = new java.util.Hashtable();
    DObject d;
    Vector aux = new Vector();
    for (int i = 0; i < v.size(); i++) {
      d = (DObject) v.elementAt(i);
      if (d.getID() < 0) {
        hs.remove(String.valueOf(d.getID() * -1));
      }
      else {
        hs.remove(String.valueOf(d.getID()));
        hs.put(String.valueOf(d.getID()), d);
      }
    }
    java.util.Enumeration e = hs.elements();
    while (e.hasMoreElements()) {
      d = (DObject) e.nextElement();
      aux.addElement(d);
    }
    return aux;
  }
}
