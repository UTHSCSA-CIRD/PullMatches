
package helperspkg;
import java.util.Random;

     
/**
 * This class is just a wrapper for the random function.
 * @author Laura Manuel
 */
public class GetRand {
    Random r;
    public int getOneOrZero(){
        return r.nextInt(2);
    }
    public int getFromZeroTo(int to){
        return r.nextInt(to);//non inclusive of to;
    }
    /**
     * This just exists to reduce the issues with selecting from a uniform dist. 
     * @param to The range for the selecting non-inclusive of to.
     * @return The integer selected. 
     */
    public int getNormalFromZeroTo(int to){
        to = to-1;
        double gaus;
        gaus = r.nextGaussian();
        gaus = gaus*(to/3)+(to/2); 
        //note: to get 3 standard deviations from the mean to the edge you would use to/6, but we don't want to offset
        //the values that much, we just don't want to remove the first values as much as we would with a uniform dist.
        while(gaus < 0 || gaus > to){//changing mean from 0 to to/2
            gaus = (r.nextGaussian()*(to/3))+(to/2);
        }
        return (int)Math.round(gaus);
    }
    /**
     * Initiates a new random number with the seed i
     * @param i Seed
     */
    public GetRand(long i){
        r = new Random(i);
    }
    public GetRand(){
        r = new Random();
    }
        
    
}
