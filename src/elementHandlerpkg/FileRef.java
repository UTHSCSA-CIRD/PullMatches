package elementHandlerpkg;

/**
 *
 * @author manuells
 */
import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;

public class FileRef implements Comparable<FileRef>{
    private RandomAccessFile f;
    private long startIndex;
    private long endIndex;
    private String name;
    
    public long getStartIndex(){
        return startIndex;
    }
    public long getEndIndex(){
        return endIndex;
    }
    public String getName(){
        return name;
    }
    @Override
    public int compareTo(FileRef e){
        if(e.getStartIndex() < startIndex) return -1;
        if(e.getStartIndex() > startIndex) return 1;
        return 0;
    }
    
    public int inIndex(int a){
        if(a < startIndex){
            return -1;
        }
        if(a>endIndex){
            return 1;
        }
        return 0;
    }
    public double getSingleIndex(long index){
        //this reads a single byte as a double 
        if(index >= endIndex){
            System.err.println("Index: " + index+ " is greater than endIndex!!! " + endIndex);
            return -1;
        }
        long st = index - startIndex;
        try{
            byte[] bb = new byte[8];
            f.seek(st*8);
            int a = f.read(bb);
            if(a < 8){
                System.err.println("Uh oh! We couldn't read in a full double! "+index);
                System.exit(-1);
            }
            return ByteBuffer.wrap(bb).order(ByteOrder.LITTLE_ENDIAN).getDouble();
        }catch(IOException e){
            System.err.println("Houston, we have a problem.... in file " +name+" getting index: "+ index + " failed!!!" + e.getMessage());
            return -1;
        }
    }
    public double[] getMultipleIndex(long stIndex, int numIndexes){
        //this will read in multiple bytes, then convert them to doubles.
        if(stIndex > endIndex)return null;
        if(stIndex +numIndexes >= endIndex) return null;
        long index = (int) (stIndex - startIndex);
        
        byte[] b = new byte[numIndexes*8];
        try{
            f.seek(index*8);
            f.read(b);
        }catch(IOException e){
            System.err.println("There was an exception when reading from " + name 
                    + " stIndex " + stIndex + " reading " + numIndexes + " doubles./n" 
                    + e.getMessage());
            System.exit(-1);
        }
        DoubleBuffer bytee = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer();
        double[] ret = new double[bytee.capacity()];
        bytee.get(ret);
        return ret;
    }
    
    public boolean initFile(File a, long startIndex){
        //initialize the file and return true if correct false if error. 
        this.startIndex = startIndex;
        endIndex = (int)startIndex + (a.length()/8);
        name = a.getName();
        try{
            f = new RandomAccessFile(a, "r");
            return true;
        }catch(IOException e){
            System.err.println("Error, can't open " + a.getName()+ " as RandomAccess. /n" +e.getMessage());
            return false;
        }
    }
    public void close(){
        try{
            f.close();
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
    }
    
    public FileRef(){
        f = null;
        startIndex = 0;
        endIndex = 0;
    }
    public FileRef(long start){
        //comparator for tree
        f = null;
        startIndex = start;
    }
    
}
