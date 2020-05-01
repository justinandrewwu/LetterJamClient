import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {

	private static final int MARKER = 0x4C54524A;
	private static final int MSG_JOIN = 1001;
	private static final int MSG_MORE_LETTERS = 1002;
	private static final int MSG_MY_WORD = 1003;
	private static final int MSG_SUGGEST = 1004;
	private static final int MSG_VOTE = 1005;
	private static final int MSG_CHOSEN_CLUE = 1006;
	private static final int MSG_GIVE_CLUE = 1007;
	private static final int MSG_PLAYER_DECIDES = 1008;
	private static final int MSG_LETTER_GUESS = 1009;

	private static final int MSG_USER_JOIN = 2001;
	private static final int MSG_PICK_WORD = 2002;
	private static final int MSG_PLAYER_READY = 2003;
	private static final int MSG_TABLE_READY = 2004;
	private static final int MSG_VALID_CLUE = 2005;
	private static final int MSG_VOTED = 2006;
	private static final int MSG_SEND_CLUE = 2007;
	private static final int MSG_ANAGRAM = 2008;
	private static final int MSG_NUMBER_OF_LETTERS_RIGHT = 2009;

	private static final int MSG_ERROR = 3001;

	private static int nullread = 0;
	private static int printnullread = 1;

	private ByteBuffer rbuf = ByteBuffer.allocate(1024);
	private ByteBuffer wbuf = ByteBuffer.allocate(1024);
	SocketChannel client;

	Client()
	{
		try {
			client = SocketChannel.open(new InetSocketAddress("localhost", 8089));
			wbuf = ByteBuffer.allocate(1024);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {

			System.out.println("Starting client...");

			Client client = new Client();
			System.out.println("sendJoin junesen");
			client.sendStringMsg(MSG_JOIN, "junesen");

			while(true) {
				client.handleRead();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int handleRead()
	{
		int numread = 0;
		try {
			if (rbuf.limit() > 0) {
				System.out.println("before read position = " + rbuf.position() + " limit = " + rbuf.limit());
			}
			int res = client.read(rbuf);
			if (res < 0) {
				System.out.println("Client disconnected");
				client.close();
				return -1;
			}
			if (res == 0) {
				nullread++;
				if (nullread > printnullread) {
					System.out.println("Number of null reads = " + nullread);
					printnullread *= 10;
				}
			}
			rbuf.flip();
			if (res > 0) {
				System.out.println("read " + res + " bytes position = " + rbuf.position() + " limit = " + rbuf.limit());
			}
			int avail = rbuf.limit() - rbuf.position();
			if (avail > 0) {
				System.out.println("avail = "+avail);
			}
			while (avail >= 12) {        // we have to have at least the header to proceed
				int marker = rbuf.getInt(rbuf.position());
				if (marker != MARKER) {
					closeConnection("Bad Marker detected in Message " + marker);
				}
				int length = rbuf.getInt(rbuf.position()+4);
				System.out.println("parsed length = "+length);
				if (length > avail) {
					// we don't have the whole message, wait for it
					System.out.println("Not enough data to read a whole message length = "+length+ " avail = "+ avail);
					System.out.println("  position ="+rbuf.position()+" limit = "+rbuf.limit());
					rbuf.compact();
					System.out.println("After compact position ="+rbuf.position()+" limit = "+rbuf.limit());
					// rbuf.flip();
					// System.out.println("After flip ="+rbuf.position()+" limit = "+rbuf.limit());
					return numread;
				}
				int msgid = rbuf.getInt(rbuf.position()+8);
				switch (msgid) {
					case MSG_USER_JOIN: {
						String str = decodeString(length-12, 12);
						System.out.println("Received MSG_USER_JOIN " + str);
						break;
					}
					case MSG_PICK_WORD: {
						String str = decodeString(length-12, 12);
						System.out.println("Received MSG_PICK_WORD " + str);
						break;
					}
					case MSG_PLAYER_READY: {

					}
					case MSG_TABLE_READY: {

					}
					case MSG_VALID_CLUE: {

					}
					case MSG_VOTED: {

					}
					case MSG_SEND_CLUE: {

					}
					case MSG_ANAGRAM: {

					}
					case MSG_NUMBER_OF_LETTERS_RIGHT: {

					}
					case MSG_ERROR: {
						String str = decodeString(length-12, 12);
						System.out.println("Received MSG_ERROR " + str);
						break;
					}
					default: {
						// Unknown message
						closeConnection("Unknown message id "+ msgid);
						return -1;
					}
				}
				numread++;
				avail = rbuf.limit() - rbuf.position();
				System.out.println("avail = "+avail);
			}
			if (avail == 0) {
				rbuf.clear();
			} else {
				rbuf.compact();
			}
			return numread;

		} catch (Exception e) {
			System.out.println("handleRead Exception: " + e);
			e.printStackTrace();
			return -1;
		}
	}

	public int sendStringMsg(int msgid, String str)
	{
		try {
			int length = str.length() + 12;
			System.out.println("Length = "+length);
			wbuf.clear();
			wbuf.putInt(MARKER);
			wbuf.putInt(length);
			wbuf.putInt(msgid);
			wbuf.put(str.getBytes());
			wbuf.flip();
			int bytes = client.write(wbuf);
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
			wbuf.clear();
			wbuf.putInt(MARKER);
			wbuf.putInt(length);
			wbuf.putInt(msgid);
			wbuf.flip();
			int bytes = client.write(wbuf);

			Thread.sleep(100);

			wbuf.clear();
			wbuf.put(str.getBytes());
			wbuf.flip();
			bytes += client.write(wbuf);

			return bytes;
		} catch (Exception e) {
			System.out.println("sendJoin Exception: " + e);
			e.printStackTrace();
			return -1;
		}
	}

	//  decode the string from the read rbuf
	//  position in the rbuf will change to the end of the string
	public String decodeString(int len, int index)
	{
		byte arr[] = new byte[len];
		rbuf.position(rbuf.position()+index);
		rbuf.get(arr, 0, len);
		return new String(arr);

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

	public int closeConnection(String str)
	{
		if (str != null) {
			System.out.println("closing Connection: "+str);
			sendStringMsg(MSG_ERROR, str);
		}
		// free rbufs?
		try {
			client.close();
		} catch (Exception e) {
			System.out.println("socket close Exception: " + e);
			e.printStackTrace();
		}
		return 0;
	}
}
