
import java.io.*;

public class ValueManager {
    public ValueManager(String name){
        long numRecords = 0;
        try{
            // creates and reads the data.bt and data.vl files
            File dv = new File(name);
            RandomAccessFile dVal = new RandomAccessFile(dv, "rwd");
            if(dv.exists())
                numRecords = dVal.readLong(); //num of records/cases
        }
        catch(IOException a){
        }       
        // returns a long the index where the value was placed
        
    }
    public long insert(RandomAccessFile dv,String name,long numRecords) throws IOException{
        dv.seek(fNumRecords(numRecords));
        dv.writeByte(name.length()); //writes length of the string
        dv.writeBytes(name); //writes the string itself
       // insert (String value)
       return numRecords++; 
       // close
    }
    public long fNumRecords(long numRecords){
        // every record is 256 bytes
        long bytes = 256*numRecords;
        return bytes;
    }
}
