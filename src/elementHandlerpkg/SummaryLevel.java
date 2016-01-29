/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elementHandlerpkg;
import java.io.Serializable;
/**
 *
 * @author manuells
 */
public class SummaryLevel implements Comparable<SummaryLevel>, Serializable{
    public final int level;
    public final int value;
    private static final long serialVersionUID = 1;
    
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
