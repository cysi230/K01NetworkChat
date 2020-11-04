package chat6;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultiServer {
	
	//멤버변수
	static ServerSocket serverSocket = null;
	static Socket socket = null;
	
	//클라이언트 정보저장을 위한 Map컬렉션생성
	Map<String, PrintWriter> clientMap;

	public MultiServer() {
		//클라이언트의 이름과 출력스트림을 저장할 HashMap 컬렉션생성
		clientMap = new HashMap<String, PrintWriter>();
		//HashMap 동기화설정 , 쓰레드가 사용자정보에 동시에 접근을 하는것을 차단
		Collections.synchronizedMap(clientMap);
	}
	
	//채팅서버의 초기화
	public void init() {
		try {
			/*
			 9999번 포트를 설정하여 서버객체를 생성하고 클라이언트의
			 접속을 대기한다
			 */
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다");
	
		while(true) {
			socket = serverSocket.accept();
			System.out.println(socket.getInetAddress()+"클라이언트의"
								+socket.getPort()+"포트를 통해"
								+socket.getLocalAddress()+"(서버)의"
								+socket.getLocalPort()+"포트로연결됨");
			
			/*
			 클라이언트의 메세지를 모든 클라이언트에게 전ㄷ알하기 위한
			 쓰레드 생성 및 시작 . 한명당 하나씩의 쓰레드가 생성된다.
			 */
			Thread mst = new MultiServerT(socket);
			mst.start();
		}
	}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				serverSocket.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
		
	public static void main(String[] args) {
		MultiServer ms = new MultiServer();
		ms.init();
	}
	
	// 접속된 모든 클라이언트에게 메세지를 전달하는 메소드
	public void sendAllMsg(String name, String msg) {
		
		//Map에 저장된 객체의 키값(접속자명)을 먼저 얻어온다.
		Iterator<String> it = clientMap.keySet().iterator();
		
		//저장된 객체(클라이언트)의 갯수만큼반복한다.
	while(it.hasNext()) {
		try {
			//각 클라이언트의 PrintWriter객체를 얻어온다.
			PrintWriter it_out = (PrintWriter)
			clientMap.get(it.next());
			
			/*
			 클라이언트에게 메세지를 전달한다.
			 매개변수로 name이 있는경우와 없는경우 구분해서
			 메세지를 전달하게 된다.
			 */
			if(name.equals("")) {
				//접속, 퇴장에서 사용되는 부분
				it_out.println(msg);
			}
			else {
				//메세지를 보낼때 사용되는부분
				it_out.println("["+name+"]"+msg);
			}
		}
		catch (Exception e) {
			System.out.println("예외"+e);
		}
		
	}
	}
	//내부클래스
	/*
	 init()에 기술되었던 스트림을 생성후 메세지를 읽기/쓰기 하던
	 부분이 해당 내부클래스로 이동되었다
	 */
	class MultiServerT extends Thread{
		//멤버변수
		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;
		//생성자: socket을 기반으로 입충력 스트림 생성
		public MultiServerT(Socket socket) {
			this.socket=socket;
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (Exception e) {
				System.out.println("예외"+e);
			}
		}
		
		@Override
		public void run() {
			
			String name="";//클라이언트의 이름을 저장
			String s= "";//클라이언트의 메세지를 저장
			
		try {
			//클라이언트 이름을 읽어와서 저장
			name = in.readLine();
			/*
			 방금접속한 클라이언트를 제외한 나머지에게 사용자의
			 입자을 알려준다
			 */
			sendAllMsg("", name+"님이입장하셨습니다");
			//현재 접속자의 정보를 HashMap에 저장한다.
			clientMap.put(name, out);
			
			//HashMap에저장된 객체의 수로 접족자수를 파악할수있다
			System.out.println(name+"접속");
			System.out.println("현재 접속자수는"
					+ clientMap.size()+"명입니다");
				//입력한 메세지는 모든 클라이언트에 Echo된다.
				while(in!=null) {
				s=in.readLine();
				if(s==null) {
					break;
				}
				System.out.println(name+" >> "+s);
				sendAllMsg(name,s);
			}
		} catch (Exception e) {
			System.out.println("예외1:"+e);
			e.printStackTrace();
		}
		finally {
			/*
			 클라이언트가 접속을 종료하면 Socket 예외가 발생하게되어
			 finally절로 진입하게된다 이떄"대화명"을 통해 정보를
			 삭제한다.
			 */
			clientMap.remove(name);
			sendAllMsg("", name+"님이퇴장하셨습니다");
			//퇴장하는 클라이언트의 쓰르드명을 높여준다.
			System.out.println(name+"["
					+Thread.currentThread().getName()+ "] 퇴장");
			System.out.println("현재접속자수는"
					+ clientMap.size()+"명입니다");
	
			try {
				in.close();
				out.close();
				serverSocket.close();
			}
			catch (Exception e) {
				System.out.println("예외2"+e);
				e.printStackTrace();
			}
		}
	}	
		
	}
}