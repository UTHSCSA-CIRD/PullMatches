/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elementHandlerpkg;
import java.io.Serializable;
import java.util.TreeSet;
import java.util.Iterator;
/**
 *
 * @author manuells
 */
public class LevelHolder implements Serializable{
    //this class exists because of the need to give each level in the tree set a 
    //unique ID (Integer)
    private TreeSet<Level> levels;
    private int currentID;
    private static final long serialVersionUID = 1;
    
    public int treeContains(String a){
        //returns -1 if not in tree or returns the level
        Level test = new Level(a);
        Level e = levels.ceiling(test);
        if(e == null) return -1;
        if(e.levelValue.equals(a))return e.level;
        return -1;
    }
    public int addLevel(String a){
        boolean e = levels.add(new Level(currentID, a));
        if(e){
            currentID++;
            return (currentID -1);
        }
        return -1;
    }
    public Iterator<Level> getIterator(){
        return levels.iterator();
    }
    public LevelHolder(){
        levels = new TreeSet<>();
        currentID = 0;
    }
}
