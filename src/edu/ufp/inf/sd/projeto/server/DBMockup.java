package edu.ufp.inf.sd.projeto.server;

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.projeto.util.RabbitUtils;
import edu.ufp.inf.sd.projeto.util.threading.ThreadPool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static edu.ufp.inf.sd.projeto.client.TaskGroupClient.getSHA;
import static edu.ufp.inf.sd.projeto.client.TaskGroupClient.toHexString;

/**
 * This class simulates a DBMockup for managing users and books.
 *
 * @author rmoreira
 *
 */
public class DBMockup {

    ThreadPool threadPool=new ThreadPool(4);

    private final Set<Task> tasks = new HashSet<>();
    private final Set<User> users=new HashSet<>();

    /**
     * This constructor creates and inits the database with some books and users.
     */
    public DBMockup() {
        //Add 3 books
        String[] s={"oi", "ola"};
        tasks.add(new Task("task1", "SHA-256",  "admin", s));
        tasks.add(new Task("task2", "SHA-256",  "null", s));
        tasks.add(new Task("task3", "SHA-256",  "admin", s));
        //Add one user
        users.add(new User("admin", "admin", 10000));
        users.add(new User("rui", "rui", 10000));

        this.threadPool.execute(new ListenTopicServer());
    }


    public void register(String u, String p) {
        if (!exists(u, p)) {
            users.add(new User(u, p, 0));
        }
    }

    public boolean exists(String u, String p) {
        for (User usr : this.users) {
            if (usr.getUname().compareTo(u) == 0 && usr.getPword().compareTo(p) == 0) {
                return true;
            }
        }
        return false;
        //return ((u.equalsIgnoreCase("guest") && p.equalsIgnoreCase("ufp")) ? true : false);
    }

    public boolean addMatch(String tN, String hashFound, String match ){
        threadPool.execute(new PauseTopic(tN, "worker.remove", hashFound));
        for (Task t:tasks){
            if(t.getTaskName().equals(tN)){
                return t.addMatch(hashFound, match);
            }
        }
        return false;
    }

    public int returnUserCredits(String userName){
        for(User user: users)
            if (user.getUname().equals(userName))
                return user.getCreditos();
            return 0;
    }

    public String hashType(String taskName){
        for(Task t: tasks)
            if (t.getTaskName().equals(taskName))
                return t.getHashType();
            return null;
    }

    public boolean insert(String tN, String hT, String u, String[] h) {
        Task aux=new Task(tN, hT, u, h);
        for(User user: users){
            if(user.getUname().equals(u) && user.getCreditos()>h.length*20+100){
                aux.setPause(false);
                user.removeCredits(h.length*20);
                tasks.add(aux);
                return true;
            }
        }
        //tasks.add(aux);
        return false;
    }

    public Task serchTaskName(String t){
        for (Task task: this.tasks) {
            System.out.println("DB - select(): taskName = " + task.getTaskName() + ", " + task.getHashType());
            if (task.getTaskName().equals(t) && !task.isDone() && !task.isPause()){
                return task;
            }
        }
        return null;
    }

    public Task[] searchHash(String h){
        ArrayList<Task> tasks=new ArrayList<>();
        for (Task task: this.tasks) {
            System.out.println("DB - select(): taskName = " + task.getTaskName() + ", " + task.getHashType());
            if (task.getHashType().toLowerCase().contains(h.toLowerCase()) && !task.isDone() && !task.isPause()){
                tasks.add(task);
            }
        }
        Task[] vTask= new Task[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            vTask[i] = (Task) tasks.get(i);
        }
        return vTask;
    }

    public Task[] returnUserTask(String userName){
        ArrayList<Task> tasks=new ArrayList<>();
        for (Task task: this.tasks) {
            System.out.println("DB - select(): taskName = " + task.getTaskName() + ", " + task.getUser());
            if (task.getUser().equals(userName)){
                tasks.add(task);
            }
        }
        Task[] vTask= new Task[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            vTask[i] = (Task) tasks.get(i);
        }
        return vTask;
    }

    public void pauseTask(String taskName){
        for (Task t: tasks){
            if (t.getTaskName().equals(taskName)){
                t.setPause(true);
                return;
            }
        }
    }

    public void resumeTask(String taskName){
        for (Task t: tasks){
            if (t.getTaskName().equals(taskName)){
                t.setPause(false);
                return;
            }
        }
    }

