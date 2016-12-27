package struct;

/**
 * Esta clase se encarcara de guardar la estadistica, con respecto a
 * cantidad de elementos en cada nivel.
 * @author: Lai Chun-Hau
 */
public class Stadistic {
	public static int countAccessBlocks=0;
	private int nivel;
	private int countElementAtLevel;
	private int countBlocks=0;
	
	private int countPoints = 0;
	private int countPointers=0;
	public int countCuts=0;
/**
 * Crear el objeto sin instanciar ningun metodo
 */
public Stadistic() {}
/**
 * Inicializar las variables, el nivel: nivel del elemento que se encuentra
 * count: cantidad total de los elementos existentes en un nivel
 * @param nivel int
 * @param count int
 */
public Stadistic(int nivel, int count) {
	 this.nivel = nivel;
	 this.countElementAtLevel = count;
	}
/**
 * Retorna cantidad de elementos en el nivel
 * @return int
 */
public int getCount() {
	return countElementAtLevel;
}
/**
 * Retorna la cantidad total de bloques
 * @return int
 */
public int getCountBlocks() {
	return countBlocks;
}
/**
 * Insert the method's description here.
 * Creation date: (2004/8/25 ?? 11:46:59)
 * @return int
 */
public int getCountCuts() {
	return countCuts;
}
/**
 * Retorna la cantidad total de punteros
 * @return int
 */
public int getCountPointers() {
	return countPointers;
}
/**
 * Retorna la cantidad total de puntos existentes
 * @return int
 */
public int getCountPoints() {
	return countPoints;
}
/**
 * Retorna el nivel 
 * @return int
 */
public int getNivel() {
	return this.nivel;
}
/**
 * Retornar el porcentaje de la utilizacion total del arbol.
 * Cantidad de puntedos x 8 bytes +
 * Cantidad de cortes (Cantidad de punteros a regiones -1 ) * 16 bytes +
 * Cantidad de puntos x 16 bytes  +
 * tamanio del ID 8 byte +
 * Los dos puntos de los rincones diagonales 16 byte
 * -----------------------------------------------------
 *            1024 bytes x Cantidad de Bloques 
 * @return int
 */
public int getUtilization() {
	double u =16 + 8 + countPoints*16 + countPointers * 4 + countCuts * 16;///(1024   * countBlocks);
	u = u / 1024;
	u = u / countBlocks;
	u *= 100;
//	System.out.print(u);
    return (int)u;
}
/**
 * Registrar la cantidad total de bloques, punteros, cortes 
 * y los puntos exsitentes en el arbol.
 * @param countBlocks int
 * @param countPoints int
 */
public void setCountBlocksAndPoints(int countBlocks, int countPoints) {
    this.countBlocks = countBlocks;
    this.countPoints = countPoints;
    if (countBlocks != 1) {
        this.countPointers = countBlocks - 1;
        this.countCuts = countBlocks - 2;
    }
}
}
