
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
    NodeManager nm;
    public BTManager(RandomAccessFile db)throws IOException{
        nm = new NodeManager();
        nm.addNode(db, -1, 0,-1,-1);
    }
    //check from top which way to go(left right) then check child.....
    public void insert(long parent,RandomAccessFile db,long key,long numRecords) throws IOException{
        //check if full
        if(numRecords+1%5==0){ //records is incremented after so check the +1 instead
            checkSide(db,key,numRecords); //check which side?
            nm.split(db, numRecords, arr); //array of nums in a specific record
            nm.addNode(db, parent, numRecords, key, key);
        }
        long pbytes = 16;//8 bytes numrecords, 8 bytes root record num (modify into formula for more nodes)
        db.seek(pbytes);
        db.writeLong(parent); //write parent
        long bytes = 24*numRecords+24; //24 48 72
        db.seek(bytes);
        db.writeLong(-1);//write child id
        db.writeLong(key); //write key
        db.writeLong(numRecords); //write offset(just numRecords)
        //end of record
        sort(db,numRecords+1,key);
        db.seek(0);
        db.writeLong(numRecords+1);
        db.seek(8); //write the root num after
        db.writeLong(0); //root
    }
    //NOTES: 24*i = child node
    //24*i+8 = key
    //24*i+16 = offset
    public void sort(RandomAccessFile db, long numRecords,long key)throws IOException{
        if(numRecords>1){ //fix
            for(long i=1;i<numRecords;i++){
                long j = i+1;
                long read = 24*i+8; //key
                long read2 = 24*j+8; //key
                long offset1 = read+8; //offset
                long offset2 = read2+8; //offset
                db.seek(read);
                long compare = db.readLong(); //previous key
                if(key<compare){
                    db.seek(offset1);
                    long off1 = db.readLong();
                    db.seek(offset2);
                    long off2 = db.readLong();
                    db.seek(read);
                    db.writeLong(key);
                    db.seek(read2);
                    db.writeLong(compare);
                    db.seek(offset1);
                    db.writeLong(off2);
                    db.seek(offset2);
                    db.writeLong(off1);
                }
            }
        }
    }
    //check if you need to make a new node
    //CONDITIONS(?)
    //1. there is no existing node
    //2. 1st high and low (higher than first input and lower than first input)
    //3. all nodes are full
    
    //NOTE TO SELF: IN 112*I+16, RECORD NUMS START WITH 0
    public boolean check(long current,long numRecords, RandomAccessFile db,long numNodes) throws IOException{
        boolean b = false;
        if(numRecords==0)
            b = true;
        else{
            
        }
        return b;
    }
    //createa new node (doesnt include numRecords and root nuum)
    public void createNode(RandomAccessFile db,long numNodes) throws IOException{
        db.seek(numNodes*112+16);
        for(int i=1;i<=14;i++){
            db.writeLong(-1);
        }
        numNodes++;
    }
    //check whether to put as left child or right
    public void checkSide(RandomAccessFile db,long key,long parent,long numNodes) throws IOException{
        long first = -1; //just to compare the key to a "left" value (NOTE: ALL KEYS ARE AT LEAST 0)
        long second;
//        do{
        for(long i=0;i<4;i++){
            db.seek(24*i+8); //right key
            second = db.readLong();
            if(key>first && key<second){ //WRITES THE RECORD ONLY (DOESNT ADJUST FIRST YET)
                //write at the end
                db.seek(24*i+8);
                db.seek(112*numNodes+16); //112 = next record (MODIFY)(only leads you to second record)
                db.writeLong(parent);
//                long j=1;
//                while (true){
                for(long j=0;j<4;j++){
                    if(db.readLong()==-1){ //check for a slot
                        db.writeLong(key);
                        break;
                    }
                    j++;
                    db.seek(112+16+8*j); //adjust to check the slot for the next key
                }
//                }
//                break;
            }
            first = second; //left key
        }
//            i++;
//        }while(true);
    }
//    public void checkTop(RandomAccessFile db,long key)throws IOException{
//        db.seek(8);
//        long root = db.readLong();
//        for(long i=0;i<3;i++){
//            long keyl = 24*i+8;
//            long j = i+1;
//            long key2l = 24*i
//            
//        }
//    }
}
