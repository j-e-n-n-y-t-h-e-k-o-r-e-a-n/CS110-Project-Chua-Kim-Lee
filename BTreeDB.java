import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
public class BTreeDB {
    
    
    /*
    NOTE: I UPDATED THE SELECT AND CREATED A METHOD TO CHECK IF THE KEY TO BE INSERTED ALREADY EXISTS
    I ALREADY PLACED THE SELECT METHOD WHERE IT SHOULD BE BUT I HAVENT PLACED THE ONE THAT DETECTS IF
    THE KEY EXISTS ALREADY
    
    I WILL JUST FIX IT TOM AHAHHAHA IMMA GO CRAM SOMETHING FIRST
    */
    
    
    // RandomAccessFile and File set as universal variables so that other methods
    //outside the try catch statement can reference to it.
    public static RandomAccessFile dVal,dBt;
    public static File dv,db;
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
        BTManager btm = new BTManager(dBt,numNodes);
        
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
                    // ADD AN ERROR HANDLING if input[1] isnt a num
                    //FIX THISSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
                    //ALSO IFKEY IS DOUBLED
                    if(input.length>=2){
                        //we might need the key but for now I havent used it in anything
                        if(isNum(input[1])){
                            long key = Long.parseLong(input[1]);
                            btm.insert(-1,dBt,key,numRecords);
                            // adds also the key?
                            valueMan.insert(dVal,word,numRecords);
                            System.out.println(key + " inserted.");
                            numRecords++;
                        }
                        else{
                            error("invalid",-1); //just random values to call the "invalid command" error
                        }
                    }else{ // if input is worng or added too much stuff
                        error("insert", Integer.parseInt(input[1]));
                    }
                   
