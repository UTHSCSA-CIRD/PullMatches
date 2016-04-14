package elementHandlerpkg;
import java.io.Serializable;
import java.util.TreeSet;
import java.util.Iterator;
/**
 * This class contains the tree of levels and some interaction functions. This class is meant to be
 * serialized and loaded later so that it can be reused. This file is ONLY used when loading from a file.
 * Its main use is to keep the level ID stable so that new files can be loaded and the level rules don't have to change.
 * This allows some future development to apply real expressions etc to the levels only once and store the results.
 * Useful if your tree contains some million strings or more ;). 
 * @author Laura Manuel
 */
public class LevelHolder implements Serializable{
    //this class exists because of the need to give each level in the tree set a 
    //unique ID (Integer)
    private final TreeSet<Level> levels;
    private int currentID;
    private static final long serialVersionUID = 1;
    
    /**
     * Does the tree contain this string? If so what is its ID?
     * @param a A string to find the level ID for. 
     * @return Returns either the level ID for the string or -1 if it's not found 
     */
    public int treeContains(String a){
        //returns -1 if not in tree or returns the level
        Level test = new Level(a);
        Level e = levels.ceiling(test);
        if(e == null) return -1;
        if(e.levelValue.equals(a))return e.level;
        return -1;
    }
    /**
     * Quick and easy call to add a new level.
     * @param a The string belonging to the new level.
     * @return The ID assigned to the new level or -1 if the level already exists. 
     */
    public int addLevel(String a){
        boolean e = levels.add(new Level(currentID, a));
        if(e){
            currentID++;
            return (currentID -1);
        }
        return -1;
    }
    /**
     * Get the iterator for the levels tree.
     * @return Returns the iterator for the levels tree.
     */
    public Iterator<Level> getIterator(){
        return levels.iterator();
    }
    /**
     * Create a new empty LevelHolder with a brand new tree.
     */
    public LevelHolder(){
        levels = new TreeSet<>();
        currentID = 0;
    }
}
