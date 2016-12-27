package struct;

import java.util.*;
import struct.*;

/**
 * Es el nodo que guarde los cambios tanto del area de reconstruccion
 * como los cambios parciales
 * @author: Lai Chun-Hau
 */
public class NodeChange {
  private NodeChange next, nextTimeChange;
  private Vector refreshPoints, changes;
  private int size_total;

  /**
   * Inicializar las varibales
   */
  public NodeChange() {
    size_total = 4 + 4; //el tamanio del puntero al siguiente bloque y bid
    //vector que almacena los puntos del area de reconstruccion
    refreshPoints = new Vector();
    //vector que almacena los cambios parciales
    changes = new Vector();
    //puntero al siguiente
    next = null;
  }

  /**
   * Inicializar el bloque con los puntos existentes en la hoja del KDB
   * @param points java.util.Vector
   */
  public NodeChange(Vector points) {
    next = null;
    // el primer 4 bytes: puntero al siguiente
    // points.size()*12: cantidad de puntos por 12 bytes : id + x + y
    // el segundo 4 bytes: puntero al ultimo nodo del area de reconstruccion
    //el tercer 4 bytes: bid
    size_total = 4 + points.size() * 12 + 4 + 4;
    refreshPoints = new Vector(points);
    changes = new Vector();
    nextTimeChange = null;
  }

  /**
   * Agregar los cambios parciales en el bloque
   * Si el id del objeto es negativo, significa que es eliminacin
   * @return changes.NodeChange
   * @param o struct.DObject
   */
  public void addChengeParcial(DObject o) {
    // 12: oid + x + y
    if (size_total + 12 <= 1024) {
      changes.addElement(o);
      size_total += 12;
    }
    else
    if (next == null) {
      next = new NodeChange();
      next.addChengeParcial(o);
    }

  }

  /**
   * Agregar los puntos de reconstruccion en el bloque
   * Obs: En primer tiempo, la cantidad total de puntos no sobrepasara a un
   * bloque,porque en la hoja del arbol KDB nunca permite que sobrepase
   * @return changes.NodeChange
   * @param o struct.DObject
   */
  public void addRefreshPoints(DObject o) {
    if (size_total + 12 <= 1024) {
      refreshPoints.addElement(o);
      size_total += 12;
    }
    else {
      if (next == null) {
        next = new NodeChange();
      }
      next.addRefreshPoints(o);
    }

  }

  /**
   * Agregar el separador en el bloque tal que indique la cantidad de cambios
   * existentes para un momento determinado
   * @return changes.NodeChange
   * @param separator changes.TimeSeparator
   */
  public void addTimeSeparator(TimeSeparator separator) {
    // 8: la clave del tiempo + contador
    if (size_total + 8 <= 1024) {
      changes.addElement(separator);
      size_total += 8;
    }
    else if (next == null) {
      next = new NodeChange();
      next.addTimeSeparator(separator);
    }

  }

  /**
   * Retornar el vector que guarde los cambios
   * @return java.util.Vector
   */
  public Vector getChanges() {
    return changes;
  }

  /**
   * Insert the method's description here.
   * Creation date: (2004/10/13 ?? 04:12:24)
   * @return changes.NodeChange
   */
  public NodeChange getNext() {

    return next;
  }

  /**
   * Retornar el ultimo nodo del area de reconstruccion
   * @return changes.NodeChange
   */
  public NodeChange getNextTimeNode() {

    return this.nextTimeChange;
  }

  /**
   * retornar los puntos del area de reconstruccion
   * @return java.util.Vector
   */
  public Vector getRefreshPoints() {
    return refreshPoints;
  }

  /**
   * Enlazar al ultimo nodo del area de reconstruccion
   */
  public void setNextTimeNode(NodeChange n) {
    nextTimeChange = n;
  }
}
