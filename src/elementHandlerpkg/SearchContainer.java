package elementHandlerpkg;

/**
 * SearchContainer processes and caches all of the distances for the patients you are matching.
 * 
 * @author Laura Manuel
 */
import helperspkg.DistanceCalculator;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Iterator;
import java.util.ArrayList;
import mainpkg.main;

public class SearchContainer {
    //this class is used for finding the top X patients that match this one.
    private final int ptID, ptIndex, needs; 
    private int has;
    private final TreeSet<Element> search;
    
    /**
     * 
     * @return Returns the patient number NOT the index for the patient whose search container this is
     * The patient ID is cached in the SearchContainer because otherwise it would have to be looked up with each "pull row"
     * from the 
     */
    
    public int getID(){
        return ptID;
    }
    /**
     * 
     * @return The Index of the patient in the main.ptList array. (needed by the distance calculator to access the summary file)
     */
    public int getIndex(){
        return ptIndex;
    }
    /**
     * 
     * @return If it has matched patients, returns the lowest distance value in the tree, otherwise returns -1
     */
    public double getMin(){
        if( has ==0){
            return -1;
        }
        return search.first().getValue();
    }
    /**
     * 
     * @return If it has matched patients, returns the highest distance value in the tree, otherwise returns -1
     */
    public double getMax(){
        if(has == 0){
            return -1;
        }
        return search.last().getValue();
    }
    /**
     * 
     * @return Returns the entire element containing the highest distance value. 
     */
    public Element getMaxElement(){
        return search.last();
    }
    /**
     * 
     * @return Returns the entire element containing the lowest distance value. 
     */
    public Element getFirstElement(){
        return search.first();
    }
    /**
     * 
     * @return Returns the number of elements in the tree.
     */
    public int getNumElements(){
        return has;
    }
    /**
     * 
     * @return Returns the maximum number of elements that this tree is supposed to contain.
     */
    public int getMaxElements(){
        return needs;
    }
    /**
     * This is the method that MatchSelector calls when  popping out the lowest value form the tree. IF 
     * there are multiple lowest values it will randomly select one of the values (otherwise it is always 
     * selecting the patient with the lowest ID in non data rich systems where matching distances are more likely
     * that would cause a skew, for data rich networks this adds extra processing, however profiling shows that it
     * really doesn't take up much processing time in comparison to other methods like processCross)
     * @return Returns the an Element with the minimum distance value. 
     */
    public Element popMin(){
        if(has <1)return null;
        double val = search.first().getValue();
        SortedSet<Element> t;
        t = search.headSet(new Element(main.ptList.size()+1,val));
        if(t.size()>1){
            int r = main.rand.getFromZeroTo(t.size());
            int i = 0;
            Iterator itr = t.iterator();
            while(itr.hasNext()){
                if(i==r){
                    Element a = (Element)itr.next();
                    search.remove(a);//this should be safe since it's iterating over the headset, not the search tree
                    has--;
                    return a;
                }
                i++;
                itr.next();
            }
        }else{
            has--;
            return search.pollFirst();
        }
        return null;//error with the random!!
    }
    /**
     * Because the tree sorts first by distance and then by patient index popping the highest
     * value from the tree will result in the highest patient index being selected during 
     * a pop, since this is used to remove one of the highest values that means that the patients with the
     * lowest indexes would be unevenly selected for all patients. For highly populated value matrixes
     * the likelihood of a tied distance is low. For smaller level sets distance matches are 
     * more likely. Randomly sampling from a uniform distribution each time would make it more likely that matches
     * later on in the selection would be selected as the lower indexes are sampled multiple times. Thus a normal distribution
     * is used to encourage more pulls from the middle. 
     * 
     * @return Returns an element containing the highest distance value in the tree.
    */
    public Element popMax(){
        if(has <1)return null;
        double val = search.last().getValue();
        SortedSet<Element> t;
        //-1 will be smaller than any valid index containing val, thus we get all greater than that or all elements containing val.
        t = search.tailSet(new Element(-1,val));
        if(t.size()>1){
            int r = main.rand.getNormalFromZeroTo(t.size());
            int i = 0;
            Iterator itr = t.iterator();
            while(itr.hasNext()){
                if(i==r){
                    Element a = (Element)itr.next();
                    search.remove(a);
                    has--;
                    return a;
                }
                i++;
                itr.next();
            }
        }else{
            
            has--;
            return search.pollLast();
        }
        return null;//error with the random!!
    }
    /**
     * The two versions of processCross are the main CPU crunchers. They call the distance calculator 
     * for each and every available patient.
     * This version of processCross is called during the initialization process. It calls an iterator from
     * main.ptAvail to walk over all of the available patients. It caches a number of matches. Usually up to 1000 times
     * the size of needs on the argument line. Within the class that cap is called "needs".
     */
    public void processCross(){
        //Calculates the distances for all available patients and caches the best "needs" matches. 
        if(has == needs) return;
        Iterator itr = main.ptAvail.iterator();
        if(main.method == 'e'){//if we get more of these create a switch; 
            while(itr.hasNext()){
                int tmp = (Integer)itr.next();//the index in pt list
                addElement(new Element(tmp, DistanceCalculator.euclideanDistance(main.ptList.get(ptIndex),main.ptList.get(tmp) )));
            }
        }else{
            while(itr.hasNext()){
                int tmp = (Integer)itr.next();//the index in pt list
                addElement(new Element(tmp, DistanceCalculator.binaryDistance(main.ptList.get(ptIndex),main.ptList.get(tmp) )));
            }
        }
    }
    /**
     * The two versions of processCross are the main CPU crunchers. They call the distance calculator 
     * for each and every available patient.
     * This version of processCross is only called from within MatchHolder when the cache is empty. This refills the cache without
     * using any items already taken in this row. While this might cause an exception to the stipulation that the best match for
     * a matching patient should get it, it prevents the issue with the number of search containers exceeding the needs size and causing
     * an infinite loop of rejections on re-querying. (Note: to solve this perhaps "needs" should be adjusted based on the number of
     * competing patients. 
     * @param dont A tree of indexes we should not use. (Note, this is basically a copy of the tree MatchSelector uses.)
     */
    public void processCross(TreeSet<MatchHolder> dont){
        //This saves us time if for some reason the patient is called twice between which 
        //there were no changes
        //note: has really should be zero when this is called...
        if(has == needs) return;
        TreeSet<Integer> pullFromPTs = new TreeSet<>();
        Iterator itr;
        MatchHolder e;
        pullFromPTs.addAll(main.ptAvail);//copy available set
        itr = dont.iterator();
        while(itr.hasNext()){
            e = (MatchHolder)itr.next();
            pullFromPTs.remove(e.getMatchIndex());
        }
        
        Iterator iter = pullFromPTs.iterator();
        if(main.method == 'e'){//if we get more of these create a switch; 
            while(iter.hasNext()){
                int tmp = (Integer)iter.next();//the index in pt list
                addElement(new Element(tmp, DistanceCalculator.euclideanDistance(main.ptList.get(ptIndex),main.ptList.get(tmp) )));
            }
        }else{
            while(iter.hasNext()){
                int tmp = (Integer)iter.next();//the index in pt list
                addElement(new Element(tmp, DistanceCalculator.binaryDistance(main.ptList.get(ptIndex),main.ptList.get(tmp) )));
            }
        }
        
    }
    /**
     * Will attempt to add element a. If needs is fulfilled and a's distance is greater than the greatest distance in the tree
     * then a is rejected and false is returned. If a is tied for greatest distance it will randomly decide whether or not to add 
     * it and pop another value. If needs is fulfilled, but a is LESS THAN the highest distance value a higher distance value
     * is popped and a is added. 
     * @param a An element to add to the searchContainer
     * @return True if the element is added false if it is not. 
     */
    public boolean addElement(Element a){
        if(has == needs){
            if(a.getValue() > search.last().getValue()){
                return false;
            }else{
                if(a.getValue() == search.last().getValue()){
                    if(main.rand.getOneOrZero() == 0) return false; // otherwise we will proceed with popping a random max and adding this one.
                }
                popMax();
                search.add(a);
            }
        }
        has++;
        return search.add(a);
    }
    /**
     * Debugging method. Returns the first e elements in the iterator. 
     * @param e Number of elements to return.
     * @return A string containing the the ID of this patient, the toString of main.ptList's entry for the element's index and the value separated by tabs. 
     */
    public String toStringElements(int e){
        if(e<1)return null;
        String s = ptID+"~";
        int i = 0;
        Iterator itr = search.iterator();
        Element a;
        while(itr.hasNext() && i<e){
            a = (Element)itr.next();
            s = s+"\t"+main.ptList.get(a.getIndex()) + ":" + a.getValue();
            i++;
        }
        return s;
    }
    /**
     * This is probably a bad way of doing it, but clearTaken just removes the Elements that were chosen in one run of 
     * pullRow from MatchSelector from the searchContainer.
     */
    public void clearTaken(){
        //this is run at the end of a "pull line" command to remove all of the patients that were chosen in that round.
        Iterator itr = search.iterator();
        Element a;
        ArrayList<Element> toRemove = new ArrayList<>();
        while(itr.hasNext()){
            a = (Element)itr.next();
            if(!main.ptAvail.contains(a.getIndex())) {
                toRemove.add(a);//can't change the tree while using the iterator. ;) Live and learn. 
                has --;
            }
        }
        toRemove.stream().forEach((i) -> {
            search.remove(i);
        });
        
        if(has == 0)processCross();
    }
    
    public SearchContainer(int pt, int ptInd, int needs){
        ptID = pt;
        ptIndex = ptInd;
        has = 0;
        this.needs = needs;
        search = new TreeSet<>(); 
    }
}