                    break;
                case "select":
                    if(input.length>2)
                        error("select", Integer.parseInt(input[1]));
                    else if(!isNum(input[1])){
                        error("invalid",-1);
                    }
                    else{
                        dBt.seek(8);
                        long par = dBt.readLong(); //get the id num of the root
                        select("something","select",par,Long.parseLong(input[1]),dBt,dVal,numRecords);
                        //printing of select is done in printSelect()
                    }
                    break;
                case "update":
                    if(isNum(input[1])){
                        dBt.seek(8);
                        long par = dBt.readLong(); //get the root
                        select(word,"update",par,Long.parseLong(input[1]),dBt,dVal,numRecords);
                    }
                    else
                        error("invalid",-1);
                case "exit":
                    break OUT;
                default:
                    error("wrong",0);
            }
        }
        dVal.close();
        dBt.close();
        
    }
    
    //TEST SELECT (HYPOTHETICALLY shold work)
    //return the offset
    
    /**
     * start from the root node and check all keys.. if you find the key then do the command (either print it
     * or update it, depends on the command arg)
     * if not, continue downwards (take the proper path)
     * if you still can't find it and you've reached the end, print error
     * @param word word to change into (if update)
     * @param command whether it's update or select
     * @param root the record id of the node you're currently looking at
     * @param key the key to be found
     * @param dBt the RandomAccessFile of data.bt
     * @param dVal the RandomAccessFile of data.val
     * @param numRecords the current number of records
     * @return the offset that you found (honestly just doing this to stop recursion)
     * @throws IOException 
     */
    public static long select(String word,String command,long root,long key,RandomAccessFile dBt,RandomAccessFile dVal,long numRecords)throws IOException{
        long location = 112*root+8;
        for(int i=0;i<4;i++){
            location+=24;
            dBt.seek(location); //checks the ith key
            long ikey = dBt.readLong(); //left key
            dBt.seek(location+8);
            //if keys r same and it aint empty
            if(key==ikey && dBt.readLong()!=-1){
                dBt.seek(location+8); //get offset
                long offset = dBt.readLong(); //check the offset (written in bt)
                printSelect(command,key,offset,dVal,word);
                return offset;
            }
            //if comparing wif first key (check if less than left key or between left and right)
            if(i==0 && !checkIfLast(root,dBt)){
                dBt.seek(location+24); //check the next key (key after ith key
                long skey1 = dBt.readLong(); //right key
                //make sure the keys you're comparing it to are just negative (if ever) and NOT EMPTY
                if(key<ikey && !isEmpty(location,dBt)){ //if the key being searched is less than the first key in the node, access the child to its left
                    location-=8;
                    return select(word,command,location,root,dBt,dVal,numRecords); //go to the child node and start scanning there
                }
                else if(key>ikey && key <skey1 && !isEmpty(location,dBt) && !isEmpty(location+24,dBt)){
                    location+=16;
                    return select(word,command,location,root,dBt,dVal,numRecords); //go to the child node and start scanning there
                }
            }
            //compare wif left and right key
            else if(i<3 && i>0 && !checkIfLast(root,dBt)){
                dBt.seek(location+24); //check the next key (key after ith key
                long skey2 = dBt.readLong(); //right key
                if(key>ikey && key <skey2 && !isEmpty(location,dBt) && !isEmpty(location+24,dBt)){
                    location+=16;
                    return select(word,command,location,root,dBt,dVal,numRecords); //go to the child node and start scanning there
                }
            }
            //compare wif right key
            else if(i==3 && !checkIfLast(root,dBt)){
                //make sure the keys you're comparing it to are just negative (if ever) and NOT EMPTY
                if(key>ikey && !isEmpty(location,dBt)){ //if the key being searched is less than the first key in the node, access the child to its left
                    location-=8;
                    return select(word,command,location,root,dBt,dVal,numRecords); //go to the child node and start scanning there
                }
            }
            //if none of the above, print the error message (offset = -1) (doesn't exist)
            else{
                printSelect(command,key,-1,dVal,word);
            }
        }
        //print error if not found
        printSelect(command,key,-1,dVal,word);
        return -1;
    }
    public static void update(String change,long key,RandomAccessFile dBt,RandomAccessFile dVal,long numRecords) throws IOException{
        for(int i=1;i<=numRecords;i++){
            dBt.seek(8+24*i); //checks all keys in bt (CHANGE WHEN BT USES TREE STRUCTURE ALR)
            if(key==dBt.readLong()){
                dBt.seek(16+24*i); //get offset
                long offset = dBt.readLong(); //check the offset (written in bt)
                dVal.seek(256*offset+8); //8 = numrecord (seek the num
                dVal.writeByte(change.length()); //writes length of the string
                //this writes the word converted to bytes
                dVal.writeBytes(change); //writes the string itself in byte conversion
                System.out.println(key+" updated.");
            }
        }
    }
    public static void error(String word, long key){
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
    
    /**
     * check if the key you want to input already exists
     * @param root the record id that you're currently looking at
     * @param key the key you want to check
     * @param dBt the RandomAccessFile of data.bt
     * @param dVal the RandomAccessFile of data.val
     * @param numRecords the current number of records
     * @return true if the key already exists and false if not
     * @throws IOException 
     */
    public static boolean checkIfKeyExists(long root,long key,RandomAccessFile dBt,RandomAccessFile dVal,long numRecords)throws IOException{
        long location = 112*root+8;
        for(int i=0;i<4;i++){
            location+=24;
            dBt.seek(location); //checks the ith key
            long ikey = dBt.readLong(); //left key
            dBt.seek(location);
            //if the keys match and it aint empty
            if(key==ikey && dBt.readLong()!=-1){
                return true;
            }
            //as long as current node aint the bottommost, keep chhecking children
            if(i==0 && !checkIfLast(root,dBt)){
                dBt.seek(location+24); //check the next key (key after ith key
                long skey1 = dBt.readLong(); //right key
                //make sure the keys you're comparing it to are just negative (if ever) and NOT EMPTY
                if(key<ikey && !isEmpty(location,dBt)){ //if the key being searched is less than the first key in the node, access the child to its left
                    location-=8;
                    return checkIfKeyExists(location,root,dBt,dVal,numRecords); //go to the child node and start scanning there
                }
                else if(key>ikey && key <skey1 && !isEmpty(location,dBt) && !isEmpty(location+24,dBt)){
                    location+=16;
                    return checkIfKeyExists(location,root,dBt,dVal,numRecords); //go to the child node and start scanning there
                }
            }
            else if(i<3 && i>0 && !checkIfLast(root,dBt)){
                dBt.seek(location+24); //check the next key (key after ith key
                long skey2 = dBt.readLong(); //right key
                if(key>ikey && key <skey2 && !isEmpty(location,dBt) && !isEmpty(location+24,dBt)){
                    location+=16;
                    return checkIfKeyExists(location,root,dBt,dVal,numRecords); //go to the child node and start scanning there
                }
            }
            else if(i==3 && !checkIfLast(root,dBt)){
                //make sure the keys you're comparing it to are just negative (if ever) and NOT EMPTY
                if(key>ikey && !isEmpty(location,dBt)){ //if the key being searched is less than the first key in the node, access the child to its left
                    location-=8;
                    return checkIfKeyExists(location,root,dBt,dVal,numRecords); //go to the child node and start scanning there
                }
            }
            else
                return false;
        }
        return false;
    }
    
    /**
     * depending on whether the key is found in select(), it either prints the value or prints the error
     * @param command command (select/update) (merged both so it won't be too much)
     * @param key the key you were looking for
     * @param offset the offset of the key (-1 if it wasn't found)
     * @param dVal the RandomAccessFile of data.val
     * @param change the string you wanna change into (for update)
     * @throws IOException 
     */
    public static void printSelect(String command,long key, long offset, RandomAccessFile dVal,String change)throws IOException{
        if(offset!=-1){
            dVal.seek(256*offset+8); //8 = numrecord
            if(command.equals("select")){
                Byte b = dVal.readByte(); //reads the string length written in val
                int strlen = b.intValue(); //chhange it to int
                byte [] strb = new byte[strlen]; //string length*2 (2 bytes per letter?) PLS CONFIRM
                dVal.readFully(strb); //read strlen*2 bytes (puts it in array too?)
                String word = new String(strb,StandardCharsets.UTF_8); //changes it into a string
                System.out.println(key+" => "+word);
            }
            else{
                dVal.writeByte(change.length());
                dVal.writeBytes(change);
            }
        }
        else
            error("select",key);
    }
    
    //see if a key is really empty or if it's just negative
    
    /**
     * check if a key is empty (given its location)
     * @param keyloc location of the key itself (not the record's location)
     * @param db the RandomAccessFile of data.bt
     * @return true if the key is empty and false if it isn't
     * @throws IOException 
     */
    public static boolean isEmpty(long keyloc,RandomAccessFile db)throws IOException{
        long location = keyloc+8;
        db.seek(keyloc);
        if(db.readLong()==-1)
            return true;
        return false;
    }
    
    //return true  if the node is the bottommost alr
    
    /**
     * check if the current node is at the bottommost part (couldn't access the one in BTManager so yea)
     * @param id record id of the node you wanna check
     * @param db the RandomAccessFile of data.bt
     * @return
     * @throws IOException 
     */
    public static boolean checkIfLast(long id, RandomAccessFile db) throws IOException{
        long recid = 112*id+16+8; //16 = header, 16 = skip parent and child and key (read offsets)
        for(int i=0;i<5;i++){
            db.seek(recid);
            long child = db.readLong();
            if(child!=-1)
                return false;
            recid+=24;
        }
        return true;
    }
    
    /**
     * check if a string is "parse-able" into a long (got this online)
     * @param s the string to be checked
     * @return true if "parse-able"
     */
    public static boolean isNum(String s){
        try{
            long num = Long.parseLong(s);
        }catch(NumberFormatException | NullPointerException ex){
            return false;
        }
        return true;
    }
}
