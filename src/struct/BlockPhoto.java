package struct;
import java.util.Vector;

public class BlockPhoto {
	
  Vector dObjects;
  BlockPhoto ptrNext;
  short n_marcas;
  	
  public BlockPhoto(BlockPhoto next){
    this.dObjects = new Vector();
    this.ptrNext = next;
    this.n_marcas = 0; 
  }	
  
}