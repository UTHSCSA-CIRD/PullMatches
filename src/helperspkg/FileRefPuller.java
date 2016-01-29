/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helperspkg;
import elementHandlerpkg.FileRef;
import mainpkg.main;
import java.util.ArrayList;
/**
 *
 * @author manuells
 */
public abstract class FileRefPuller {
    public static double getIndex(long index){
        //accepts a long containing the index (in the ffdat files) of the file.
        //find the right FileRef
        FileRef tmp = new FileRef(index);
         //get the ceiling (first >= -- since it compares on startIndex)
        tmp = main.refs.ceiling(tmp);
        //if the FileRef exists... 
        if(tmp == null){
            System.err.println("Index: " + index + " does not exist!!");
            System.exit(-1);
        }
        return tmp.getSingleIndex(index);
    }
    public static double[] getIndexRange(long start, int length){
        //inclusive of both start and stop;
        //initialize ret
        double[] ret = new double[length];
        //find the right FileRef
        FileRef tmp = new FileRef(start);
        tmp = main.refs.ceiling(tmp); 
        if(tmp == null)System.exit(-1);
        long end = tmp.getEndIndex();
        if((start + length) < end ){//< since EndIndex is non-inclusive.
             //in one file
            return tmp.getMultipleIndex(start, length);
        }else{
            //in 2 -- if it's in more than 2.... That shouldn't happen. Files should
            //be near int max in size to reduce the number of files. Cannot have more than int max 
            //comparisons. 
            //This can be upgraded in the future if # of patients approaches int max or if smaller files seem to be easier.
            double[] d = tmp.getMultipleIndex(start, (int)(tmp.getEndIndex()-start));
            tmp = new FileRef(end);
            tmp = main.refs.ceiling(tmp); 
            if(tmp == null) System.exit(-1);
            double[] c = tmp.getMultipleIndex(end, (length - d.length));
            //chunk mem copy
            System.arraycopy(d, 0, ret, 0, d.length);
            System.arraycopy(c, 0, ret, d.length, c.length);
            return ret;
        }
    }
}
