package edu.ufp.inf.sd.projeto.server;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * <p>Title: Projecto SD</p>
 * <p>Description: Projecto apoio aulas SD</p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: UFP </p>
 * @author Rui S. Moreira
 * @version 3.0
 */
public class TaskGroupFactoryImpl extends UnicastRemoteObject implements TaskGroupFactoryRI {

    private DBMockup db;// = new DBMockup();
    //private ThreadPool pool;// = new ThreadPool(10);
    private HashMap<String, TaskGroupSessionRI> sessions;// = new HashMap();

    /**
     * Uses RMI-default sockets-based transport. Runs forever (do not
     * passivates) hence, does not need rmid (activation deamon) Constructor
     * must throw RemoteException due to super() export().
     */
    public TaskGroupFactoryImpl() throws RemoteException {
        // Invokes UnicastRemoteObject constructor which exports remote object
        super();
        db = new DBMockup();
        //pool = new ThreadPool(10);
        sessions = new HashMap();
    }

    @Override
    public boolean register(String uname, String pw) throws RemoteException {
        if(!db.exists(uname,pw)){
            db.register(uname,pw);
            return true;
        }
        return false;

    }

    @Override
    public void logout(String name) throws RemoteException {
        this.sessions.remove(name);
    }

    @Override
    public TaskGroupSessionRI login(String uname, String pw) throws RemoteException {
        if (db.exists(uname, pw)) {
            if (!sessions.containsKey(uname)) {
                TaskGroupSessionRI taskGroupSession = new TaskGroupSessionImpl(db);
                return taskGroupSession;
            } else {
                return this.sessions.get(uname);
            }
        }
        return null;
    }
}

