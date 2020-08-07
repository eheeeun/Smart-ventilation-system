package device;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.util.Timer;
import java.util.TimerTask;

class TestClient extends JFrame implements ActionListener, KeyListener {
	
	public class SharedArea {
		int temperature;
		int humidity;
		int temperature2;
		int humidity2;
	}

	SharedArea sa = new SharedArea();

	off offThread = null;
	on onThread = null;

	off2 offThread2 = null;
	on2 onThread2 = null;
	
	TimerTest2 ttt = null;
	TimerTest3 tttt = null;
	
	int temperature = 26;
	int humidity = 60;
	int temperature2 = 26;
	int humidity2 = 60;
	
	int flag = 0;
	int mltime = 0;
	int ttime = 0;
	int endd = 0;

	String Lstatus = "off";
	String Rstatus = "off";
	String uid;
	String dv1 = "IOT";
	String dv2 = "717";

	JFrame f = new JFrame();

	JButton btn1 = new JButton("On");
	JButton btn2 = new JButton("Off");
	JButton btn3 = new JButton("On");
	JButton btn4 = new JButton("Off");

	ImageIcon image1 = new ImageIcon("C:\\FAN_OFF.png"); // �̹��� ���
	ImageIcon image2 = new ImageIcon("C:\\FAN_OFF.png"); // �̹��� ���

	JButton con = new JButton("���ӿ�û");

	JTextField uMsg = new JTextField(20);

	JPanel panel = new JPanel();

	JLabel label = new JLabel(image1);
	JLabel label1 = new JLabel(image2);

	JLabel jLabel = new JLabel("  �µ� ");
	JLabel jLabel2 = new JLabel(String.valueOf(temperature));
	JLabel jLabel3 = new JLabel("  ��");
	JLabel jLabel4 = new JLabel("  ���� ");
	JLabel jLabel5 = new JLabel(String.valueOf(humidity));
	JLabel jLabel6 = new JLabel("  %");

	JLabel jLabel1 = new JLabel("  �µ� ");
	JLabel jLabel7 = new JLabel(String.valueOf(temperature2));
	JLabel jLabel8 = new JLabel("  ��");

	JLabel jLabel9 = new JLabel("  ���� ");
	JLabel jLabel10 = new JLabel(String.valueOf(humidity2));
	JLabel jLabel11 = new JLabel("  %");

	JLabel jLabel12 = new JLabel("ID : ");
	JLabel jLabel13 = new JLabel(dv1);

	JLabel jLabel14 = new JLabel("ID : ");
	JLabel jLabel15 = new JLabel(dv2);

	JLabel jLabel16 = new JLabel("  �ð�: ");
	JLabel jLabel17 = new JLabel(String.valueOf(mltime));
	JLabel jLabel18 = new JLabel("  ��");
	
	JLabel jLabel19 = new JLabel("  �ð�: ");
	JLabel jLabel20 = new JLabel(String.valueOf(ttime));
	JLabel jLabel21 = new JLabel("  ��");
	
	
	// ������ ����
	Socket socket;
	
	DataOutputStream out;

