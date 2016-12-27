package struct;

import java.util.Vector;

public class BlockNode {
	
 short level;
 BlockNode ptrs[];
 short n_objs;
 Vector dObjects;
 int time;
 BlockNode ptrNext;
 BlockPhoto ptrPhoto;
	 
 public BlockNode(short level,int time, BlockNode ptrNext, BlockPhoto ptrPhoto){
 	  this.n_objs   = 0;
 	  this.dObjects = new Vector();
	  this.level    = level;
	  this.time     = time;
	  this.ptrNext  = ptrNext;
	  this.ptrPhoto = ptrPhoto;
	  this.ptrs     = new BlockNode[level];
	  for( short i=0; i<level; i++)
	    this.ptrs[i] = null;
 }
 public void addObj( DObject obj ){
 	 this.dObjects.addElement(obj);
 	 this.n_objs++;
 }
 public DObject getObj( int i ){
 	 return  ((DObject)this.dObjects.get(i));
 }
 
 public void setLevel(short level){
 	this.level = level; 
 	this.ptrs  = new BlockNode[level];
  	for( short i=0; i<level; i++)
	    this.ptrs[i] = null;
 }
 
}