    public Task[] listAllTasks() {
        ArrayList<Task> tasks=new ArrayList<>();
        for (Task task: this.tasks) {
            if (!task.isDone() && !task.isPause()){
                tasks.add(task);
            }
        }
        Task[] vTask= new Task[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            vTask[i] = (Task) tasks.get(i);
        }
        return vTask;
    }

    public boolean addCredits(String userName, int creditos){
        for(User u: users){
            if(u.getUname().equals(userName)){
                u.setCreditos(u.getCreditos()+creditos);
            }
            for(Task t:tasks){
                if(t.getUser().equals(userName) && t.isPause() && creditos>=1000){
                    t.setPause(false);
                }
            }
            return true;
        }
        return false;
    }

    public void payWorker(String taskName, String userNAme){
        for(Task t:tasks){
            if(t.getTaskName().equals(taskName)){
                t.workers.remove(userNAme);
                for(User user: users)
                    if(user.getUname().equals(t.getUser())){
                        user.removeCredits(100);
                        if(user.getCreditos()< t.getSizeHash()*20+100 && !t.isPause()){
                            t.setPause(true);
                            threadPool.execute(new PauseTopic(taskName,"worker.pause", "pause"));
                        }
                        break;
                    }
                for (User u:users){
                    if(u.getUname().equals(userNAme)){
                        u.addCredits(100);
                        return;
                    }
                }
            }

        }

    }

    public void addWorker(String[] string){
        for (Task t:tasks){
            if(t.getTaskName().equals(string[0]))
                t.workers.put(string[1],string[2]);
        }
    }

    public ArrayList<String> checkSleeper(String task){
        ArrayList<String> w=new ArrayList<>();
        for (Task t: tasks){
            if(t.getTaskName().equals(task)){
                w.addAll(t.workers.values());
                break;
            }
        }
        if (!w.isEmpty())
            return w;
        return null;
    }

    class ListenTopicServer implements Runnable{

