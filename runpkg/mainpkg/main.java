package mainpkg;

/**
 *
 * @author Laura Manuel
 */
import helperspkg.*;
import elementHandlerpkg.*;
import java.io.*;
import java.util.ArrayList;
import java.util.TreeSet;

public class main {
    public static int ptSize; //the number of patients in the .ffdat files.
    public static ArrayList<Summary> ptList = null; // static list containing the pt ID numbers for the row/cols of .ffdat files. 
    public static TreeSet<Integer> ptAvail = null; //static list holds the indexes of the patients that are avilable. 
    public static ArrayList<SearchContainer> scList = null; //static list holds the search container for each pt in args[0]
    public static int needs; //the number of records each pt needs
    public static GetRand rand;// gives all classes access to a random number generator (ie. SearchContainer)
    public static boolean restrictLevels = false;
    public static TreeSet<Integer> availLevels = null;
    private static final String ARGERROR = "Error, in expected arguments. \n Expected:"
                    + "\n1-Sorted ascii file with no duplicates: pt to match one pt per line"
                    + "\n2-int number of matches to pull per patient"
                    + "\n3- (optional) \"-L|-LS\" \"loadFile.txt\" - ascii summary file from oracle sorted by ptNum, comma deliminated."
                    + "this is used when you need to load a new patient set. Note that if it exists the level table is reused."
                    + "As a memory intensive, processing light option you can choose -L instead of -LS to not save the summary file."
                    + "\n4- (optional) \"-o\" \"output file\" - can be omitted. Default file is matched.txt"
                    + "\n5- (optional) \"-i|e\"\"inclusion/exclusionFile\" - Sorted ascii file with no duplicates: Can "
                    + "be omitted ascii list of ptIDs that should be included or excluded from the comparison."
                    + "\n6- (optional) \"-m\" \"e|b\" e- euclidean distance b-binary distance - default binary"
                    + "\n7 - (optional) \"-r\" ### - Set a defined random seed. This allows you to run the same analysis multiple times with the same results."
                    + "\n8- (optional) \"-li|-le\" \"inclusion\\exclusion file\" -Ascii file one element per line of facts that should be included or excluded";
    public static char method = 'b';
    public static boolean setRandSeed = false;
    public static long randSeed;
    private static BufferedWriter wout;
    //tree set has more efficient add/delete/search than 
    /**
     * @param args the command line arguments
     * @throws java.io.IOException Throws IOExceptions... 
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        if(args.length!=2 && args.length !=4 && args.length != 6 && args.length!=8 && args.length!=10 && args.length != 12 && args.length !=14){
            System.err.println(ARGERROR+ "\n**SIZE CHECK** Arglength is: " + args.length);
            for(String arg : args){
                System.err.println(arg);
            }
            System.exit(-1);
        }
        
        boolean saveSummary = true;
        int inclExcl =-1;//temporary int to hold inclusion exclusion. 
        int incEx = 0; //this is an error check to make sure that the user did not try to both include and exclude lists.
        int load = -1;
        int levelInc = 0;
        int levIncFile = -1;
        String setWOut = "matched.txt";
        if(args.length >2){
            //this section handles all of the optional argument pairs. 
            String arg;
            for(int i=2; i<args.length;i= i+2){
                arg = args[i];//+2 starts at index 2(third element first optional) + x*2 will take us to each subsequent x
                switch(arg){
                    case "-L":
                        //we need to load in a new patient set! 
                        if (load != -1){
                            System.err.print(ARGERROR + "\n ** multiple loads! **");
                            for(String argi : args){
                                System.err.println(argi);
                            }
                            System.exit(-1);
                        }
                        saveSummary = false;
                        load = (i+1);
                        break;
                    case "-LS":
                        //we need to load in a new patient set! 
                        if (load != -1){
                            System.err.print(ARGERROR + "\n ** multiple loads! **");
                            for(String argi : args){
                                System.err.println(argi);
                            }
                            System.exit(-1);
                        }
                        load = (i+1);
                        break;
                    case "-o":
                        //output file
                        setWOut = args[i+1];
                        break;
                    case "-i":
                        //inclusion file
                        if(incEx!=0){
                            System.err.print(ARGERROR + "\n ** multiple inclusion/exclusions! **");
                            for(String argi : args){
                                System.err.println(argi);
                            }
                            System.exit(-1);
                        }
                        incEx = 1;
                        inclExcl = i+1;
                        break;
                    case "-e":
                        //exclusion
                        if(incEx!=0){
                            System.err.print(ARGERROR + "\n ** multiple inclusion/exclusions! **");
                            for(String argi : args){
                                System.err.println(argi);
                            }
                            System.exit(-1);
                        }
                        incEx = -1;
                        inclExcl = i+1;
                        break;
                    case "-m":
                        //changing method?
                        char meth = args[i+1].charAt(0);
                        if(meth == 'e' || meth == 'b'){
                            method = meth;
                        }else{
                            System.err.print(ARGERROR + "\n **"+meth+" is an invalid method!! Options are e(euclidean) or b(binary) **");
                            for(String argi : args){
                                System.err.println(argi);
                            }
                            System.exit(-1);
                        }
                        break;
                    case "-r":
                        //changing method?
                        setRandSeed = true;
                        try{
                            randSeed = Long.parseLong(args[i+1]);
                        }catch(NumberFormatException e){
                            System.err.println("Error, could not parse " + args[i+1] + " to a long." + ARGERROR);
                            System.exit(-1);
                        }
                        break;
                    case "-li":
                        //inclusion levels list
                        if(levelInc != 0){
                            System.err.println("You have included multiple inclusion/exclusion lists for the levels, please condense this into one list.");
                            System.exit(-1);
                        }
                        levelInc = 1;
                        levIncFile = i+1;
                        break;
                    case "-le":
                        //exclusion levels list
                        if(levelInc != 0){
                            System.err.println("You have included multiple inclusion/exclusion lists for the levels, please condense this into one list.");
                            System.exit(-1);
                        }
                        levelInc = -1;
                        levIncFile = i+1;
                        break;
                    default:
                        System.err.print(ARGERROR + "\n ** Argument: "+arg+" is not valid!!!**");
                        for(String argi : args){
                            System.err.println(argi);
                        }
                        System.exit(-1);
                }//end switch
            }//end for loop for handling arguments;   
        }//end if arglength >2  
        wout = ArgHandler.openOutFile(setWOut);
        if(load == -1){
            //load existing summary.
            ArgHandler.loadSummaries();
        }else{
            ArgHandler.loadNewInputThrottled(args[load], saveSummary);
        }
        if(levelInc != 0){//if we're restricting the levels, lets call up the level.dat file and send it on to the inclusion/exclusion methods.
            if(levelInc == -1){
                //exclusion
                ArgHandler.levelExclusion(ArgHandler.openLevels(), args[levIncFile]);
            }else{
                //inclusion
                ArgHandler.levelInclusion(ArgHandler.openLevels(), args[levIncFile]);
            }
        }
        if(setRandSeed){
            rand = new GetRand(randSeed);
        }else{
            rand = new GetRand(System.currentTimeMillis());
        }
        try{
            needs = Integer.parseInt(args[1]);
        }catch(NumberFormatException e){
            System.err.println("ERROR! Could not parse matches integer: "+ args[1] +"!" + ARGERROR);
            System.exit(-1);
        }
        
        //handle everything for the second argument. Note that this must be done prior to the first
        //argument being handled because the first argument needs to setup "SearchContainers"
        //which require that the patient index in ptList be provided. 
        //ArgHandler.getFileRefTree(args[1]);
        ArgHandler.createSearchContainers(args[0]);
        //if inclusion exclusion
        if(incEx ==-1){
            ArgHandler.handleExclusion(args[inclExcl]);
        }
        if(incEx == 1){
            ArgHandler.handleInclusion(args[inclExcl]);
        }
        if(ptAvail.size() < (main.needs* scList.size()) ){
            
            if(main.needs < 1){
                System.err.println("Your available list after inclusions and exclusions is not large enough for your selection size.");
                System.exit(-1);
            }
            System.err.print("Warning: After Exclusion/Inclusion needs would require: " + ((scList.size()*main.needs)) + " have: " + main.ptAvail.size()
                   + " reducing needs to "); 
            main.needs = ptAvail.size()/scList.size(); // floor is naturally taken in integer arithmatic. 
            System.err.print(main.needs + "\n");
        }
        //Yay! We're done with the args!
        //Begin handling the processing.
        //First -- code for iterating through
        String str = "ID\tMatches ID \tDistance";
        scList.parallelStream().forEach((scList1) -> {
            scList1.processCross();
        });
        try{//try to print out the headers; 
            wout.write(str);
            //wout.write("Patient\tMatch\tDistance");
            wout.flush();
        }catch(IOException e){
            System.err.println("There was an issue with printing headers\n"+e.getMessage());
        }
        MatchSelector a;
        if(setRandSeed){
            a = new MatchSelector(randSeed);
        }else{
            a = new MatchSelector(System.currentTimeMillis());
        }
        for(int i = 0; i<needs;i++ ){
            //for each i in needs, 
            str = a.pullRow();
            try{
                wout.write(str);
                wout.flush();
            }catch(IOException e){
                System.err.println("There was an issue with printing row... "+i+"\n"+e.getMessage());
            }
            scList.parallelStream().forEach((scList1) -> {
                scList1.clearTaken();
            });
        }
        wout.close();
    }
}
