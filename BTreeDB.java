import java.util.*;
import java.io.*;
public class BTreeDB {
    // to allow BTreeDB access to the fil
    public static RandomAccessFile dVal;
    
    public static void main(String[] args) throws IOException{
        HashMap<Integer,String > hash = new HashMap<>();
        ValueManager valueMan = new ValueManager(args[1]); //maybe something wrong why?it doesnt pass values to dVal? right. 
        long numRecords = 0;
        try{
            // creates and reads the data.bt and data.vl files
            File db = new File(args[0]);
            RandomAccessFile dBt = new RandomAccessFile(db, "rwd");

        }
        catch(IOException a){
        }
        //1.  check if file already exists
        
        //2.  if file exists then numRecords<-- readlong()
        
        // else numRecords <-- 0
        
        // 3. insert 1 A
        // file.seek(f(numRecords)); seek the parts
        // file. writeByte(value.length())   value is the string passed
        // file.writeByte(value)
        // ++ numRecords;
        
        
        Scanner in = new Scanner(System.in);
        // breaks out of while loop
        // and program ends
        OUT:
        while(true){
            // gets input and splits 
            String s = in.nextLine();
            // what happens if its not a word but a phrase or sentence?
            String[] input = s.split(" ");
            String word = null;
            // adds all the separated wordsS
            if(input.length>3){
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
                    if(input.length==3){
                        long key = Long.parseLong(input[1]);
                        // adds also the key?
                       valueMan.insert( dVal, s, numRecords, key);
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
