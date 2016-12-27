package struct;

/**
 * Esta clase contiene la informacion sobre el area que abarca una region
 *  determinada, cuya forma de representacion es tomar el rectangulo que abarca
 * todos los puntos fijando en los dos rincones del rectangulo superior derecho
 * e inferior izquierdo.
 * Uno de los objetivos de la creacion de esta clase es verificar con facilidad
 * hacia que region pertenezcan dado un elemento con su ubicacion respectiva
 * para facilitar re-definir el rectangulo en caso del corte incidencial..
 * @author: Lai Chun-Hau
 */
public class Zone {
  public Coordinate rigthUp;
  public Coordinate leftDown;

  /**
   * Instanciar MBR de las regioens
   * @param rUp struct.Coordinate
   * @param lDown struct.Coordinate
   */
  public Zone(Coordinate rUp, Coordinate lDown) {
    rigthUp = rUp;
    leftDown = lDown;
  }

  /**
   * Retorna el punto izquierdo inferior del MBR
   * @return struct.Coordinate
   */
  public Coordinate getLeftDown() {
    return leftDown;
  }

  /**
   * Retorna el punto derecho superior del MBR
   * @return struct.Coordinate
   */
  public Coordinate getRightUp() {
    return rigthUp;
  }

  /**
   * Verifica si la ubicacion de un punto se encuentra en el area definida
   * por MBR
   * @return boolean
   * @param site struct.Coordinate
   */
  public boolean isIncluded(DObject d) {
    if ( (d.getX() <= rigthUp.getX() && d.getY() <= rigthUp.getY())) {
      if (d.getX() >= leftDown.getX() && d.getX() == 0 &&
          d.getY() > leftDown.getY()) {
        return true;
      }
      else
      if (d.getY() >= leftDown.getY() && d.getY() == 0 &&
          d.getX() > leftDown.getX()) {
        return true;
      }
      else
      if (d.getY() == 0
          && d.getX() == 0
          && leftDown.getX() == 0
          && leftDown.getY() == 0) {
        return true;
      }
      else
      if ( (d.getX() > leftDown.getX() && d.getY() > leftDown.getY())) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }
}
