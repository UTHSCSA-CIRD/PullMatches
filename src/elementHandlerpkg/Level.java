
package elementHandlerpkg;
import java.io.Serializable;
/**
 *  This class holds the "levels" this is using the R terminology and basically references the level system of a factor.
 *  This allows the system to flexibly accept any number or type of level. Later we'll add some real expressions or something
 *  to allow the user to group or prioritize the levels.
 * @author Laura Manuel
 */
public class Level implements Comparable<Level>, Serializable{
    public final int level; //the index of the level
    public final String levelValue; //the string for this level 
    private static final long serialVersionUID = 1;
    
    /**
     * compareTo function needed for the binary tree. 
        *@param a The level to be compared to it. This is basically a string compare and ignores the level's ID.
        *@return Returns the result of the string compare of levelValue.
    */
    @Override
    public int compareTo(Level a){
        //utilizes string compare.
        return levelValue.compareTo(a.levelValue);
    }
    /**
     * This is a mock version of the generation that creates an invalid levelID of -1.
     * It's used to get the level ID of the existing level.
     * @param value The string value of the level, basically the text of the concept code.
     */
    public Level(String value){
        //for searching the tree; 
        levelValue = value;
        level = -1;
    }
    /**
     * @param l This is the numerical value of the level. The numerical value is the only thing used later on to save on memory.
     * @param val The string value of the level, basically the text of the concept code.
     */
    public Level(int l, String val){
        //for storing in the tree; 
        level = l;
        levelValue = val;
    }
    
}
