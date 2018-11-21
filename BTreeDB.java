import java.util.*;
import java.io.*;
public class BTreeDB {
    // RandomAccessFile and File set as universal variables so that other methods
    //outside the try catch statement can reference to it.
    public static RandomAccessFile dVal;
    public static File dv;
    public static void main(String[] args) throws IOException{
       
        HashMap<Integer,String > hash = new HashMap<>();
        //instantiate ValueManager which has an empty constructor.
        ValueManager valueMan = new ValueManager();
        long numRecords = 0;       
        
        try{
            // creates and reads the data.bt files
            File db = new File(args[0]);
            RandomAccessFile dBt = new RandomAccessFile(db, "rwd");
            
            dv = new File(args[1]);
            dVal = new RandomAccessFile(dv, "rwd");
            System.out.println("dval created");
            if(dv.exists())
            numRecords = dVal.readLong();
        
        }
        catch(IOException e){ 
        }
        
        /*
            This is the start of looking at the inputs
        */
        Scanner in = new Scanner(System.in);
        // breaks out of while loop
        // and program ends
        OUT:
        while(true){
            System.out.println("accepting input...");
            // gets input and splits 
            String s = in.nextLine();
            String[] input = s.split(" ");
            String word = "";
            // in the case that the value has more than 1 words, this for loop adds all of it
            if(input.length>=3){
                for(int i =2; i<=input.length-1; i++){
                    if(i == input.length-1){
                        word+= input[i];
                    }else{
                          word+=input[i]+ " ";
                    }
                }
            }
            
            // checks the first word
            // if properly inputted
            switch(input[0]){
                case "insert":
                    // if input length is correct
                    if(input.length>=3){
                        //we might need the key but for now I havent used it in anything
                        long key = Long.parseLong(input[1]);
                    // adds also the key?
                    valueMan.insert(dVal,word,numRecords);
                    numRecords++;
                    }else{ // if input is worng or added too much stuff
                        error("insert", Integer.parseInt(input[1]));
                    }
                   
                    break;
                case "select":
                    if(input.length>2)
                        error("select", Integer.parseInt(input[1]));
                    else
                        select();
                    break;
                case "update":
                    update();
                    break;
                case "exit":
                    break OUT;
                default:
                    error("wrong",0);
            }
        }
        dVal.close();
        
        
    }
   
    public static void select(){
        
    }
    public static void update(){
        
    }
    public static void error(String word, int key){
        System.out.print("ERROR: ");
        switch (word) {
            case "select":
                // error for select is not specified
                 System.out.println(key+" does not exist.");
                break;
            case "insert":
                System.out.println(key+" already exists.");
                break;
            case "update":
                System.out.println(key+" does not exist.");
                break;
            default:
                System.out.println( "invalid command.");
                break;
        }
            
    }
    
    
}
