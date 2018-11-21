
import java.io.*;

public class ValueManager {

    public void insert(RandomAccessFile dv,String word, long numRecords) throws IOException{
        System.out.println("writing...");
        try{
        dv.seek(fNumRecords(numRecords));
        }catch(IOException bitch){
            System.out.println("lifes a bitvh");
        }
        
        //then it writes the length of the string
        dv.writeByte(word.length()); //writes length of the string
        //this writes the word converted to bytes
        dv.writeBytes(word); //writes the string itself in byte conversion
        System.out.println(numRecords);
        //updates the number of records to be written at the beginning of the bytes
        dv.seek(0);
        dv.writeLong(numRecords+1);
       // close
       
    }
    public long fNumRecords(long numRecords){
        // every record is 256 bytes
        long bytes = 8+(256*numRecords);
        System.out.println(bytes);
        return bytes;
    }
}
