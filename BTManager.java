
import java.io.IOException;
import java.io.RandomAccessFile;

public class BTManager {
    NodeManager nm;
//    long nNodes;
    /**
     * constructor of BTManager (create a new node if there is none existing)
     * @param db the RandomAccessFile of data.bt
     * @param numNodes the current number of nodes (to know if you have to make a new node)
     * @throws IOException 
     */
    public BTManager(RandomAccessFile db,long numNodes, long numRecords)throws IOException{
        nm = new NodeManager(numNodes, numRecords);
        //when a node does not exist
        //when the data.bt is just being created for the first time
        if(numNodes==0){
            nm.addNode(db, -1, -1,-1,-1,-1,-1,-1,-1);
           // nNodes = nm.returnNodes();
        }
        //in the case a data.bt already exists and nodes exist
        else
            nm.updateNodes(numNodes);
            //nNodes=numNodes;
    }
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
        if(nm.returnNodes()==1){ // conditional for only when there is only one node
            if(checkNotFull(nm.returnNodes()-1,db)){ //checks if true then root is not full
                // if not full 
                // we insert key and offset to current node
                long keyLocation = 8+24*(numRecords+1); 
                db.seek(keyLocation); 
                db.writeLong(key);
                db.writeLong(numRecords);

                // just in case writes the value in number of nodes and root node
                db.seek(0);
                db.writeLong(1);
                db.writeLong(0);
                db.writeLong(-1); //parent
              
            }else{ // if root is full then split
                nm.split("m",db,getAllKeys(nm.returnNodes()-1,key,db),nm.returnNodes()-1, getAllOffsets(numRecords,nm.returnNodes()-1,db), getAllChildren(nm.returnNodes()-1,-1, db));
            }
            //end of record
            sort(db,numRecords+1,nm.returnNodes()-1);
            
        }else{
            db.seek(8);
            insert2childOrnot(db.readLong()-1,db,key,numRecords);
        }
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
        long location = 112*node+16+8; 
        long location2 = location;

        if(numRecords>1){ // if no record no need to sort
            for(int i =3; i>0; i--){// outer loop 1 2 3
                for(int j = i; j>0; j-- ){ // 
                    db.seek(location);
                    long child1 = db.readLong();
                    long key1 = db.readLong();
                    long off1 = db.readLong();
                    
                    if(off1 ==-1 ){
                        break;
                    }
                    long child2 = db.readLong();
                    long key2 = db.readLong();
                    long off2 = db.readLong();
                    
                    if(key2<key1 && off2 != -1){
                       db.seek(location);
                       db.writeLong(child2);
                       db.writeLong(key2);
                       db.writeLong(off2);
                    //go to location of keyVal key and write the first key and offset instead
                       db.writeLong(child1);
                       db.writeLong(key1);
                       db.writeLong(off1);
                    }
                    location+=24;
                }
                location=location2;
               }
              }          
            }
    
    /**
     * determine which path the key should follow (which child to insert it to)
     * @param root the node to be checked (start from root then see which child node is right then go down)
     * @param db the RandomAccessFile of data b t
     * @param key the key to be inserted
     * @param numRecords the current number of records (used as offset)
     * @throws IOException 
     */
    public void insert2childOrnot(long root,RandomAccessFile db,long key,long numRecords) throws IOException{
        long keyVal;  long offsetVal; long offsetVal2; long leftChildVal; long rightChildVal; long lastChildVal; long offset = numRecords; 
        long locationOfParent = 112*(root)+16; //location of parent
        db.seek(locationOfParent);
        //records parentId
        long parentVal = db.readLong(); //parent of current node
        for(long i=1;i<=4;i++){
            // checks through all keys with offset
            // must check if offset is -1 or not with the key
            
            // records key value
            long locationOfKey = locationOfParent -8+24*(i); // (i) because we need to increase the value for every iteration and i starts with 1;
            db.seek(locationOfKey); //read ith key of the node
            keyVal = db.readLong();
            // records the offset value
            long locationOfOffset = locationOfParent +24*(i); 
            db.seek(locationOfOffset); 
            offsetVal = db.readLong();
            
            if(i != 4){
            db.seek(locationOfOffset+24);
            offsetVal2 =db.readLong();
            }else{
                offsetVal2 = -1;
            }
            
            // records left child value
            long locationOfLeftChild = locationOfParent +8+24*(i-1);
            db.seek(locationOfLeftChild);
            leftChildVal = db.readLong();
            
            // records the right child value
            long locationOfRightChild = locationOfLeftChild+24;
            db.seek(locationOfRightChild);
            rightChildVal = db.readLong();
            
            // records last child 
            // used in the case that key is greater than last key
            long locationOfLastChild = locationOfParent +112-8;
            db.seek(locationOfLastChild);
            lastChildVal = db.readLong();
            
            //key - to be inserted
            // keyVal - to be compared to
            // offset - check if not -1
            // when it reaches 4th iteration we can assume theres no space
            if(key<keyVal && offset != -1){ // if key is smaller than key val check leftchild
                if(leftChildVal == -1){ // checks if at bottomost
                    // check if full
                    if(checkNotFull(root, db)){ // if true then not full, there's space :)
                        db.writeLong(key);
                        db.writeLong(offset);
                        // calls sort
                        sort(db, numRecords, root);
                        break;
                     
                    }else{ // if full, split node :(
                        nm.split("m",db,getAllKeys(root,key,db),root, getAllOffsets(numRecords,root,db), getAllChildren(root, lastChildVal, db));
                        break;
                    }
                }else{ // if left child val is not -1 go to the child node
                    root = leftChildVal;
                    // will repeat the search hell
                    insert2childOrnot(root, db, key, numRecords );
                }
                break;
            }else if(i==4 && offsetVal != -1 && rightChildVal == -1){ // check if current node is full and no child node after
                // then add current key to current node and split
                  nm.split("m",db,getAllKeys(root,key,db),root, getAllOffsets(numRecords,root, db),getAllChildren(root,lastChildVal,db));
                  break;
            }else if(offsetVal != -1 && offsetVal2 == -1 && key>keyVal){ // for checking the right child if current node its checking is not full
                // if right child is not -1, there is a right child
                if(rightChildVal != -1){ // enter rightchild and search right child through recursion
                    root = rightChildVal;
                    insert2childOrnot(root, db, key, numRecords );
                }else{// if none then insert key to current node
                        locationOfKey+=24;
                        db.seek(locationOfKey);
                        db.writeLong(key);
                        // adds key's offset to the node
                        locationOfOffset+=24;
                        db.seek(locationOfOffset);
                        db.writeLong(offset);
                        // calls sort
                        sort(db, numRecords, root);
                        break; 
                }
                break;// two breaks cause theres a iteration inside a recursion
            }else{
                // blank just to move on to the next iteration
            }
        }
        ;
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
    public long[] getAllKeys(long id,long key, RandomAccessFile db)throws IOException{
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
    
    public long[] getAllChildren (long id, long child, RandomAccessFile db)throws IOException{
        long[] arr = new long[5];
        long recid = 112*id + 16 + 8; //16=header, 8 = skip parent (reads the child)
                for(int i = 0; i < 4; i++){
                    db.seek(recid);
                    arr[i] = db.readLong();
                    recid += 24;
                }
                arr[4] = child;
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
