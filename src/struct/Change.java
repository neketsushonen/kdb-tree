package struct;

import java.util.Vector;
import java.io.*;

public class Change {
  Skip skip;
  private BlockNode lastBlockNode;
  private BlockPhoto lastBlockPhoto;
  public int n_blocks;
  private int lastTimeReal;
  private Vector vpage;
  private int n_access_interval;
  private int n_access_timeslice;
  private int n_access_event;
  private int n_access_photo;

  public Change(Vector page) {
    lastBlockNode = new BlockNode(Util.MAX_LEVEL, -1, null, null);
    skip = new Skip(lastBlockNode);
    skip.indexNode.ptrNext = this.lastBlockNode;
    lastBlockPhoto = null;
    vpage = page;
    n_blocks = 1;
    lastTimeReal = -1;
  }

  /*--------------------------------------------------------------------------*/
  public void addObj(DObject obj) {
    if (lastBlockNode.time == -1) {
      lastBlockNode.time = obj.time; // actualiza tiempo del 1?BN
      lastTimeReal = obj.time; // guarda tiempo del ultimo BN creado
    }
    if (!Util.puedeAddObj2BN(lastBlockNode)) { // si objeto no cabe
      makeBlockNode(obj.time); // se crea un nuevo BlockNode
    }
    obj.time -= lastTimeReal; // transforma tiempo del obj. a tiempo relativo
    lastBlockNode.addObj(obj); // agrega objeto al ultimo BN
  }

  /*--------------------------------------------------------------------------*/
  private void makeBlockNode(int time) {
    BlockPhoto ptrPhoto = null;
    if ( (n_blocks % Util.D) == 0) { //umbral que indica crear Foto
      ptrPhoto = makePhoto(time); // Crea foto
    }
    BlockNode newBN = new BlockNode( (short) 0, time, lastBlockNode, ptrPhoto);

    skip.addNode(newBN); // se agrega BN a skiplist
    lastBlockNode = newBN; // guarda ultimo BN creado (newBN)
    lastTimeReal = time; // guarda tiempo del ultimo BN creado
    if (ptrPhoto == null) { // si no se creo foto, entonces :
      n_blocks++; // aumenta el numero de bloques creados
    }
    skip.indexNode.ptrNext = lastBlockNode; // actualiza ptro. del indexNode
  }

  /*--------------------------------------------------------------------------*/
  private BlockPhoto makePhoto(int time) {
    BlockNode cursor = lastBlockNode;
    Vector vobjs = new Vector();
    Vector vobjsPhoto = null;
    DObject dObj;

    while (cursor != null) { //Recorre los BN hacia atras desde el ultimo creado
      for (int x = cursor.n_objs - 1; x >= 0; x--) { // recorre BN apuntado
        dObj = cursor.getObj(x).cloneDObj(); // obtiene una copia del objeto
        dObj.time = (dObj.time + cursor.time); // vuelve tiempo del obj. al real
        vobjs.add(0, dObj); // almacena objeto obtenido
      }
      if (cursor.ptrPhoto != null) { // si encuentra foto apuntada por actual BN
        vobjsPhoto = getPhoto(cursor.ptrPhoto, cursor.time); // obtiene foto
        break;
      }
      cursor = cursor.ptrNext; // avanza a siguiente BN (apuntado hacia atras)
    }
    if (cursor == null) { // si no existe ninguna foto creada hasta el momento
      vobjs = Util.joinVectores(this.vpage, vobjs); //une hoja de KDB y vobjs
    }
    else { // si enonctro una foto, une foto con vobjs (cambios)
      vobjs = Util.joinVectores(vobjsPhoto, vobjs);
    }
    vobjs = Util.reduceRepetidos(vobjs); //deja solo ultimo cambio para cada obj.
    addObjs2BP(vobjs, time); // agrega objetos objs. a BP
    return lastBlockPhoto;
  }

  /*--------------------------------------------------------------------------*/
  private Vector getPhoto(BlockPhoto bp, int time) {
    BlockPhoto cursor = bp;
    DObject dObj;
    Vector vobjs = new Vector();
    boolean flag = false;
    n_access_photo = 0;

    while (cursor != null) {
      for (int i = cursor.dObjects.size() - 1; i >= 0; i--) {
        if (cursor.dObjects.get(i) == null){//ha encontrado marca de nueva foto
          if (flag) { //  ha encontrado foto segun tiempo dado
            return vobjs; // retorn objetos de la foto encontrada
          }
          if (i > 0 && ((Integer) cursor.dObjects.get(--i)).intValue() == time){
            flag = true;
          }
        }
        else
        if (flag) { // si tiempo de esta foto corresponde al tiempo dado
          dObj = ( (DObject) cursor.dObjects.get(i)).cloneDObj();
          dObj.time = time;
          vobjs.add(0, dObj); // guarda el objeto obtenido
        }
      }
      cursor = cursor.ptrNext;
      n_access_photo++; // aumenta contador de accesos a BP
    }
    return vobjs;
  }

  /*--------------------------------------------------------------------------*/
  private void addObjs2BP(Vector v, int time) {
    if (lastBlockPhoto == null) { // si no se a creado ninguna foto hasta ahora
      lastBlockPhoto = new BlockPhoto(null); // crea 1?BP
      n_blocks++;
    }
    for (int i = 0; i < v.size(); i++) {
      if (!Util.puedeAddObjBlockPhoto(this.lastBlockPhoto)) {
        BlockPhoto tmp = new BlockPhoto(this.lastBlockPhoto);
        lastBlockPhoto = tmp;
        n_blocks++;
      }
      lastBlockPhoto.dObjects.addElement(v.get(i));
    }
    lastBlockPhoto.dObjects.addElement(new Integer(time)); // tiempo
    lastBlockPhoto.dObjects.addElement(null); // marca
    lastBlockPhoto.n_marcas++;
  }

