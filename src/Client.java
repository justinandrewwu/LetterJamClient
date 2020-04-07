import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {

	public static void main(String[] args) {
		try {
			String[] messages = {"Hindenburg", "junsen", "Trynoia", "LTRG141Goodbye"};
			System.out.println("Starting client...");
			SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 8089));

			int length;

			for (int i = 0; i < messages.length; i++)
			{
				length = messages[i].length();
			}

			for (String msg : messages) {
				System.out.println("Prepared message: " + msg);
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				buffer.putInt(10);
				buffer.flip();
				int bytesWritten = client.write(buffer);
				System.out.println(String.format("Sending Message: %s\nbufforBytes: %d", msg, bytesWritten));
			}

			client.close();
			System.out.println("Client connection closed");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
