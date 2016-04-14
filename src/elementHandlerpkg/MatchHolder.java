
package elementHandlerpkg;

/**
 *This class is a container class that holds the Element data from the SearchContainer and adds the additional wrapper of which
 * patient's SearchContainer it came from. This is utilized by the MatchSelector's tree to keep track of
 * which patient IDs are being used each row. 
 * @author Laura Manuel
 */
public class MatchHolder implements Comparable<MatchHolder>{
    private final Element el; //the element that it's matching to // note that the "index" in here is the patient being matched to
    private final int index; //this is the scList index of the patient this match belongs to 
    //index of patient whose match is el.index with a value of el.value; 
     
    /**
     * 
     * @return Returns the index of the SearchContainer whose element this is. 
     */
    public int getIndex(){
        return index;
    }
    /**
     * 
     * @return Returns the index of the patient that is being matched 
     */
    public int getMatchIndex(){
        return el.getIndex();
    }
    /**
     * 
     * @return Returns the distance value for the patient match
     */
    public double getMatchValue(){
        return el.getValue();
    }
    /**
     * 
     * @return Returns the stored element.
     */
    public Element getElement(){
        return el;
    }
    /**
     * This is a function for the tree in the MatchSelector and compares only on the patient IDs. 
     * Simply put this makes sure that the same patient isn't going to be in the list twice. 
     * @param a A MatchHolder to compare it to. 
     * @return Returns -1 for greater than 1 for less than or 0 if both matched patients are the same matched patient. 
     */
    @Override
    public int compareTo(MatchHolder a){
        //we're comparing the indexes for our element tree to make sure that we don't have duplicate patients that we are matching to
        if(el.getIndex()< a.getMatchIndex()) return 1;
        if(el.getIndex() > a.getMatchIndex()) return -1;
        return 0;
    }
    /**
     * 
     * @param ind The index of the SearchContainer that contains the element passed in the next parameter. 
     * @param e The element that was popped from the SearchContainer in the ind parameter. 
     */
    public MatchHolder(int ind, Element e){
        index = ind;
        el = e;
    }
    
}
