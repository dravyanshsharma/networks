package udpprobe.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Random;

public class uplink_q1 {
	final static int NUM_TRIALS = 20;
	final static boolean datacollection = true;

	public static void main(String args[]) throws IOException {
		InetAddress IPAddress = InetAddress
				.getByName("glenstorm.iitd.ernet.in");
		byte[] receiveData = new byte[10240];
		DatagramSocket clientSocket = new DatagramSocket();
		clientSocket.setSoTimeout(1000);
		int[] bufsize = new int[NUM_TRIALS];
		int sum = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (int trial = 0; trial < NUM_TRIALS; trial++) {
			out: for (int k = 1; k <= 10; k++) {
				for (int j = 1; j <= 24; j++) {
					int id = new Random().nextInt(Integer.MAX_VALUE);
					for (int i = 0; i < j; i++) {
						String sentence = "Method: ECHO\nId: " + id
								+ "\nSeqno: " + i + "\nLength: 0";
						final char[] s = new char[1024 * k - sentence.length()];
						Arrays.fill(s, ' ');
						sentence = sentence + new String(s);
						byte[] sendData = sentence.getBytes();
						DatagramPacket sendPacket = new DatagramPacket(
								sendData, sendData.length, IPAddress, 9010);
						clientSocket.send(sendPacket);
					}
					String sentence = "Method: STAT\nId: " + id;
					byte[] sendData = sentence.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, 9010);
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					boolean flag;
					do {
						flag = false;
						try {
							clientSocket.send(sendPacket);
							clientSocket.receive(receivePacket);
						} catch (SocketTimeoutException e) {
							flag = true;
						}
					} while (flag);
					String response = new String(receivePacket.getData(), 0,
							receivePacket.getLength());
					String[] s = response.split("\n");
					if (s.length - 2 != j) {
						bufsize[trial] = k * (j - 1);
						if (datacollection)
							System.out.println(bufsize[trial]);
						sum += bufsize[trial];
						if (min > bufsize[trial])
							min = bufsize[trial];
						if (max < bufsize[trial])
							max = bufsize[trial];
						break out;
					}
				}
			}
		}
		clientSocket.close();
		double mean = ((double) sum) / NUM_TRIALS;
		double ss = 0;
		for (int i = 0; i < NUM_TRIALS; i++)
			ss += (bufsize[i] - mean) * (bufsize[i] - mean);
		double std = Math.sqrt(ss / NUM_TRIALS);
		System.out.println("UPLINK BUFFER SIZE:\nAverage: " + mean
				+ "\nMinimum: " + min + "\nMaximum: " + max
				+ "\nStandard Deviation: " + std + "\n");
	}
}