	{
		panel.setLayout(null);
		// ��ư ����
		panel.add(btn1);
		panel.add(btn2);
		panel.add(con);

		panel.add(btn3);
		panel.add(btn4);

		panel.add(label);
		panel.add(label1);

		panel.add(jLabel);
		panel.add(jLabel2);
		panel.add(jLabel3);
		panel.add(jLabel4);
		panel.add(jLabel5);
		panel.add(jLabel6);
		panel.add(jLabel1);
		panel.add(jLabel7);
		panel.add(jLabel8);
		panel.add(jLabel9);
		panel.add(jLabel10);
		panel.add(jLabel11);
		panel.add(jLabel12);
		panel.add(jLabel13);
		panel.add(jLabel14);
		panel.add(jLabel15);
		
		panel.add(jLabel16);
		panel.add(jLabel17);
		panel.add(jLabel18);
		panel.add(jLabel19);
		panel.add(jLabel20);
		panel.add(jLabel21);

		jLabel.setBounds(70, 350, 35, 15);
		jLabel2.setBounds(105, 350, 35, 15);
		jLabel3.setBounds(115, 350, 35, 15);
		// �̹��� 1�� �µ�

		jLabel4.setBounds(150, 350, 35, 15);
		jLabel5.setBounds(185, 350, 35, 15);
		jLabel6.setBounds(195, 350, 35, 15);
		// �̹��� 1�� ����

		btn1.setBounds(40, 20, 100, 20); // �̹���1 on ��ư
		btn2.setBounds(150, 20, 100, 20); // �̹��� 1 off��ư
		// �̹��� 1�� ID
		jLabel12.setBounds(130, 80, 35, 15);
		jLabel13.setBounds(150, 80, 35, 15);

		btn3.setBounds(665, 20, 100, 20); // �̹���2 on ��ư
		btn4.setBounds(775, 20, 100, 20); // �̹��� 2 off��ư
		// �̹��� 2�� ID
		jLabel14.setBounds(755, 80, 35, 15);
		jLabel15.setBounds(775, 80, 35, 15);

		con.setBounds(410, 10, 100, 100); // ���ӿ�û

		// jLabel.setBounds(r);
		label.setBounds(25, 90, 250, 250); // �̹���1
		label1.setBounds(650, 90, 250, 250); // �̹��� 2

		jLabel1.setBounds(690, 350, 35, 15);
		jLabel7.setBounds(730, 350, 35, 15);
		jLabel8.setBounds(740, 350, 35, 15);
		// �̹��� 2�� �µ�
		jLabel9.setBounds(770, 350, 35, 15);
		jLabel10.setBounds(810, 350, 35, 15);
		jLabel11.setBounds(820, 350, 35, 15);
		// �̹��� 2�� ����
		jLabel16.setBounds(70, 400, 40, 15);
		jLabel17.setBounds(120, 400, 40, 15);
		jLabel18.setBounds(160, 400, 40, 15);
		
		jLabel19.setBounds(610, 400, 40, 15);
		jLabel20.setBounds(650, 400, 40, 15);
		jLabel21.setBounds(660, 400, 40, 15);

		btn1.addActionListener(this);
		btn2.addActionListener(this);
		btn3.addActionListener(this);
		btn4.addActionListener(this);

		con.addActionListener(this);

		uMsg.addKeyListener(this);

		this.add(panel);
		this.setVisible(true);
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String uip = "192.168.0.20";
		int uport = 3001;
		Object obj = e.getSource();

		if (obj == con) {
//			try {
//        		(new Serial()).connect("COM8");
//    	} catch (Exception e1) {
//        		e1.printStackTrace();
//    	}

			String serverIp = uip;
			// ������ �����Ͽ� ������ ��û�Ѵ�.
			try {
				socket = new Socket(serverIp, uport);
				System.out.println("������ ����Ǿ����ϴ�.");
				offThread = new off(socket, dv1, temperature, humidity, flag);
				offThread.sa = sa;

				offThread2 = new off2(socket, dv2, temperature, humidity, flag);
				offThread2.sa = sa;

				Thread receiver = new Thread(new ClientReceiver(socket));
				//receiver.sa = sa;
				receiver.start();

				if (socket != null) {
					offThread.start();
					offThread2.start();
				}

			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		if (obj == btn1) {

			System.out.println(dv1 + " on");
			panel.remove(label);
			image1 = new ImageIcon("C:\\FAN_ON.gif"); // �̹��� ��� label = new JLabel(image1);
			label = new JLabel(image1);
			panel.add(label);
			label.setBounds(25, 90, 250, 250);
			setVisible(true);

			offThread.interrupt();

			onThread = new on(socket, dv1, sa.temperature, sa.humidity);
			onThread.sa = sa;
			if (socket != null) {
				onThread.start();
			}

		}

		if (obj == btn2) {

			System.out.println(dv1 + " off");
			panel.remove(label);
			image1 = new ImageIcon("C:\\FAN_OFF.png"); // �̹��� ���
			label = new JLabel(image1);
			panel.add(label);

			label.setBounds(25, 90, 250, 250);
			setVisible(true);

			onThread.interrupt();

			offThread = new off(socket, dv1, sa.temperature, sa.humidity, 1);
			offThread.sa = sa;

			if (socket != null) {
				offThread.start();
			}
		}

		if (obj == btn3) {
			System.out.println(dv2 + " on");
			panel.remove(label1);
			image2 = new ImageIcon("C:\\FAN_ON.gif"); // �̹��� ���
			label1 = new JLabel(image2);
			panel.add(label1);

			label1.setBounds(650, 90, 250, 250);
			setVisible(true);

			offThread2.interrupt();

			onThread2 = new on2(socket, dv2, sa.temperature2, sa.humidity2);
			onThread2.sa = sa;
			if (socket != null) {
				onThread2.start();
			}

		}
		if (obj == btn4) {

			System.out.println(dv2 + " off");
			panel.remove(label1);
			image2 = new ImageIcon("C:\\FAN_OFF.png"); // �̹��� ���
			label1 = new JLabel(image2);
			panel.add(label1);

			label1.setBounds(650, 90, 250, 250);
			setVisible(true);

			onThread2.interrupt();

			offThread2 = new off2(socket, dv2, sa.temperature2, sa.humidity2, 1);
			offThread2.sa = sa;

			if (socket != null) {
				offThread2.start();
			}

		}
	}

	// on �߰�
	public class on extends Thread {

		public SharedArea sa = new SharedArea();

		private Socket socket;
		private String dvid;
		int temperature;
		int humidity;

		public on(Socket socket, String dvid, int temperature, int humidity) {
			this.socket = socket;
			this.dvid = dvid;
			this.temperature = temperature;
			this.humidity = humidity;

		}

		public void run() {
			OutputStream os;
			try {
				os = socket.getOutputStream();
				Protocol protocol = new Protocol();
				// �⺻������ 1000�� ��������

				protocol = new Protocol(Protocol.PT_ID_ORDER);
				protocol.setMyOrder("on");
				protocol.setMyId(dvid);
				os.write(protocol.getPacket());

				protocol = new Protocol(Protocol.PT_RES_DEV_STATUS);

				while (!Thread.currentThread().isInterrupted()) {

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity); // �µ� 27

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 28

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 27

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 26

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 25

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 24

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 25

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity--;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException ie) {

			} finally {
				System.out.println("On thread died");
			}

		}
	}

	// off �߰�
	class off extends Thread {

		SharedArea sa = new SharedArea();

		public Socket socket;
		public String dvid;
		int temperature;
		int humidity;
		int flag;

		public off(Socket socket, String dvid, int temperature, int humidity, int fla) {
			this.socket = socket;
			this.dvid = dvid;
			this.temperature = temperature;
			this.humidity = humidity;
			this.flag = fla;
		}

		public void run() {
			try {
				if(flag == 1)
				{
					jLabel16.setText("�ð� : ");
					jLabel17.setText("...");
					jLabel18.setText("��");
				}
				
				OutputStream os = socket.getOutputStream();
				InputStream is = socket.getInputStream();

				Protocol protocol = new Protocol();
				protocol = new Protocol(Protocol.PT_ID_ORDER);
				protocol.setMyOrder("off");
				protocol.setMyId(dvid);

				os.write(protocol.getPacket());

				protocol = new Protocol(Protocol.PT_RES_DEV_STATUS);

				while (!Thread.currentThread().isInterrupted()) {

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity++;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity); // �µ� 27

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity++;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 28

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity++;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 27

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity--;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 26

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity++;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 25

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity--;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 24

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity++;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 25

					sa.temperature = temperature;
					sa.humidity = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					jLabel2.setText(String.valueOf(temperature));
					jLabel5.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity++;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException ie) {

			} finally {
				System.out.println("Off thread died");
			}

		}

	}

	public void init(String uip, int uport) {

		try {
			String serverIp = uip;
			// ������ �����Ͽ� ������ ��û�Ѵ�.
			socket = new Socket(serverIp, uport);
			out = new DataOutputStream(socket.getOutputStream());
			System.out.println("������ ����Ǿ����ϴ�.");

		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
		}
	}

	// on �߰�
	public class on2 extends Thread {

		public SharedArea sa = new SharedArea();

		private Socket socket;
		private String dvid;
		int temperature;
		int humidity;

		public on2(Socket socket, String dvid, int temperature, int humidity) {
			this.socket = socket;
			this.dvid = dvid;
			this.temperature = temperature;
			this.humidity = humidity;

		}

		public void run() {
			OutputStream os;
			try {
				os = socket.getOutputStream();
				InputStream is = socket.getInputStream();
				Protocol protocol = new Protocol();
				// �⺻������ 1000�� ��������

				protocol = new Protocol(Protocol.PT_ML);
				protocol.setMLOrder("on");
				protocol.setMLId(dvid);
				os.write(protocol.getPacket());

				protocol = new Protocol(Protocol.PT_RES_DEV_STATUS);

				while (!Thread.currentThread().isInterrupted()) {

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity); // �µ� 27

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 28

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 27

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 26

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 25

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 24

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity--;
					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 25

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity--;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException ie) {

			} finally {
				System.out.println("On thread died");
			}

		}
	}

	// off �߰�
	class off2 extends Thread {

		SharedArea sa = new SharedArea();

		public Socket socket;
		public String dvid;
		int temperature;
		int humidity;
		int flag;

		public off2(Socket socket, String dvid, int temperature, int humidity, int flag) {
			this.socket = socket;
			this.dvid = dvid;
			this.temperature = temperature;
			this.humidity = humidity;
			this.flag = flag;
		}

		public void run() {
			try {
				
				if(flag == 1)
				{
					jLabel19.setText("�ð� : ");
					jLabel20.setText("...");
					jLabel21.setText("��");
				}
				
				OutputStream os = socket.getOutputStream();
				InputStream is = socket.getInputStream();

				Protocol protocol = new Protocol();
				protocol = new Protocol(Protocol.PT_ML);
				protocol.setMLOrder("off");
				protocol.setMLId(dvid);

				os.write(protocol.getPacket());

				protocol = new Protocol(Protocol.PT_RES_DEV_STATUS);

				while (!Thread.currentThread().isInterrupted()) {

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity++;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity); // �µ� 27

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));
					;
					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity++;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 28

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity++;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 27

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity--;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 26

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity++;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 25

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;
					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature--;
					humidity--;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 24

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;
					

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity++;

					if (humidity > 99)
						humidity = 99;

					System.out.println("ȯǳ�� ID : " + dvid + " | �µ� :" + temperature + " ���� :" + humidity);// �µ� 25

					sa.temperature2 = temperature;
					sa.humidity2 = humidity;

					protocol.setHumidity(Integer.toString(humidity));
					protocol.setTemperature(Integer.toString(temperature));

					jLabel7.setText(String.valueOf(temperature));
					jLabel10.setText(String.valueOf(humidity));
					os.write(protocol.getPacket());
					Thread.sleep(2800);

					temperature++;
					humidity++;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException ie) {

			} finally {
				System.out.println("Off thread died");
			}

		}
	}
	
