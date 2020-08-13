package edu.ufp.inf.sd.projeto.client;

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.projeto.server.Task;
import edu.ufp.inf.sd.projeto.server.TaskGroupFactoryRI;
import edu.ufp.inf.sd.projeto.server.TaskGroupSessionRI;
import edu.ufp.inf.sd.projeto.util.RabbitUtils;
import edu.ufp.inf.sd.projeto.util.rmisetup.SetupContextRMI;
import edu.ufp.inf.sd.projeto.util.threading.ThreadPool;


import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.ufp.inf.sd.projeto.client.TaskGroupClient.getSHA;
import static edu.ufp.inf.sd.projeto.client.TaskGroupClient.toHexString;

/**
 * <p>
 * Title: Projecto SD</p>
 * <p>
 * Description: Projecto apoio aulas SD</p>
 * <p>
 * Copyright: Copyright (c) 2017</p>
 * <p>
 * Company: UFP </p>
 *
 * @author Rui S. Moreira
 * @version 3.0
 */
public class TaskGroupClient {

    /**
     * Context for connecting a RMI client to a RMI Servant
     */
    private SetupContextRMI contextRMI;

    private String userName;

    ThreadPool threadPool=new ThreadPool(6);

    Notification notification =new Notification(false, false);



    /**
     * Remote interface that will hold the Servant proxy
     */
    private TaskGroupFactoryRI taskGroupFactoryRI;

    public static void main(String[] args) {
        if (args != null && args.length < 2) {
            System.err.println("usage: java [options] edu.ufp.sd.inf.rmi.diglib.client.ObserverClient <rmi_registry_ip> <rmi_registry_port> <service_name>");
            System.exit(-1);
        } else {
            //1. ============ Setup client RMI context ============
            TaskGroupClient hwc = new TaskGroupClient(args);
            //2. ============ Lookup service ============
            hwc.lookupService();
            //3. ============ Play with service ============
            hwc.playService();
        }
    }

