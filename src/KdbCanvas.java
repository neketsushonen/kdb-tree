import struct.*;
import java.awt.*;
import javax.swing.*;
/**
 * Esta clase encarga de dibujar las regiones existentes
 * @author: Lai Chun-Hau
 */
class KdbCanvas extends Canvas implements java.awt.event.MouseListener, java.awt.event.MouseMotionListener {
	private JLabel x,y;
	private boolean clicked=false;
	private KDBTree kdb;
public KdbCanvas() {
	super();
	initialize();
}
/**
 * connEtoC1:  (KdbCanvas.initialize() --> KdbCanvas.kdbCanvas_Initialize()V)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC1() {
	try {
		// user code begin {1}
		// user code end
		this.kdbCanvas_Initialize();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	// System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	// exception.printStackTrace(System.out);
}
/**
 * Initializes connections
 * @exception java.lang.Exception The exception description.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initConnections() throws java.lang.Exception {
	// user code begin {1}
	// user code end
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("KdbCanvas");
	
		setSize(64, 16);
		initConnections();
		connEtoC1();
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
	// user code begin {2}
	// user code end
}
/**
 * Comment
 */
public void kdbCanvas_Initialize() {
	
	addMouseListener(this);
	addMouseMotionListener(this);
	return;
}
	/**
	 * Invoked when the mouse has been clicked on a component.
	 */
public void mouseClicked(java.awt.event.MouseEvent e) {
	 x.setText(String.valueOf(e.getX()));
	 y.setText(String.valueOf(e.getY()));
	 clicked = true;
	}
	/**
	 * Invoked when a mouse button is pressed on a component and then 
	 * dragged.  Mouse drag events will continue to be delivered to
	 * the component where the first originated until the mouse button is
	 * released (regardless of whether the mouse position is within the
	 * bounds of the component).
	 */
public void mouseDragged(java.awt.event.MouseEvent e) {}
	/**
	 * Invoked when the mouse enters a component.
	 */
public void mouseEntered(java.awt.event.MouseEvent e) {}
	/**
	 * Invoked when the mouse exits a component.
	 */
public void mouseExited(java.awt.event.MouseEvent e) {}
	/**
	 * Invoked when the mouse button has been moved on a component
	 * (with no buttons no down).
	 */
public void mouseMoved(java.awt.event.MouseEvent e) {
	if (!clicked){
	  x.setText(String.valueOf(e.getX()));
	  y.setText(String.valueOf(e.getY()));
	 }
	}
	/**
	 * Invoked when a mouse button has been pressed on a component.
	 */
public void mousePressed(java.awt.event.MouseEvent e) {}
	/**
	 * Invoked when a mouse button has been released on a component.
	 */
public void mouseReleased(java.awt.event.MouseEvent e) {}
public void paint(Graphics g){
	 //g.drawRect(0,0,400,400);
	 Page.g = g;
	 g.setColor(Color.blue);

	 if(kdb!=null)
		 kdb.printAll();
	
	}
/**
 * Insert the method's description here.
 * Creation date: (2004/8/28 ?? 02:07:13)
 * @param x java.awt.Label
 * @param y java.awt.Label
 */
public void setCanvas(JLabel x, JLabel y) {
	 this.x = x;
	 this.y = y;
	}
/**
 * Insert the method's description here.
 * Creation date: (2004/8/28 ?? 02:17:50)
 */
public void setEnableEvent() {
	clicked = false;
}
	
/**
 * Insert the method's description here.
 * Creation date: (2004/8/28 ?? 02:44:23)
 * @param tree probar.KDBTree
 */
public void setKDB(KDBTree tree) {
	 kdb = tree;
	
	}
}
