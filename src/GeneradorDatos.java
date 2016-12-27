import java.io.*;
import struct.*;
import java.util.*;

public class GeneradorDatos {
  public static void main(String[] args) throws Exception {
    int cantidades[] = new int[] {
        3000, 10000, 50000};

    KDBTree kdbTree;

    FileWriter f2 = null;
    PrintWriter out = null;

    DObject d;
    for (int j = 0; j < cantidades.length; j++) {
      f2 = new FileWriter(String.valueOf(cantidades[j]));
      out = new PrintWriter(f2);
      kdbTree =  new KDBTree(new Coordinate(100000, 100000), new Coordinate(0, 0));
      for (int i = 1; i <= cantidades[j]; i++) {
        while (!kdbTree.insert(d = new DObject(i, (int) (Math.random() * 100000),
                                               (int) (Math.random() * 100000)))) {
          ;
        }

        out.write(i + "\t" + (float) 0 + "\t" +
                  (float) d.getX() / 100000 + "\t" +
                  (float) d.getY() / 100000 + "\n");
        out.flush();
      }
    }
    out.close();
    f2.close();
  }
}
