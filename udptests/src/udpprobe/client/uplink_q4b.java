package udpprobe.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class uplink_q4b {
	final static int NUM_TRIALS = 10000;

	public static void main(String[] args) throws IOException {
		InetAddress IPAddress = InetAddress
				.getByName("glenstorm.iitd.ernet.in");
		byte[] receiveData = new byte[10240];
		DatagramSocket clientSocket1 = new DatagramSocket();
		DatagramSocket clientSocket2 = new DatagramSocket();
		clientSocket1.setSoTimeout(1000);
		clientSocket2.setSoTimeout(1000);
		int dropped[] = new int[24];
		trials_loop: for (int trials = 0; trials < NUM_TRIALS; trials++) {
			int id = new Random().nextInt(Integer.MAX_VALUE);
			for (int i = 0; i < 24; i++) {
				String sentence = "Method: ECHO\nId: " + id + "\nSeqno: " + i
						+ "\nLength: 0";
				final char[] s = new char[5120 - sentence.length()];
				Arrays.fill(s, ' ');
				sentence = sentence + new String(s);
				byte[] sendData = sentence.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, IPAddress, 9010);
				clientSocket2.send(sendPacket);
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

			if (s.length - 2 == 24)
				continue trials_loop;
			else {
				HashSet<Integer> h = new HashSet<Integer>();
				for (int c = 0; c < 24; c++)
					h.add(c);
				for (int c = 2; c < s.length; c++) {
					try {
						String str = s[c].split(" ")[1];
						h.remove(Integer.parseInt(str.substring(0,
								str.length() - 1)));
					} catch (NullPointerException n) {
						continue trials_loop;
					}
				}
				Iterator<Integer> H = h.iterator();
				while (H.hasNext())
					dropped[H.next()]++;
			}
		}
		clientSocket1.close();
		clientSocket2.close();
		for (int i = 0; i < 24; i++)
			System.out.print(dropped[i] + " ");
		System.out.println();

	}

}
