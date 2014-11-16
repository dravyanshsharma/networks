package udpprobe.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random;

public class downlink_q4a {

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
					+ "\nLength: 128";
			byte[] sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, 9010);
			if (i % 2 == 0)
				clientSocket1.send(sendPacket);
			else
				clientSocket2.send(sendPacket);
		}
		boolean flag = false;
		for (int i = 0; i < 24 && !flag; i++) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			try {
				clientSocket1.receive(receivePacket);
				String modifiedSentence = new String(receivePacket.getData(),
						0, receivePacket.getLength());
				int seq = Integer.parseInt(modifiedSentence.split("\n")[2]
						.split(" ")[1]);
				if (seq % 2 != 0) {
					System.out.println("SHARED BUFFER FOUND\n");
					flag = true;
				}
			} catch (SocketTimeoutException e) {
			}
		}
		for (int i = 0; i < 24 && !flag; i++) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			try {
				clientSocket2.receive(receivePacket);
				String modifiedSentence = new String(receivePacket.getData(),
						0, receivePacket.getLength());
				int seq = Integer.parseInt(modifiedSentence.split("\n")[2]
						.split(" ")[1]);
				if (seq % 2 == 0) {
					System.out.println("SHARED BUFFER FOUND\n");
					flag = true;
				}
			} catch (SocketTimeoutException e) {
			}
		}
		if (!flag)
			System.out.println("PER SOURCE BUFFER FOUND\n");
		clientSocket1.close();
		clientSocket2.close();

	}

}
