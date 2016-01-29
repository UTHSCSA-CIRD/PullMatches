/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elementHandlerpkg;
import java.io.Serializable;
import java.util.ArrayList;
/**
 *
 * @author manuells
 */
public class Summary implements Serializable{
    private final int ptNum;
    private final ArrayList<SummaryLevel> levels;//sorted list of levels
    private static final long serialVersionUID = 1;
    
    public int getPTNum(){
        return ptNum;
    }
    public ArrayList getLevels(){
        return levels; 
    }
    public Summary(int id, ArrayList<SummaryLevel> sortedList){
        ptNum = id;
        levels = sortedList;
    }
    
}
