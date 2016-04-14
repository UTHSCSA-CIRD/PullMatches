
package elementHandlerpkg;

/**
 * The MatchSelector class handles popping the best matched patient from each SearchContainer and making sure that no 
 * patient is duplicated in the matched output (all patient matches are unique and you don't have 2 controls that are the same
 * patient.)
 * @Laura Manuel
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import mainpkg.main;
import java.util.TreeSet;
import java.util.Iterator;

public class MatchSelector {
    ArrayList<Integer> cache; // caches the previous random order. This order will be shuffled every time. 
    Random r;
    /**
     * This pulls a row of matched patients from each of the search containers. Ahem:
     * 1) First the matcher uses its random class and the Java Collections class to shuffle the cache of SearchContainers. 
     * 2) MatchSelector iterates through the shuffled cache of SearchContainers
     * 3) It pops the Element with the best distance from the SearchContainer with popMin()
     * 3.5) If the SearchContainer has expended its list of Elements it calls processCross(taken) and refreshes the list of elements.
     * 4) A MatchHolder is created and wrapped around the popped Element, then there is an attempt to add it to the table.
     * 5) If there is a conflict (another patient has this patient) the matcher attempts to take the MatchHolder with the lowest distance score. 
     * If the distance scores are equal (highly unlikely) it randomly selects one of the two matches to win.
     * The loser becomes the new current SearchContainer, and steps 3-5 are repeated until there is no longer a conflict when
     * attempting to add the element.
     * 6)MatchSelector iterates over the taken list of MatchHolders, looks up the patient numbers for the indexes
     * and creates a return string of each matched patient. It removes all of the "taken" patients from the main.ptAvail array
     * and, if another row needs to be pulled, the calling class will then have the SearchContainers remove those patients 
     * from their caches.
     * 
     * Note:Program fails if there are not enough patients to match. Note that there are checks earlier in the program to make sure that this error never happens. 
     * @return A return string of the unique patients matched for this row from the SearchContainer. 
     */
    public String pullRow(){
        //runs through all of the scList elements in random order and matches one row at a time.
        //returns the string that is printed
        //updates avail patients
        TreeSet<MatchHolder> taken = new TreeSet<>();//caches the patients taken in this round. 
        SearchContainer a;
        MatchHolder ea;
        MatchHolder eb;
        Element popped;
        
        //shuffle cache
        Collections.shuffle(cache, r);
        
        // might need something here. 
        //start looping through cache
        for (Integer cachei : cache) {
            a = main.scList.get(cachei);
            int current = cachei;
            popped = a.popMin();
            if(popped == null){
                a.processCross(taken);
                popped = a.popMin();//we shouldn't run out of elements because we ran checks to make sure we'd have enough;
                if(popped == null){
                    System.err.println("Error processing taken process " + a.getID());
                    System.exit(-1);
                }
            }
            ea = new MatchHolder(current,popped);
            while(!taken.add(ea)){// note, there is no get equal to, so this is the fastest way to check if the element already exists.
                //we have a conflict! 
                //3 possibilities
                eb = taken.ceiling(ea);//greater than or equal to, if we can't add ea there is a value equal to it. 
                if(eb.getMatchValue()<ea.getMatchValue()){
                    //if the current one has the best value we leave it be and pop another value for 
                    popped = a.popMin();
                    if(popped == null){
                        a.processCross(taken);
                        popped = a.popMin();//we shouldn't run out of elements because we ran checks to make sure we'd have enough;
                    }
                    ea = new MatchHolder(current,popped); //set ea to equal the next pop and let it loop. 
                }else{if(eb.getMatchValue() > ea.getMatchValue()){
                    taken.remove(eb);
                    taken.add(ea);
                    current = eb.getIndex();
                    a = main.scList.get(current);
                    popped = a.popMin();
                    if(popped == null){
                        a.processCross(taken);
                        popped = a.popMin();//we shouldn't run out of elements because we ran checks to make sure we'd have enough;
                    }
                    ea = new MatchHolder(current,popped);
                }else{
                    //they are equal, select a random one. 
                    int rand = r.nextInt(2); // next integer either 0 or 1(exclusive of 2);
                    if(rand == 0){
                        popped = a.popMin();
                        if(popped == null){
                            a.processCross(taken);
                            popped = a.popMin();//we shouldn't run out of elements because we ran checks to make sure we'd have enough;
                        }
                        ea = new MatchHolder(current,popped); //set ea to equal the next pop and let it loop. 
                    }else{
                        taken.remove(eb);
                        taken.add(ea);
                        current = eb.getIndex();
                        a = main.scList.get(current);
                        popped = a.popMin();
                        if(popped == null){
                            a.processCross(taken);
                            popped = a.popMin();//we shouldn't run out of elements because we ran checks to make sure we'd have enough;
                        }
                        ea = new MatchHolder(current,popped);
                    }
                }//end else not eb > ea
                }//end else not eb < ea
            }//end while our chosen index is taken. 
        }
        Iterator iter = taken.iterator();
        String ret = "";
        while(iter.hasNext()){
            //O(n) get each element  from taken, add it to "log" then remove it from ptAvail.
            ea = (MatchHolder)iter.next();
            main.ptAvail.remove(ea.getMatchIndex());//(Ologn)
            ret = ret + "\r\n" + main.scList.get(ea.getIndex()).getID()+"\t"+ main.ptList.get(ea.getMatchIndex()).getPTNum() + "\t" + ea.getMatchValue();
        }
        
        //Note that the calling class should then have all of the search containers make sure that they remove the
        //elements that are no longer in ptAvail and run their cross again if they need to. 
        //This should be done in the main list because we don't want to run it again if we don't need another row 
        //as it can be very processing and io intensive if we need to process cross again. 
        return ret; 
    }
    /**
     * MatchSelector creates and fills a cache array with the SearchContainers from the main.scList. This cache
     * allows the MatchSelector to shuffle the search containers without altering the 
     * 
     * @param seed The random seed for the randomization algorithm. This is either passed as an argument for debugging purposes
     * or it is defaulted to the system long time. 
     */
    public MatchSelector(long seed){
        //for debugging this allows you to use a set seed.
        cache = new ArrayList<>();
        for(int i = 0; i< main.scList.size(); i++){
            cache.add(i);
        }
        r = new Random(seed);
    }
    
}
