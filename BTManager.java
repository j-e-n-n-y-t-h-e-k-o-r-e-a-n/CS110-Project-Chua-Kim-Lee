
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
        nm.addNode(db, -1, 0,-1,-1,-1,-1);
    }
    /*
    1.	ID of Parent Node 8 location
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
    public void insert(long parent,RandomAccessFile db,long key,long numRecords,long numNodes) throws IOException{
        //check if full
        if(checkNotFull(parent,db) && numRecords+1>5){
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
            
        }
        else{
            insertLocation(parent,db,key,numNodes,numRecords);
        }
        db.seek(0);
        db.writeLong(numNodes+1);
        db.writeLong(0); //root
    }
    //NOTES: 24*i = child node
    //24*i+8 = key
    //24*i+16 = offset
    public void sort(RandomAccessFile db, long numRecords,long node)throws IOException{
        //node = node id to be sorted
        long location = 112*node+16+8; //8 = parent(skip it)
        if(numRecords>1){ //fix
            for(long i=0;i<3;i++){
                db.seek(location);
                //first key and offset
                long child1 = db.readLong();
                long first = db.readLong();
                long off1 = db.readLong();
                //second key and offset
                long child2 = db.readLong();
                long second = db.readLong();
                long off2 = db.readLong();
                //if i ==3, only exchange the
                if(second<first && off2!=-1){ //if the right key is smaller than left (ex: 5 3) and second isn't empty
                    //go back to location of first key and write the second key and offset instead
                    db.seek(location-16);
                    db.writeLong(second);
                    db.writeLong(off2);
                    //go to location of second key and write the first key and offset instead
                    db.seek(location+16);
                    db.writeLong(first);
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
    //check whether to put as left child or right
    //recursive
    //root = parent as of the moment (top to bottom so yea)
    public void insertLocation(long root,RandomAccessFile db,long key,long numNodes,long numRecords) throws IOException{
        long first = -1; //just to compare the key to a "left" value (NOTE: ALL KEYS ARE AT LEAST 0)
        long second;
//        do{
        long location = 112*root+16; //location of record
        long par = db.readLong(); //parent of current node
        for(long i=0;i<4;i++){
            location+=24; //8*3
            db.seek(location); //read ith key of the node
            second = db.readLong();
            if(key>first && key<second){ 
                location-=8;
                db.seek(location); //check the child node's value
                long childl = db.readLong(); //find where the child node is
                if(childl==-1 && root<numNodes){ //find out if you're at bottommost node alr (MODIFY MAYBE)
                    //HOW TO FIND OUT IF ITS THE BOTTOMMOST OMG
                    //if bottommost is not full, insert
                    if(checkNotFull(root,db)){
                        db.writeLong(key);
                        db.writeLong(numRecords-1);
                        //sort here
                    }
                    else{ //if full then split it
                        //db,numnodes,arr of keys,parent, arr of offsets
                        nm.split(db, numNodes, getAllNums(key,root,db),par,getAllOffsets(numRecords-1,root,db));
                    }
                }
                else if(childl==-1){
                    //if there is no child node but the key should go there, make node
                }
                else{
        //check first if full before going to child
                    insertLocation(childl,db,key,numNodes,numRecords); //go to where the child is and check all keys
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
        db.seek(0);
        db.writeLong(0); //num of root id supposedly
        db.writeLong(numNodes);
    }
    public long[] getAllOffsets(long offset, long id, RandomAccessFile db) throws IOException{
        long[] offsets = new long[5];
        long recid = 112*id+16+24; //16 = header, 16 = skip parent and child and key (read offsets)
        for(int i=0;i<4;i++){
            offsets[i] = db.readLong();
            recid+=16;
        }
        offsets[4] = offset;
        return offsets;
    }
    public long[] getAllNums(long key,long id,RandomAccessFile db)throws IOException{
        long[] arr = new long[5];
        long recid = 112*id+16+16; //16 = header, 16 = skip parent and child (read the key)
        for(int i=0;i<4;i++){
            arr[i] = db.readLong();
            recid+=16;
        }
        arr[4] = key;
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
