
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
    public void insert(long numNodes,long parent,RandomAccessFile db,long key,long numRecords) throws IOException{
        checkFull(numNodes, numRecords,db);
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
        db.writeLong(numNodes);
        db.seek(8); //write the root num after
        db.writeLong(0); //root
    }
    //NOTES: 24*i+24= child node
    //24*i+8+32= key
    //24*i+40= offset
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
    public boolean check(long current,long numRecords, RandomAccessFile db) throws IOException{
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
    public void checkSide(RandomAccessFile db,long key,long parent) throws IOException{
        long first = -1; //just to compare the key to a "left" value (NOTE: ALL KEYS ARE AT LEAST 0)
        long second;
        long i=0;
        do{
            db.seek(8*i+16); //right key
            second = db.readLong();
            if(key>first && key<second){ //WRITES THE RECORD ONLY (DOESNT ADJUST FIRST YET)
                db.seek(112+16); //112 = next record (MODIFY)(only leads you to second record)
                db.writeLong(parent);
                long j=1;
                while (true){
                    if(db.readLong()==-1){ //check for a slot
                        db.writeLong(key);
                        break;
                    }
                    j++;
                    db.seek(112+16+8*j); //adjust to check the slot for the next key
                }
                break;
            }
            first = second; //left key
            i++;
        }while(true);
    }
    //check the parent to know whether left or right
//    public void parent(){
//        
//    }
    public void initialize(RandomAccessFile db,long numNodes) throws IOException{
      // creates the first record Values inside are -1 for now
      long seekwhere = numNodes*112+16; //where to put the new record/node
      db.seek(seekwhere);
      int i =0;
      while(i <14){
          db.writeLong(-1);
          i++;
      }
      numNodes++;
    }
    public void  checkFull (long numNodes,long numRecords,RandomAccessFile db)throws IOException{
        if(numRecords == 0){ //beginning
            System.out.println("initialized at 0");
            initialize(db,numNodes);
            numNodes++;

        }
        else if(numRecords==5){ //first full
            System.out.println("initialized after 1st node is full");
            initialize(db,numNodes);
            initialize(db,numNodes);
            numNodes+=2;
        }
        else{
            for(int i=0;i<numRecords;i++){ //check if full
                long poffset=(24*i+40)+numRecords; //location of offset
                db.seek(poffset);
                if(db.readLong()==-1) //if there is empty space
                    break;
                else if(i==3 && db.readLong()!=-1){ //if full
                    System.out.println("Initialized when a node gets full");
                    initialize(db,numNodes);
                }
            }
        }
        
        
        
    }
}
