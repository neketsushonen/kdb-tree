package struct;

public class DObject {

  public int oid; //	4 bytes
  public int x; //	4 bytes
  public int y; //	4 bytes
  public int time;
//	2 bytes, cuando se guarde ser un tiempo relativo en Skip List
 //       4 bytes, cuando se guarda en el Lista Variante
  private char change;

  public DObject(int oid, int x, int y, int time) {
    this.oid = oid;
    this.x = x;
    this.y = y;
    this.time = time;
  }

  public DObject(int oid, int x, int y) {
    this.oid = oid;
    this.x = x;
    this.y = y;
  }

  public DObject(int oid, int x, int y, int time, char change) {
    this.oid = oid;
    this.x = x;
    this.y = y;
    this.time = time;
    this.change = change;
  }

  public int getID() {
    return this.oid;
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  public int getTime() {
    return this.time;
  }

  public void setDelete() {
    oid = oid * -1;
  }

  public DObject cloneDObj() {
    return new DObject(this.oid, this.x, this.y, this.time);
  }

}
