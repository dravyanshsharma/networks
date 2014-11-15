package udpprobe.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random;

class downlink {
	final static int NUM_TRIALS = 1;

	public static void main(String args[]) throws IOException {
		InetAddress IPAddress = InetAddress
				.getByName("glenstorm.iitd.ernet.in");
		byte[] receiveData = new byte[10240];
		int[] bufsize = new int[NUM_TRIALS];
		// //////////////////// QUESTION 1 //////////////////////
		DatagramSocket clientSocket1 = new DatagramSocket();
		DatagramSocket clientSocket2 = new DatagramSocket();
		clientSocket1.setSoTimeout(1000);
		clientSocket2.setSoTimeout(1000);
		int sum = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (int trial = 0; trial < NUM_TRIALS; trial++) {
			out: for (int j = 1;; j++) {
				int id = new Random().nextInt(Integer.MAX_VALUE);
				for (int i = 0; i < j; i++) {
					String sentence = "Method: ECHO\nId: " + id + "\nSeqno: "
							+ i + "\nLength: 1024";
					byte[] sendData = sentence.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, 9010);
					clientSocket1.send(sendPacket);
				}
				for (int i = 0; i < j; i++) {
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					try {
						clientSocket1.receive(receivePacket);
					} catch (SocketTimeoutException e) {
						String sentence = "Method: STAT\nId: " + id;
						byte[] sendData = sentence.getBytes();
						DatagramPacket sendPacket = new DatagramPacket(
								sendData, sendData.length, IPAddress, 9010);
						receivePacket = new DatagramPacket(receiveData,
								receiveData.length);
						boolean flag;
						do {
							flag = false;
							try {
								clientSocket2.send(sendPacket);
								clientSocket2.receive(receivePacket);
							} catch (SocketTimeoutException e1) {
								flag = true;
							}
						} while (flag);
						String modifiedSentence = new String(
								receivePacket.getData(), 0,
								receivePacket.getLength());
						String[] s = modifiedSentence.split("\n");
						if (s.length - 3 == i)
							continue out;
						else {
							bufsize[trial] = j - 1;
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
		}
		clientSocket1.close();
		clientSocket2.close();
		double mean = ((double) sum) / NUM_TRIALS;
		double ss = 0;
		for (int i = 0; i < NUM_TRIALS; i++)
			ss += (bufsize[i] - mean) * (bufsize[i] - mean);
		double std = Math.sqrt(ss / NUM_TRIALS);
		System.out.println("DOWNLINK BUFFER SIZE:\nAverage: " + mean
				+ "\nMinimum: " + min + "\nMaximum: " + max
				+ "\nStandard Deviation: " + std + "\n");
		// //////////////////// QUESTION 4 //////////////////////
		clientSocket1 = new DatagramSocket();
		clientSocket2 = new DatagramSocket();
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
					break;
				}
			} catch (SocketTimeoutException e) {
			}
		}
		System.out.println("PER SOURCE BUFFER FOUND\n");
		clientSocket1.close();
		clientSocket2.close();
	}
}
