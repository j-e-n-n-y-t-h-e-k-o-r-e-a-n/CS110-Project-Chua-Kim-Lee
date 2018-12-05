
import java.io.*;

public class ValueManager {
/**
 * This method writes the values to be inserted as bytes
 * First, it writes spaces for the length of the string
 * Next, it writes the string itself converted to bytes.
 * @param dv
 * @param word the word to be inserted
 * @param numRecords
 * @throws IOException 
 */
    public void insert(RandomAccessFile dv,String word, long numRecords) throws IOException{
        dv.seek(fNumRecords(numRecords));
        
        //then it writes the length of the string
        dv.writeByte(word.length()); //writes length of the string
        //this writes the word converted to bytes
        dv.writeBytes(word); //writes the string itself in byte conversion
        //updates the number of records to be written at the beginning of the bytes
        dv.seek(0);
        dv.writeLong(numRecords+1);
       // close
       
    }
    /**
     * Returns how many bytes at the nth numRecord
     * @param numRecords
     * @return 
     */
    public long fNumRecords(long numRecords){
        // every record is 256 bytes
        long bytes = 8+(256*numRecords);
        return bytes;
    }
}