	public class TimerTest extends Thread{
		 int start = 0;
		 int end;
			public TimerTest(int end) {
				try {
					this.end = end;

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		    public void run()
		    {	       	   
		        while(true)
		        {
		          
		            try {
		                Thread.sleep(1000);
		            } catch (InterruptedException e) {
		                e.printStackTrace();
		            }
		            if(end < start) {
		            	System.out.println("���̵� ���� end�� :" + end + " start�� : " +start);
		            	break;
		            }
		            else {
		            	mltime = start;
		            	jLabel17.setText(String.valueOf(mltime));
		            start++;
		            }
		        }
		    }
		}
	
		public class TimerTest2 extends Thread{
			 int start = 0;
			 int end;
			 private boolean stop; // stop �÷���
				public TimerTest2(int end) {
					try {
						this.end = end;
						this.stop = false;

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			    public void run()
			    {	       	   
			        while(!stop)
			        {		          
			            try {
			                Thread.sleep(1000);
			            } catch (InterruptedException e) {
			                e.printStackTrace();
			            }     
			            	ttime = start;
			            	jLabel20.setText(String.valueOf(ttime));
			            	start++;		            
			        }
			        jLabel20.setText(String.valueOf(ttime));
			    }
			    public void threadStop(boolean stop){
					this.stop = stop;
				}
			}
		
		public class TimerTest3 extends Thread{
			 int start = 0;
			 int end;
			 private boolean stop; // stop �÷���
				public TimerTest3(int end) {
					try {
						this.end = end;
						this.stop = false;

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			    public void run()
			    {	       	   
			        while(!stop)
			        {		          
			            try {
			                Thread.sleep(1000);
			            } catch (InterruptedException e) {
			                e.printStackTrace();
			            }     
			            	mltime = start;
			            	jLabel17.setText(String.valueOf(mltime));
			            	start++;		            
			        }
			        jLabel13.setText(String.valueOf(mltime));
			    }
			    public void threadStop(boolean stop){
					this.stop = stop;
				}
			}

	class ClientReceiver extends Thread {
		private Socket socket;
		String order;
		String time;

		public ClientReceiver(Socket socket) {
			try {
				this.socket = socket;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();

				while (true) {
					Protocol protocol = new Protocol();
					// �⺻������ 1000�� ��������
					byte[] buf = protocol.getPacket();
					is.read(buf);
					// ��Ŷ Ÿ���� ����
					int packetType = buf[0];
					protocol.setPacket(packetType, buf);

					if (packetType == Protocol.PT_EXIT) {
						protocol = new Protocol(Protocol.PT_EXIT);
						os.write(protocol.getPacket());
						System.out.println("���� ����");

						break;
					}
					switch (packetType) {
					case Protocol.PT_USERHUM_SET:
						String temp2[] = (protocol.getMLId()).split("-");
						order = temp2[0];
						uid = temp2[1];
						time = protocol.getMyOrder();
						System.out.println("�������� ORDER" + order + "�������� ID" + uid);

						double itime2 = Double.parseDouble(time);

						System.out.println("�˰��� �ð�" + itime2);

						if (uid.contentEquals(dv1)) {
							if (order.contentEquals("on")) {
								Lstatus = "on";
								System.out.println("�������� on, ID : " + dv1);
								label.setText(null);
								System.out.println(order + "�ۿ��� ������");

								panel.remove(label);

								image1 = new ImageIcon("C:\\FAN_ON.gif"); // �̹��� ���
								label = new JLabel(image1);
								panel.add(label);

								label.setBounds(25, 90, 250, 250);

								setVisible(true);

								protocol = new Protocol(Protocol.PT_RES_FAN);

								protocol.setResId(uid);
								protocol.setResfan(order);

								System.out.println("���̵�� " + uid + "������ : " + order);
								os.write(protocol.getPacket());

								Timer m_timer = new Timer();
								TimerTask m_task = new TimerTask() {
									public void run() {
										label.setText(null);
										Lstatus = "off";

										panel.remove(label);

										image1 = new ImageIcon("C:\\FAN_OFF.png"); // �̹��� ���
										label = new JLabel(image1);

										panel.add(label);

										label.setBounds(25, 90, 250, 250);
										setVisible(true);

										off thread = new off(socket, uid,sa.temperature, sa.humidity, 0);
										thread.start();
									}
								};
								m_timer.schedule(m_task, (long) (itime2 * 1000));

							} else if (order.contentEquals("off")) {
								label.setText(null);
								Lstatus = "off";
								System.out.println("�������� off, ID : " + dv1);
								System.out.println(order + "�ۿ��� ������");

								panel.remove(label);

								image1 = new ImageIcon("C:\\FAN_OFF.png"); // �̹��� ���
								label = new JLabel(image1);

								panel.add(label);

								label.setBounds(25, 90, 250, 250);
								setVisible(true);
							} else
								System.out.println("����� ���� ����");
						} else if (uid.contentEquals(dv2)) {
							if (order.contentEquals("on")) {
								Rstatus = "on";
								System.out.println("�������� on, ID : " + dv2);
								label1.setText(null);
								System.out.println(order + "�ۿ��� ������");

								panel.remove(label1);

								image2 = new ImageIcon("C:\\FAN_ON.gif"); // �̹��� ���
								label1 = new JLabel(image2);
								panel.add(label1);

								label1.setBounds(650, 90, 250, 250);

								protocol = new Protocol(Protocol.PT_RES_FAN);

								protocol.setResId(uid);
								protocol.setResfan(order);

								os.write(protocol.getPacket());

								setVisible(true);
							} else if (order.contentEquals("off")) {
								label1.setText(null);
								Rstatus = "on";
								System.out.println("�������� off, ID : " + dv2);
								System.out.println(order + "�ۿ��� ������");

								panel.remove(label1);

								image2 = new ImageIcon("C:\\FAN_OFF.png"); // �̹��� ���
								label1 = new JLabel(image2);

								panel.add(label1);

								label1.setBounds(650, 90, 250, 250);
								setVisible(true);

							} else
								System.out.println("����� ���� ����");
						}
						break;

					case Protocol.PT_ID_ORDER:
						System.out.println("����ڰ� ������ ���½��ϴ�.");

						String temp[] = (protocol.getMyId()).split("-");
						order = temp[0];
						uid = temp[1];
						time = protocol.getMyOrder();
						System.out.println("����� Order " + order + "����� Id " + uid);

						double itime = Double.parseDouble(time);

						System.out.println("�˰��� �ð� " + itime);
						
						int time3 = (int) itime;


						if (uid.contentEquals(dv1)) {
							if (order.contentEquals("on")) {
								Lstatus = "on";
								System.out.println("�ۿ��� ���� on, ID : " + dv1);
								label.setText(null);

								offThread.interrupt();

								onThread = new on(socket, dv1, sa.temperature, sa.humidity);
								onThread.sa = sa;
								onThread.start();

								panel.remove(label);

								image1 = new ImageIcon("C:\\FAN_ON.gif"); // �̹��� ���
								label = new JLabel(image1);
								panel.add(label);

								label.setBounds(25, 90, 250, 250);

								setVisible(true);
								
								TimerTest tt = new TimerTest(time3);            
		                        tt.start();


								protocol = new Protocol(Protocol.PT_RES_FAN);

								protocol.setResId(uid);
								protocol.setResfan(order);

								System.out.println("���̵�� " + uid + "������ : " + order);
								os.write(protocol.getPacket());

								Timer m_timer = new Timer();
								TimerTask m_task = new TimerTask() {
									public void run() {
										label.setText(null);
										Lstatus = "off";

										panel.remove(label);

										image1 = new ImageIcon("C:\\FAN_OFF.png"); // �̹��� ���
										label = new JLabel(image1);

										panel.add(label);

										label.setBounds(25, 90, 250, 250);
										setVisible(true);

										onThread.interrupt();
										 off thread = new off(socket, uid,sa.temperature, sa.humidity,0);

										thread.start();
									}
								};
								m_timer.schedule(m_task, (long) (itime * 1000));

							} else if (order.contentEquals("off")) {
								label.setText(null);
								
								onThread.interrupt();

								offThread = new off(socket, dv1, sa.temperature, sa.humidity,1);
								offThread.sa = sa;
								offThread.start();
								
								offThread = new off(socket, dv2, sa.temperature, sa.humidity, 1);
								offThread.sa = sa;
								
								Lstatus = "off";
								System.out.println("�ۿ��� ���� off, ID : " + dv1);

								panel.remove(label);

								image1 = new ImageIcon("C:\\FAN_OFF.png"); // �̹��� ���
								label = new JLabel(image1);

								panel.add(label);

								label.setBounds(25, 90, 250, 250);
								setVisible(true);

							} else
								System.out.println("����� ���� ����");
						}

						else if (uid.contentEquals(dv2)) {
							if (order.contentEquals("on")) {
								Rstatus = "on";
								System.out.println("�ۿ��� ���� on, ID : " + dv2);
								label1.setText(null);

								offThread2.interrupt();

								onThread2 = new on2(socket, dv2, sa.temperature2, sa.humidity2);
								onThread2.sa = sa;
								
								onThread2.start();
								panel.remove(label1);

								image2 = new ImageIcon("C:\\FAN_ON.gif"); // �̹��� ���
								label1 = new JLabel(image2);
								panel.add(label1);

								label1.setBounds(650, 90, 250, 250);

								protocol = new Protocol(Protocol.PT_RES_FAN);

								protocol.setResId(uid);
								protocol.setResfan(order);

								os.write(protocol.getPacket());

								setVisible(true);
								
								 ttt = new TimerTest2(ttime);            
			                        ttt.start();

			                        
							} else if (order.contentEquals("off")) {
								label1.setText(null);
								Rstatus = "on";
								System.out.println("�ۿ��� ���� off, ID : " + dv2);
								System.out.println(order + "�ۿ��� ������");

								onThread2.interrupt();
					
								offThread2 = new off2(socket, dv1, sa.temperature, sa.humidity,1);
								offThread2.sa = sa;
								offThread2.start();
								panel.remove(label1);

								image2 = new ImageIcon("C:\\FAN_OFF.png"); // �̹��� ���
								label1 = new JLabel(image2);

								panel.add(label1);
								ttt.threadStop(true);

								label1.setBounds(650, 90, 250, 250);
								setVisible(true);
							} else
								System.out.println("����� ���� ����");
						}
						break;
					}
				}

			} catch (IOException e) {
			}
		}
	}
	

	public static void main(String[] args) 
			throws IOException, ClassNotFoundException, InterruptedException {
		JFrame jf = new TestClient();
		jf.setSize(1000, 500);
		jf.setVisible(true);


	}

}