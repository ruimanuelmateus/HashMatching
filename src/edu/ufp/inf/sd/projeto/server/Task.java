package edu.ufp.inf.sd.projeto.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author rmoreira
 */
public class Task implements Serializable {

    private String taskName = "";
    private String hashType = "";
    private String user="";
    private String[] hash=null;
    private String[] matches=null;
    private int sizeHash;
    private boolean pause;
    private boolean done;
    public HashMap<String, String> workers=new HashMap<>();

    public int getSizeHash() {
        return sizeHash;
    }

    public Task(String tN, String hT, String user, String[] h) {
        taskName = tN;
        hashType = hT;
        this.user=user;
        hash=h;
        matches=new String[h.length];
        sizeHash=h.length;
        pause =true;
        done=false;
    }

    public String[] getMatches() {
        return matches;
    }

    public void setMatches(String[] matches) {
        this.matches = matches;
    }

    public String[] returnHash(){
        String[] h=new String[sizeHash];
        int j=0;
        for (int i=0; i<hash.length; i++){
            if(matches[i]==null){
                h[j]=hash[i];
                j++;
            }
        }
        return h;
    }

    public boolean addMatch(String hashReceived, String match){
        for(int i=0; i<=hash.length; i++){
            if(hashReceived.equals(hash[i])){
                matches[i]=match;
                sizeHash--;
                System.out.println("hash adicionado");
                if(sizeHash==0){
                    done=true;
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public String[] getHash() {
        return hash;
    }

    public void setHash(String[] hash) {
        this.hash = hash;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Task{" + "taskName=" + getTaskName() + ", hashType=" + getHashType() + '}';
    }


    public boolean isDone() {
        return done;
    }

    /**
     * @return the author
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * @param taskName the author to set
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    /**
     * @return the title
     */
    public String getHashType() {
        return hashType;
    }

    /**
     * @param hashType the title to set
     */
    public void setHashType(String hashType) {
        this.hashType = hashType;
    }
    
    
}