package struct;

/**
 * Un simple separador en el bloque de cambios que indica los siguientes:
 * el tiempo correspondiente y la cantidad de cambios que existen con respecto
 * al tiempo indicado
 * @author: Lai Chun-Hau
 */
public class TimeSeparator {
  public int time;
  public int count = 0;

  /**
   * Inicializar el separador del tiempo con time: tiempo actual del cambio
   *                                        count: contador del cambio total.
   * la variable count se incrementan en uno a
   * edida que venga un cambio en el mismo instant time
   * @param time int
   * @param count int
   */
  public TimeSeparator(int time, int count) {
    this.time = time;
    this.count = count;
  }
}
