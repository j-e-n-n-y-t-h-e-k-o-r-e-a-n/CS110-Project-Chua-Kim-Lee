
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kim
 */
public class NodeManager {
    long nNodes;
    public NodeManager(long numNodes){
        nNodes = numNodes;
    }
    //only works for beginning(when new nodes r made down
    //all new nodes have at least 2 keys
    //num 1 = less
    //num2 = greater
    
    /**
     * instead of making 2 nodes when splitting, i decided to just make 1 (the right node)
     * and change the current node into the left node (cos idk how to delete a node)
     * @param location the location of the node to be changed (formula alr done at this point)
     * @param db the RandomAccessFile of data.bt
     * @param parent the id number of its new "parent"
     * @param num1 its first key
     * @param num2 its second key
     * @param off1 offset of 1st key
     * @param off2 offset of 2nd key
     * @throws IOException 
     */
    public void changeNode(long location,RandomAccessFile db, long parent, long num1, long num2, long off1, long off2)throws IOException{
        db.seek(location);
        db.writeLong(parent);
        for(int i=0;i<11;i++){
            switch (i) {
                case 1: //write 1st child and offset in proper place
                    db.writeLong(num1); //write the first num in slot 1
                    db.writeLong(off1); //write its parent
                    break;
                case 3: //write 2nd child and offset in proper place
                    db.writeLong(num2); //write 2nd num in slot 2
                    db.writeLong(off2);
                    break;
                default:
                    db.writeLong(-1);
                    break;
            }
        }
    }
    
    /**
     * create a new node that automatically contains the parent and its first 2 keys and their offsets
     * @param db the RandomAccessFile for data.bt
     * @param parent the node id that is to be placed as the "parent" of the new node
     * @param num1 the first key
     * @param num2 the second key
     * @param off1 the offset of the first key
     * @param off2 the offset of the second key
     * @throws IOException 
     */
    public void addNode(RandomAccessFile db,long parent,long num1,long num2,long off1,long off2)throws IOException{
        long lastRecord = 112*nNodes+16;
        db.seek(lastRecord); //go to the end of the latest record (EOF)
        db.writeLong(parent); //write the parent
        for(int i=0;i<11;i++){
            switch (i) {
                case 1: //write 1st child and offset in proper place
                    db.writeLong(num1); //write the first num in slot 1
                    db.writeLong(off1); //write its parent
                    break;
                case 3: //write 2nd child and offset in proper place
                    db.writeLong(num2); //write 2nd num in slot 2
                    db.writeLong(off2);
                    break;
                default:
                    db.writeLong(-1);
                    break;
            }
        }
        nNodes++;
        db.seek(0);
        db.writeLong(nNodes); //update the numnodes at heade
        if(checkRoot(nNodes-1,db))
            db.writeLong(nNodes-1);
    }
    
    public void newParent(RandomAccessFile db,long parent,long num1,long off1,long child1,long child2)throws IOException{
        long lastRecord = 112*nNodes+16;
        db.seek(lastRecord); //go to the end of the latest record (EOF)
        db.writeLong(parent); //write the parent
        for(int i=0;i<9;i++){
            switch (i) {
                case 0:
                    db.writeLong(child1);//write 1st child and offset in proper place
                    db.writeLong(num1); //write the first num in slot 1
                    db.writeLong(off1); //write its parent
                    db.writeLong(child2);
                default:
                    db.writeLong(-1);
                    break;
            }
        }
        nNodes++;
        db.seek(0);
        db.writeLong(nNodes); //update the numnodes at heade
        System.out.println(nNodes);
        if(checkRoot(nNodes-1,db))
            db.writeLong(nNodes-1);
    }
    //turn the split shit into -1
    //offset = offset of currently inserted value (in case it is the mid)
    //NOTE CHECK IF A PARENT ALR EXISTS
    
    /**
     * swap 2 elements in an array
     * @param arr the array containing the elements
     * @param first first element to be swapped
     * @param second second element to be swapped
     */
    public void swap(long[] arr, int first, int second){
        long hold = arr[first];
        arr[first] = arr[second];
        arr[second] = hold;
    }
    
