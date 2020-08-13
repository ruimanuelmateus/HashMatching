package edu.ufp.inf.sd.projeto.client;

import java.util.ArrayList;
import java.util.Arrays;

public class Notification {
    public boolean pause;
    public boolean kill;
    public String hashType;
    public ArrayList<String> hashes=new ArrayList<>();

    public Notification(boolean pause, boolean kill) {
        this.pause = pause;
        this.kill=kill;
    }

    public String getHashType() {
        return hashType;
    }

    public void setHashType(String hashType) {
        this.hashType = hashType;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public void setKill(boolean kill) {
        this.kill = kill;
    }

    public void removeHash(String hash){
        hashes.remove(hash);
        System.out.println(hashes.size());
    }

    public void setArray(String[] strings){
        this.hashes.addAll(Arrays.asList(strings));
    }

}
