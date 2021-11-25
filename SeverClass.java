package severpart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList; //arratlist 패키지 임포트

public class SeverClass {
	
	private ServerSocket server;
	ArrayList<UserClass> userList; //알아서 동적으로 변경되므로 사용자 객체를 정리할 용도로 arraylist 사용

	public static void main(String[] args) {
		
		new SeverClass(); //메인메소드 안에서 서버클래스를 불렀어 그러니까 서버클래스를 실행하겠다 이말이지
	}
	
	public SeverClass() {
		try {
			userList=new ArrayList<UserClass>(); //객체 생성
			server=new ServerSocket(52232);//서버 소켓 열기 
			ConnectionThread thread= new ConnectionThread();
			thread.start(); //사용자 접속 대기 스레드 시작하라고 했어
		}catch(Exception e) 
		{e.printStackTrace();}
	}
	
	
	class ConnectionThread extends Thread{ //[[[사용자 접속 대기 스레드]]]
		@Override
		public void run() {
			try {
				while(true) {
					System.out.println("사용자 접속 대기");
					Socket socket=server.accept();
					System.out.println("사용자가 접속하였습니다.");
					// 사용자 닉네임을 처리하는 스레드 가동
					NickNameThread thread=new NickNameThread(socket);
					thread.start();
				}
			}catch(Exception e) {e.printStackTrace();}
		}
	}
	
	public class NickNameThread extends Thread{ //닉네임스레드
		private Socket socket;
		UserClass user;
		
		public NickNameThread(Socket socket) {
			this.socket=socket;
		}
		public void run() {
			try {
				//입출력 스트림
				InputStream is = socket.getInputStream();
				OutputStream os= socket.getOutputStream();
				DataInputStream dis=new DataInputStream(is);
				DataOutputStream dos=new DataOutputStream(os);
				String nickName=dis.readUTF(); //닉네임 받음
				String roomName=dis.readUTF(); //채팅방 이름 받음
				dos.writeUTF(nickName+"님이 ["+roomName+ "] 채팅방에 입장하셨습니다."); //채팅방 접속 했다는거 알려줌
				sendToClient("서버 : "+nickName+"님이 접속하였습니다.");//원래 있던 접속자들한테 새 접속자 생긴거 알려줘
				// 사용자 정보를 관리하는 객체를 생성한다.
				user= new UserClass(nickName,socket);
				user.start();
				userList.add(user);
			}catch(Exception e) {e.printStackTrace();}
		}
	}
	
	class UserClass extends Thread {//사용자 정보 관리
		String nickName;
		Socket socket;
		DataInputStream dis;
		DataOutputStream dos;
		
		
		public UserClass(String nickName,Socket socket) {
			try {
			this.nickName=nickName;
			this.socket=socket;
			InputStream is=socket.getInputStream();
			OutputStream os=socket.getOutputStream();
			dis = new DataInputStream(is);
			dos=new DataOutputStream(os);
			
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		// 사용자로부터 메세지를 수신받는 스레드
		public void run() {
			try {
				while(true) {
					//클라이언트에게 메세지를 수신받는다.
					String msg=dis.readUTF();
					// 사용자들에게 메세지를 전달한다
					sendToClient(nickName+ " : "+ msg); 
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public synchronized void sendToClient(String msg) {//한번에 하나의 쓰레드에만 접근하여 처리하도록하려고 sy~추가
		try {
			for (UserClass user : userList) {
				// 메세지를 클라이언트들에게 전달한다.
				user.dos.writeUTF(msg); //메시지 클라이언트 한테 전달
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}


	