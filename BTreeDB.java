
import java.util.*;
import java.io.*;
public class BTreeDB {
    public static void main(String[] args){
        HashMap<Integer,String > node = new HashMap<>();
        try{
        RandomAccessFile dBt = new RandomAccessFile(new File(args[0]), "rwd");
        RandomAccessFile dVal = new RandomAccessFile(new File(args[1]), "rwd");
        }
        catch(FileNotFoundException a){
            
        }
        
        Scanner in = new Scanner(System.in);
        OUT:
        while(true){
            String s = in.nextLine();
            String[] input = s.split(" ");
            switch(input[0]){
                case "insert":
                    insert();
                    break;
                case "select":
                    select();
                    break;
                case "update":
                    update();
                    break;
                case "exit":
                    break OUT;
            }
        }
        
        
    }
    public static void insert(){
        
    }
    public static void select(){
        
    }
    public static void update(){
        
    }
    
    
}
