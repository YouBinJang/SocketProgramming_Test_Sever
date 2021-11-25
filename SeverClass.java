package severpart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList; //arratlist ��Ű�� ����Ʈ

public class SeverClass {
	
	private ServerSocket server;
	ArrayList<UserClass> userList; //�˾Ƽ� �������� ����ǹǷ� ����� ��ü�� ������ �뵵�� arraylist ���

	public static void main(String[] args) {
		
		new SeverClass(); //���θ޼ҵ� �ȿ��� ����Ŭ������ �ҷ��� �׷��ϱ� ����Ŭ������ �����ϰڴ� �̸�����
	}
	
	public SeverClass() {
		try {
			userList=new ArrayList<UserClass>(); //��ü ����
			server=new ServerSocket(52232);//���� ���� ���� 
			ConnectionThread thread= new ConnectionThread();
			thread.start(); //����� ���� ��� ������ �����϶�� �߾�
		}catch(Exception e) 
		{e.printStackTrace();}
	}
	
	
	class ConnectionThread extends Thread{ //[[[����� ���� ��� ������]]]
		@Override
		public void run() {
			try {
				while(true) {
					System.out.println("����� ���� ���");
					Socket socket=server.accept();
					System.out.println("����ڰ� �����Ͽ����ϴ�.");
					// ����� �г����� ó���ϴ� ������ ����
					NickNameThread thread=new NickNameThread(socket);
					thread.start();
				}
			}catch(Exception e) {e.printStackTrace();}
		}
	}
	
	public class NickNameThread extends Thread{ //�г��ӽ�����
		private Socket socket;
		UserClass user;
		
		public NickNameThread(Socket socket) {
			this.socket=socket;
		}
		public void run() {
			try {
				//����� ��Ʈ��
				InputStream is = socket.getInputStream();
				OutputStream os= socket.getOutputStream();
				DataInputStream dis=new DataInputStream(is);
				DataOutputStream dos=new DataOutputStream(os);
				String nickName=dis.readUTF(); //�г��� ����
				String roomName=dis.readUTF(); //ä�ù� �̸� ����
				dos.writeUTF(nickName+"���� ["+roomName+ "] ä�ù濡 �����ϼ̽��ϴ�."); //ä�ù� ���� �ߴٴ°� �˷���
				sendToClient("���� : "+nickName+"���� �����Ͽ����ϴ�.");//���� �ִ� �����ڵ����� �� ������ ����� �˷���
				// ����� ������ �����ϴ� ��ü�� �����Ѵ�.
				user= new UserClass(nickName,socket);
				user.start();
				userList.add(user);
			}catch(Exception e) {e.printStackTrace();}
		}
	}
	
	class UserClass extends Thread {//����� ���� ����
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
		// ����ڷκ��� �޼����� ���Ź޴� ������
		public void run() {
			try {
				while(true) {
					//Ŭ���̾�Ʈ���� �޼����� ���Ź޴´�.
					String msg=dis.readUTF();
					// ����ڵ鿡�� �޼����� �����Ѵ�
					sendToClient(nickName+ " : "+ msg); 
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public synchronized void sendToClient(String msg) {//�ѹ��� �ϳ��� �����忡�� �����Ͽ� ó���ϵ����Ϸ��� sy~�߰�
		try {
			for (UserClass user : userList) {
				// �޼����� Ŭ���̾�Ʈ�鿡�� �����Ѵ�.
				user.dos.writeUTF(msg); //�޽��� Ŭ���̾�Ʈ ���� ����
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}


	