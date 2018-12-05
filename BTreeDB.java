import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
/*
    theres another variable for number of nodes here, maybe theres a way to have this reference to nodemanager but im not so sure
*/
public class BTreeDB {
    // RandomAccessFile and File set as universal variables so that other methods
    //outside the try catch statement can reference to it.
    public static RandomAccessFile dVal,dBt;
    public static File dv,db;
    public static BTManager btm;
    public static void main(String[] args) throws IOException{
        //instantiate ValueManager which has an empty constructor.
        ValueManager valueMan = new ValueManager();
        long numRecords = 0;       
        long numNodes = 0;
        try{
            // creates and reads the data.bt files
            db = new File(args[0]);
            dBt = new RandomAccessFile(db, "rwd");
            
            dv = new File(args[1]);
            dVal = new RandomAccessFile(dv, "rwd");
            if(dv.exists()){
                numRecords = dVal.readLong();
                numNodes = dBt.readLong();
            }
        
        }
        catch(IOException e){ 
        }
        btm = new BTManager(dBt,numNodes, numRecords);
        
        /*
            This is the start of looking at the inputs
        */
        Scanner in = new Scanner(System.in);
        // breaks out of while loop
        // and program ends
        OUT:
        while(true){
            // gets input and splits 
            String s = in.nextLine();
            String[] input = s.split(" ");
            String word = "";
            String wholeWord = "";
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
                    if(input.length == 1 ){
                        error("no", "0");
                        break;
                    }else if(isNum(input[1]) != true){
                        error("nope", input[1]);
                     }else if(input.length>=2){
                        long key = Long.parseLong(input[1]);
                        //search for key if exists
                        if(doesKeyExist(Long.parseLong(input[1]),dBt, dVal, numRecords )){
                            error("insert", input[1]);
                           
                        }else{
                            dBt.seek(8);
                            btm.insert(dBt.readLong(),dBt,key,numRecords);
                            valueMan.insert(dVal,word,numRecords);
                            System.out.println(key + " inserted.");
                            numRecords++;
                        }
                    }
                    break;
                    
                case "select":
                    if(input.length == 1){
                        error("no", "0");
                        break;
                    }
                    else if(input.length>2)
                        error("no", input[1]);
                    else if(isNum(input[1]) != true)
                        error("nope", input[1]);
                    else
                        select(Long.parseLong(input[1]),dBt,dVal,numRecords);
                    break;
                case "update":
                    if(input.length == 1){
                        error("no", "key");
                        break;
                    }else if(isNum(input[1]) != true)
                        error("nope", input[1]);
                    else if(input.length == 2)
                         error("no", "word");
                    else if (input.length >=3)
                    update(word,Long.parseLong(input[1]),dBt,dVal,numRecords);
                    break;
                       
                case "exit":
                    break OUT;
                default:
                    error("wrong","0");
            }
        }
        dVal.close();
        dBt.close();
        
    }
    /**
     * This method checks if the key to be inserted already exists or not.
     * returns true if key already exists
     * @param key the key to be inserted
     * @param dBt
     * @param dVal
     * @param numRecords
     * @throws IOException 
     */
    public static boolean isNum(String num){
        try{
            Long num2 = Long.parseLong(num);
        }catch(NumberFormatException| NullPointerException nfe){
            return false;
        }
        return true;
    }
    
    
    public static boolean doesKeyExist(long key, RandomAccessFile dBt, RandomAccessFile dVal, long numRecords) throws IOException{
         boolean ok = false;
        //goes through each node in the btree
        for(long j = 0; j<=btm.nm.returnNodes()-1;j++){
            //goes through each key in the node
            for(long i=0;i<4;i++){
                long keyLoc = (112*j)+16+16+(24*i); //112*j is the node. 16 is the header, 16 is the parent+1st child. 24*i is the ith key.
                dBt.seek(keyLoc); 
                long keyItself = dBt.readLong();
                dBt.seek(keyLoc+8); //get offset
                long offset = dBt.readLong(); //check the offset (written in bt)
                if(key==keyItself && offset != -1){
                    return true;
                }
            }
            }
        return false;
    }
    public static void select(long key,RandomAccessFile dBt,RandomAccessFile dVal,long numRecords)throws IOException{
      
        boolean ok = false;
        //goes through each node in the btree
        for(long j = 0; j<=btm.nm.returnNodes()-1;j++){
            //goes through each key in the node
            for(long i=0;i<4;i++){
                long keyLoc = (112*j)+16+16+(24*i); //112*j is the node. 16 is the header, 16 is the parent+1st child. 24*i is the ith key.
                if(keyLoc > ((112*(btm.nm.returnNodes()))+16)){
                    error("select",  Long.toString(key));
                    ok = true;
                    break;
                }
                
                dBt.seek(keyLoc); 
                long keyItself = dBt.readLong();
                dBt.seek(keyLoc+8); //get offset
                long offset = dBt.readLong(); //check the offset (written in bt)
                if(key==keyItself && offset != -1){
                    dVal.seek(256*offset+8); //8 = numrecord
                    Byte b = dVal.readByte(); //reads the string length written in val
                    int strlen = b.intValue(); //chhange it to int
                    byte [] strb = new byte[strlen]; //string length*2 (2 bytes per letter?) PLS CONFIRM
                    dVal.readFully(strb); //read strlen*2 bytes (puts it in array too?)
                    String word = new String(strb,StandardCharsets.UTF_8); //changes it into a string
                    System.out.println(key+" => "+word);
                    ok = true;
                    break;
                }
            }
            if (ok)
                break;
            }
            if(!ok)
            error("select",  Long.toString(key));
    }
    
    public static void update(String change,long key,RandomAccessFile dBt,RandomAccessFile dVal,long numRecords) throws IOException{
          boolean ok = false;
        //goes through each node in the btree
        for(long j = 0; j<=btm.nm.returnNodes();j++){
            //goes through each key in the node
            for(long i=0;i<4;i++){
                long keyLoc = (112*j)+16+16+(24*i); //112*j is the node. 16 is the header, 16 is the parent+1st child. 24*i is the ith key.
                if(keyLoc > ((112*(btm.nm.returnNodes()))+16)){
                    error("select", Long.toString(key));
                    ok = true;
                    break;
                }
                dBt.seek(keyLoc); 
                long keyItself = dBt.readLong();
                dBt.seek(keyLoc+8); //get offset
                long offset = dBt.readLong(); //check the offset (written in bt)
                if(key==keyItself && offset != -1){
                    dVal.seek(256*offset+8); //8 = numrecord
                    dVal.writeByte(change.length());
                    dVal.writeBytes(change);
                    System.out.println(key+" updated.");
                    ok = true;
                    break;
                }
            }
            if (ok)
                break;
            
        }
        if(!ok)
            error("update", Long.toString(key));
    }
    public static void error(String word, String key){
        System.out.print("ERROR: ");
        switch (word) {
            case "select":
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
    public static long checkParent(long key,long numRecords){
        long parent = -1;
//        if(numRecords==0)
//            parent=-1;
//        else{ //look for the parent
//            parent=0;
//        }
        return parent;
    }
}
