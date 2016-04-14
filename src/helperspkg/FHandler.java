package helperspkg;
/**
 * This class, like ArgHandler, is a static method class used to keep the other static classes looking cleaner
 * and to keep like actions together. (Note that this handler can easily be used in other programs requiring these functions
 * whereas ArgHandler is specific to this program.)
 * @author Laura Manuel
 */

import java.io.*;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public abstract class FHandler {
    //this class holds abstract method to handle the opening of files for Main 
    //and keep that code clean
    /**
     * Opens a the file in fStr and verifies that the opts operations can be performed on the file. 
     * 
     * @param fStr The string containing the name of the file to verify.
     * @param opts The operations that we want to verify: 'R' read, 'W' Write, 'B' both. 
     * @return The filePointer(File class) to that file opened with that operation or null.
     */
    public static File verifyOpenFile(String fStr, char opts){
        if(opts != 'R' && opts != 'W' && opts!= 'B'){
            System.err.println("ERROR! Option: " + opts + " is not valid, may only use: R, W, or B for read, write, or both.");
            System.exit(-1);
        }
        File f = new File(fStr);
        if(!f.exists()){
            System.err.println("Warning: File " + fStr + " does not exist!!");
            return null;
        }
        if(!f.isFile()){
            System.err.println("Error: " + fStr + " is not a file!!");
            return null;
        }
        if(opts == 'R' || opts == 'B'){
            if(!f.canRead()){
                    System.err.println("Error: No read access to " + fStr);
                    return null;
                }
        }
        if(opts == 'W' || opts == 'B'){
            if(!f.canWrite()){
                    System.err.println("Error: No write access to " + fStr);
                    return null;
                }
        }
        return f;
    }
    /**
     * Reads in all lines of an Ascii file and returns them in an array.
     * @param f The file to read in
     * @return All the lines of the file one line per array element and sorted by their appearance in the file. 
     */
    public static ArrayList<String> readLinesFromAscii(File f){
        BufferedReader fr;
        try {
            //read in the catalogue file and open all of its parts
            fr = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException ex) {
            System.err.println("Error, file in readFilesFromAscii not found");
            return null;
        }
        ArrayList<String> ret = new ArrayList<>();
        String a;
        try{
            while((a=fr.readLine())!= null){
                //until EOF
                ret.add(a);
            }
            fr.close();
        }catch(IOException e){
            System.err.println("IO Exception in readLinesFromAscii!!");
        }finally{
            try{
                fr.close();
            }catch(IOException e){
                
            }
        }
        return ret;
    }
    /*Depreciated -- but keeping it just in case I want to read from a binary file later. 
    public static ArrayList<Integer> readIntFromffDat(File f){
        ArrayList<Integer> ret = new ArrayList<>();
        
        try(FileInputStream fi = new FileInputStream(f)){
            byte[] buff = new byte[8*1024];//read 2048 integers into buffer
            IntBuffer bb;
            int read;
            int[] ar;
            while((read = fi.read(buff)) != -1){
                //Wrap in a bytebuffer and convert to int buffer
                //using thsi form incase we don't read size buffer integers (had been getting 0s)
                bb = ByteBuffer.wrap(buff,0,read).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
                ar = new int[bb.limit()];
                bb.get(ar);
                //get the int[] array backing the intbuffer.
                for(Integer i:ar){
                    ret.add(i);
                }
            }
        }catch(IOException e){
            System.err.println("Uh oh! File read error in readIntFromFFDat " + f.getName());
            return null;
        }
        return ret;
    }
    */
    /**
     * Reads all lines from an ascii file, then parses all of those lines into integers
     * Prints an error message and continues if any of the numbers don't parse.
     * @param f The file from which to read the integers. 
     * @return An array of integers. 
     */
    public static ArrayList<Integer> readIntFromAscii(File f){
        ArrayList<Integer> ret = new ArrayList<>();
        ArrayList<String> s = readLinesFromAscii(f);
        if(s == null)return null;
        int n;
        for (String item : s) {
            try{
                n = Integer.parseInt(item);
            }catch(NumberFormatException e){
              System.err.println("Error reading from file " + f.getName() + " could not parse line " + item + " to int, skipping.");
              continue;
            }
            ret.add(n);
        }
        return ret;
    }
}
