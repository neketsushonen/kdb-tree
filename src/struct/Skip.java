package struct;

public class Skip {

  int MAX_TIME = Integer.MAX_VALUE;
  short MAX_LEVEL = Util.MAX_LEVEL; // [257,1024]
  BlockNode indexNode;
  BlockNode initNode;
  int n_access;

  public Skip(BlockNode initNode) {
    this.initNode = initNode;
    this.initNode.setLevel(MAX_LEVEL);
    indexNode = new BlockNode(MAX_LEVEL, MAX_TIME, initNode, null);
    for (short n = 0; n < MAX_LEVEL; n++) {
      indexNode.ptrs[n] = this.initNode;
    }
  }

  public void addNode(BlockNode newNode) {
    short newLevel = getRandomLevel();
    newNode.setLevel(newLevel);
    for (short n = 0; n < newLevel; n++) {
      newNode.ptrs[n] = this.indexNode.ptrs[n];
      this.indexNode.ptrs[n] = newNode;
    }
  }

  public BlockNode getNode(int time) {
    n_access = 0;
    if (time > indexNode.ptrNext.time) {
      n_access++; //acceso
      return indexNode;
    }
    n_access = 0;
    BlockNode cursor = indexNode;
    BlockNode pointerNode = null;
    for (int i = MAX_LEVEL - 1; i >= 0; i--) {
      pointerNode = cursor.ptrs[i]; // acceso
      n_access++;
      while (pointerNode != null && time < pointerNode.time) {
        cursor = pointerNode;
        pointerNode = cursor.ptrs[i]; // acceso
        n_access++;
      }
    }
    return cursor;
  }

  private short getRandomLevel() {
    short level = 1;
    while (level < this.MAX_LEVEL && Math.random() < 0.25f) {
      level++;
    }
    return level;
  }

}
