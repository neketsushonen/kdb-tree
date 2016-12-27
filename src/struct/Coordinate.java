package struct;

/**
 * Clase que contenga la ubicacion de un elemnto cualquiera que se representa
* //en las coordenadas (X,Y) dentro de una region fija
 * @author: Chun-Hau Lai
 */
public class Coordinate {
  private int x;
  private int y;
  private byte eje;

  /**
   * Asignar la ubicacion de los puntos.
   * @param x int
   * @param y int
   */
  public Coordinate(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Instanciar las coordenadas del punto medio y guardar la direccion del
   * corte en la vairable eje.
   * Si el corte es ejercido en direccion vertical, el corte es 0,
   * en caso contrario, 1.
   * @param x int
   * @param y int
   * @param eje byte
   */
  public Coordinate(int x, int y, byte eje) {
    this.x = x;
    this.y = y;
    this.eje = eje;
  }

  /**
   * Retorna el sentido del corte
   * @return byte
   */
  public byte getEje() {
    return eje;
  }

  /**
   * Retornar el valor de la coordenada Y
   * @return int
   */
  public int getX() {
    return x;
  }

  /**
   * Retornar el valor de la coordenada Y
   * @return int
   */
  public int getY() {
    return y;
  }
}
