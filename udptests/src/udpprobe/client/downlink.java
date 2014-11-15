package udpprobe.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

class downlink {
	final static int NUM_TRIALS = 20;

	public static void main(String args[]) throws IOException {
		InetAddress IPAddress = InetAddress
				.getByName("glenstorm.iitd.ernet.in");
		byte[] receiveData = new byte[10240];
		int[] bufsize = new int[NUM_TRIALS];

		// ///////////////////// QUESTION 1 //////////////////////
		DatagramSocket clientSocket1 = new DatagramSocket();
		DatagramSocket clientSocket2 = new DatagramSocket();
		clientSocket1.setSoTimeout(1000);
		clientSocket2.setSoTimeout(1000);
		int sum = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (int trial = 0; trial < NUM_TRIALS; trial++) {
			for (int k = 1; k <= 10; k++) {
				out: for (int j = 1; j <= 24; j++) {
					int id = new Random().nextInt(Integer.MAX_VALUE);
					for (int i = 0; i < j; i++) {
						String sentence = "Method: ECHO\nId: " + id
								+ "\nSeqno: " + i + "\nLength: " + 1024 * k;
						byte[] sendData = sentence.getBytes();
						DatagramPacket sendPacket = new DatagramPacket(
								sendData, sendData.length, IPAddress, 9010);
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
							String statResponse = new String(
									receivePacket.getData(), 0,
									receivePacket.getLength());
							String[] s = statResponse.split("\n");
							if (s.length - 3 == i)
								continue out;
							else {
								bufsize[trial] = (j - 1) * k;
								sum += bufsize[trial];
								if (min > bufsize[trial])
									min = bufsize[trial];
								if (max < bufsize[trial])
									max = bufsize[trial];
								System.out.println(bufsize[trial]);
								break out;
							}
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

		// //////////////////// QUESTION 4(a) //////////////////////
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
					flag = true;
				}
			} catch (SocketTimeoutException e) {
			}
		}
		System.out.println("PER SOURCE BUFFER FOUND\n");
		clientSocket1.close();
		clientSocket2.close();

		// //////////////////// QUESTION 4(b) //////////////////////
		clientSocket1 = new DatagramSocket();
		clientSocket2 = new DatagramSocket();
		clientSocket1.setSoTimeout(1000);
		clientSocket2.setSoTimeout(1000);
		int dropped[] = new int[24];
		trials_loop: for (int trials = 0; trials < NUM_TRIALS; trials++) {
			id = new Random().nextInt(Integer.MAX_VALUE);
			for (int i = 0; i < 24; i++) {
				String sentence = "Method: ECHO\nId: " + id + "\nSeqno: " + i
						+ "\nLength: 5120";
				byte[] sendData = sentence.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, IPAddress, 9010);
				clientSocket1.send(sendPacket);
			}
			String echoResponse[] = new String[24];
			for (int i = 0; i < 24; i++) {
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				try {
					clientSocket1.receive(receivePacket);
					echoResponse[i] = new String(receivePacket.getData(), 0,
							receivePacket.getLength());
				} catch (SocketTimeoutException e) {
					String sentence = "Method: STAT\nId: " + id;
					byte[] sendData = sentence.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, 9010);
					receivePacket = new DatagramPacket(receiveData,
							receiveData.length);
					do {
						flag = false;
						try {
							clientSocket2.send(sendPacket);
							clientSocket2.receive(receivePacket);
						} catch (SocketTimeoutException e1) {
							flag = true;
						}
					} while (flag);
					String statResponse = new String(receivePacket.getData(),
							0, receivePacket.getLength());
					String[] s = statResponse.split("\n");
					if (s.length - 3 == 24)
						continue trials_loop;
					else {
						HashSet<Integer> h = new HashSet<Integer>();
						int[] seqs = new int[s.length - 2];
						for (int c = 2; c < s.length; c++) {
							String str = s[c].split(" ")[1];
							seqs[c - 2] = Integer.parseInt(str.substring(0,
									str.length() - 1));
							h.add(seqs[c - 2]);
						}
						for (int c = 0; c < i; c++)
							h.remove(Integer.parseInt(echoResponse[c]
									.split("\n")[2].split(" ")[1]));
						Iterator<Integer> H = h.iterator();
						while (H.hasNext())
							dropped[H.next()]++;
					}
				}
			}
		}
		clientSocket1.close();
		clientSocket2.close();
		for (int i = 0; i < 24; i++)
			System.out.print(dropped[i] + " ");
	}
}
