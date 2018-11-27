
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
    /*
    1.	ID of Parent Node 8
    2.	ID of 1st Child Node 16
    3.	1st Key 24
    4.	Offset of 1st Value 32
    5.	ID of 2nd Child Node 40
    6.	2nd Key 48
    7.	Offset of 2nd Value 56
    8.	ID of 3rd Child Node 64
    9.	3rd Key 72
    10.	Offset of 3rd Value 80
    11.	ID of 4th Child Node 88
    12.	4th Key 96
    13.	Offset of 4th Value 104
    14.	ID of 5th Child Node 112
    */
//    //check from top which way to go(left right) then check child.....
//    public void insert(long parent,RandomAccessFile db,long key,long numRecords) throws IOException{
//        //check if full
//        long pbytes = 16;//8 bytes numrecords, 8 bytes root record num (modify into formula for more nodes)
//        db.seek(pbytes);
//        db.writeLong(parent); //write parent
//        long bytes = 24*numRecords+24; //24 48 72
//        db.seek(bytes);
//        db.writeLong(-1);//write child id
//        db.writeLong(key); //write key
//        db.writeLong(numRecords); //write offset(just numRecords)
//        //end of record
//        sort(db,numRecords+1,key);
//        db.seek(0);
//        db.writeLong(numRecords+1);
//        db.seek(8); //write the root num after
//        db.writeLong(0); //root
//    }
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
    //check whether to put as left child or right
    //recursive
    public void insertLocation(long root,RandomAccessFile db,long key,long numNodes,long numRecords) throws IOException{
        long first = -1; //just to compare the key to a "left" value (NOTE: ALL KEYS ARE AT LEAST 0)
        long second;
//        do{
        long location = 112*root+16; //location of record
        for(long i=0;i<4;i++){
            location+=24; //8*3
            db.seek(location); //read ith key of the node
            second = db.readLong();
            if(key>first && key<second){ //WRITES THE RECORD ONLY (DOESNT ADJUST FIRST YET)
                location-=8;
                db.seek(location); //go to the "child node"
                long childl = db.readLong(); //find where the child node is
                if(childl==-1 && root<numNodes){ //find out if you're at bottommost node alr
                    //if bottommost is not full, insert
                    if(checkNotFull(root,db)){
                        db.writeLong(key);
                        db.writeLong(numRecords-1);
                        //sort here
                    }
                    else{ //if full then split it
                        nm.split(db, numNodes, getAllNums(key,root,db));
                    }
                }
                else{
        //check first if full before going to child
                    insertLocation(childl,db,key,numNodes); //go to where the child is and check all keys
                    //and find a spot
                }
//                //write at the end
//                db.seek(24*i+8);
//                db.seek(112*numNodes+16); //112 = next record (MODIFY)(only leads you to second record)
//                db.writeLong(parent);
//                long j=1;
//                while (true){
//                for(long j=0;j<4;j++){
//                    if(db.readLong()==-1){ //check for a slot
//                        db.writeLong(key);
//                        break;
//                    }
//                    j++;
//                    db.seek(112+16+8*j); //adjust to check the slot for the next key
//                }
//                }
//                break;
            first = second; //left key
            }
        }
    }
    public long[] getAllNums(long key,long id,RandomAccessFile db)throws IOException{
        long[] arr = new long[5];
        long recid = 112*id+16+16; //16 = header, 16 = skip parent and child (read the key)
        for(int i=0;i<4;i++){
            arr[i] = db.readLong();
            recid+=16;
        }
        return arr;
    }
    //return true if node not full
    public boolean checkNotFull(long id,RandomAccessFile db) throws IOException{
        long recid = 112*id+16+8; //finding the record first(skip the parent also)
        for(int i=0;i<4;i++){
            recid+=16; //location of offset
            db.seek(recid);
            db.readLong();
            if(recid==-1){
                db.seek(recid-8); //go to the key that has an empty space 
                return true;
            }
        }
        return false;
    }
}
