package INF;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import APP.DTO_Message;
import DOMAIN.Service;
import UI.ViewChat;
import UI.ViewLogIn;

public class Client {

	private String serverIP;
	private int serverPort;
	@SuppressWarnings("unused")
	private String clientIP;
	private ObjectOutputStream out;
	private Socket socket;

	public Client(String clientIP, String serverIP, int serverPort) {
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.clientIP = clientIP;
	}

	public void start(String nameBtn, List<String> list) throws Exception {
		socket = new Socket(serverIP, serverPort);
		out = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

		new Thread(new ReceiveMessageHandler(in)).start();

		List<String> encryptedList = new ArrayList<String>();
		List<String> listo = new ArrayList<String>();
		if (nameBtn.equals("Đăng ký")) {
			String username = list.get(0);
			String email = list.get(1);
			String password = list.get(2);
			String key = Service.createKey();
			String encryptedStringPassword = Service.encrypt(key, password);
			encryptedList.add(username);
			encryptedList.add(email);
			encryptedList.add(encryptedStringPassword);
			encryptedList.add(key);
			sendMessage(nameBtn, encryptedList);
		} else if (nameBtn.equals("Đăng nhập")) {
			String username = list.get(0);
			String password = list.get(1);
			String ClientIP = list.get(2);
			String key = Service.createKey();
			String encryptedStringPassword = Service.encrypt(key, password);
			encryptedList.add(username);
			encryptedList.add(encryptedStringPassword);
			encryptedList.add(key);
			encryptedList.add(ClientIP);
			sendMessage(nameBtn, encryptedList);
		} else if (nameBtn.equals("accept_e")) {
			String email = list.get(0);
			String clientIP = list.get(1);
			listo.add(email);
			listo.add(clientIP);
			sendMessage(nameBtn, listo);
		} else if (nameBtn.equals("Reload message")) {
			String sender = list.get(0);
			String receiver = list.get(1);
			String clientIP = list.get(2);
			listo.add(sender);
			listo.add(receiver);
			listo.add(clientIP);
			sendMessage(nameBtn, listo);
		} else if (nameBtn.equals("Send")) {
			String sender = list.get(0);
			String receiver = list.get(1);
			String content = list.get(2);
			String destinationIP = list.get(3);
			String clientIP = list.get(4);
			String key = Service.createKey();
			String encryptedStringContent = Service.encrypt(key, content);
			listo.add(sender);
			listo.add(receiver);
			listo.add(encryptedStringContent);
			listo.add(destinationIP);
			listo.add(clientIP);
			listo.add(key);
			sendMessage(nameBtn, listo);
		}
	}

	public void sendMessage(String nameButton, List<String> message) {
		try {
			out.writeUTF(nameButton);
			out.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class ReceiveMessageHandler implements Runnable {
		private ObjectInputStream in;

		public ReceiveMessageHandler(ObjectInputStream in) {
			this.in = in;
		}

		@Override
		public void run() {
			try {
				DTO_Message message = (DTO_Message) in.readObject();
				if (message.getSourceIP().equals("Đăng nhập")) {
					if (message.getContent().equals("true")) {
						ViewChat.main(null);
						ViewLogIn.frame.setVisible(false);
					}
				} else if (message.getSourceIP().equals("accept_e")) {
					if (!message.getContent().equals("null")) {
						ViewChat.listModel.addElement(message.getContent());
					}
				} else if (message.getSourceIP().equals("Reload message")) {
					ViewChat.txtrSdgd.setText("");
					List<String> content = message.getL();
					for (String string : content) {
						ViewChat.txtrSdgd.setText(ViewChat.txtrSdgd.getText() + string + "\n");
					}
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
