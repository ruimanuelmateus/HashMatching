package edu.ufp.inf.sd.projeto.server;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TaskGroupFactoryRI extends Remote {
    public TaskGroupSessionRI login(String uname, String pw) throws RemoteException;
    public boolean register(String uname, String pw) throws RemoteException;
    public void logout(String name) throws RemoteException;
}

