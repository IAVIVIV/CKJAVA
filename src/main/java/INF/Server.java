package INF;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import APP.DTO_Message;
import APP.UseCase;
import DOMAIN.Repository;
import DOMAIN.Service;

public class Server {
	public static String serverIP = "192.168.1.3";
	public static int serverPort = 12345;

	private int port;
	private Set<ClientHandler> clientHandlers = new HashSet<>();

	public Server(int port) {
		this.port = port;
	}

	public void startServer() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Server is listening on port " + port);

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("New client connected: " + socket.getInetAddress().getHostAddress());
				ClientHandler clientHandler = new ClientHandler(socket, this);
				clientHandlers.add(clientHandler);
				clientHandler.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void broadcast(DTO_Message message) {
		String destinationIP = message.getDestinationIP();
		for (ClientHandler client : clientHandlers) {
			if (client.getSocket().getInetAddress().getHostAddress().equals(destinationIP)) {
				client.sendMessage(message);
				break;
			}
		}
	}

	public synchronized void removeClient(ClientHandler clientHandler) {
		clientHandlers.remove(clientHandler);
	}

	private static class ClientHandler extends Thread {
		private Socket socket;
		private Server server;
		private ObjectOutputStream out;

		public ClientHandler(Socket socket, Server server) {
			this.socket = socket;
			this.server = server;
		}

		public void run() {
			try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
				out = new ObjectOutputStream(socket.getOutputStream());

				String nameButton = in.readUTF();
				@SuppressWarnings("unchecked")
				List<String> message = (List<String>) in.readObject();

				if (nameButton.equals("Đăng ký")) {
					String username = message.get(0);
					String email = message.get(1);
					String encryptedStringPassword = message.get(2);
					String key = message.get(3);
					UseCase.register(username, email, encryptedStringPassword, key);
				} else if (nameButton.equals("Đăng nhập")) {
					Service s = new Service();
					String username = message.get(0);
					String encryptedStringPassword = message.get(1);
					String key = message.get(2);
					String ClientIP = message.get(3);
					String password = Service.decrypt(key, encryptedStringPassword);
					Boolean b = s.logIn(username, password);
					DTO_Message dto_Message = new DTO_Message(b.toString(), "Đăng nhập", ClientIP);
					server.broadcast(dto_Message);
				} else if (nameButton.equals("accept_e")) {
					String email = message.get(0);
					String clientIP = message.get(1);
					Repository r = new RepositoryImp();
					String username = r.findUser(email);
					if (username == null) {
						username = "null";
					}
					DTO_Message dto_Message = new DTO_Message(username, "accept_e", clientIP);
					server.broadcast(dto_Message);
				} else if (nameButton.equals("Reload message")) {
					String sender = message.get(0);
					String receiver = message.get(1);
					String clientIP = message.get(2);
					Service s = new Service();
					List<String> l = s.reloadMessage(sender, receiver);
					DTO_Message dto_Message = new DTO_Message("Reload message", "Reload message", clientIP);
					dto_Message.setL(l);
					server.broadcast(dto_Message);
				} else if (nameButton.equals("Send")) {
					String sender = message.get(0);
					String receiver = message.get(1);
					String encryptedStringContent = message.get(2);
					String destinationIP = message.get(3);
					String key = message.get(5);
					UseCase.sendMessage(sender, receiver, encryptedStringContent, key);
					Service s = new Service();
					List<String> l = s.reloadMessage(sender, receiver);
					DTO_Message dto_Message = new DTO_Message(encryptedStringContent, "Reload message", destinationIP);
					dto_Message.setL(l);
					server.broadcast(dto_Message);
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				server.removeClient(this);
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public Socket getSocket() {
			return socket;
		}

		public void sendMessage(DTO_Message message) {
			try {
				out.writeObject(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		int port = 12345;
		Server server = new Server(port);
		server.startServer();
	}
}
