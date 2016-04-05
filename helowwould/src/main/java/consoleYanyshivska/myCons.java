package consoleYanyshivska;

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
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import lpi.server.rmi.IServer;
import lpi.server.rmi.IServer.FileInfo;
import lpi.server.rmi.IServer.Message;

public class myCons {

	static String[] partsout(String[] array, int index) {
		String[] result = new String[array.length - index];
		for (int i = index; i < (array.length); i++) {
			result[i - index] = array[i];

		}
		return result;
	}

	private static class MyTimerTask extends TimerTask {
		public void run() {
			try {
				Message ReceivedMessage = proxy.receiveMessage(sessionID);
				if (ReceivedMessage != null)
					System.out.println(
							"Incoming Message" + ReceivedMessage.getMessage() + "from" + ReceivedMessage.getSender());
				FileInfo ReceivedFile = proxy.receiveFile(sessionID);
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
			}
		}
	}

	public static String sessionID = null;
	public static IServer proxy;

	@SuppressWarnings("null")
	public static void main(String[] args)
			throws ClassNotFoundException, UnknownHostException, IOException, NotBoundException {
		try {
			Registry registry = LocateRegistry.getRegistry("lv.rst.uk.to", 152 ); 
			proxy = (IServer) registry.lookup(IServer.RMI_SERVER_NAME);
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
					try {
						proxy.ping();
					} catch (RemoteException ex) {
						// handle communication exception
					}
					break;
				case "echo":
					try {
						System.out.println(proxy.echo(String.join(" ", partsout(parts, 1))));
					} catch (RemoteException ex) {
						// handle communication exception
					}
					break;
				case "login":
					try {
						sessionID = proxy.login(parts[1], parts[2]);
						System.out.println(sessionID);
						if (sessionID != null && !isTimerStarted)
							isTimerStarted = true;
						timeToReceiveMsg.schedule(new MyTimerTask(), 0, 1000);
					} catch (RemoteException ex) {
						// handle communication exception
					}
					break;
				case "list":
					try {
						System.out.println("List of active users:" + Arrays.toString(proxy.listUsers(sessionID)));
					} catch (RemoteException ex) {
						// handle communication exception
					}
					break;
				case "msg":
					try {
						Message NewMessage = new Message(parts[1], String.join(" ", partsout(parts, 2)));
						proxy.sendMessage(sessionID, NewMessage);
					} catch (RemoteException ex) {
						// handle communication exception
					}
					break;
				case "file":
					try {
						FileInfo NewFileInfo = new FileInfo(parts[1], new File(parts[2]));
						proxy.sendFile(sessionID, NewFileInfo);
					} catch (RemoteException ex) {
						// handle communication exception
					}

					break;
				case "exit":
					try {

						proxy.exit(sessionID);
						isClosed = true;

					} catch (RemoteException ex) {
						// handle communication exception
					}
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
