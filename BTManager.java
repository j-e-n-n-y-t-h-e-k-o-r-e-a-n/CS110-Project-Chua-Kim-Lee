
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
//    long nNodes;
    /**
     * constructor of BTManager (create a new node if there is none existing)
     * @param db the RandomAccessFile of data.bt
     * @param numNodes the current number of nodes (to know if you have to make a new node)
     * @throws IOException 
     */
    public BTManager(RandomAccessFile db,long numNodes)throws IOException{
        nm = new NodeManager(numNodes);
        //when a node does not exist
        //when the data.bt is just being created for the first time
        if(numNodes==0){
            System.out.println(numNodes);
            nm.addNode(db, -1, -1,-1,-1,-1,0);
           // nNodes = nm.returnNodes();
        }
        //in the case a data.bt already exists and nodes exist
        else
            nm.updateNodes(numNodes);
            //nNodes=numNodes;
    }
    
    //NOTE: OFFSET IS JUST NUMRECORDS, NODE ID IS JUST NUMNODES-1
    /*
    header 1 8
    header 2 16
    1.	ID of Parent Node 8 location 24
    2.	ID of 1st Child Node 16 32
    3.	1st Key 24 40
    4.	Offset of 1st Value 32 48
    5.	ID of 2nd Child Node 40 56
    6.	2nd Key 48 64
    7.	Offset of 2nd Value 56 72
    8.	ID of 3rd Child Node 64 80
    9.	3rd Key 72 88
    10.	Offset of 3rd Value 80 96
    11.	ID of 4th Child Node 88 104
    12.	4th Key 96 112
    13.	Offset of 4th Value 104 120
    14.	ID of 5th Child Node 112 128
    */