  /*--------------------------------------------------------------------------*/
  public Vector queryTimeSlice(int tB) {
    Vector vobjs = new Vector();
    BlockNode cursor = null;
    Vector vobjsPhoto = null;
    DObject dObj;
    n_access_timeslice = 0;

    if (this.lastBlockPhoto != null) {
      cursor = skip.getNode(tB);
      n_access_timeslice = skip.n_access;
      cursor = cursor.ptrNext;
    }
    else {
      cursor = this.lastBlockNode;

    }
    while (cursor != null) {
      for (int x = cursor.n_objs - 1; x >= 0; x--) {
        dObj = cursor.getObj(x).cloneDObj();
        dObj.time = (dObj.time + cursor.time);
        if (dObj.time <= tB) {
          vobjs.add(0, dObj);
        }
      }
      if (cursor.ptrPhoto != null) {
        vobjsPhoto = getPhoto(cursor.ptrPhoto, cursor.time);
        n_access_timeslice += this.n_access_photo;
        break;
      }
      cursor = cursor.ptrNext;
      n_access_timeslice++;
    }
    if (cursor == null) { // no existe ninguna foto creada
      vobjs = Util.joinVectores(this.vpage, vobjs);
    }
    else {
      vobjs = Util.joinVectores(vobjsPhoto, vobjs);
      //vobjs = Util.reduceRepetidos(vobjs);
    }
    Stadistic.countAccessBlocks += n_access_timeslice;
    //return vobjs;
    return Util.reduceNegativos(vobjs);
  }

  /*--------------------------------------------------------------------------*/
  public Vector queryInterval(int tA, int tB) {
    Vector vobjs = new Vector();
    BlockNode cursor = null;
    Vector vobjsPhoto = null;
    boolean end = false;
    DObject dObj;
    n_access_interval = 0;

    if (this.lastBlockPhoto != null) {
      cursor = skip.getNode(tB);
      n_access_interval = skip.n_access;
      cursor = cursor.ptrNext;
    }
    else {
      cursor = this.lastBlockNode;

    }
    while (cursor != null) {
      if (!end) {
        for (int n = cursor.n_objs - 1; n >= 0; n--) {
          dObj = cursor.getObj(n).cloneDObj();
          dObj.time = (dObj.time + cursor.time);
          if (dObj.time <= tB && dObj.time >= tA) {
            vobjs.add(0, dObj);
          }
          if (dObj.time < tA) {
            end = true;
            break;
          }
        }
      }
      if (end && cursor.ptrPhoto != null) {
        vobjsPhoto = getPhoto(cursor.ptrPhoto, cursor.time);
        n_access_interval += this.n_access_photo;
        break;
      }
      cursor = cursor.ptrNext;
      n_access_interval++;
    }
    vobjs = Util.reduceRepetidos(vobjs);
    if (cursor == null) { // no existe ninguna foto creada
      vobjs = Util.makeInterval(this.vpage, vobjs, tA, tB);
    }
    else {
      vobjs = Util.makeInterval(vobjsPhoto, vobjs, tA, tB);

    }
    Stadistic.countAccessBlocks += n_access_interval;
    return vobjs;
  }

  /*--------------------------------------------------------------------------*/
  public Vector queryEvent(Zone zona, int time) {
    Vector vobjs_change = new Vector();
    Vector vobjs = new Vector();
    BlockNode cursor = null;
    Vector vobjsPhoto = null;
    boolean end = false;
    DObject dObj;
    n_access_event = 0;

    if (this.lastBlockPhoto != null) {
      cursor = skip.getNode(time);
      n_access_event = skip.n_access;
      cursor = cursor.ptrNext;
    }
    else {
      cursor = this.lastBlockNode;

    }
    while (cursor != null) { // encuentra cambios en tiempo time
      for (int n = cursor.n_objs - 1; n >= 0; n--) {
        dObj = cursor.getObj(n).cloneDObj();
        dObj.time = (dObj.time + cursor.time);
        if (dObj.time == time && zona.isIncluded(dObj)) {
          vobjs_change.add(0, dObj.cloneDObj());
        }
        if (dObj.time < time) {
          end = true;
          break;
        }
      }
      if (end) {
        break;
      }
      cursor = cursor.ptrNext;
      n_access_event++;
    }
    if (vobjs_change.size() < 1) {
      Stadistic.countAccessBlocks += n_access_event;
      return vobjs_change;
    }
    // obtiene objetos de tiempo anterior
    while (cursor != null) {
      for (int x = cursor.n_objs - 1; x >= 0; x--) {
        dObj = cursor.getObj(x).cloneDObj();
        dObj.time = (dObj.time + cursor.time);
        if (dObj.time < time && zona.isIncluded(dObj)) {
          vobjs.add(0, dObj.cloneDObj());
        }
      }
      if (cursor.ptrPhoto != null) {
        vobjsPhoto = getPhoto(cursor.ptrPhoto, cursor.time);
        n_access_event += this.n_access_photo;
        break;
      }
      cursor = cursor.ptrNext;
      n_access_event++;
    }
    if (cursor == null) { // no existe ninguna foto creada
      vobjs = Util.joinVectores(this.vpage, vobjs);
    }
    else {
      vobjs = Util.joinVectores(vobjsPhoto, vobjs);
    }
    Stadistic.countAccessBlocks += n_access_event;
    return vobjs_change;
  }

  /*--------------------------------------------------------------------------*/
}
