package struct;

/**
 * Almacena todos los cortes existentes en una pagina regional.
 * Se almacena las coordenadas (X,Y) de los dos extremos de la linea
 * @author:
 */
public class Corte {
  private Coordinate point1;
  private Coordinate point2;
  private byte eje;

  /**
   * Instanciar los valores existentes en la linea
   * @param x1 int
   * @param y1 int
   * @param x2 int
   * @param y2 int
   */
  public Corte(int x1, int y1, int x2, int y2) {
    point1 = new Coordinate(x1, y1);
    point2 = new Coordinate(x2, y2);
    if (x1 == x2) {
      eje = (byte) 0;
    }
    else {
      eje = (byte) 1;

    }

  }

  /**
   * Retorna el sentido del corte.
   * 0: Vertical
   * 1: Horizontal
   * @return byte
   */
  public byte getEje() {
    return eje;
  }

  /**
   * Retornar el punto 1 del extremo izquierdo o superior
   * @return struct.Coordinate
   */
  public Coordinate getPoint1() {
    return point1;
  }

  /**
   * Retornar el punto 2 del extremo derecho o inferior
   * @return struct.Coordinate
   */
  public Coordinate getPoint2() {
    return point2;
  }
}
