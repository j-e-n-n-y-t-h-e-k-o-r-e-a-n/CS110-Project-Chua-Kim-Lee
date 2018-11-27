
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
public class NodeManager {
    public NodeManager(){
        
    }
    //only works for beginning(when new nodes r made down
    public void addNode(RandomAccessFile db,long parent,long numNodes)throws IOException{
        long lastRecord = 112*numNodes+16;
        db.seek(lastRecord);
        db.writeLong(parent);
        for(int i=0;i<13;i++){
            db.writeLong(-1);
        }
    }
}
