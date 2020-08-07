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

	Socket socket; // 앱쪽
	Socket devsocket; // 가상장치쪽

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
				System.out.println("사용자 접속 대기");
				while (true) {

					socket = server.accept();

					// 사용자 닉네임을 처리하는 스레드 가동
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
				System.out.println("가상 장치 대기");

				devsocket = virtualDevice.accept();
				System.out.println("가상장치 정보:" + devsocket.toString());

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

				System.out.println("클라이언트 접속");

				OutputStream os = socket.getOutputStream();
				InputStream is = socket.getInputStream();

				while (true) {
					Protocol protocol = new Protocol();

					byte[] buf = protocol.getPacket();
					is.read(buf);
					// 패킷 타입을 얻음
					int packetType = buf[0];
					protocol.setPacket(packetType, buf);
					if (packetType == Protocol.PT_EXIT) {
						protocol = new Protocol(Protocol.PT_EXIT);
						os.write(protocol.getPacket());
						System.out.println("서버 종료");
					}

					switch (packetType) {
					// 클라이언트가 로그인 정보 응답 패킷인 경우
					case Protocol.PT_RES_LOGIN:
						System.out.println("클라이언트가" + "로그인 정보를 보냈습니다.");
						String id = protocol.getId();
						String password = protocol.getPassword();
						System.out.println(id + " " + password);

						int result = userDAO.login(id, password);

						if (result == 1) // 로그인 성공
						{
							protocol = new Protocol(Protocol.PT_LOGIN_RESULT);
							protocol.setLoginResult("1");
							UserClass user = new UserClass(id, socket, protocol);
							user.sa = sa;
							user.start();
							System.out.println("로그인 성공");
						} else if (result == 0) {
							protocol = new Protocol(Protocol.PT_LOGIN_RESULT);
							protocol.setLoginResult("2");
							System.out.println("암호 틀림");
						} else if (result == -1) {
							// 아이디 존재 안 함
							protocol = new Protocol(Protocol.PT_LOGIN_RESULT);
							protocol.setLoginResult("3");
							System.out.println("아이디 존재 안 함");
						} else if (result == -2)
							System.out.println("db error");
						System.out.println("로그인 처리 결과 전송");
						os.write(protocol.getPacket());

					case Protocol.PT_REQ_DEV_STATUS:
						while (true) {

							if (socket != null) {

								Thread.sleep(2800);
								protocol = new Protocol(Protocol.PT_RES_DEV_STATUS);
								protocol.setHumidity(sa.humidity);
								protocol.setTemperature(sa.temperature);

								System.out.println("앱으로 송신하는 데이터");
								System.out.println("온도 : " + sa.temperature + " 습도  :" + sa.humidity);

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
					// 기본적으로 1000개 잡혀있음
					byte[] buf = protocol.getPacket();
					is.read(buf);
					// 패킷 타입을 얻음
					int packetType = buf[0];
					protocol.setPacket(packetType, buf);

					if (packetType == DeviceProtocol.PT_EXIT) {
						protocol = new DeviceProtocol(DeviceProtocol.PT_EXIT);
						os.write(protocol.getPacket());
						System.out.println("서버 종료");
						break;
					}
					switch (packetType) {
					case DeviceProtocol.PT_RES_DEV_STATUS:

						System.out.println("환풍기로부터 수신된 데이터");
						String temperature = protocol.getTemperature();
						String humidity = protocol.getHumidity();
						System.out.println("온도 :" + temperature + " 습도 :" + humidity);

						sa.temperature = temperature;
						sa.humidity = humidity;

						// 디바이스에게 다시 데이터 요청하는 프로토콜 타입으로 설정
						protocol = new DeviceProtocol(DeviceProtocol.PT_REQ_DEV_STATUS);
						os.write(protocol.getPacket());
						break;

					case DeviceProtocol.PT_ID_ORDER:
						String order = protocol.getDevOrder();
						String did = protocol.getDevId();

						if (order.contentEquals("on"))
							System.out.println("환풍기 ID : " + did + " 상태 >> ON");
						else if (order.contentEquals("off"))
							System.out.println("환풍기 ID : " + did + " 상태 >> OFF");
						else
							System.out.println("환풍기 '" + did + "' 로부터 ON/OFF 응답 오지 않음");

						if (socket != null) {
							DevToUserOrder dev = new DevToUserOrder(socket, protocol, order, did);
							dev.start();
						}

						break;

					case DeviceProtocol.PT_ML:
						
						String order3 = protocol.getMLOrder();
						String did3 = protocol.getMLId();

						if (order3.contentEquals("on"))
							System.out.println("NML환풍기 ID : " + did3 + " 상태 >> ON");
						else if (order3.contentEquals("off"))
							System.out.println("NML환풍기 ID : " + did3 + " 상태 >> OFF");
						else
							System.out.println("NML환풍기 '" + did3 + "' 로부터 ON/OFF 응답 오지 않음");

						if (socket != null) {
							NMLDevToUserOrder devZ = new NMLDevToUserOrder(socket, protocol, order3, did3);
							devZ.start();
						}
						break;

					case DeviceProtocol.PT_RES_FAN:

						String order2 = protocol.getResfan();
						String did2 = protocol.getResId();

						System.out.println("환풍기 ID : " + did2 + " / 요청된 명령  >" + order2 + "< 수행 ");
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

	// 사용자 정보를 관리하는 클래스
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
				// 기본적으로 1000개 잡혀있음
				while (true) {
					protocol = new Protocol();

					byte[] buf = protocol.getPacket();
					is.read(buf);
					// 패킷 타입을 얻음
					int packetType = buf[0];
					protocol.setPacket(packetType, buf);
					if (packetType == Protocol.PT_EXIT) {
						protocol = new Protocol(Protocol.PT_EXIT);
						os.write(protocol.getPacket());
						System.out.println("서버 종료");
					}

					switch (packetType) {
					// 클라이언트가 로그인 정보 응답 패킷인 경우
					case Protocol.PT_USERHUM_SET:

						order = protocol.getOrder();
						System.out.println(order);

						int one = Integer.parseInt(order);
						int two = Integer.parseInt(sa.humidity);
						if (two > one) {
							protocol = new Protocol(Protocol.PT_ID_ORDER);
							String order2 = "on";
							UserToDevSetHum dev2 = new UserToDevSetHum(id, devsocket, protocol, order2); // 원래는 디바이스한테

							dev2.sa = sa;
							dev2.start();
						} else
							break;
						break;
					case Protocol.PT_Ventilator_ORDER:

						System.out.println("습도는 : " + Integer.parseInt(sa.humidity));
						order = protocol.getOrder();

						if (order.contentEquals("on"))
							System.out.println("사용자 아이디 : " + id + " 로부터  환풍기 ON 명령 수신...");
						else if (order.contentEquals("off"))
							System.out.println("사용자 아이디 : " + id + " 로부터  환풍기 OFF 명령 수신...");
						else
							System.out.println("사용자 아이디 : '" + id + "' 로부터 ON/OFF 명령 오지 않음");

						UserToDevOrder dev = new UserToDevOrder(id, devsocket, protocol, order); // 원래는 디바이스한테 줘야되서

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

	// 장치 에게 메시지 보내는 클래스(on/off명령)
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

		// 사용자로부터 메세지를 수신받는 스레드
		public void run() {
			try {

				OutputStream os = devsocket.getOutputStream();
				InputStream is = devsocket.getInputStream();
				// 기본적으로 1000개 잡혀있음
				String devid = deviceDAO.devlogin(id);

				if (devid.contentEquals(id)) // device 아이디 찾기 성공
				{
					DeviceProtocol protocol = new DeviceProtocol();
					// 기본적으로 1000개 잡혀있음

					protocol = new DeviceProtocol(DeviceProtocol.PT_ID_ORDER);
					protocol.setDevId(Order + "-" + id);

					double time = handleCommandLine(socket, id, protocol, sa.humidity);

					protocol.setDevOrder(Double.toString(time));

					System.out.println("시간 : " + time);

					os.write(protocol.getPacket());
				}

				else if (devid.contentEquals("fail")) {
					// 아이디 존재 안 함
					System.out.println("아이디 존재 안 함");
				} else if (devid.contentEquals("error"))
					System.out.println("db error");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 장치 에게 메시지 보내는 클래스(on/off명령)
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

		// 사용자로부터 메세지를 수신받는 스레드
		public void run() {
			try {

				OutputStream os = devsocket.getOutputStream();
				InputStream is = devsocket.getInputStream();
				// 기본적으로 1000개 잡혀있음

				DeviceProtocol protocol = new DeviceProtocol();
				// 기본적으로 1000개 잡혀있음

				protocol = new DeviceProtocol(DeviceProtocol.PT_USERHUM_SET);
				protocol.setDevId(Order + "-" + id);
				double time = handleCommandLine(socket, id, protocol, sa.humidity);
				System.out.println(Order + " 오더" + id + " 아이디");
				protocol.setDevOrder(Double.toString(time));

				os.write(protocol.getPacket());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 사용자 에게 습도설정 응답 보내는 쓰레드
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

		// 사용자로부터 메세지를 수신받는 스레드
		public void run() {
			try {

				OutputStream os = socket.getOutputStream();
				InputStream is = socket.getInputStream();
				// 기본적으로 1000개 잡혀있음

				Protocol protocol = new Protocol();

				protocol = new Protocol(Protocol.PT_RES_FAN);

				protocol.setResId(did);
				protocol.setResfan(Order);

				os.write(protocol.getPacket());

				System.out.println("환풍기 ID : " + did + " / 요청된 명령 >" + Order + "< 수행  중...");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 사용자 에게 메시지 보내는 클래스
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

		// 사용자로부터 메세지를 수신받는 스레드
		public void run() {
			try {

				OutputStream os = socket.getOutputStream();
				InputStream is = socket.getInputStream();
				// 기본적으로 1000개 잡혀있음

				Protocol protocol = new Protocol();

				protocol = new Protocol(Protocol.PT_ID_ORDER);

				protocol.setMyId(did);
				protocol.setMyOrder(Order);

				System.out.println("오더뭐임 " + Order);
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

		// 사용자로부터 메세지를 수신받는 스레드
		public void run() {
			try {

				OutputStream os = socket.getOutputStream();
				InputStream is = socket.getInputStream();
				// 기본적으로 1000개 잡혀있음

				Protocol protocol = new Protocol();

				protocol = new Protocol(Protocol.PT_TOML);

				protocol.setUMLId(did);
				protocol.setUMLOrder(Order);

				System.out.println("오더뭐임 " + Order);
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
			System.out.println("현재습도는 : " + entry);
			if (!entry.equals("exit")) {
				DecimalFormat format = new DecimalFormat();
				format.applyLocalizedPattern("0.00");
				System.out.println("Time minute : " + format.format(lr.estimateRent(entry)) + "분");
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