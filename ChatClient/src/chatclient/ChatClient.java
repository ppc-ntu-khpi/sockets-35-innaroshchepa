/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

/**
 *
 * @author admin
 */
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class ChatClient {

  private TextArea output;
  private TextField input;
  private Button sendButton;
  private Button quitButton;
  private Frame frame;
  private TextArea usernames;
  private Dialog aboutDialog;
  private Button connectBt;
  
  private Socket connection = null;
  private BufferedReader serverIn = null;
  private PrintStream serverOut = null;

  public ChatClient() {
    output = new TextArea(7,60);
    input = new TextField(40);
    sendButton = new Button("Send");
    quitButton = new Button("Quit");
    connectBt = new Button("Connect");
    usernames = new TextArea(1,20);
  }

  public void launchFrame() throws IOException {
    frame = new Frame("PPC Chat");

    // Use the Border Layout for the frame
    frame.setLayout(new BorderLayout());
    
    frame.add(output, BorderLayout.WEST);
    frame.add(input, BorderLayout.SOUTH);

    usernames.setFont(new Font("Delicious", Font.ITALIC, 14));
    sendButton.setFont(new Font("Delicious", Font.ITALIC, 14));
    quitButton.setFont(new Font("Delicious", Font.ITALIC, 14));
    connectBt.setFont(new Font("Delicious", Font.ITALIC, 14));
    
    // Create the button panel
    Panel p1 = new Panel(); 
    p1.setLayout(new GridLayout(4,2));
    p1.add(connectBt);
    p1.add(usernames);
    p1.add(sendButton);
    p1.add(quitButton);
   
    
    

    // Add the button panel to the center
    frame.add(p1, BorderLayout.CENTER);

    // Create menu bar and File menu
    MenuBar mb = new MenuBar();
    Menu file = new Menu("File");
    MenuItem quitMenuItem = new MenuItem("Quit");
    quitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	System.exit(0);
      }
    });
    file.add(quitMenuItem);
    mb.add(file);
    frame.setMenuBar(mb);

    // Add Help menu to menu bar
    Menu help = new Menu("Help");
    MenuItem aboutMenuItem = new MenuItem("About");
    aboutMenuItem.addActionListener(new AboutHandler());
    help.add(aboutMenuItem);
    mb.setHelpMenu(help);
    
    // Attach listener to the appropriate components
    connectBt.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {
            String serverIP = System.getProperty("serverIP", "127.0.0.1");
            String serverPort = System.getProperty("serverPort", "2000");
            try {
                connection = new Socket(serverIP, Integer.parseInt(serverPort));
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
                serverIn = new BufferedReader(isr);
                serverOut = new PrintStream(connection.getOutputStream());    
                Thread t = new Thread(new RemoteReader());
                t.start();
            } catch (Exception ex) {
                System.err.println("Unable to connect to server!");
                ex.printStackTrace();
            }
        }
    });
    sendButton.addActionListener(new SendHandler());
    input.addActionListener(new SendHandler());
    frame.addWindowListener(new CloseHandler());
    quitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.exit(0);
        }
    });
    frame.pack();
    frame.setVisible(true);
    frame.setLocationRelativeTo(null);
  }

  private class SendHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
       String text = input.getText();
        text = usernames.getText() + ": " + text + "\n";
        serverOut.print(text);
        input.setText("");
    }
  }

  private class CloseHandler extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      System.exit(0);
    }
  }
  
 
  private class AboutHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      // Create the aboutDialog when it is requested
      JOptionPane.showMessageDialog(frame, "The ChatClient is a neat tool that allows you to talk to other ChatClients via a ChatServer");
    }
  }

  private class AboutDialog extends Dialog implements ActionListener  {
    public AboutDialog(Frame parent, String title, boolean modal) {
      super(parent,title,modal);
      add(new Label("The ChatClient is a neat tool that allows you to talk " +
                  "to other ChatClients via a ChatServer"),BorderLayout.NORTH);
      Button b = new Button("OK");
      add(b,BorderLayout.SOUTH);
      b.addActionListener(this);
      pack();
    }
    // Hide the dialog box when the OK button is pushed
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }
  
  private class RemoteReader implements Runnable {
  public void run() {
    try {
      while ( true ) {
        String nextLine = serverIn.readLine();
        output.append(nextLine + "\n");
      }
    } catch (Exception e) {
        System.err.println("Error while reading from server.");
        e.printStackTrace();
      }
  } // закінчення методу run 
} // закінчення опису внутрішнього класу RemoteReader 
  
  

  public static void main(String[] args) throws IOException {
    ChatClient c = new ChatClient();
    c.launchFrame();
  }
    
}
