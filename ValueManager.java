
import java.io.*;

public class ValueManager {

    public void insert(RandomAccessFile dv,String word, long numRecords) throws IOException{
        System.out.println("writing...");
        dv.seek(fNumRecords(numRecords));
        dv.writeByte(word.length()); //writes length of the string
        dv.writeBytes(word); //writes the string itself in byte conversion
       numRecords++;
       
       // close
       dv.close();
    }
    public long fNumRecords(long numRecords){
        // every record is 256 bytes
        long bytes = 8+256*numRecords;
        return bytes;
    }
}