//    //check from top which way to go(left right) then check child.....
    
    /**
     * inserts the key wherever it should be inserted
     * @param parent the parent id of the node where it is to be inserted (starts from root id going down)
     * @param db the RandomAccessFile of data.bt
     * @param key the key to be inserted
     * @param numRecords the current number of records (used as the offset of the key)
     * @throws IOException 
     */
    public void insert(long parent,RandomAccessFile db,long key,long numRecords) throws IOException{
        //beginning
        if(nm.returnNodes()==1){ //if only first node is there
            System.out.println("Number of nodes: " + nm.returnNodes());
            if(checkNotFull(nm.returnNodes()-1,db)){ //FIRST ONE SAVES
                System.out.println("this node is not full.");
                long pbytes = 16;//8 bytes numrecords, 8 bytes root record num (modify into formula for more nodes)
                db.seek(pbytes);
                db.writeLong(parent); //write parent
                long bytes = 24*numRecords+24; //24 48 72
                db.seek(bytes);
                db.writeLong(-1);//write child id
                db.writeLong(key); //write key
                db.writeLong(numRecords); //write offset(just numRecords)
            }
            else{
                //split jumbles the values
                nm.split(db,getAllNums(key,nm.returnNodes()-1,db),nm.returnNodes()-1, getAllOffsets(numRecords,nm.returnNodes()-1,db));
            }
            //end of record
            
            sort(db,numRecords+1,nm.returnNodes()-1);
            
        }
        else{
            db.seek(8);
            insertLocation(db.readLong(),db,key,numRecords);
        }
    }
    
    //NOTES: 24*i = child node
    //24*i+8 = key
    //24*i+16 = offset
    
    /**
     * sort an entire node
     * @param db the RandomAccessFile of data.bt
     * @param numRecords the current number of records
     * @param node the record id of the node to be sorted
     * @throws IOException 
     */
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
                location+=24;
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
    
    /**
     * determine which path the key should follow (which child to insert it to)
     * @param root the node to be checked (start from root then see which child node is right then go down)
     * @param db the RandomAccessFile of data.bt
     * @param key the key to be inserted
     * @param numRecords the current number of records (used as offset)
     * @throws IOException 
     */
    public void insertLocation(long root,RandomAccessFile db,long key,long numRecords) throws IOException{
        long first = -1; //just to compare the key to a "left" value (NOTE: ALL KEYS ARE AT LEAST 0)
        long second;
//        do{
        long location = 112*root+16; //location of record
        System.out.println("Rooty "+ root);
        db.seek(location);
        long par = db.readLong(); //parent of current node
        for(long i=0;i<4;i++){
            location+=24; //8*3
            db.seek(location); //read ith key of the node
            second = db.readLong();
            //first = left
            //second = right
            if(key>first && key<second){ 
                location-=8;
                db.seek(location); //check the child node's value
                long childl = db.readLong(); //find where the child node is
                if(checkIfLast(root,db)){ //find out if you're at bottommost node alr (MODIFY MAYBE)
                    //HOW TO FIND OUT IF ITS THE BOTTOMMOST OMG
                    //if bottommost is not full, insert
                    if(checkNotFull(root,db)){
                        db.writeLong(key);
                        db.writeLong(numRecords);
                        //sort here
                    }
                    else{ //if full then split it
                        //db,numnodes,arr of keys,parent, arr of offsets
                        nm.split(db,getAllNums(key,root,db),par,getAllOffsets(numRecords,root,db));
                        //nNodes = nm.returnNodes();
                    }
                }
                else{
        //check first if full before going to child
                    insertLocation(childl,db,key,numRecords); //go to where the child is and check all keys
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
        db.writeLong(nm.returnNodes());
    }
    
    /**
     * collect all the offsets of the current node (including the fifth one) (for pushing purposes)
     * @param offset the offset of the fifth one (doesn't get inserted but needs to be stored)
     * @param id the record id of the node
     * @param db the RandomAccessFile of data.bt
     * @return the array of offsets
     * @throws IOException 
     */
    public long[] getAllOffsets(long offset, long id, RandomAccessFile db) throws IOException{
        long[] offsets = new long[5];
        long recid = 112*id+16+24; //16 = header, 16 = skip parent and child and key (read offsets)
        for(int i=0;i<4;i++){
            db.seek(recid);
            long off = db.readLong();
//            System.out.println(off);
            offsets[i] = off;
            recid+=24;
        }
        offsets[4] = offset;
        return offsets;
    }
    // p c k o c k o c k o c k o c
    //return true  if the node is the bottommost alr
    public boolean checkIfLast(long id, RandomAccessFile db) throws IOException{
        long recid = 112*id+16+8; //16 = header, 16 = skip parent and child and key (read offsets)
        for(int i=0;i<5;i++){
            db.seek(recid);
            long child = db.readLong();
            if(child!=-1)
                return false;
            recid+=24;
        }
        return true;
    }
    
    /**
     * get all keys of the current node (including the fifth one that doesn't get inserted)
     * @param key the fifth key
     * @param id the record id of the node
     * @param db the RandomAccessFile of data.bt
     * @return the array of keys
     * @throws IOException 
     */
    public long[] getAllNums(long key,long id,RandomAccessFile db)throws IOException{
        long[] arr = new long[5];
        long recid = 112*id+16+16; //16 = header, 16 = skip parent and child (read the key)
        for(int i=0;i<4;i++){
            db.seek(recid);
            arr[i] = db.readLong();
            recid+=24;
        }
        arr[4] = key;
        return arr;
    }
    
    //return true if node not full
    /**
     * check if a node still has space (not yet 4 keys)
     * @param id the record id of the node being checked
     * @param db the RandomAccessFile of data.bt
     * @return true if there is space, and false if it's full
     * @throws IOException 
     */
    public boolean checkNotFull(long id,RandomAccessFile db) throws IOException{
        long recid = 112*id+16+24; //finding the record first(skip the parent also)
        for(int i=0;i<4;i++){
            db.seek(recid);
            long offset = db.readLong();
            if(offset==-1){
                db.seek(recid-8); //go to the key that has an empty space 
                return true;
            }
            recid+=24; //location of offset
        }
        return false;
    }
}