        public void run() {
            try {
                Connection connection = RabbitUtils.newConnection2Server("localhost","guest", "guest");
                Channel channel= edu.ufp.inf.sd.projeto.util.RabbitUtils.createChannel2Server(connection);

                channel.exchangeDeclare("serverTopic", BuiltinExchangeType.TOPIC);
                String queueName=channel.queueDeclare().getQueue();

                channel.queueBind(queueName, "serverTopic", "#");

                //System.out.println(" [*] Waiting for messages... to exit press CTRL+C");

                //Create callback that will receive messages from topic
                DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                    switch (delivery.getEnvelope().getRoutingKey()) {
                        case "server.pause": {
                            String message = new String(delivery.getBody(), "UTF-8");
                            pauseTask(message);
                            threadPool.execute(new SendTopic(channel, message, "worker.pause", "pause"));
                            break;
                        }
                        case "server.resume": {
                            String message = new String(delivery.getBody(), "UTF-8");
                            resumeTask(message);
                            threadPool.execute(new SendTopic(channel, message, "worker.resume", "resume"));
                            break;
                        }
                        case "server.kill": {
                            String message = new String(delivery.getBody(), "UTF-8");
                            pauseTask(message);
                            threadPool.execute(new SendTopic(channel, message, "worker.kill", "kill"));
                            break;
                        }
                        case "server.pay": {
                            String message = new String(delivery.getBody(), "UTF-8");
                            String[] words = message.split(";");
                            String taskName = words[0];
                            String userName = words[1];
                            payWorker(taskName,userName);
                            break;
                        }
                        case "working": {
                            String message = new String(delivery.getBody(), "UTF-8");
                            String[] words = message.split(";");
                            addWorker(words);
                            break;
                        }
                        case "over": {
                            String message = new String(delivery.getBody(), "UTF-8");
                            threadPool.execute(new CheckSleeper(message));
                        }
                        default: {
                            String message = new String(delivery.getBody(), "UTF-8");
                            System.out.println(" [x] Received '" +
                                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
                            String[] words = message.split(";");
                            String taskName = words[0];
                            String match = words[1];
                            String hashFound = words[2];
                            try {
                                if (toHexString(getSHA(match)).equals(hashFound)) {
                                    if(addMatch(taskName, hashFound, match))
                                        threadPool.execute(new PauseTopic(taskName, "worker.kill", "kill"));
                                    System.out.println("RECEBI!!!!!!!!!!!!!!!!!!!!!!!");
                                    //threadPool.execute(new SendTopic(channel, taskName, "admin.match", hashFound));
                                    addCredits(delivery.getEnvelope().getRoutingKey(), 20);
                                }
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }

                };
                CancelCallback cancelCallback=(consumerTag) -> {
                    System.out.println(" [x] Cancel callback activated: " + consumerTag);
                };
                //Associate callback to queue with topic exchange
                //When true disables "Manual message acks";When false worker must explicitly send ack (once done with task)
                boolean autoAck = true;
                channel.basicConsume(queueName, autoAck, deliverCallback, cancelCallback);

                //Current Thread waits till interrupted (avoids finishing try-with-resources which closes channel)
                //Thread.currentThread().join();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    class CheckSleeper implements Runnable{
        String taskName;

        public CheckSleeper(String s){
            taskName=s;
        }

        public void run(){
            ArrayList<String> sleepers=checkSleeper(taskName);
            if(sleepers!=null){
                try {

                    Connection connection = RabbitUtils.newConnection2Server("localhost", "guest", "guest");
                    Channel channel = RabbitUtils.createChannel2Server(connection);

                    ArrayList<String> words = new ArrayList<>();
                    File file = new File("C:\\Users\\ruima\\OneDrive\\SD\\test\\Books\\default.txt");
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String st;
                    int j = 0;
                    int c = 0;
                    while ((st = br.readLine()) != null){
                        if (st.equals(sleepers.get(j)) && c <= 20) {
                            words.add(st);
                            c++;
                            if (c == 20) {
                                c = 0;
                                j++;
                            }
                        }
                    }
                    boolean durable=true;
                    channel.queueDeclare(taskName, durable, false, false, null);
                    String message="";
                    int i=0;
                    for(String s: words){
                        message=message+s+";";
                        i++;
                        if(i==20){
                            channel.basicPublish("", taskName,
                                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                                    message.getBytes());
                            i=0;
                            message="";
                        }
                    }
                    channel.basicPublish("", taskName,
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
    }

    static class SendTopic implements Runnable{
        Channel channel;
        String EXCHANGE_NAME;
        String routingKey;
        String message;

        public SendTopic(Channel c, String s, String r, String m) {
            channel=c;
            EXCHANGE_NAME=s;
            routingKey=r;
            message=m;
        }

        public void run() {
            try {

                //Bind exchange to channel
                channel.exchangeDeclare(EXCHANGE_NAME+"Topic", BuiltinExchangeType.TOPIC);

                //Routing keys will have two words: <facility.severity> e.g. "kern.critical"
                //String routingKey= edu.ufp.inf.sd.rabbit.util.RabbitUtils.getRouting(argv, 3);
                //String message= edu.ufp.inf.sd.rabbit.util.RabbitUtils.getMessage(argv, 4);
                //Messages are not persisted (will be lost if no queue is bound to exchange yet)
                AMQP.BasicProperties props=null;//=MessageProperties.PERSISTENT_TEXT_PLAIN

                //Publish message on exchange with routing keys
                channel.basicPublish(EXCHANGE_NAME+"Topic", routingKey, props, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    static class PauseTopic implements Runnable{
        String EXCHANGE_NAME;
        String routingKey;
        String message;

        public PauseTopic( String queueName, String rout, String m) {
            EXCHANGE_NAME=queueName;
            routingKey=rout;
            message=m;
        }

        public void run() {
            try {
                Connection connection = RabbitUtils.newConnection2Server("localhost","guest", "guest");
                Channel channel= edu.ufp.inf.sd.projeto.util.RabbitUtils.createChannel2Server(connection);
                //Bind exchange to channel
                channel.exchangeDeclare(EXCHANGE_NAME+"Topic", BuiltinExchangeType.TOPIC);

                //Routing keys will have two words: <facility.severity> e.g. "kern.critical"
                //String routingKey= edu.ufp.inf.sd.rabbit.util.RabbitUtils.getRouting(argv, 3);
                //String message= edu.ufp.inf.sd.rabbit.util.RabbitUtils.getMessage(argv, 4);
                //Messages are not persisted (will be lost if no queue is bound to exchange yet)
                AMQP.BasicProperties props=null;//=MessageProperties.PERSISTENT_TEXT_PLAIN

                //Publish message on exchange with routing keys
                channel.basicPublish(EXCHANGE_NAME+"Topic", routingKey, props, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");

            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }

    }
}
