package chat3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

//서버가 보내준 Echo메세지를 읽어오는 쓰레드 클래스
public class Receiver extends Thread {

	Socket socket = null;
	BufferedReader in = null;
	
	//Client가 접속시 생성한 Socket객체를 생성자에게 매개변수로 받음.
	public Receiver(Socket socket) {
		this.socket=socket;
		
		/*
		 Socket객체를 기반으로 input스트림을 생성한다.
		 */
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
		} catch (Exception e) {
			System.out.println("예외1"+e);
		}
	}
	
	/*
	 Thread 에서 main()의 역할을 하는 메소드로 직접 호출하면 
	 안되고 반드시 Start()를 통해 간접으로 호출해야 쓰레드가 생성된다.
	 */
	@Override
	public void run() {
		//스트림을 통해 서버가 보낸 라인단위로 읽어온다
		while(in!=null) {
			try {
				System.out.println("Thread Receive"+in.readLine());
			} catch (Exception e) {
				/*
				 클라이언트가 접속을 종료할 경우 SocketException이 발생되면서
				 무한루프에 빠지게된다
				 */
				System.out.println("예외2"+e);
			}
		}
		try {
			in.close();
		} catch (Exception e) {
			System.out.println("예외3"+e);
		}
	}
}