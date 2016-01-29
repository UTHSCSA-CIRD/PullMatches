package helperspkg;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author manuells
 */

import java.io.*;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public abstract class FHandler {
    //this class holds abstract method to handle the opening of files for Main 
    //and keep that code clean
    public static File verifyOpenFile(String fStr, char opts){
        File f = new File(fStr);
        if(!f.exists()){
            System.err.println("Warning: File " + fStr + " does not exist!!");
            return null;
        }
        if(!f.isFile()){
            System.err.println("Error: " + fStr + " is not a file!!");
            return null;
        }
        switch(opts){
            case 'R':
                if(!f.canRead()){
                    System.err.println("Error: No read access to " + fStr);
                    return null;
                }
                break;
            case 'B'://both
                if(!f.canRead()){
                    System.err.println("Error: No read access to " + fStr);
                    return null;
                }
            case 'W':
                if(!f.canWrite()){
                    System.err.println("Error: No write access to " + fStr);
                    return null;
                }
        }//end switch
        return f;
    }
    public static ArrayList<File> readFilesFromAscii(File f, char mode){
        ArrayList<String> st = readLinesFromAscii(f);
        ArrayList<File> ret = new ArrayList<>();
        if(st ==null) return null;
        for (String st1 : st) {
            File r = verifyOpenFile(st1, mode);
            if (r == null) {
                System.err.println("File " + st1 + "could not be opened ");
                return null;
            }
            ret.add(r);
        }
        return ret;
    }
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
