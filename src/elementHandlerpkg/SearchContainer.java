package elementHandlerpkg;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author manuells
 */
import helperspkg.DistanceCalculator;
import helperspkg.FileRefPuller;
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
    
    public int getID(){
        return ptID;
    }
    public int getIndex(){
        return ptIndex;
    }
    
    public double getMin(){
        if( has ==0){
            return -1;
        }
        return search.first().getValue();
    }
    public double getMax(){
        if(has == 0){
            return -1;
        }
        return search.last().getValue();
    }
    public Element getMaxElement(){
        return search.last();
    }
    public Element getFirstElement(){
        return search.first();
    }
    public int getNumElements(){
        return has;
    }
    public int getMaxElements(){
        return needs;
    }
    
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
                    search.remove(a);
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
    
    public void processCross(){
        //Calculates the distances for all available patients and caches the best "needs" matches. 
        if(has == needs) return;
        //get the lower (less than this one) that have to be picked out one at a time. 
        //NOTE: This is where our value is the column. To reduce the number of io calls we 
        //only call the files that are in available patients
        Iterator itr = main.ptAvail.headSet(ptIndex).iterator();
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
    public void processCross(TreeSet<MatchHolder> dont){
        //dont is a list of patient ids not to pull from. 
        //this tells the method to step through the available list and 
        //add the best x values to element a. Note - if at this point in time
        //has == needs we do nothing since the element list hasn't changed.
        //This saves us time if for some reason the patient is called twice between which 
        //there were no changes
        //note: has really should be zero when this is called...
        if(has == needs) return;
        //get the lower (less than this one) that have to be picked out one at a time. 
        //NOTE: This is where our value is the column. To reduce the number of io calls we 
        //only call the files that are in available patients
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
    public String toStringElements(int e){
        if(e<1)return null;
        String s = ptID+"";
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
