package udpprobe.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Random;

public class uplink_q4a {
	final static int ETA = 3;
	final static boolean datacollection = true;

	public static void main(String[] args) throws IOException {
		InetAddress IPAddress = InetAddress
				.getByName("glenstorm.iitd.ernet.in");
		byte[] receiveData = new byte[10240];
		DatagramSocket clientSocket1 = new DatagramSocket();
		DatagramSocket clientSocket2 = new DatagramSocket();
		clientSocket1.setSoTimeout(1000);
		clientSocket2.setSoTimeout(1000);
		int id = new Random().nextInt(Integer.MAX_VALUE);
		for (int i = 0; i < 24; i++) {
			String sentence = "Method: ECHO\nId: " + id + "\nSeqno: " + i
					+ "\nLength: 0";
			byte[] sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, 9010);
			clientSocket1.send(sendPacket);
		}
		String sentence = "Method: STAT\nId: " + id;
		byte[] sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, IPAddress, 9010);
		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);
		boolean flag;
		do {
			flag = false;
			try {
				clientSocket1.send(sendPacket);
				clientSocket1.receive(receivePacket);
			} catch (SocketTimeoutException e) {
				flag = true;
			}
		} while (flag);
		String response = new String(receivePacket.getData(), 0,
				receivePacket.getLength());
		String[] s = response.split("\n");
		double delta_unthrottled = (Double.parseDouble(s[s.length - 1]
				.split(" ")[2]) - Double.parseDouble(s[3].split(" ")[2]))
				/ (s.length - 2);
		id = new Random().nextInt(Integer.MAX_VALUE);
		int id1 = new Random().nextInt(Integer.MAX_VALUE);
		for (int i = 0; i < 24; i++) {
			sentence = "Method: ECHO\nId: " + id + "\nSeqno: " + i
					+ "\nLength: 0";
			final char[] s1 = new char[10240 - sentence.length()];
			Arrays.fill(s1, ' ');
			sentence = sentence + new String(s1);
			sendData = sentence.getBytes();
			sendPacket = new DatagramPacket(sendData, sendData.length,
					IPAddress, 9010);
			clientSocket2.send(sendPacket);
			sentence = "Method: ECHO\nId: " + id1 + "\nSeqno: " + i
					+ "\nLength: 0";
			sendData = sentence.getBytes();
			sendPacket = new DatagramPacket(sendData, sendData.length,
					IPAddress, 9010);
			clientSocket1.send(sendPacket);
		}
		sentence = "Method: STAT\nId: " + id1;
		sendData = sentence.getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress,
				9010);
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		do {
			flag = false;
			try {
				clientSocket1.send(sendPacket);
				clientSocket1.receive(receivePacket);
			} catch (SocketTimeoutException e) {
				flag = true;
			}
		} while (flag);
		response = new String(receivePacket.getData(), 0,
				receivePacket.getLength());
		s = response.split("\n");
		double delta_throttled = (Double
				.parseDouble(s[s.length - 1].split(" ")[2]) - Double
				.parseDouble(s[3].split(" ")[2]))
				/ (s.length - 2);
		if (datacollection) {
			System.out.println(delta_throttled);
			System.out.println(delta_unthrottled);
		}
		if (delta_throttled > ETA * delta_unthrottled)
			System.out.println("SHARED UPLINK BUFFER FOUND\n");
		else
			System.out.println("PER SOURCE UPLINK BUFFER FOUND\n");

		clientSocket1.close();
		clientSocket2.close();

	}

}
