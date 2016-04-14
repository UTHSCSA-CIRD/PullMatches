package elementHandlerpkg;

/**
 * The element class represents an element in the SearchContainer list. And is used by the MatchHolder container.
 * These lists are used to hold the patient "matches". These are cached in the SearchContainer
 * and processed in MatchSelector and stored for match processing in the MatchHolder container class. 
 * This class implements Comparable and is built to be stored in a binary search tree.
 * @author Laura Manuel
 */

public class Element implements Comparable<Element>{
    private final int index;
    private final double val;
    
    /**
     *
     * @return Returns the index of the patient whose distance value is "value".
     */
    public int getIndex(){
        return index;
    }

    /**
     *
     * @return Returns the distance value of the patient identified by "index".
     */
    public double getValue(){
        return val;
    }
    
    /**
     *The compareTo is for the binary search tree-- as multiple patients may have
         * the same distance it will use the patient's index to break distance ties.  
         * Note: This is the patient's INDEX in the main.ptList array, not the patient's number.
         * 
     * @param a The element to compare this element to. 
     * @return Returns -1 for less than 1 for greater than and 0 for equal. Should never return 0 unless there is some sort of error.
     */
    @Override
    public int compareTo(Element a){
        if(a.getValue() > val)return -1;
        if(a.getValue() < val) return 1;
        if(a.getIndex()>index) return -1;
        if(a.getIndex()<index)return 1;

        return 0;//same value and id...? duplicate!
    }
    
    /**
     *Creates a new element referencing the patient identified by ind with the distance value val.
     * @param ind This is the patients INDEX in the main.ptList array
     * @param val This is the calculated DISTANCE BETWEEN the index patient and the patient who owns this element.
     */
    public Element(int ind, double val){
        this.index = ind;
        this.val = val;
    }
    
}
