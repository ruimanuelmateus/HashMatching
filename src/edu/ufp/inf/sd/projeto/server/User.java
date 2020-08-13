package edu.ufp.inf.sd.projeto.server;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author rmoreira
 */
public class User {

    private String uname;
    private String pword;
    private int creditos;
    private final Set<String> tasks = new HashSet<>();

    public Set<String> getTasks() {
        return tasks;
    }

    public User(String uname, String pword, int c) {
        this.uname = uname;
        this.pword = pword;
        creditos=c;
    }

    public int getCreditos() {
        return creditos;
    }

    public void setCreditos(int creditos) {
        this.creditos = creditos;
    }

    @Override
    public String toString() {
        return "User{" + "uname=" + uname + ", pword=" + pword + '}';
    }

    /**
     * @return the uname
     */
    public String getUname() {
        return uname;
    }

    /**
     * @param uname the uname to set
     */
    public void setUname(String uname) {
        this.uname = uname;
    }

    /**
     * @return the pword
     */
    public String getPword() {
        return pword;
    }

    /**
     * @param pword the pword to set
     */
    public void setPword(String pword) {
        this.pword = pword;
    }

    public void addCredits(int x){this.creditos+=x;}

    public void removeCredits(int x){this.creditos-=x;}
}
