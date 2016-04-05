package consoleYanyshivska;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class myCons {
	public static Socket socketnewMessage;
	
	static String[] partsout(String[] array, int index) {
		String[] result = new String[array.length - index];
		for (int i = index; i < (array.length); i++) {
			result[i - index] = array[i];

		}

		return result;
	}

	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}

	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}

		static void ping(Socket socket) throws IOException {
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		dos.writeInt(1);
		dos.writeByte(1);
		byte[] nbm = new byte[dis.readInt()];
		dis.readFully(nbm);
		if (nbm.length == 1 && nbm[0] == 2) {
			System.out.println("Ping successfull");
		} else
			System.out.println("Ping unsuccessfull");
	}

	static void echo(Socket socket, String str) throws IOException {
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		byte[] BytesOfString = str.getBytes();
		dos.writeInt(BytesOfString.length + 1);
		dos.writeByte(3);
		dos.write(BytesOfString);
		byte[] nbm = new byte[dis.readInt()];
		dis.readFully(nbm);
		if (nbm.length > 1) {
			String str_received = new String(nbm, 0, nbm.length);
			System.out.println(str_received);
		} else
			System.out.println("Error " + nbm[0]);
	}

	private static boolean login(Socket socket, String name, String password) throws IOException, ClassNotFoundException 
	{
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		byte[] bytes = serialize(new String[]{name, password});
		dos.writeInt(bytes.length+1);
		dos.writeByte(5);
		dos.write(bytes);
		byte[] nbm = new byte[dis.readInt()]; 
		dis.readFully(nbm);
		 if(nbm.length == 1 && (nbm[0]==6 || nbm[0]==7)) {
			 System.out.println(nbm[0] == 6 ? "new user, registration ok" : "login ok");
			 }
		else 
			System.out.println("Error "+(nbm.length == 1 ? nbm[0] : Arrays.toString(nbm)));
		return(true); 
	}

			static void list(Socket socket) throws IOException, ClassNotFoundException {
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		dos.writeInt(1);
		dos.writeByte(10);
		byte[] nbm = new byte[dis.readInt()];
		dis.readFully(nbm);
		if (nbm.length > 1) {
			String[] obj = (String[]) deserialize(nbm);
			System.out.println("List of active users:" + Arrays.toString(obj));
		} else
			System.out.println("Error " + (nbm.length == 1 ? nbm[0] : Arrays.toString(nbm)));
	}

	static void msg(Socket socket, String user, String message) throws IOException, ClassNotFoundException {
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		byte[] bytes = serialize(new String[] { user, message });
		dos.writeInt(bytes.length + 1);
		dos.writeByte(15);
		dos.write(bytes);
		byte[] nbm = new byte[dis.readInt()];
		dis.readFully(nbm);
		if (nbm.length == 1 && (nbm[0] == 16)) {
			System.out.println("message sent");
		} else
			System.out.println("Error " + (nbm.length == 1 ? nbm[0] : Arrays.toString(nbm)));
	}

	static void file(Socket socket, String user, String file) throws IOException, ClassNotFoundException {
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		Path filePath = Paths.get(file);
		byte[] content = Files.readAllBytes(filePath);
		Path filename = filePath.getFileName();
		byte[] bytes = serialize(new Object[] { user, filename.toString(), content });
		dos.writeInt(bytes.length + 1);
		dos.writeByte(20);
		dos.write(bytes);
		byte[] nbm = new byte[dis.readInt()];
		dis.readFully(nbm);
		if (nbm.length == 1 && (nbm[0] == 21)) {
			System.out.println("file sent");
		} else
			System.out.println("Error " + (nbm.length == 1 ? nbm[0] : Arrays.toString(nbm)));
	}

	static void receiveMsg(Socket socketnewMessage) throws IOException, ClassNotFoundException {
		DataInputStream dis = new DataInputStream(socketnewMessage.getInputStream());
		DataOutputStream dos = new DataOutputStream(socketnewMessage.getOutputStream());
		dos.writeInt(1);
		dos.writeByte(25);
		byte[] nbm = new byte[dis.readInt()];
		dis.readFully(nbm);
		if (nbm.length > 1 && nbm[0] != 26) {
			String[] obj = (String[]) deserialize(nbm);
			System.out.println("Incoming Message:" + Arrays.toString(obj));
		} else if (nbm[0] != 26)
			System.out.println("Error " + (nbm.length == 1 ? nbm[0] : Arrays.toString(nbm)));
	}

	static void receiveFile(Socket socketnewMessage) throws IOException, ClassNotFoundException {
		DataInputStream dis = new DataInputStream(socketnewMessage.getInputStream());
		DataOutputStream dos = new DataOutputStream(socketnewMessage.getOutputStream());
		dos.writeInt(1);
		dos.writeByte(30);
		byte[] nbm = new byte[dis.readInt()];
		dis.readFully(nbm);
		if (nbm.length > 1 && nbm[0] != 31) {
			Object[] obj = (Object[]) deserialize(nbm);
			Path path = Paths.get("D:\\Desktop", (String) obj[1]);
			byte[] filecontent = ((byte[]) obj[2]);
			Path content = Files.write(path, filecontent, StandardOpenOption.CREATE);
			System.out.println("Incoming File:" + Arrays.toString(obj));
		} else if (nbm[0] != 31)
			System.out.println("Error " + (nbm.length == 1 ? nbm[0] : Arrays.toString(nbm)));
	}

	private static class MyTimerTask extends TimerTask {
		public void run() {
			try {
				receiveMsg(socketnewMessage);
				receiveFile(socketnewMessage);
			} catch (ClassNotFoundException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}
	@SuppressWarnings("null")
		public static void main(String[] args) throws ClassNotFoundException {
			try {
				Socket socket = new Socket("lv.rst.uk.to", 151);
				socketnewMessage = new Socket("lv.rst.uk.to", 151);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter outnewMessage = new PrintWriter(socketnewMessage.getOutputStream(), true);
				BufferedReader innewMessage = new BufferedReader(
						new InputStreamReader(socketnewMessage.getInputStream()));

				String[] parts;

				InputStreamReader isr = new InputStreamReader(System.in);
				BufferedReader br = new BufferedReader(isr);
				String s = null;
				Timer timeToReceiveMsg = new Timer();
				System.out.printf("Enter String%n");
				boolean isTimerStarted=false;
				boolean isClosed = false;
				while (!isClosed) {
					 s = br.readLine ();
					parts = s.split(" ");
					

					switch (parts[0]) {

					case "ping":
						ping(socket);
						// System.out.printf("Your command is ping%n");
						break;

					case "echo":
						echo(socket, String.join(" ", partsout(parts, 1)));
						// System.out.printf("Your command is 'echo' with the
						// parameter %s%n",String.join(" ", partsout(parts,1)));
						break;
					case "login":
						if (login(socket, parts[1], parts[2]) && login(socketnewMessage, parts[1], parts[2])&& !isTimerStarted);
								isTimerStarted = true;
						timeToReceiveMsg.schedule(new MyTimerTask(), 0, 1000);
						break;
					case "list":
						list(socket);
						break;
					case "msg":
						msg(socket, parts[1], String.join(" ", partsout(parts, 2)));
						break;
					case "file":
						file(socket, parts[1], parts[2]);
						break;
					case "exit":
						isr.close();
						br.close();
						isTimerStarted = false;
						timeToReceiveMsg.cancel();
						socket.close();
						socketnewMessage.close();
						System.out.println("Program successfully terminated");
						break;
					default:
						System.out.println("Invalid command");
						break;

					}
				}
			} catch (IOException ioe) {
				// won't happen too often from the keyboard
			}
		}
	}

