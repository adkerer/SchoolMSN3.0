/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.Border;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author AdamEriksson
 */
interface ObjectStreamListener {

    /**
     * This method is called whenever an object is received or an exception is
     * thrown.
     *
     * @param number The number of the manager as defined in its constructor
     * @param object The object received on the stream
     * @param exception The exception thrown when reading from the stream. Can
     * be IOException or ClassNotFoundException. One of name and exception will
     * always be null. In case of an exception the manager and stream are
     * closed.
     *
     */
    public void objectReceived(int number, Object object, Exception exception);
}

public class Chatt {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Client myAccount = new Client("130.238.252.149");
        //myAccount.startChatting();
    }

}

class Client implements ObjectStreamListener, ActionListener {

    Socket clientSocket;
    String serverIP;
    int socketNum = 10000;
    ObjectOutputStream out;
    ObjectInputStream in;
    ObjectStreamManager osm;

    JFrame theFrame;
    JTextArea chatArea;
    JTextField messageHandler;
    JTextArea connectedArea;
    JButton exitButton;
    
    final String exitMessage = new String("f8 1c 9f db 47 3e cd 22 70 23 0b 53 55 da 0f 9c");

    Client(String IP) throws IOException, ClassNotFoundException {
        try {
            serverIP = IP;
            clientSocket = new Socket(serverIP, socketNum);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            osm = new ObjectStreamManager(1, in, this);
            this.components();
            this.startChatting();
        } catch (IOException e) {
            //System.out.println("Find right IP");
        } catch (ClassNotFoundException e) {
            //System.out.println("Find right IP");
        } finally {
            //clientSocket.close();
            out.close();
            in.close();
            osm.closeManager();
            this.clientSocket.close();
        }

    }

    void components() {
        theFrame = new JFrame();
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel thePanel = new JPanel();
        thePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        connectedArea = new JTextArea();
        connectedArea.setEditable(false);
        messageHandler = new JTextField();

        exitButton = new JButton();
        exitButton.setActionCommand("button");
        exitButton.setText("Exit");
        exitButton.addActionListener(this);

        chatArea.setPreferredSize(new Dimension(300, 300));
        connectedArea.setPreferredSize(new Dimension(100, 300));
        connectedArea.setBackground(Color.LIGHT_GRAY);
        messageHandler.setPreferredSize(new Dimension(300, 40));
        exitButton.setPreferredSize(new Dimension(100, 40));

        c.fill = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 0;
        thePanel.add(chatArea, c);

        c.fill = GridBagConstraints.NORTHEAST;
        c.gridx = 1;
        c.gridy = 0;
        thePanel.add(connectedArea, c);

        c.fill = GridBagConstraints.SOUTHWEST;
        c.gridx = 0;
        c.gridy = 1;
        thePanel.add(messageHandler, c);

        c.fill = GridBagConstraints.SOUTHEAST;
        c.gridx = 1;
        c.gridy = 1;
        thePanel.add(exitButton, c);

        theFrame.add(thePanel);
        theFrame.pack();
        theFrame.setVisible(true);

        //ActionListener listener = new sendMessage();
        messageHandler.addActionListener(this);
    }

    public void objectReceived(int number, Object object, Exception exception) {
        if(object != null){
            String inString = (String) object;
            if (inString.equals(exitMessage)){
                JOptionPane.showMessageDialog(theFrame, "Oliver lämnade", "Title", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
            chatArea.append("Server: " + inString + "\n");
        }
     
    }

    public void actionPerformed(ActionEvent event) {
        //System.out.println(messageHandler.getText()); 
        try {
            if (event.getActionCommand().equalsIgnoreCase("button")) {
                //button    
                out.writeObject(exitMessage);
                System.exit(0);
            } else {
                //messageHandler
                String outString = messageHandler.getText();
                out.writeObject(outString);
                chatArea.append("Adam: " + outString + "\n");
                messageHandler.setText("");
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }

    void startChatting() throws IOException, ClassNotFoundException {
        if (clientSocket.isConnected()) {
            System.out.println("connect");
            this.connectedArea.setText("Oliver\nAdam");
        }

        Scanner soutIn = new Scanner(System.in);
        while (true) {
            System.out.println("in");
            String outString = soutIn.next();
            out.writeObject(outString);

            System.out.println("skickat");

        }

    }

}

class ObjectStreamManager {

    private final ObjectInputStream theStream;
    private final ObjectStreamListener theListener;
    private final int theNumber;
    private volatile boolean stopped = false;

    /**
     *
     * This creates and starts a stream manager for a stream. The manager will
     * continually read from the stream and forward objects through the
     * objectReceived() method of the ObjectStreamListener parameter
     *
     *
     * @param number The number you give to the manager. It will be included in
     * all calls to readObject. That way you can have the same callback serving
     * several managers since for each received object you get the identity of
     * the manager.
     * @param stream The stream on which the manager should listen.
     * @param listener The object that has the callback objectReceived()
     *
     *
     */
    public ObjectStreamManager(int number, ObjectInputStream stream, ObjectStreamListener listener) {
        theNumber = number;
        theStream = stream;
        theListener = listener;
        new InnerListener().start();  // start to listen on a new thread.
    }

    // This private method accepts an object/exception pair and forwards them
    // to the callback, including also the manager number. The forwarding is scheduled
    // on the Swing thread through an anonymous inner class.
    private void callback(final Object object, final Exception exception) {
        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() {
                if (!stopped) {
                    theListener.objectReceived(theNumber, object, exception);
                    if (exception != null) {
                        closeManager();
                    }
                }
            }
        });
    }

    // This is where the actual reading takes place.
    private class InnerListener extends Thread {

        @Override
        public void run() {
            while (!stopped) {                            // as long as no one stopped me
                try {
                    callback(theStream.readObject(), null); // read an object and forward it
                } catch (Exception e) {                 // if Exception then forward it
                    callback(null, e);
                }
            }
            try {                   // I have been stopped: close stream
                theStream.close();
            } catch (IOException e) {
            }

        }
    }

    /**
     * Stop the manager and close the stream.
     *
     */
    public void closeManager() {
        stopped = true;
    }
}      // end of ObjectStreamManager
