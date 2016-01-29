
package elementHandlerpkg;
import java.io.Serializable;
/**
 *
 * @author manuells
 */
public class Level implements Comparable<Level>, Serializable{
    public final int level; //the index of the level
    public final String levelValue; //the string for this level 
    private static final long serialVersionUID = 1;
    
    @Override
    public int compareTo(Level a){
        //utilizes string compare.
        return levelValue.compareTo(a.levelValue);
    }
    public Level(String value){
        //for searching the tree; 
        levelValue = value;
        level = -1;
    }
    public Level(int l, String val){
        //for storing in the tree; 
        level = l;
        levelValue = val;
    }
    
}
