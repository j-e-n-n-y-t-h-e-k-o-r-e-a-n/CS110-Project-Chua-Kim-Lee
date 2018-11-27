
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
    public NodeManager(){
        
    }
    //only works for beginning(when new nodes r made down
    //all new nodes have at least 2 keys
    //num 1 = less
    //num2 = greater
    public void addNode(RandomAccessFile db,long parent,long numNodes,long num1,long num2)throws IOException{
        long lastRecord = 112*numNodes+16;
        db.seek(lastRecord);
        db.writeLong(parent);
        for(int i=0;i<13;i++){
            if(i==2)
                db.writeLong(num1); //write the first num in slot 1
            else if(i==5)
                db.writeLong(num2); //write 2nd num in slot 2
            else
                db.writeLong(-1);
        }
        numNodes++;
    }
    //turn the split shit into -1
    public void split(RandomAccessFile db,long numNodes,long[] arr)throws IOException{
        Arrays.sort(arr);
        long mid = arr[2];
        addNode(db,mid,numNodes,arr[0],arr[1]);
        addNode(db,mid,numNodes,arr[3],arr[4]);
        
    }
}
