/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elementHandlerpkg;
import java.io.Serializable;
/**
 * This contains an individual "level" summary for a patient. Showing how many instances of a particular level they have.
 * 
 * @author Laura Manuel
 */
public class SummaryLevel implements Comparable<SummaryLevel>, Serializable{
    public final int level;
    public final int value;
    private static final long serialVersionUID = 1;
    
    /**
     * compareTo required by the sort function.
     * @param a The SummaryLevel to compare it to.
     * @return 1, -1, 0. Only compares the value of the level itself. 
     */
    @Override
    public int compareTo(SummaryLevel a){
        if(level>a.level)return 1;
        if(level<a.level)return -1;
        return 0;
    }
    
    public SummaryLevel(int level, int value){
        this.level = level;
        this.value = value;
    }
}
