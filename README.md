# SchoolMSN3.0
Chatt
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author AdamEriksson
 */
public class Chatt {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
       Client myAccount = new Client("130.238.244.43");
       myAccount.startChatting();
    }
    
}

class Client {
    Socket clientSocket;
    String serverIP;
    int socketNum = 10000;
    
    Client(String IP) throws IOException{
        try{
            serverIP = IP;
            clientSocket = new Socket(serverIP, socketNum);
        }
        
        catch (IOException e){
            //System.out.println("Find right IP");
        }
        
    }
    
    void startChatting() throws IOException, ClassNotFoundException{
        if(clientSocket.isConnected()) System.out.println("connect");
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
        //ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
        
        /*System.out.println((String)in.readObject());
        out.writeObject("då");*/
        
        Scanner temp = new Scanner(System.in);
        while(true){
            String inString = (String)in.readObject();
            System.out.println(inString);
            
            //String outString = temp.next();
            //out.writeObject(outString);
            
        }
        
        
    }
   /*Socket socket = new Socket("130.238.244.43", 10000);
        if(socket.isConnected()) System.out.println("connect");
         ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        System.out.println((String)in.readObject());
        
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject("då");
     
        out.close();*/
}

