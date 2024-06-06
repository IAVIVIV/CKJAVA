package UI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import INF.Client;
import INF.Server;

public class ControllerLogIn implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		String nameBtn = e.getActionCommand();
		if (nameBtn == "Đăng nhập") {
			String taiKhoan = ViewLogIn.txtTiKhon.getText();
			String matKhau = ViewLogIn.txtMtKhu.getText();
			if (!taiKhoan.equals("Tài khoản") && !taiKhoan.equals("") && !matKhau.equals("Mật khẩu")
					&& !matKhau.equals("")) {
				String serverIP = Server.serverIP;
				int serverPort = Server.serverPort;
				String clientIP = Client.clientIp;
				Client client = new Client(clientIP, serverIP, serverPort);
				List<String> list = new ArrayList<String>();
				list.add(taiKhoan);
				list.add(matKhau);
				list.add(clientIP);
				try {
					ViewLogIn.username = taiKhoan;
					client.start(nameBtn, list);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		} else if (nameBtn == "Tạo tài khoản mới") {
			ViewRegister.main(null);
		}
	}
}
