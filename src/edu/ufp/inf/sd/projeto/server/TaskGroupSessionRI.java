package edu.ufp.inf.sd.projeto.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * <p>Title: Projecto SD</p>
 * <p>Description: Projecto apoio aulas SD</p>
 * <p>Copyright: Copyright (c) 2009</p>
 * <p>Company: UFP </p>
 * @author Rui Moreira
 * @version 1.0
 */
public interface TaskGroupSessionRI extends Remote {
    public Task[] listAll() throws RemoteException;
    public Task[] searchHashType(String hashType) throws RemoteException;
    public boolean createTask(String taskName, String hashType, String user, String[] hash, int tam, String url) throws RemoteException;
    public boolean uploadCredits(String user, int credits) throws RemoteException;
    public void logout() throws RemoteException;
    public String[] hashToMatch(String taskName) throws RemoteException;
    public Task[] returnUserTasks(String userName) throws RemoteException;
    public int showCredits(String user) throws RemoteException;
    public String hashType(String taskName) throws RemoteException;
}

