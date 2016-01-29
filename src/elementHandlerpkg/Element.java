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
public class Element implements Comparable<Element>{
    private int index;
    private double val;
    
    public int getIndex(){
        return index;
    }
    public double getValue(){
        return val;
    }
    

    @Override
    public int compareTo(Element a){
        //this is for the binary search tree-- as multiple patients may have
        //the same distance it will use the patient's index.  Note: index is used
        //to reduce the number of calls to get the patient ID from the patient list. 
        //should only do this when ready to print to file, everything else utilizes
        //the index of the patient. 
        if(a.getValue() > val)return -1;
        if(a.getValue() < val) return 1;
        if(a.getIndex()>index) return -1;
        if(a.getIndex()<index)return 1;

        return 0;//same value and id...? duplicate!
    }
    
    public Element(int ind, double val){
        this.index = ind;
        this.val = val;
    }
    
}
