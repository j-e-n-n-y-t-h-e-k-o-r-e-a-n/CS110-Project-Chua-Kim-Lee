
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kim
 */
public class BTManager {
    public BTManager(){
        
    }
    public void insert(long parent,RandomAccessFile db,long key,long numRecords) throws IOException{
        numRecords++;
        long pbytes = 16;//8 bytes numrecords, 8 bytes root record num
        db.seek(pbytes);
        db.writeLong(parent); //write parent
        long bytes = 24*numRecords; //24 48 80
        db.seek(bytes);
        db.writeLong(-1);//write child id
        db.writeLong(key); //write key
        db.writeLong(numRecords-1);
        checkPreviousKeys(db,numRecords,key);
        db.seek(0);
        db.writeLong(numRecords);
        db.seek(8); //write the root num after
        db.writeLong(0); //root
    }
    public void checkPreviousKeys(RandomAccessFile db, long numRecords,long key)throws IOException{
        if(numRecords>1){ //fix
            for(long i=1;i<numRecords;i++){
                long j = i+1;
                long read = 24*i+8;
                long read2 = 24*j+8;
                db.seek(read);
                long compare = db.readLong(); //previous key
                System.out.println(key+" "+compare);
                if(key<compare){
                    db.seek(read);
                    db.writeLong(key);
                    db.seek(read2);
                    db.writeLong(compare);
                }
            }
        }
//        else{
//            db.seek(24*numRecords);
//            db.writeLong(-1);
//            db.writeLong(key);
//            db.writeLong(numRecords-1);
//        }
    }
    //check if you need to make a new node
    public boolean check(long numRecords, RandomAccessFile db) throws IOException{
        boolean b = false;
        if(numRecords==0)
            b = true;
        else{
            
        }
        return b;
    }
    //createa new node (doesnt include numRecords and root nuum)
    public void createNode(RandomAccessFile db) throws IOException{
        for(int i=1;i<=14;i++){
            db.writeLong(-1);
        }
    }
    //check whether to put as left child or right
//    public void checkSide(RandomAccessFile db,long key,long parent) throws IOException{
//        long first = -1;
//        long second;
//        long i=0;
//        do{
//            db.seek(8*i+16); //right key
//            second = db.readLong();
//            if(key>first && key<second){
//                db.seek(112+16); //122 = next record
//                db.writeLong(parent);
//                long j=1;
//                while (true){
//                    if(db.readLong()==-1){ //check for a slot
//                        db.writeLong(key);
//                        break;
//                    }
//                    j++;
//                    db.seek(112+16+8*j); //adjust to check the slot for the next key
//                }
//                break;
//            }
//            first = second; //left key
//            i++;
//        }while(true);
//    }
//    public void newNode(RandomAccessFile db){
//        
//    }
//    public void parent(){
//        
//    }
}