    public TaskGroupClient(String args[]) {
        try {
            //List ans set args
            SetupContextRMI.printArgs(this.getClass().getName(), args);
            String registryIP = args[0];
            String registryPort = args[1];
            String serviceName = args[2];
            //Create a context for RMI setup
            contextRMI = new SetupContextRMI(this.getClass(), registryIP, registryPort, new String[]{serviceName});
        } catch (RemoteException e) {
            Logger.getLogger(TaskGroupClient.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private Remote lookupService() {
        try {
            //Get proxy to rmiregistry
            Registry registry = contextRMI.getRegistry();
            //Lookup service on rmiregistry and wait for calls
            if (registry != null) {
                //Get service url (including servicename)
                String serviceUrl = contextRMI.getServicesUrl(0);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "going to lookup service @ {0}", serviceUrl);
                
                //============ Get proxy to HelloWorld service ============
                taskGroupFactoryRI = (TaskGroupFactoryRI) registry.lookup(serviceUrl);
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "registry not bound (check IPs). :(");
                //registry = LocateRegistry.createRegistry(1099);
            }
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return taskGroupFactoryRI;
    }
    
    private void playService() {
        try {
            //============ Call HelloWorld remote service ============

            System.out.println("Username");
            Scanner userN = new Scanner(System.in);
            userName=userN.nextLine();
   /*         System.out.println("Password");
            Scanner passW = new Scanner(System.in);
            String hashType = userN.nextLine();
            TaskGroupSessionRI sessionRI=this.taskGroupFactoryRI.login(userN.nextLine(),passW.nextLine());

    */
            TaskGroupSessionRI sessionRI=this.taskGroupFactoryRI.login(userName,userName);
            if(sessionRI!=null){

                while(true) {
                    System.out.println("Criar task-1");
                    System.out.println("Listar task-2");
                    System.out.println("Procurar task-3");
                    System.out.println("Join task-4");
                    System.out.println("Upload Credits-5");
                    System.out.println("Credits-7");
                    System.out.println("MyTasks-8");
                    System.out.println("Notify Task-9");
                    System.out.println("Logout-0");
                    Scanner ler = new Scanner(System.in);
                    Task[] tasks=null;
                    int n = ler.nextInt();
                    if (n == 0) {
                        sessionRI.logout();
                        break;
                    }
                    switch (n) {
                        case 1:
                            System.out.println("Nome task");
                            Scanner tN = new Scanner(System.in);
                            String taskName = tN.nextLine();
                            System.out.println("Tipo de hash");
                            Scanner tH = new Scanner(System.in);
                            String hashType = tH.nextLine();
                            System.out.println("Tamanho da palavra");
                            Scanner tP = new Scanner(System.in);
                            int tamaPala=tP.nextInt();
                            System.out.println("URL");
                            Scanner urlSca = new Scanner(System.in);
                            String url=urlSca.nextLine();

                            //createTaskConnection(taskName, hashType, creditos);
                            //createTask(taskName, hashType, creditos, sessionRI);
                            String[] hashes=readHash("C:\\Users\\ruima\\OneDrive\\SD\\test\\Books\\"+userName+".txt");
                            //ArrayList<String> words=readWords("C:\\Users\\ruima\\OneDrive\\SD\\test\\Books\\default.txt");
                            boolean boo=sessionRI.createTask(taskName, hashType, userName, hashes, tamaPala, url);
                            if(boo){
                                System.out.println("Task criada!!");
                            }
                            else
                                System.out.println("Nao foi possivel criar task, ver creditos");
                            break;
                        case 2:
                            tasks = sessionRI.listAll();
                            for (Task b : tasks) {
                                System.out.println(b.toString());
                            }
                            break;
                        case 3:
                            System.out.println("Tipo de hash");
                            Scanner inHash = new Scanner(System.in);
                            String hash = inHash.nextLine();
                            tasks = sessionRI.searchHashType(hash);
                            for (Task b : tasks) {
                                System.out.println(b.toString());
                            }
                            break;
                        case 4:
                            System.out.println("Task");
                            Scanner workTask = new Scanner(System.in);
                            String workT = workTask.nextLine();
                            if(sessionRI.hashToMatch(workT)!=null) {
                                notification.setArray(sessionRI.hashToMatch(workT));
                                notification.setHashType(sessionRI.hashType(workT));
                                System.out.println(notification.hashes);
                                joinTask(workT);
                            }
                            break;
                        case 5:
                            System.out.println("Updload Credits");
                            Scanner case5 = new Scanner(System.in);
                            int credits = case5.nextInt();
                            sessionRI.uploadCredits(userName, credits);
                            break;
                        case 6:
                            threadPool.execute(new SendNotifyTopic("as", "worker.pause", "resume"));
                            break;
                        case 7:
                                System.out.println(sessionRI.showCredits(userName));
                            break;
                        case 8:
                            //System.out.println(myTasks.size());
                            tasks = sessionRI.returnUserTasks(userName);
                            for (Task b : tasks) {
                                System.out.println(b.toString());
                            }
                            break;
                        case 9:
                            System.out.println("Task");
                            Scanner case9 = new Scanner(System.in);
                            String case9String = case9.nextLine();
                            System.out.println("Notification");
                            Scanner case9Option = new Scanner(System.in);
                            String case9StringOpt = case9Option.nextLine();
                            threadPool.execute(new SendNotifyTopic("server", "server."+case9StringOpt, case9String));
                            break;
                        default:
                            System.out.println("Valor   incorreto");
                    }
                }
            }

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "going to finish, bye. ;)");

        } catch (RemoteException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }

    public void joinTask(String TASK_QUEUE_NAME) throws IOException, TimeoutException {
        notification.setPause(false);
        notification.setKill(false);
        Connection connection = RabbitUtils.newConnection2Server("localhost","guest", "guest");
        Channel channel=RabbitUtils.createChannel2Server(connection);

        boolean durable = true;
        //channel.queueDeclare(Send.QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
        //channel.queueBind(TASK_QUEUE_NAME+"topic", EmitLogTopic.EXCHANGE_NAME, "admin.*");
        //channel.exchangeDeclare(TASK_QUEUE_NAME+"pubsub", BuiltinExchangeType.TOPIC);

        //System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        int prefetchCount = 1;
        channel.basicQos(prefetchCount);
        Thread pauseThread = new Thread(){
            @Override
            public void run(){
                try {
                    Channel secondChannel=RabbitUtils.createChannel2Server(connection);
                    secondChannel.exchangeDeclare(TASK_QUEUE_NAME+"Topic", BuiltinExchangeType.TOPIC);
                    String secondQueueName=secondChannel.queueDeclare().getQueue();

                    secondChannel.queueBind(secondQueueName, TASK_QUEUE_NAME+"Topic", "worker.*");

                    System.out.println(" [*] Waiting for messages... to exit press CTRL+C");

                    //Create callback that will receive messages from topic
                    DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                        String message=new String(delivery.getBody(), "UTF-8");
                        System.out.println(" [x] Received '" +
                                delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
                        if(delivery.getEnvelope().getRoutingKey().equals("worker.match"))
                            notification.removeHash(message);
                        switch (message) {
                            case "pause":
                                System.out.println("TASK IS PAUSED");
                                notification.pause = true;
                                break;
                            case "resume":
                                System.out.println("TASK IS RESUMED");
                                notification.pause = false;
                                break;
                            case "kill":
                                System.out.println("TASK IS OVER");
                                notification.kill = true;
                                notification.hashes.clear();
                                break;
                            default:

                        }
                    };
                    CancelCallback cancelCallback=(consumerTag) -> {
                        System.out.println(" [x] Cancel callback activated: " + consumerTag);
                    };
                    //Associate callback to queue with topic exchange
                    //When true disables "Manual message acks";When false worker must explicitly send ack (once done with task)
                    boolean autoAck = true;
                    secondChannel.basicConsume(secondQueueName, autoAck, deliverCallback, cancelCallback);
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        };

        pauseThread.start();

        DeliverCallback deliverCallback=(consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
            System.out.println(notification.pause);
            try {
                if(!notification.kill) {
                    switch (notification.hashType){
                        case "SHA-256":
                            doWork256(message, notification.hashes ,channel, TASK_QUEUE_NAME);
                            break;
                        case "SHA-512":
                            doWork512(message, notification.hashes ,channel, TASK_QUEUE_NAME);
                            break;
                    }
                    this.threadPool.execute(new SendTopic(channel, "server", "server.pay", TASK_QUEUE_NAME + ";" + userName));
                    Thread.sleep(1000);
                    while (notification.pause) {
                        Thread.sleep(5000);
                        System.out.println("dormir");
                        System.out.println(notification.pause);
                    }
                }
                else {
                    System.out.println("Killing task");
                }

            } catch (InterruptedException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            } finally {
                System.out.println(" [x] Done processing task");
                threadPool.execute(new SendTopic(channel, "server", "over", TASK_QUEUE_NAME));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        boolean autoAck = false;
        channel.basicConsume(TASK_QUEUE_NAME, autoAck, deliverCallback, consumerTag -> { });
    }

    private void doWork256(String task, ArrayList<String> hashToMatch, Channel channel, String taskQueue) throws InterruptedException, NoSuchAlgorithmException, IOException {
        String[] words=task.split(";");
        SendTopic s=new SendTopic(channel, "server", "working", taskQueue+";"+userName+";"+words[0]);
        this.threadPool.execute(s);
        for (String word : words) {
            for (String toMatch : hashToMatch) {
                if (toHexString(getSHA(word)).equals(toMatch)) {
                    SendTopic st=new SendTopic(channel, "server", userName,taskQueue+";"+word+";"+toMatch);
                    this.threadPool.execute(st);
                }
            }
        }
    }

    private void doWork512(String task, ArrayList<String> hashToMatch, Channel channel, String taskQueue) throws InterruptedException, NoSuchAlgorithmException, IOException {
        String[] words=task.split(";");
        SendTopic s=new SendTopic(channel, "server", "working", taskQueue+";"+userName+";"+words[0]);
        this.threadPool.execute(s);
        for (String word : words) {
            for (String toMatch : hashToMatch) {
                if (encryptThisString(word).equals(toMatch)) {
                    SendTopic st=new SendTopic(channel, "server", userName,taskQueue+";"+word+";"+toMatch);
                    this.threadPool.execute(st);
                }
            }
        }
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException
    {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }
    public static String encryptThisString(String input)
    {
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHexString(byte[] hash)
    {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
/*
    public boolean createTask(String TASK_QUEUE_NAME, ArrayList<String> words) throws RemoteException {
        try {

            Connection connection = RabbitUtils.newConnection2Server("localhost","guest", "guest");
            Channel channel= RabbitUtils.createChannel2Server(connection);


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

            return true;
        }catch (Exception e){
            System.out.println("TaskGroupSessionImpl createTask error");
            return false;
        }
    }
/*
    private ArrayList<String> readWords(String fileName){
        try {
            ArrayList<String> words=new ArrayList<>();
            File file = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            while ((st = br.readLine()) != null)
                words.add(st);
            return words;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
*/
    private String[] readHash(String fileName){
        try {
            File file = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            int i=0;
            while ((st = br.readLine()) != null)
                i++;
            br=new BufferedReader(new FileReader(file));
            String[] words=new String[i];
            i=0;
            while ((st = br.readLine()) != null){
                words[i]=st;
                i++;
            }
            return words;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}

class SendTopic implements Runnable{
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

class SendNotifyTopic implements Runnable{
    String EXCHANGE_NAME;
    String routingKey;
    String message;

    public SendNotifyTopic(String queueName, String routKey, String m) {
        EXCHANGE_NAME=queueName;
        routingKey=routKey;
        message=m;
    }

    public void run() {
        try {
            Connection connection = RabbitUtils.newConnection2Server("localhost","guest", "guest");
            Channel channel=RabbitUtils.createChannel2Server(connection);

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


class ListenTopicAdmin implements Runnable{
    Connection connection;
    String EXCHANGE_NAME;
    int count;
    ThreadPool threadPool=new ThreadPool(2);

    public ListenTopicAdmin(Connection c, String s, int i) {
        connection=c;
        EXCHANGE_NAME=s;
        count=i;
    }

    public void run() {
        try {
            Channel channel= edu.ufp.inf.sd.projeto.util.RabbitUtils.createChannel2Server(connection);

            channel.exchangeDeclare(EXCHANGE_NAME+"Topic", BuiltinExchangeType.TOPIC);
            String queueName=channel.queueDeclare().getQueue();

            channel.queueBind(queueName, EXCHANGE_NAME+"Topic", "admin.*");

            System.out.println(" [*] Waiting for messages... to exit press CTRL+C");

            //Create callback that will receive messages from topic
            DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                String message=new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
                String[] words=message.split(";");
                String first=words[0];
                String second=words[1];
                try {
                    if(toHexString(getSHA(first)).equals(second)){
                        count--;
                        this.threadPool.execute(new SendTopic(channel, "server", "server.match", EXCHANGE_NAME+";"+second+";"+first));
                        if(count==0){
                            this.threadPool.execute(new SendTopic(channel, EXCHANGE_NAME, "admin.info", "kill"));
                        }

                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
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

class ListenTopicWorker implements Runnable{
    Connection connection;
    String EXCHANGE_NAME;
    Boolean pause;

    public ListenTopicWorker(Connection c, String s, boolean p) {
        connection=c;
        EXCHANGE_NAME=s;
        pause=p;
    }

    public void run() {
        try {
            Channel channel= edu.ufp.inf.sd.projeto.util.RabbitUtils.createChannel2Server(connection);

            channel.exchangeDeclare(EXCHANGE_NAME+"Topic", BuiltinExchangeType.TOPIC);
            String queueName=channel.queueDeclare().getQueue();

            channel.queueBind(queueName, EXCHANGE_NAME+"Topic", "worker.*");

            System.out.println(" [*] Waiting for messages... to exit press CTRL+C");

            //Create callback that will receive messages from topic
            DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                String message=new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
                if(message.equals("pause")){
                    pause=true;
                    System.out.println("pausado");
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