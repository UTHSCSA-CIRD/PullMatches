/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elementHandlerpkg;

/**
 *
 * @author manuells
 */
public class MatchHolder implements Comparable<MatchHolder>{
    private final Element el; //the element that it's matching to // note that the "index" in here is the patient being matched to
    private final int index; //this is the scList index of the patient this match belongs to 
    //index of patient whose match is el.index with a value of el.value; 
     
    public int getIndex(){
        return index;
    }
    public int getMatchIndex(){
        return el.getIndex();
    }
    public double getMatchValue(){
        return el.getValue();
    }
    public Element getElement(){
        return el;
    }
    
    @Override
    public int compareTo(MatchHolder a){
        //we're comparing the indexes for our element tree to make sure that we don't have duplicate patients that we are matching to
        if(el.getIndex()< a.getMatchIndex()) return 1;
        if(el.getIndex() > a.getMatchIndex()) return -1;
        return 0;
    }
    public MatchHolder(int ind, Element e){
        index = ind;
        el = e;
    }
    
}
