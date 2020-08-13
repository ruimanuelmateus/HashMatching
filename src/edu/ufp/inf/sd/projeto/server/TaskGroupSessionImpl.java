package edu.ufp.inf.sd.projeto.server;

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.projeto.util.RabbitUtils;
import edu.ufp.inf.sd.projeto.util.threading.ThreadPool;

import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static edu.ufp.inf.sd.projeto.client.TaskGroupClient.getSHA;
import static edu.ufp.inf.sd.projeto.client.TaskGroupClient.toHexString;

public class TaskGroupSessionImpl extends UnicastRemoteObject implements TaskGroupSessionRI {

    ThreadPool threadPool=new ThreadPool(6);

    private DBMockup db;


    //private String topic="DEFAULT";


    public TaskGroupSessionImpl(DBMockup db) throws RemoteException {
        super();
        this.db=db;
    }
    @Override
    public Task[] listAll() throws RemoteException {
        return db.listAllTasks();
    }

    @Override
    public Task[] searchHashType(String hashType) throws RemoteException {
        return db.searchHash(hashType);
    }

    @Override
    public Task[] returnUserTasks(String userName) throws RemoteException {
        return db.returnUserTask(userName);
    }

    @Override
    public int showCredits(String user) throws RemoteException {
        return db.returnUserCredits(user);
    }

    @Override
    public String hashType(String taskName) throws RemoteException {
        return db.hashType(taskName);
    }

    @Override
    public boolean createTask(String TASK_QUEUE_NAME, String hashType,  String user, String[] hash, int tam, String url) throws RemoteException {
        if(db.insert(TASK_QUEUE_NAME, hashType, user, hash)){
            if(url.equals("default")){
                threadPool.execute(new CreateQueue(TASK_QUEUE_NAME, tam, url));
                return true;
            }else {
                try (BufferedInputStream inputStream = new BufferedInputStream(new URL(
                        url
                ).openStream());
                     FileOutputStream fileOS = new FileOutputStream("C:\\Users\\ruima\\OneDrive\\SD\\test\\Books\\"+TASK_QUEUE_NAME+".txt")){
                    byte data[]=new byte[1024];
                    int byteContent;
                    while ((byteContent=inputStream.read(data,0,1024))!=-1){
                        fileOS.write(data, 0, byteContent);
                    }
                }catch (IOException e){
                    System.out.println("createTask Impl error");
                }
                threadPool.execute(new CreateQueue(TASK_QUEUE_NAME, tam, TASK_QUEUE_NAME));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean uploadCredits(String user, int credits) throws RemoteException {
        return db.addCredits(user, credits);
    }


    @Override
    public void logout() throws RemoteException {
    }

    @Override
    public String[] hashToMatch(String taskName) throws RemoteException {
        Task task=db.serchTaskName(taskName);
        if(task==null)
            return null;
        return task.returnHash();
    }

}

class CreateQueue implements Runnable{
    String TASK_QUEUE_NAME;
    int tamanho;
    String fileTxt;


    public CreateQueue(String queueName, int t, String f) {
        TASK_QUEUE_NAME=queueName;
        tamanho=t;
        fileTxt=f;
    }

    public void run() {
        try {

            Connection connection = RabbitUtils.newConnection2Server("localhost","guest", "guest");
            Channel channel= RabbitUtils.createChannel2Server(connection);

            ArrayList<String> words=new ArrayList<>();
            File file = new File("C:\\Users\\ruima\\OneDrive\\SD\\test\\Books\\"+ fileTxt +".txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            if(tamanho==0)
                tamanho=30;
            while ((st = br.readLine()) != null)
                if(st.length()<=tamanho)
                    words.add(st);

            boolean durable=true;
            channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
            String message="";
            int i=0;
            for(String s: words){
                message=message+s+";";
                i++;
                if(i==20){
                    channel.basicPublish("", TASK_QUEUE_NAME,
                            MessageProperties.PERSISTENT_TEXT_PLAIN,
                            message.getBytes());
                    i=0;
                    message="";
                }
            }
            channel.basicPublish("", TASK_QUEUE_NAME,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.getBytes());

            //System.out.println(" [x] Sent '" + message + "'");

            //ListenTopicAdmin tp=new ListenTopicAdmin(connection, TASK_QUEUE_NAME, words.size());
            //this.threadPool.execute(tp);
        }catch (Exception e){
            System.out.println("TaskGroupSessionImpl createTask error");
        }
    }

}