    /**
     * split the current node into 2 more nodes + the parent
     * @param db the RandomAccessFile of data.bt
     * @param arr an array of keys
     * @param topid the id of the current node that is to be split
     * @param offsets an array of offsets
     * @throws IOException 
     */
    public void split(RandomAccessFile db,long[] arr,long topid,long[] offsets)throws IOException{
//        long location = 112*topid+16+8; //skip parent
        for(long n:offsets){
            System.out.println(n);
        }
        long location = 112*topid+16; //check parent of current node being checked
        System.out.println(" bitches "+ topid);
        db.seek(location);
        long p = db.readLong(); //parent of current node
        for(int i=4;i>0;i--){ //read til 4th lang
            for(int j=i-1;j>0;j--){
                if(arr[i]<arr[j]){
//                    System.out.println(offsets[i]+" "+offsets[i+1]);
                    swap(arr,i,j);
                    swap(offsets,i,j);
//                    System.out.println(offsets[i]+" "+offsets[i+1]);
                }
            }
        }
//        long[] arr2 = arr.clone(); //contains all keys (sorted cept for fifth
//        //offsets is array of all offsets (ex: offsets[1] is the offset of arr2[1])
//        long[] offsets2 = new long[5];
//        Arrays.sort(arr); //arr is now sorted (including 5th)
//        for(int i=0;i<5;i++){
//            long hold = arr[i]; //check the sorted array
//            for(int j=0;j<5;j++){
//                if(arr2[j]==arr[i]){// 5 4 1 => 1 4 5
//                    offsets2[i] = offsets[j]; //sort the offsets to pair up wif the ordered keys
//                }
//            }
//        }
        //WHAT HAPPENS AFTER THE FOR LOOP??? HERE"S THE ANSWER
        //each element in arr is sorted alr rite?
        //so now theres this other array for offsets called offsets2
        //it pairs up each key wif its offset
        //ex: the offset of arr[1] is now in offsets2[1]
        //WHY DO THIS????
        //well, after sorting arr, all its keys are now messed up
        //that's where the clone comes in
        //before even sorting arr, we make a clone of it THAT IS STILL PAIRED WIF ITS OFFSETS
        //so we can use that (^^^^) pairing to be able to fix the keys' order
        //WITH REGARDS to the sorted array
        //NOTE TO SELF: make 1 new array (top) (handleparent) and another (right)
        //just use current for left
        long mid = arr[2];
//        System.out.println();
//        System.out.println(mid);
//        System.out.println();
        long midoff = offsets[2];
//        db.seek(location);
        long id = findParentID(p,db,mid,midoff);
        System.out.println();
        for(long n:arr){
            System.out.println(n);
        }
        //addNode(db,mid,numNodes,arr[0],arr[1],offsets2[0],offsets2[1]); //id = numnodes-2 (left node)
        changeNode(location,db,id,arr[0],arr[1],offsets[0],offsets[1]);
        addNode(db,id,arr[3],arr[4],offsets[3],offsets[4]); //id = numnodes-1 (right node)
        placeParent(id,mid,midoff,db,topid,nNodes-1);
//        db.writeLong(numNodes-2);
//        db.writeLong(mid);
//        db.writeLong(midoff);//offset of mid
//        db.writeLong(numNodes-1);
    }
    
    public void placeParent(long id, long key,long offset,RandomAccessFile db,long child1,long child2)throws IOException{
//        System.out.println(id);
        if(id>=nNodes){
            newParent(db,-1,key,offset,child1,child2);
            setRootNode(id,db);
        }
        else{
            checkNotFull(id,db);
            db.writeLong(key);
            db.writeLong(offset);
        }
    }
    
    //id = id of parent
    
