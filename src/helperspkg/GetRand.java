/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helperspkg;
import java.util.Random;

     
/**
 *
 * @author manuells
 */
public class GetRand {
    Random r;
    public int getOneOrZero(){
        return r.nextInt(2);
    }
    public int getFromZeroTo(int to){
        return r.nextInt(to);//non inclusive of to;
    }
    public int getNormalFromZeroTo(int to){
        to = to-1;
        double gaus;
        
        gaus = r.nextGaussian();
        gaus = gaus*(to/3)+(to/2);
        while(gaus < 0 || gaus > to){//changing mean from 0 to to/2
            gaus = (r.nextGaussian()*(to/3))+(to/2);
        }
        return (int)Math.round(gaus);
    }
    
    public GetRand(long i){
        r = new Random(i);
    }
    public GetRand(){
        r = new Random();
    }
        
    
}
