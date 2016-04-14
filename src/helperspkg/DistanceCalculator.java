
package helperspkg;
import elementHandlerpkg.Summary;
import elementHandlerpkg.SummaryLevel;
import java.util.ArrayList;
import mainpkg.main;

/**
 *  This class contains the distance calculators for the matcher. These calculators can be update or new ones added. 
 * Euclidean and BinaryDistance were created based on R's distance comparison function, but are optimized for 
 * sparse- sorted lists of levels. 
 * @author Laura Manuel
 */
public abstract class DistanceCalculator {
    /**
     * Calculates the Euclidean distance from one patient to the other. This means that the 
     * function takes into account the VALUE (or number of instances) for each level as well as the existence of a level
     * You are less likely to get duplicate distance values with this than with the binary distance. 
     * @param a One of the summaries. (it doesn't matter which)
     * @param b The other summary. (again, it doesn't matter which because the distance will be the same either way.
     * @return The distance between summary a and summary b
     */
    public static double euclideanDistance(Summary a, Summary b){
        /*for each level- if both na/0 skip. if one or neither 0 process 
        *subtract a.value from b.vaue
        *square values
        *add squared to running dist
        *if no matches return 1; 
        *return sqrt(dist/count)
        */
        ArrayList<SummaryLevel> aL = a.getLevels();
        ArrayList<SummaryLevel> bL = b.getLevels();
        if(main.restrictLevels){
            ArrayList<SummaryLevel> tmp = new ArrayList<>();
            for (SummaryLevel sum : aL) {
                if(main.availLevels.contains(sum.level)){
                    tmp.add(sum);
                }
            }
            if(tmp.size()<1){
                return 1;
            }
            aL = tmp;
            tmp = new ArrayList<>();
            for (SummaryLevel sum : bL) {
                if(main.availLevels.contains(sum.level)){
                    tmp.add(sum);
                }
            }
            if(tmp.size()<1){
                return 1;
            }
            bL = tmp;
        }
        int ai, bi, an,bn, count;
        double dist;
        ai = bi = 0;
        count =0;
        dist = 0;
        an = aL.size();
        bn = bL.size();
        while(ai<an || bi<bn){
            //while either ai or bi is less than the length an,bn
            count++;
            if(ai<an){
                SummaryLevel aa = aL.get(ai);
                if(bi<bn){
                    SummaryLevel bb = bL.get(bi);
                    if(aa.level< bb.level){
                        dist += aa.value*aa.value;
                        ai++;
                    }else{
                        if(aa.level>bb.level){
                           dist += bb.value*bb.value;
                           bi++; 
                        }else{
                            //they are equal
                            dist += (aa.value-bb.value)*(aa.value-bb.value);
                            ai++;bi++;
                        }//end else aa[0]==bb[01]
                    }//end else
                }else{//else !bi<bn
                    dist += (aa.value)*(aa.value);
                    ai++;
                }//end else !bi<bn
            }else{//else !ai<an
                //no more as add the bs;
                SummaryLevel bb = bL.get(bi);
                dist += bb.value*bb.value;
                bi++; 
            }//end else !ai<an
        }//end while
        return Math.sqrt(dist/(double)count);
    }
    /**
     * Calculates the binary distance from one patient to the other. This means that the 
     * function takes into account ONLY the existence of a level, not its value.
     * You are more likely to get duplicate distance values with this than with the Euclidean distance. 
     * @param a One of the summaries. (it doesn't matter which)
     * @param b The other summary. (again, it doesn't matter which because the distance will be the same either way.
     * @return The distance between summary a and summary b
     */
    public static double binaryDistance(Summary a, Summary b){
        ArrayList<SummaryLevel> aL = a.getLevels();
        ArrayList<SummaryLevel> bL = b.getLevels();
        
        if(main.restrictLevels){
            ArrayList<SummaryLevel> tmp = new ArrayList<>();
            for (SummaryLevel sum : aL) {
                if(main.availLevels.contains(sum.level)){
                    tmp.add(sum);
                }
            }
            if(tmp.size()<1){
                return 1;
            }
            aL = tmp;
            tmp = new ArrayList<>();
            for (SummaryLevel sum : bL) {
                if(main.availLevels.contains(sum.level)){
                    tmp.add(sum);
                }
            }
            if(tmp.size()<1){
                return 1;
            }
            bL = tmp;
        }
        int ai, bi, an,bn, dist, total;
        ai = bi = 0;
        dist =total=0;
        an = aL.size();
        bn = bL.size();
        int aa;
        int bb;
        while(ai<an && bi<bn){
            total++;
            aa = aL.get(ai).level;
            bb = bL.get(bi).level;
            if(aa > bb){
                bi++;
                dist++;
            }else if(aa < bb){
                ai++;
                dist++;
            }else{
                ai++;bi++;
            }
        }//end while
        //optimization - we don't need to loop through the remaining entries
        if(ai<an){
            dist += (an-ai);
            total += (an-ai);
        }else if(bi<bn){
            dist += (bn - bi);
            total += (bn - bi);
        }
        return (double)dist/(double)total;
    }
    
}
