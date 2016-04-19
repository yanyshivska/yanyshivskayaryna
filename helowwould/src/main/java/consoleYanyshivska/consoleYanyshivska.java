package consoleYanyshivska;

import java.awt.List;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import lpi.dst.chat.soap.proxy.ArgumentFault;
import lpi.dst.chat.soap.proxy.ChatServer;
import lpi.dst.chat.soap.proxy.FileInfo;
import lpi.dst.chat.soap.proxy.IChatServer;
import lpi.dst.chat.soap.proxy.LoginFault;
import lpi.dst.chat.soap.proxy.Message;
import lpi.dst.chat.soap.proxy.ServerFault;

public class consoleYanyshivska {

	static String[] partsout(String[] array, int index) {
		String[] result = new String[array.length - index];
		for (int i = index; i < (array.length); i++) {
			result[i - index] = array[i];

		}
		return result;
	}
	
	private static FileInfo createFileInfo(String receiver, File file) throws IOException{
		 String filename = file.getName();
		 byte[] content =  Files.readAllBytes(file.toPath());

		FileInfo fileInfo = new FileInfo();
		fileInfo.setReceiver(receiver);
		fileInfo.setFilename(filename);
		fileInfo.setFileContent(content);

		return fileInfo;
		}
	
	private static Message createMessage(String receiver, String message){
		Message newMessage = new Message();
		newMessage.setReceiver(receiver);
		newMessage.setMessage(message);
		return newMessage;
		
	}
	
	
	private static class MyTimerTask extends TimerTask {
		public void run() {
			try {
				Message ReceivedMessage = serverProxy.receiveMessage(sessionID);
				if (ReceivedMessage != null)
					System.out.println(
							"Incoming Message" + ReceivedMessage.getMessage() + "from" + ReceivedMessage.getSender());
				FileInfo ReceivedFile = serverProxy.receiveFile(sessionID);
				if (ReceivedFile != null) {
					Path path = Paths.get("D:\\Desktop", ReceivedFile.getFilename());
					Path content = Files.write(path, ReceivedFile.getFileContent(), StandardOpenOption.CREATE);
					System.out
							.println("Incoming File:" + ReceivedFile.getFilename() + "from" + ReceivedFile.getSender());
				}
			} catch (RemoteException ex) {
				// handle communication exception
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ArgumentFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static String sessionID = null;
	public static IChatServer serverProxy;

	@SuppressWarnings("null")
	public static void main(String[] args) throws ClassNotFoundException, UnknownHostException, IOException,
			NotBoundException, ArgumentFault, ServerFault, LoginFault {
		try {
			ChatServer serverWrapper = new ChatServer(new URL("http://192.168.121.216:4321/chat?wsdl"));
			serverProxy = serverWrapper.getChatServerProxy();
			String[] parts;
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			String s = null;
			Timer timeToReceiveMsg = new Timer();
			System.out.printf("Enter String%n");
			boolean isClosed = false;
			boolean isTimerStarted = false;
			while (!isClosed) {

				s = br.readLine();
				parts = s.split(" ");
				switch (parts[0]) {
				case "ping":
					serverProxy.ping();
					break;
				case "echo":
					System.out.println(serverProxy.echo(String.join(" ", partsout(parts, 1))));
					break;
				case "login":
					sessionID = serverProxy.login(parts[1], parts[2]);
					System.out.println(sessionID);
					if (sessionID != null && !isTimerStarted)
						isTimerStarted = true;
					timeToReceiveMsg.schedule(new MyTimerTask(), 0, 1000);
					break;
				case "list":
					System.out.println("List of active users:" + serverProxy.listUsers(sessionID));
					break;
				case "msg":
					serverProxy.sendMessage(sessionID, createMessage(parts[1], String.join(" ", partsout(parts, 2))));
					break;
				case "file":
					serverProxy.sendFile(sessionID, createFileInfo(parts[1], new File(parts[2])));
					break;
				case "exit":
					serverProxy.exit(sessionID);
					isClosed = true;
					break;
				default:
					System.out.println("Invalid command");
					break;
				}
			}

		} catch (UnknownHostException e) {
			System.out.println("Unknown host: 0.0.0.0");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
