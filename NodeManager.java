
import java.io.IOException;
import java.io.RandomAccessFile;

public class NodeManager {
    BTManager bt;
    long nNodes;
    long numRecords;
    int num;
    public NodeManager(long numNodes, long nRecords){
        num=0;
        nNodes = numNodes;
        numRecords = nRecords;
    }
    
    
    /**
     * change the current node into the 1st child whenever a split happens.
     * @param location the location of the node to be changed
     * @param db the RandomAccessFile of data.bt
     * @param parent the record number of its new "parent"
     * @param num1 its first key
     * @param num2 its second key
     * @param off1 offset of 1st key
     * @param off2 offset of 2nd key
     * @throws IOException 
     */
    public void changeNode(long location,RandomAccessFile db, long parent, long num1, long num2, long off1, long off2, long child1, long child2, long child3)throws IOException{
        db.seek(location);
        db.writeLong(parent);
        for(int i=0;i<7;i++){
            switch (i) {
                case 0: //write 1st child, key and offset in proper place
                    db.writeLong(child1);
                    db.writeLong(num1); //write the first num in slot 1
                    db.writeLong(off1); //write its parent
                    db.writeLong(child2);
                    db.writeLong(num2); //write 2nd num in slot 2
                    db.writeLong(off2);
                    db.writeLong(child3);
                    break;
                default:
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
    public void addNode(RandomAccessFile db,long parent,long num1,long num2,long off1,long off2, long child1, long child2, long child3)throws IOException{
            // gets the location at end of file
            long lastRecord = (nNodes*112)+ 16;
            db.seek(lastRecord);
            
        //go to the end of the latest record (EOF)
        db.writeLong(parent); //write the parent
        for(int i=0;i<7;i++){
            switch (i) {
                case 0: //write 1st child, key and offset in proper place
                    db.writeLong(child1);
                    db.writeLong(num1); //write the first num in slot 1
                    db.writeLong(off1); //write its parent
                    db.writeLong(child2);
                    db.writeLong(num2); //write 2nd num in slot 2
                    db.writeLong(off2);
                    db.writeLong(child3);
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
    
    /**
     * Creates a new node that contains the parent. This happens when a node is split. 
     * @param db
     * @param parent the record number of the parent
     * @param num1 key to be inserted to the parent
     * @param off1 record number of the key
     * @param child1 left child of the key
     * @param child2 right child of the key
     * @throws IOException 
     */
    public void insert2Parent(RandomAccessFile db,long parent,long num1,long off1,long child1,long child2)throws IOException{
        long lastRecord = 112*nNodes+16;
        db.seek(lastRecord); //go to the end of the latest record (EOF)
        db.writeLong(-1); //write the parent
        
        for(int i=0;i<10;i++){
            switch (i) {
                case 0:
                    db.writeLong(child1);//write 1st child and offset in proper place
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
        db.writeLong(nNodes-1);
        
    }
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
     * this method also checks if the node that the key will be inserted to will split or not.
     * @param db the RandomAccessFile of data b t
     * @param arr an array of keys
     * @param topid the id of the current node that is to be split
     * @param offsets an array of offsets
     * @throws IOException 
     */
    public void split(String s,RandomAccessFile db,long[] arr,long topid,long[] offsets, long[] children)throws IOException{
//        long location = 112*topid+16+8; //skip parent
        /*
            before we do something, we should check first if the node to be split is the root node
        */
        long locationOfParent = 112*topid+16; //check parent of current node being checked
        db.seek(locationOfParent);
        long parent = db.readLong(); //Record number of the node of the parent
        
        //sorting part
        for(int i=4;i>0;i--){ //read til 4th lang
            for(int j=i-1;j>0;j--){
                if(arr[i]<arr[j]){
                    swap(children, i, j);
                    swap(arr,i,j);
                    swap(offsets,i,j);
                    
                }
            }
        }
        //isolates the middle 
        long mid = arr[2];
        long midoff = offsets[2];
        long parentId = findParentId(parent,db,mid,midoff,nNodes-1); // returns parent id
        changeNode(locationOfParent,db,parentId,arr[0],arr[1],offsets[0],offsets[1], children[0], children[1],children[2]);
        if(s.equals("true")){
            addNode(db,parentId,arr[3],arr[4],offsets[3],offsets[4], children[3], children[4],children[5]);
        }
        else
            addNode(db,parentId,arr[3],arr[4],offsets[3],offsets[4], children[2], children[3],children[4]);
        //record id of this node should be the current number of nodes, because split always puts the right child at the bottom
        // we rewrtite parentif(checkNotFull(node, db)){
        placeParent(parentId,mid,midoff,db,topid,nNodes-1);
        adjustRightSide(db);
        
    }
    /**
     * This method is called in the split and determines if the key will be put to the parent or if it will make a new node for the parent.
     * @param parentId record number of parent
     * @param key key to be inserted
     * @param offset offset of key
     * @param db
     * @param child1 left child of key
     * @param child2 right child of key
     * @throws IOException 
     */
    public void placeParent(long parentId, long key,long offset,RandomAccessFile db,long child1,long child2)throws IOException{
    // happens after split we place the middlemost value to parent id
        db.seek(8);
        long root = db.readLong();
        if(nNodes ==2 || parentId == -1 || parentId >=nNodes){
            insert2Parent(db,parentId,key,offset,child1,child2); 
        }

        else{ //if not full, write to parent
             if(parentId<nNodes && num==0){
                if(checkNotFull(parentId,db)){
                    db.writeLong(key);
                    db.writeLong(offset);
                    db.writeLong(child2);
                    sort(db,numRecords,parentId);
                    db.seek(0);
                    db.writeLong(nNodes);
                }
            }
        }
        num=0;
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
    public long findParentId(long parentid,RandomAccessFile db,long key,long offset,long child2)throws IOException{
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
                long[] arr = getAllKeysOfParent(key,parentid,db);
                long[] children = getAllChildrenOfParent(child2,parentid,db);
                long[] offsets = getAllOffsetsOfParent(offset,parentid,db);
                children[5] = nNodes+2;
                for(int i=4;i>0;i--){ //read til 4th lang
                    for(int j=i-1;j>0;j--){
                        if(arr[i]<arr[j]){
                            swap(arr,i,j);
                            swap(children,i,j);
                            swap(offsets,i,j);
                        }
                    }
                }
                //check if current parent is to be new root
                //if not, its id is nNodes+2 (current nodes + 1 for right side + 1 for parent split)
                if(arr[2]!=key){
                    pid = parentid;
                    split("true",db,arr,parentid,offsets,children);
                    num++;
                    return pid;
                }
                //else keep looking up (FIX)
                else{
                    num++;
                    pid=parentid+1;
                }
            }
        }
        return pid;
    }
    
    /**
     * check if a node is full or not. It is full if it has 4 keys
     * @param nodeId the record id of the node to be checked
     * @param db the RandomAccessFile of data.bt
     * @return true if it still has space and returns false if the node has no space.
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
     * @return the record number of the parent of the node
     * @throws IOException 
     */
    public long checkParentId(RandomAccessFile db,long id)throws IOException{
        long location = 112*id+16;
        db.seek(location);
        return db.readLong();
    }
    
    public void adjustRightSide(RandomAccessFile db)throws IOException{
        db.seek(8);
        long root = db.readLong();
        long location = 112*root+16+32; //read right child
        db.seek(location);
        long right = db.readLong(); //id of right child
        long location2 = 112*right+16+8; //go to right child's first key
        long newParent = right;
        for(int i=0;i<4;i++){
            db.seek(location2);
            long child = db.readLong();
            if(child!=-1){
                db.seek(child*112+16);
                db.writeLong(newParent);
            }
            location2+=24;
        }
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
        
    /**
     * get all keys of the current node (including the fifth one that doesn't get inserted)
     * @param key the fifth key
     * @param id the record id of the node
     * @param db the RandomAccessFile of data.bt
     * @return the array of keys
     * @throws IOException 
     */
    public long[] getAllKeysOfParent(long key,long id,RandomAccessFile db)throws IOException{
        long[] arr = new long[5];
        long recid = 112*id+16+16; //16 = header, 16 =  skips parent and child 
        for(int i=0;i<4;i++){
            db.seek(recid);
            arr[i] = db.readLong();
            recid+=24;
        }
        
        arr[4] = key;
        for(int n = 0; n<4; n++){
                }
        return arr;
    }
    
    /**
     * puts all of the children of the node into the array (5 children in total)
     * @param child the 6th child to be inserted
     * @param id the node we are currently looking at
     * @param db
     * @return array of children
     * @throws IOException 
     */
    public long[] getAllChildrenOfParent (long child, long id, RandomAccessFile db)throws IOException{
        long[] arr = new long[6];
        long recid = 112*id + 16 + 8; //16=header, 8 = skip parent (reads the child)
                for(int i = 0; i < 4; i++){
                    db.seek(recid);
                    arr[i] = db.readLong();
                    recid += 24;
                }
                arr[4]=child;
        return arr;
    }
    /**
     * puts all of the offsets of a node inside an array (5 offsets in total)
     * @param offset the 5th offset to be inserted
     * @param id the node we are currently looking at
     * @param db
     * @return an array of offsets
     * @throws IOException 
     */
        public long[] getAllOffsetsOfParent(long offset, long id, RandomAccessFile db) throws IOException{
        long[] offsets = new long[5];
        long recid = 112*id+16+24; //16 = header, 16 = skip parent and child and key (read offsets)
        for(int i=0;i<4;i++){
            db.seek(recid);
            long off = db.readLong();
            offsets[i] = off;
            recid+=24;
        }
        offsets[4] = offset;
        for(int n = 0; n<4; n++){
                }
        return offsets;
    }
    
}