    /**
     * remember how the parent gets pushed up? this is the method that determines where the parent gets
     * pushed to or (if needed) makes a new node for the parent
     * @param id the parent id of the node that is currently being split (to see if a new node is needed)
     * @param db the RandomAccessFile of data.bt
     * @param key the key that is to be pushed up
     * @param offset the offset of the key
     * @return the id of where it gets pushed to (so its "children" know)
     * @throws IOException 
     */
    public long handleParent(long id,RandomAccessFile db,long key,long offset)throws IOException{
        long pid=0; //id of where itll be placed
        if(id==-1){
            //if it has no parent to be pushed to, make a new node and set it as root
            newParent(db,-1,key,-1,offset,-1);
            pid = nNodes;
            setRootNode(pid,db);
            //randomaccessfile,parent id,numnodes,key1,key2,offset1,offset2
        }
        else{
            //if it has a parent alr, and has space, write it there (shld prolly add a sort here)
            //prolly just call the sort function in btmanager after every split
            if(checkNotFull(id,db)){ //check the record based on id
                db.writeLong(nNodes-2);
                db.writeLong(key);
                db.writeLong(offset);
                db.writeLong(nNodes-1);
                pid = id;
            }
            else{ //if the one to be pushed to it is the 5th num
                long[] arr = new long[5]; //arr of keys
                long[] offsets = new long[5]; //arr of offsets
                long recid = 112*id+16;
                db.seek(recid);
                long par = db.readLong();//parent of record
                for(int i=0;i<4;i++){
                    recid+=8; //location of keys
                    db.seek(recid);
                    arr[i] = db.readLong(); //place key
                    offsets[i] = db.readLong(); //place respective offset
                }
                split(db,arr,par,offsets);
                //^^^ WHAT IF IT SPLITS AND THE NUMBER RN IS THE NEW PARENT
            }
        }
        return pid;
    }
    /**
     * find the new id of the "parent" (middle element before splitting)
     * @param id
     * @param db
     * @param key
     * @param offset
     * @return
     * @throws IOException 
     */
    public long findParentID(long id,RandomAccessFile db,long key,long offset)throws IOException{
        long pid=0; //id of where itll be placed
//        System.out.println(id);
        if(id==-1){
            pid = nNodes+1;
        }
        else{
            //if it has a parent alr, and has space, write it there (shld prolly add a sort here)
            //prolly just call the sort function in btmanager after every split
            if(checkNotFull(id,db)){ //check the record based on id
                pid = id;
            }
            else{ //if the one to be pushed to it is the 5th num
                long[] arr = new long[5]; //arr of keys
                long[] offsets = new long[5]; //arr of offsets
                long recid = 112*id+16;
                db.seek(recid);
                long par = db.readLong();//parent of record
                for(int i=0;i<4;i++){
                    recid+=8; //location of keys
                    db.seek(recid);
                    arr[i] = db.readLong(); //place key
                    offsets[i] = db.readLong(); //place respective offset
                }
                split(db,arr,par,offsets);
                //^^^ WHAT IF IT SPLITS AND THE NUMBER RN IS THE NEW PARENT
            }
        }
        return pid;
    }
    
    /**
     * check if a node is full or not (if it has 4 keys alr
     * @param id the record id of the node to be checked
     * @param db the RandomAccessFile of data.bt
     * @return whether it's full or not
     * @throws IOException 
     */
    public boolean checkNotFull(long id,RandomAccessFile db) throws IOException{
        long recid = 112*id+16+24; //finding the record first(skip the parent also)
        for(int i=0;i<4;i++){
            db.seek(recid);
            long offset = db.readLong();
            if(offset==-1){
                db.seek(recid-16); //go to the child node before the key thts empty
                return true;
            }
            recid+=24; //location of offset
        }
        return false;
    }
    
    /**
     * check the parent of a node
     * @param db the RandomAccessFile of data.bt
     * @param id the record id of the node to be checked
     * @return the parent id of the node
     * @throws IOException 
     */
    public long checkParentId(RandomAccessFile db,long id)throws IOException{
        long location = 112*id+16;
        db.seek(location);
        return db.readLong();
    }
    
    /**
     * set the root node id in the header
     * @param id id of the root node
     * @param db RandomAccessFile of data.bt
     * @throws IOException 
     */
    public void setRootNode(long id,RandomAccessFile db)throws IOException{
        db.seek(8);
        db.writeLong(id);
    }
    
    /**
     * return the current number of nodes so BTManager can access it (cos idk how to connect em)
     * @return the current number of nodes
     */
    public long returnNodes(){
        return nNodes;
    }
    public boolean checkRoot(long id,RandomAccessFile db)throws IOException{
        long location = 112*id+8;
        db.seek(id);
        return(db.readLong()==-1);
    }
}
