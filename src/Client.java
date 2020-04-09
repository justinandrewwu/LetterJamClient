import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {

	static final int MARKER = 0x4C54524A;
	static final int MSG_JOIN = 1001;

	ByteBuffer buffer;
	SocketChannel client;

	Client()
	{
		try {
			client = SocketChannel.open(new InetSocketAddress("localhost", 8089));
			buffer = ByteBuffer.allocate(1024);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int sendStringMsg(int msgid, String str)
	{
		try {
			int length = str.length() + 12;
			System.out.println("Length = "+length);
			buffer.clear();
			buffer.putInt(MARKER);
			buffer.putInt(length);
			buffer.putInt(msgid);
			buffer.put(str.getBytes());
			buffer.flip();
			int bytes = client.write(buffer);
			System.out.println("bytes written "+bytes);
			return bytes;
		} catch (IOException e) {
			System.out.println("sendJoin Exception: " + e);
			e.printStackTrace();
			return -1;
		}
	}

	//  This is to test the Server in receiving messages, we put a deliberate delay in sending the Message
	public int sendStringMsgDelay(int msgid, String str)
	{
		try {
			int length = str.length() + 12;
			buffer.clear();
			buffer.putInt(MARKER);
			buffer.putInt(length);
			buffer.putInt(msgid);
			buffer.flip();
			int bytes = client.write(buffer);

			Thread.sleep(100);

			buffer.clear();
			buffer.put(str.getBytes());
			buffer.flip();
			bytes += client.write(buffer);

			return bytes;
		} catch (Exception e) {
			System.out.println("sendJoin Exception: " + e);
			e.printStackTrace();
			return -1;
		}
	}

	public void close()
	{
		try {
			client.close();
		} catch (IOException e) {
			System.out.println("close Exception: " + e);
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			String[] messages = {"Hindenburg", "junsen", "Trynoia", "LTRG141Goodbye"};
			System.out.println("Starting client...");

			Client client = new Client();
			System.out.println("sendJoin junesen");
			client.sendStringMsg(MSG_JOIN, "junesen");

			Thread.sleep(2000);

			System.out.println("sendJoin test2");
			client.sendStringMsg(MSG_JOIN, "test2");

			System.out.println("sendJoin delayed xx");
			client.sendStringMsgDelay(MSG_JOIN, "xx");
			client.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
