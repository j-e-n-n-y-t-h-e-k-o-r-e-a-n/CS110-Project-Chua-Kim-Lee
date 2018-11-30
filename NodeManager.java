
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class NodeManager {
    BTManager bt;
    long nNodes;
    long numRecords;
    public NodeManager(long numNodes, long nRecords){
        
        nNodes = numNodes;
        numRecords = nRecords;
    }
    //only works for beginning(when new nodes r made down
    //all new nodes have at least 2 keys
    //num 1 = less
    //num2 = greater
    
    /**
     * change the current node into the 1st child
     * @param location the location of the node to be changed (formula alr done at this point)(formula: 8+recNum of node * 112)?
     * @param db the RandomAccessFile of data.bt
     * @param parent the record number of its new "parent"
     * @param num1 its first key
     * @param num2 its second key
     * @param off1 offset of 1st key
     * @param off2 offset of 2nd key
     * @throws IOException 
     */
    /*
        position of the 1st child is [location of parent node-(2*112)]
        position of the 2nd child is [location of parent node - (1*112)]
    
        because the order is 
        child node #1
        child node #2
        parent node
    
        but when it spilts..
        child node #1
        child node #2
        parent node
                child node #3
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
                    System.out.println("writing -1 to db");
                    db.writeLong(-1);
                    break;
            }
        }
    }
    
    /**
     * create a new node that automatically contains the parent and its first 2 keys and their offsets
     * this is for the 2nd child
     * @param db the RandomAccessFile for data.bt
     * @param parent the node id that is to be placed as the "parent" of the new node
     * @param num1 the first key
     * @param num2 the second key
     * @param off1 the offset of the first key
     * @param off2 the offset of the second key
     * @param start 0 if called at the very first case, 1 if called in other cases
     * @throws IOException 
     */
    // 
    public void addNode(RandomAccessFile db,long parent,long num1,long num2,long off1,long off2)throws IOException{
        /*
            I'm thinking that we dont need to seek since the pointer from changeNode should be
            exactly in the front of the 2nd child node after changeNode executes.
            but this add node method is also used in the start.
        */
            // gets the location at end of file
            long lastRecord = (nNodes*112)+ 16;
            db.seek(lastRecord);
            
        //go to the end of the latest record (EOF)
        db.writeLong(parent); //write the parent
        for(int i=0;i<11;i++){
            switch (i) {
                case 1: //write 1st child and offset in proper place
                    // db.seek(16)
                    db.writeLong(num1); //write the first num in slot 1
                    db.writeLong(off1); //write its parent
                    //*/
                    break;
                case 3: //write 2nd child and offset in proper place
                    ///*
                    db.writeLong(num2); //write 2nd num in slot 2
                    db.writeLong(off2);
                    //*/
                    break;
                default:
                    db.writeLong(-1);
                    break;
            }
        }
        nNodes++;
        db.seek(0);
        db.writeLong(nNodes); //update the numnodes at heade
        if(checkIfRoot(nNodes-1,db)){ //moves the pointer
            db.seek(8);
            db.writeLong(nNodes-1);
        }
    }
    
    //make a new parent id
    public void insert2Parent(RandomAccessFile db,long parent,long num1,long off1,long child1,long child2)throws IOException{
        long lastRecord = 112*nNodes+16;
        db.seek(lastRecord); //go to the end of the latest record (EOF)
        db.writeLong(-1); //write the parent
        
        for(int i=0;i<10;i++){
            switch (i) {
                case 0:
                    db.writeLong(child1);//write 1st child and offset in proper place
                    System.out.println("CHILD "+child1);System.out.println("CHILD "+child2);
                    db.writeLong(num1); //write the first num in slot 1
                    db.writeLong(off1); //write its parent
                    db.writeLong(child2);
                    break;
                default:
                    db.writeLong(-1);
                    break;
            }
        }
        nNodes++;
        db.seek(0);
        db.writeLong(nNodes); //update the numnodes at heade
        System.out.println(nNodes);
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
        long temp = arr[first];
        arr[first] = arr[second];
        arr[second] = temp;
    }
    
    /**
     * split the current node into 2 more nodes + the parent
     * @param db the RandomAccessFile of data b t
     * @param arr an array of keys
     * @param topid the id of the current node that is to be split
     * @param offsets an array of offsets
     * @throws IOException 
     */
    public void split(RandomAccessFile db,long[] arr,long topid,long[] offsets)throws IOException{
//        long location = 112*topid+16+8; //skip parent
        /*
            before we do something, we should check first if the node to be split is the root node
        */
        long locationOfParent = 112*topid+16; //check parent of current node being checked
        System.out.println(" splitted, top id is  "+ topid);
        db.seek(locationOfParent);
        long parent = db.readLong(); //Record number of the node of the parent
        System.out.println("parent is: "+parent);
        //sorting part
        for(int i=4;i>0;i--){ //read til 4th lang
            for(int j=i-1;j>0;j--){
                if(arr[i]<arr[j]){
                    swap(arr,i,j);
                    swap(offsets,i,j);
                }
            }
        }
        
        //isolates the middle 
        long mid = arr[2];
        long midoff = offsets[2];
        long parentId = findParentId(parent,db,mid,midoff); // returns parent id
       // System.out.println("parent location: "+parentId);
        
        // rewrites the current node into left child
        changeNode(locationOfParent,db,parentId,arr[0],arr[1],offsets[0],offsets[1]);
        // adds the right node and rewrites the the node
        addNode(db,parentId,arr[3],arr[4],offsets[3],offsets[4]); //id = numnodes-1 (right node)
        // we rewrtite parent
        placeParent(parentId,mid,midoff,db,topid,nNodes-1);
        
        // check if parent node has been created if not
//        if(parentId>=nNodes){
//             insert2Parent(db,-1,mid,midoff,topid,nNodes-1);  // creates parent node
//             // parentId, key, offset, db, child1, child 2
//         }else{
//             checkNotFull(parentId,db);
//             db.writeLong(-1);
//             db.writeLong(midoff);
//         }
        
        // steps
        // add key to parent
        // if parent is 
    
         
        // happens after split we place the middlemost value to parent id
        //placeParent(parentId,mid,midoff,db,topid,nNodes-1);

        //theres another setRootNode, will try to fix it so it wont overlap
       

    }
    // key - middlemost value
    public void placeParent(long parentId, long key,long offset,RandomAccessFile db,long child1,long child2)throws IOException{
        // happens after split we place the middlemost value to parent id
        
        // go to parent id and check if full
        // if not full insert value 
        // else
        // if full split 
        
        // nm.split(db,getAllKeys(key,nm.returnNodes()-1,db),nm.returnNodes()-1, getAllOffsets(numRecords,nm.returnNodes()-1,db));
        
        System.out.println("parentId in placeParent" + parentId);
        // gets location of parent
        long parentLocation = (112*(parentId)) + 16;
        db.seek(parentLocation);
        if(nNodes==2){
            insert2Parent(db,parentId,key,offset,child1,child2); // makes a new node and insert child key and others
        }
//        System

        
        //loops while a parent exists
        //while(db.readLong()!= -1){
            //updates the current parent id per iteration
            //if its not full, insert the key and all of its information onto its parent node
        else{
            if(checkNotFull(parentId, db)){
                System.out.println("TRUE");
                db.writeLong(key);
                db.writeLong(offset);
                db.writeLong(child2);
                sort(db,numRecords,parentId);
            }else{
               // this conditional says that the parent node is full, so it should make a new parent node.
               //this happens at the very first split and the succeeding split when the parent node is already full
                insert2Parent(db,parentId,key,offset,child1,child2);
            }
        }
            //moves on to the next parent id
            /*
                i have to search the whole thing to find the recordId of a node which have parentId - 1
            */
//            db.seek(parentLocation);
//            parentId = db.readLong();
//            parentLocation = 112*parentId + 16;
//            
//            db.seek(parentLocation); 
//            System.out.println("current location of parent" + db.readLong());
        }
        
            
//        // places parent in proper location
//        if(id>=nNodes){
//            insert2Parent(db,-1,key,offset,child1,child2);
//        }
//        else{
//            checkNotFull(id,db);
//            db.writeLong(key);
//            db.writeLong(offset);
//        }
        
    
//    }
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
            insert2Parent(db,-1,key,-1,offset,-1);
            pid = nNodes;
            setRootNode(pid,db);
            //randomaccessfile,parent id,numnodes,key1,key2,offset1,offset2
        }
        else{
            //if it has a parent alr, and has space, write it there (shld prolly add a sort here)
            //prolly just call the sort function in btmanager after every split
            if(checkNotFull(id,db)){ //check the record based on id
                db.writeLong(key);
                db.writeLong(offset);
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
     * @param parentid
     * @param db
     * @param key
     * @param offset
     * @return
     * @throws IOException 
     */
    public long findParentId(long parentid,RandomAccessFile db,long key,long offset)throws IOException{
        long pid=0; //id of where itll be placed
        if(parentid==-1){
            pid = nNodes+1;
        }
        else{
            //if it has a parent alr, and has space, write it there (shld prolly add a sort here)
            //prolly just call the sort function in btmanager after every split
            if(checkNotFull(parentid,db)){ //check the record based on id
                pid = parentid;
            }
            else{ //if the one to be pushed to it is the 5th num
                long[] arr = new long[5]; //arr of keys
                long[] offsets = new long[5]; //arr of offsets
                long recid = 112*parentid+16;
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
     * @param nodeId the record id of the node to be checked
     * @param db the RandomAccessFile of data.bt
     * @return whether it's full or not
     * @throws IOException 
     */
    public boolean checkNotFull(long nodeId,RandomAccessFile db) throws IOException{
        long recid = 112*nodeId+16+24; //finding the record first(skip the parent also)
        for(int i=0;i<4;i++){
            db.seek(recid);
            long offset = db.readLong();
            if(offset==-1){
                db.seek(recid-8); //go to the the key thts empty
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
     * return the current number of nodes so BTManager can access it
     * @return the current number of nodes
     */
    public long returnNodes(){
        return nNodes;
    }
    /**
     * Modifies the current number of nodes in the case that a different class would
     * modify the number of nodes
     * @param n what the value of nNodes should be
     * @return 
     */
    
    // updates the number of nodes
    public long updateNodes(long n){
        nNodes=n;
        return nNodes;
    }
    
    // checks if the new node is the root
    public boolean checkIfRoot(long newNodeid,RandomAccessFile db)throws IOException{
        long locationOfParent = 112*newNodeid+16;
        db.seek(locationOfParent);
        return(db.readLong()==-1);
    }
    /**
     * sort an entire node
     * @param db the RandomAccessFile of data.bt
     * @param numRecords the current number of records
     * @param node the record id of the node to be sorted
     * @throws IOException 
     */
    public void sort(RandomAccessFile db, long numRecords,long node)throws IOException{
    // lets sort! :(
        long location = 112*node+8+24; 
        long location2 = location;

            for(int i =3; i>0; i--){// outer loop 1 2 3
                for(int j = i; j>0; j-- ){ // 
                    db.seek(location);
                    long key1 = db.readLong();
                    long off1 = db.readLong();
                    long child1 = db.readLong();
                    
                    if(off1 ==-1 ){
                        break;
                    }
                    long key2 = db.readLong();
                    long off2 = db.readLong();
                    long child2 = db.readLong();
                    System.out.println(key1+" "+key2);
                    if(key2<key1 && off2 != -1){
                       db.seek(location);
                       db.writeLong(key2);
                       db.writeLong(off2);
                       db.writeLong(child2);
                    //go to location of keyVal key and write the first key and offset instead
                       db.writeLong(key1);
                       db.writeLong(off1);
                       db.writeLong(child1);
                    
                    location+=24;
                }
                location=location2;
            }
            
        }          
    }
    public long findKey(long key, long id, RandomAccessFile db)throws IOException{
        long location = 0;
        long current = 112*id+8+24;
        for(long i=0;i<4;i++){
            db.seek(current);
            long keyhold = db.readLong();
            if(keyhold==key && db.readLong()!=-1)
                return i;
            current+=24;
        }
        return location;
    }
}
