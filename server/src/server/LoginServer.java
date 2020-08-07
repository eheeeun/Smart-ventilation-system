package server;

import java.net.*;
import java.text.DecimalFormat;
import java.util.stream.IntStream;
import device.DeviceDAO;
import user.UserDAO;
import java.io.*;

public class LoginServer {

	private ServerSocket server;
	private ServerSocket virtualDevice;

	Socket socket; // ����
	Socket devsocket; // ������ġ��

	UserDAO userDAO = new UserDAO();
	DeviceDAO deviceDAO = new DeviceDAO();

	public class SharedArea {
		String temperature;
		String humidity;
		Socket socket;
	}

	public LoginServer() {
		SharedArea sa = new SharedArea();
		try {
			server = new ServerSocket(3000);
			virtualDevice = new ServerSocket(3001);
			
			ConnectionThread thread = new ConnectionThread();
			thread.sa = sa;
			thread.start();

			DevConnectionThread devthread = new DevConnectionThread();
			devthread.sa = sa;
			devthread.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class ConnectionThread extends Thread {

		SharedArea sa = new SharedArea();

		public void run() {
			try {
				System.out.println("����� ���� ���");
				while (true) {

					socket = server.accept();

					// ����� �г����� ó���ϴ� ������ ����
					RequestThread thread = new RequestThread(socket);
					thread.sa = sa;
					thread.start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class DevConnectionThread extends Thread {

		SharedArea sa = new SharedArea();

		public void run() {
			try {
				System.out.println("���� ��ġ ���");

				devsocket = virtualDevice.accept();
				System.out.println("������ġ ����:" + devsocket.toString());

				DeviceThread thread = new DeviceThread(devsocket);
				thread.sa = sa;
				thread.start();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class RequestThread extends Thread {
		private Socket socket;
		SharedArea sa = new SharedArea();

		public RequestThread(Socket socket) {
			this.socket = socket;
		}

		public void run() {

			try {

				System.out.println("Ŭ���̾�Ʈ ����");

				OutputStream os = socket.getOutputStream();
				InputStream is = socket.getInputStream();

				while (true) {
					Protocol protocol = new Protocol();

					byte[] buf = protocol.getPacket();
					is.read(buf);
					// ��Ŷ Ÿ���� ����
					int packetType = buf[0];
					protocol.setPacket(packetType, buf);
					if (packetType == Protocol.PT_EXIT) {
						protocol = new Protocol(Protocol.PT_EXIT);
						os.write(protocol.getPacket());
						System.out.println("���� ����");
					}

					switch (packetType) {
					// Ŭ���̾�Ʈ�� �α��� ���� ���� ��Ŷ�� ���
					case Protocol.PT_RES_LOGIN:
						System.out.println("Ŭ���̾�Ʈ��" + "�α��� ������ ���½��ϴ�.");
						String id = protocol.getId();
						String password = protocol.getPassword();
						System.out.println(id + " " + password);

						int result = userDAO.login(id, password);

						if (result == 1) // �α��� ����
						{
							protocol = new Protocol(Protocol.PT_LOGIN_RESULT);
							protocol.setLoginResult("1");
							UserClass user = new UserClass(id, socket, protocol);
							user.sa = sa;
							user.start();
							System.out.println("�α��� ����");
						} else if (result == 0) {
							protocol = new Protocol(Protocol.PT_LOGIN_RESULT);
							protocol.setLoginResult("2");
							System.out.println("��ȣ Ʋ��");
						} else if (result == -1) {
							// ���̵� ���� �� ��
							protocol = new Protocol(Protocol.PT_LOGIN_RESULT);
							protocol.setLoginResult("3");
							System.out.println("���̵� ���� �� ��");
						} else if (result == -2)
							System.out.println("db error");
						System.out.println("�α��� ó�� ��� ����");
						os.write(protocol.getPacket());

					case Protocol.PT_REQ_DEV_STATUS:
						while (true) {

							if (socket != null) {

								Thread.sleep(2800);
								protocol = new Protocol(Protocol.PT_RES_DEV_STATUS);
								protocol.setHumidity(sa.humidity);
								protocol.setTemperature(sa.temperature);

								System.out.println("������ �۽��ϴ� ������");
								System.out.println("�µ� : " + sa.temperature + " ����  :" + sa.humidity);

								os.write(protocol.getPacket());
							}
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	class DeviceThread extends Thread {
		private Socket dsocket;

		SharedArea sa = new SharedArea();

		DeviceProtocol protocol = new DeviceProtocol(DeviceProtocol.PT_REQ_DEV_STATUS);

		public DeviceThread(Socket dsocket) {
			try {
				this.dsocket = dsocket;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				InputStream is = dsocket.getInputStream();
				OutputStream os = dsocket.getOutputStream();

				os.write(protocol.getPacket());
				while (true) {
					protocol = new DeviceProtocol();
					// �⺻������ 1000�� ��������
					byte[] buf = protocol.getPacket();
					is.read(buf);
					// ��Ŷ Ÿ���� ����
					int packetType = buf[0];
					protocol.setPacket(packetType, buf);

					if (packetType == DeviceProtocol.PT_EXIT) {
						protocol = new DeviceProtocol(DeviceProtocol.PT_EXIT);
						os.write(protocol.getPacket());
						System.out.println("���� ����");
						break;
					}
					switch (packetType) {
					case DeviceProtocol.PT_RES_DEV_STATUS:

						System.out.println("ȯǳ��κ��� ���ŵ� ������");
						String temperature = protocol.getTemperature();
						String humidity = protocol.getHumidity();
						System.out.println("�µ� :" + temperature + " ���� :" + humidity);

						sa.temperature = temperature;
						sa.humidity = humidity;

						// ����̽����� �ٽ� ������ ��û�ϴ� �������� Ÿ������ ����
						protocol = new DeviceProtocol(DeviceProtocol.PT_REQ_DEV_STATUS);
						os.write(protocol.getPacket());
						break;

					case DeviceProtocol.PT_ID_ORDER:
						String order = protocol.getDevOrder();
						String did = protocol.getDevId();

						if (order.contentEquals("on"))
							System.out.println("ȯǳ�� ID : " + did + " ���� >> ON");
						else if (order.contentEquals("off"))
							System.out.println("ȯǳ�� ID : " + did + " ���� >> OFF");
						else
							System.out.println("ȯǳ�� '" + did + "' �κ��� ON/OFF ���� ���� ����");

						if (socket != null) {
							DevToUserOrder dev = new DevToUserOrder(socket, protocol, order, did);
							dev.start();
						}

						break;

					case DeviceProtocol.PT_ML:
						
						String order3 = protocol.getMLOrder();
						String did3 = protocol.getMLId();

						if (order3.contentEquals("on"))
							System.out.println("NMLȯǳ�� ID : " + did3 + " ���� >> ON");
						else if (order3.contentEquals("off"))
							System.out.println("NMLȯǳ�� ID : " + did3 + " ���� >> OFF");
						else
							System.out.println("NMLȯǳ�� '" + did3 + "' �κ��� ON/OFF ���� ���� ����");

						if (socket != null) {
							NMLDevToUserOrder devZ = new NMLDevToUserOrder(socket, protocol, order3, did3);
							devZ.start();
						}
						break;

					case DeviceProtocol.PT_RES_FAN:

						String order2 = protocol.getResfan();
						String did2 = protocol.getResId();

						System.out.println("ȯǳ�� ID : " + did2 + " / ��û�� ���  >" + order2 + "< ���� ");
						Ressethum dev3 = new Ressethum(socket, protocol, order2, did2);
						dev3.start();
						break;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ����� ������ �����ϴ� Ŭ����
	class UserClass extends Thread {
		String id;
		Socket socket;
		Protocol protocol;
		DataInputStream dis;
		DataOutputStream dos;
		String order;

		SharedArea sa = new SharedArea();

		public UserClass(String id, Socket socket, Protocol protocol) {
			try {
				this.id = id;
				this.socket = socket;
				this.protocol = protocol;
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				dis = new DataInputStream(is);
				dos = new DataOutputStream(os);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {

				OutputStream os = socket.getOutputStream();
				InputStream is = socket.getInputStream();
				// �⺻������ 1000�� ��������
				while (true) {
					protocol = new Protocol();

					byte[] buf = protocol.getPacket();
					is.read(buf);
					// ��Ŷ Ÿ���� ����
					int packetType = buf[0];
					protocol.setPacket(packetType, buf);
					if (packetType == Protocol.PT_EXIT) {
						protocol = new Protocol(Protocol.PT_EXIT);
						os.write(protocol.getPacket());
						System.out.println("���� ����");
					}

					switch (packetType) {
					// Ŭ���̾�Ʈ�� �α��� ���� ���� ��Ŷ�� ���
					case Protocol.PT_USERHUM_SET:

						order = protocol.getOrder();
						System.out.println(order);

						int one = Integer.parseInt(order);
						int two = Integer.parseInt(sa.humidity);
						if (two > one) {
							protocol = new Protocol(Protocol.PT_ID_ORDER);
							String order2 = "on";
							UserToDevSetHum dev2 = new UserToDevSetHum(id, devsocket, protocol, order2); // ������ ����̽�����

							dev2.sa = sa;
							dev2.start();
						} else
							break;
						break;
					case Protocol.PT_Ventilator_ORDER:

						System.out.println("������ : " + Integer.parseInt(sa.humidity));
						order = protocol.getOrder();

						if (order.contentEquals("on"))
							System.out.println("����� ���̵� : " + id + " �κ���  ȯǳ�� ON ��� ����...");
						else if (order.contentEquals("off"))
							System.out.println("����� ���̵� : " + id + " �κ���  ȯǳ�� OFF ��� ����...");
						else
							System.out.println("����� ���̵� : '" + id + "' �κ��� ON/OFF ��� ���� ����");

						UserToDevOrder dev = new UserToDevOrder(id, devsocket, protocol, order); // ������ ����̽����� ��ߵǼ�

						dev.sa = sa;
						dev.start();
						break;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ��ġ ���� �޽��� ������ Ŭ����(on/off���)
	class UserToDevOrder extends Thread {
		String id;
		Socket devsocket;
		String Order;
		Protocol protocol;
		DataInputStream dis;
		DataOutputStream dos;
		SharedArea sa = new SharedArea();

		public UserToDevOrder(String id, Socket devsocket, Protocol protocol, String Order) {
			try {
				this.id = id;
				this.devsocket = devsocket;
				this.protocol = protocol;
				this.Order = Order;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// ����ڷκ��� �޼����� ���Ź޴� ������
		public void run() {
			try {

				OutputStream os = devsocket.getOutputStream();
				InputStream is = devsocket.getInputStream();
				// �⺻������ 1000�� ��������
				String devid = deviceDAO.devlogin(id);

				if (devid.contentEquals(id)) // device ���̵� ã�� ����
				{
					DeviceProtocol protocol = new DeviceProtocol();
					// �⺻������ 1000�� ��������

					protocol = new DeviceProtocol(DeviceProtocol.PT_ID_ORDER);
					protocol.setDevId(Order + "-" + id);

					double time = handleCommandLine(socket, id, protocol, sa.humidity);

					protocol.setDevOrder(Double.toString(time));

					System.out.println("�ð� : " + time);

					os.write(protocol.getPacket());
				}

				else if (devid.contentEquals("fail")) {
					// ���̵� ���� �� ��
					System.out.println("���̵� ���� �� ��");
				} else if (devid.contentEquals("error"))
					System.out.println("db error");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ��ġ ���� �޽��� ������ Ŭ����(on/off���)
	class UserToDevSetHum extends Thread {
		String id;
		Socket devocket;
		String Order;
		Protocol protocol;
		DataInputStream dis;
		DataOutputStream dos;
		SharedArea sa = new SharedArea();

		public UserToDevSetHum(String id, Socket devsocket, Protocol protocol, String Order) {
			try {
				this.id = id;
				this.devocket = devsocket;
				this.protocol = protocol;
				this.Order = Order;
				InputStream is = devsocket.getInputStream();
				OutputStream os = devsocket.getOutputStream();
				dis = new DataInputStream(is);
				dos = new DataOutputStream(os);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// ����ڷκ��� �޼����� ���Ź޴� ������
		public void run() {
			try {

				OutputStream os = devsocket.getOutputStream();
				InputStream is = devsocket.getInputStream();
				// �⺻������ 1000�� ��������

				DeviceProtocol protocol = new DeviceProtocol();
				// �⺻������ 1000�� ��������

				protocol = new DeviceProtocol(DeviceProtocol.PT_USERHUM_SET);
				protocol.setDevId(Order + "-" + id);
				double time = handleCommandLine(socket, id, protocol, sa.humidity);
				System.out.println(Order + " ����" + id + " ���̵�");
				protocol.setDevOrder(Double.toString(time));

				os.write(protocol.getPacket());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ����� ���� �������� ���� ������ ������
	class Ressethum extends Thread {

		String id;
		Socket socket;
		String Order;
		DeviceProtocol protocol;
		String did;
		DataInputStream dis;
		DataOutputStream dos;
		String on, off;

		public Ressethum(Socket socket, DeviceProtocol protocol, String Order, String did) {
			try {
				this.socket = socket;
				this.Order = Order;
				this.did = did;
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				dis = new DataInputStream(is);
				dos = new DataOutputStream(os);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// ����ڷκ��� �޼����� ���Ź޴� ������
		public void run() {
			try {

				OutputStream os = socket.getOutputStream();
				InputStream is = socket.getInputStream();
				// �⺻������ 1000�� ��������

				Protocol protocol = new Protocol();

				protocol = new Protocol(Protocol.PT_RES_FAN);

				protocol.setResId(did);
				protocol.setResfan(Order);

				os.write(protocol.getPacket());

				System.out.println("ȯǳ�� ID : " + did + " / ��û�� ��� >" + Order + "< ����  ��...");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ����� ���� �޽��� ������ Ŭ����
	class DevToUserOrder extends Thread {

		String id;
		Socket socket;
		String Order;
		DeviceProtocol protocol;
		String did;
		DataInputStream dis;
		DataOutputStream dos;
		String on, off;

		public DevToUserOrder(Socket socket, DeviceProtocol protocol, String Order, String did) {
			try {
				this.socket = socket;
				this.Order = Order;
				this.did = did;
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				dis = new DataInputStream(is);
				dos = new DataOutputStream(os);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// ����ڷκ��� �޼����� ���Ź޴� ������
		public void run() {
			try {

				OutputStream os = socket.getOutputStream();
				InputStream is = socket.getInputStream();
				// �⺻������ 1000�� ��������

				Protocol protocol = new Protocol();

				protocol = new Protocol(Protocol.PT_ID_ORDER);

				protocol.setMyId(did);
				protocol.setMyOrder(Order);

				System.out.println("�������� " + Order);
				os.write(protocol.getPacket());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class NMLDevToUserOrder extends Thread {

		String id;
		Socket socket;
		String Order;
		DeviceProtocol protocol;
		String did;
		DataInputStream dis;
		DataOutputStream dos;
		String on, off;

		public NMLDevToUserOrder(Socket socket, DeviceProtocol protocol, String Order, String did) {
			try {
				// this.dsocket = socket;
				this.socket = socket;
				this.Order = Order;
				this.did = did;
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				dis = new DataInputStream(is);
				dos = new DataOutputStream(os);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// ����ڷκ��� �޼����� ���Ź޴� ������
		public void run() {
			try {

				OutputStream os = socket.getOutputStream();
				InputStream is = socket.getInputStream();
				// �⺻������ 1000�� ��������

				Protocol protocol = new Protocol();

				protocol = new Protocol(Protocol.PT_TOML);

				protocol.setUMLId(did);
				protocol.setUMLOrder(Order);

				System.out.println("�������� " + Order);
				os.write(protocol.getPacket());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private double handleCommandLine(Socket socket, String id, DeviceProtocol protocol, String humidity)
			throws Exception {

		double time = 0;

		double[][][] TRAINING_DATA = { { { 1.0, 48.9 }, { 13 } }, { { 1.0, 28.5 }, { 0 } }, { { 1.0, 37.5 }, { 0 } },
				{ { 1.0, 40.4 }, { 10 } }, { { 1.0, 35.8 }, { 0 } }, { { 1.0, 43.8 }, { 10 } },
				{ { 1.0, 44.9 }, { 12 } }, { { 1.0, 46.8 }, { 12 } }, { { 1.0, 93.0 }, { 40 } },
				{ { 1.0, 94.6 }, { 43 } }, { { 1.0, 95.4 }, { 45 } }, { { 1.0, 74.4 }, { 20 } },
				{ { 1.0, 69.6 }, { 17 } }, { { 1.0, 77.1 }, { 25 } }, { { 1.0, 52.4 }, { 13 } },
				{ { 1.0, 50.9 }, { 10 } }, { { 1.0, 50.5 }, { 10 } }, { { 1.0, 42.3 }, { 10 } },
				{ { 1.0, 50.1 }, { 10 } }, { { 1.0, 87.4 }, { 33 } }, { { 1.0, 74.9 }, { 20 } },
				{ { 1.0, 39.5 }, { 0 } }, { { 1.0, 80.5 }, { 30 } }, { { 1.0, 86.7 }, { 32 } },
				{ { 1.0, 75.2 }, { 20 } }, { { 1.0, 90.2 }, { 40 } }, { { 1.0, 91.3 }, { 40 } },
				{ { 1.0, 65.0 }, { 15 } }, { { 1.0, 59.5 }, { 14 } }, { { 1.0, 54.4 }, { 10 } },
				{ { 1.0, 60.0 }, { 14 } }, { { 1.0, 71.6 }, { 20 } }, { { 1.0, 73.0 }, { 20 } },
				{ { 1.0, 71.9 }, { 20 } }, { { 1.0, 60.6 }, { 10 } }, { { 1.0, 63.0 }, { 15 } },
				{ { 1.0, 71.9 }, { 20 } }, { { 1.0, 68.1 }, { 16 } }, { { 1.0, 67.1 }, { 15 } },
				{ { 1.0, 66.4 }, { 15 } }, { { 1.0, 89.9 }, { 38 } }, { { 1.0, 72.5 }, { 20 } },
				{ { 1.0, 60.3 }, { 10 } }, { { 1.0, 62.1 }, { 10 } }, { { 1.0, 67.5 }, { 15 } },
				{ { 1.0, 93.8 }, { 40 } }, { { 1.0, 80.5 }, { 30 } }, { { 1.0, 93.8 }, { 40 } },
				{ { 1.0, 80.5 }, { 30 } }, { { 1.0, 59.0 }, { 13 } }, { { 1.0, 57.5 }, { 14 } },
				{ { 1.0, 58.0 }, { 14 } }, { { 1.0, 68.8 }, { 16 } }, { { 1.0, 59.1 }, { 14 } },
				{ { 1.0, 70.4 }, { 20 } }, { { 1.0, 57.0 }, { 13 } }, { { 1.0, 43.3 }, { 10 } },
				{ { 1.0, 62.5 }, { 12 } }, { { 1.0, 54.5 }, { 10 } }, { { 1.0, 60.4 }, { 11 } },
				{ { 1.0, 85.3 }, { 30 } }, { { 1.0, 69.0 }, { 16 } }, { { 1.0, 84.1 }, { 30 } },
				{ { 1.0, 73.3 }, { 20 } }, { { 1.0, 58.9 }, { 13 } }, { { 1.0, 61.9 }, { 15 } },
				{ { 1.0, 59.3 }, { 12 } }, { { 1.0, 67.8 }, { 16 } }, { { 1.0, 66.4 }, { 16 } },
				{ { 1.0, 68.0 }, { 15 } }, { { 1.0, 60.5 }, { 12 } }, { { 1.0, 62.4 }, { 12 } },
				{ { 1.0, 61.8 }, { 11 } }, { { 1.0, 69.9 }, { 17 } }, { { 1.0, 62.8 }, { 12 } },
				{ { 1.0, 63.6 }, { 13 } }, { { 1.0, 67.9 }, { 15 } }, { { 1.0, 65.4 }, { 15 } },
				{ { 1.0, 64.1 }, { 14 } }, { { 1.0, 66.9 }, { 15 } }, { { 1.0, 68.3 }, { 16 } },
				{ { 1.0, 70.4 }, { 18 } }, { { 1.0, 68.6 }, { 16 } }, { { 1.0, 75.9 }, { 20 } },
				{ { 1.0, 67.8 }, { 15 } }, { { 1.0, 64.0 }, { 14 } }, { { 1.0, 75.5 }, { 20 } },
				{ { 1.0, 69.3 }, { 16 } }, { { 1.0, 69.8 }, { 20 } } };

		LinearRegression lr;

		double[][] xArray = new double[TRAINING_DATA.length][TRAINING_DATA[0][0].length];
		double[][] yArray = new double[TRAINING_DATA.length][1];
		IntStream.range(0, TRAINING_DATA.length).forEach(i -> {
			IntStream.range(0, TRAINING_DATA[0][0].length).forEach(j -> xArray[i][j] = TRAINING_DATA[i][0][j]);
			yArray[i][0] = TRAINING_DATA[i][1][0];
		});

		lr = new LinearRegression(xArray, yArray);

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

		try {
			String entry = humidity;
			System.out.println("��������� : " + entry);
			if (!entry.equals("exit")) {
				DecimalFormat format = new DecimalFormat();
				format.applyLocalizedPattern("0.00");
				System.out.println("Time minute : " + format.format(lr.estimateRent(entry)) + "��");
				time = lr.estimateRent(entry);
				System.out.println(id);
				return time;
			} else
				System.exit(0);
		} catch (Exception e) {
			System.out.println("invalid input");
		}
		return time;
	}

	public static void main(String[] args) throws Exception {

		new LoginServer();

	}
}