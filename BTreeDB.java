import java.util.*;
import java.io.*;
public class BTreeDB {
    public static void main(String[] args){
        HashMap<Integer,String > hash = new HashMap<>();
        try{
            // creates and reads the data.bt and data.vl files
        RandomAccessFile dBt = new RandomAccessFile(new File(args[0]), "rwd");
        RandomAccessFile dVal = new RandomAccessFile(new File(args[1]), "rwd");
        }
        catch(FileNotFoundException a){
        }
        
        Scanner in = new Scanner(System.in);
        // breaks out of while loop
        // and program ends
        OUT:
        while(true){
            // gets input and splits 
            String s = in.nextLine();
            String[] input = s.split(" ");
            // checks the first word
            // if properly inputted
            switch(input[0]){
                case "insert":
                    // 
                    String word = "";
                    for(int i=2;i<input.length;i++){
                        word+=input[i];
                        if(i!=input.length-1)
                            word+=" ";
                    }
                    insert(Integer.parseInt(input[1]),word, hash);
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
                    
            }
        }
        
        
    }
    public static void insert(int key, String word, HashMap<Integer,String> hash){
        
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
