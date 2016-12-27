package struct;

import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
/**
 * Cada pagina existente en el arbol KDB puede ser una pagina de region o
 * de punto. Esta clase es la superclase de la cual se hereda las clases de
 * region y punto con el fin de facilitar la facil y comprensiva abastraccion
 * en el proceso de desarrollo del proyecto semestral.
 * En esta clase se define los diferentes estados que puede estar una pagina
 * dada, tales como FULL(la pagina esta lleno de puntos),
 * NOTFULL (aun hay espacio suficiente para agregacion de otros elementos.),
 * WAITFORCUT (pagina que espera ser dividida de acuerdo a un eje seleccionado
 * adecuado.), etc.
 * Tambien se ha declarado los metodos comunes entre las dos sub-clases que
 * los utilizara.
 * @author: Chun-Hau Lai
 */
public abstract class Page implements Cloneable{
	public static long timeSlice=0;
	public static Graphics g;
	protected byte status;
	public static int corteeje=1;
	public final static byte FULL = 1;
	public final static byte NOTFULL = 2;
	public final static byte WAITFORCUT = 3;
        //SIZEPOINT :
        //(Lista Variante): 12* x + 4 = 1024, donde x es la cantidad de punteros
          //                               x = 85
           // (SKIP List): 12* x + 4 = 1024, donde x es la cantidad de punteros
           //                               x = 84

	public static int SIZEPOINT=84;
          //SIZEREGION : (8+8+4) * x + 4 = 1024,
          //donde x es la cantidad de punteros
          // x = 50
	public static int SIZEREGION=50;
        //elemntos internos, podria ser puntos o regiones
	protected java.util.Vector elements;
	protected String id;
	protected Zone zone;   //MBR
	public final static byte CUTED = 4;


/**
 * Inicilizar los atributos asociados a las paginas diferentes, tales como Id,
 * localizacion, etc.
 * @param id int
 * @param ldown struct.Coordinate
 * @param rup struct.Coordinate
 */
public Page(String id, Coordinate ldown, Coordinate rup) {
	 this.id = id;
	 zone = new Zone(rup,ldown);
	 status = NOTFULL;
	 elements = new Vector();

	}
/**
 * Construccion de los nodos del arbol KDB en parte visual
 * @param pnode javax.swing.tree.DefaultMutableTreeNode
 * @param p struct.Page
 */
public abstract void addNodes(javax.swing.tree.DefaultMutableTreeNode pnode);
/**
 * Calcular la estadistica del bloque. Guardar toda la informacion existente
 * en el vector
 * @return java.util.Vector
 */
public abstract Vector calculateStadistic();
/**
 * Calcuar la utilizacion de los bloques
 * @return struct.Stadistic
 */
public abstract Stadistic calculateUtilization();
/**
 * Retorna el punto mediano que separa equilibradamente todos los
 * elementos de una pagina en dos paginas
 * @return struct.Coordinate
 */
public abstract Coordinate getAverage();
/**
 * Retorna todos los elementos existentes.
 * @return java.util.Vector
 */
public Vector getElements() {
	return elements;
}
/**
 * Retorna la identificacion de una pagina determinada
 * @return java.lang.String
 */
public String getID() {
	return this.id;
}
/**
 * Retornar el estado de la pagina actual.
 * @return byte
 */
public byte getStatus() {
	return this.status;
}
/**
 * Retorna MBR de una pagina determinada.
 * @return struct.Zone
 */
public Zone getZone() {
	return zone;
}
/**
 * Insertar el cambio de acuerdo el tiempo
 * @param time int
 * @param d struct.DObject
 */
public abstract void insert(int time, DObject d);
/**
 * Insertar un elemento determinado en la pagina. En caso de que no haya
 * espacio, se divide para poder equilibrar el arbol.
 * @return struct.Page
 * @param elem org.omg.CORBA.Object
 * @exception java.lang.CloneNotSupportedException The exception description.
 */
public abstract Page insert(Object elem) throws
    java.lang.CloneNotSupportedException;
/**
 * Insert the method's description here.
 * Creation date: (2004/10/3 ?? 02:07:25)
 * @return boolean
 * @param zone struct.Zone
 */
public boolean intersect(Zone zone){
	Zone este = getZone();
		// modificacion....
		//zone.getRightUp().getX() >= este.getLeftDown().getX() &&
		// zone.getLeftDown().getY() <= este.getRightUp().getY()
		// para que no intersecte con regiones adyacentes

	return zone.getLeftDown().getX() <=este.getRightUp().getX() &&
		   zone.getRightUp().getX() > este.getLeftDown().getX() &&
		   zone.getLeftDown().getY() <= este.getRightUp().getY() &&
		   zone.getRightUp().getY() > este.getLeftDown().getY();
	}
/**
 * Verifica si la ubicacion de un punto se encuentra en el area definida por MBR
 * @return boolean
 * @param site struct.Coordinate
 */
public boolean isIncluded(DObject d) {
	return zone.isIncluded(d);
}
/**
 * Montar los puntos existentes en la hoja de KDB en la lista de cambios
 */
public abstract void mountPointAtList();
/**
 * Imprimir el contenido del Arbol conpleto por nivel.
 */
public abstract void printAll(String salto);
/**
 * Insert the method's description here.
 * Creation date: (2004/8/26 ?? 09:03:47)
 * @return struct.Page
 * @param criterio int
 * @param valorEje int
 */
public abstract Page setMBR(int criterio, int valorEje);
/**
 * Actualizar el estado de la pagina de acuerdo a los diferentes
  * criterios predefinidos.
 * @param status byte
 */
public void setStatus(byte status) {
	 this.status = status;
	}
/**
 * Metodo que divide las paginas en caso de Overflow en dos paginas que contiene
 * una cantidad similar de puntos.
 * @return struct.Page
 * @param criterio byte
 * @exception java.lang.CloneNotSupportedException The exception description.
 */
public abstract Page split() throws java.lang.CloneNotSupportedException;
}
