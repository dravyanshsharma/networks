package udpprobe.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random;

public class pairpacket_blocksize {
	final static int NUM_TRIALS = 100;
	final static boolean datacollection = true;

	public static void main(String args[]) throws IOException {
		InetAddress IPAddress = InetAddress
				.getByName("glenstorm.iitd.ernet.in");
		byte[] receiveData = new byte[10240];
		int[] bufsize = new int[NUM_TRIALS];

		DatagramSocket clientSocket1 = new DatagramSocket();
		DatagramSocket clientSocket2 = new DatagramSocket();
		clientSocket1.setSoTimeout(1000);
		clientSocket2.setSoTimeout(1000);
		int sum = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, count = 0;
		for (int trial = 0; trial < NUM_TRIALS; trial++) {
			out: for (int j = 1; j <= 24; j++) {
					int id = new Random().nextInt(Integer.MAX_VALUE);
					for (int i = 0; i < j; i++) {
						String sentence = "Method: ECHO\nId: " + id
								+ "\nSeqno: " + i + "\nLength: " + 128;
						byte[] sendData = sentence.getBytes();
						DatagramPacket sendPacket = new DatagramPacket(
								sendData, sendData.length, IPAddress, 9010);
						clientSocket1.send(sendPacket);
					}
							String sentence = "Method: STAT\nId: " + id;
							byte[] sendData = sentence.getBytes();
							DatagramPacket sendPacket = new DatagramPacket(
									sendData, sendData.length, IPAddress, 9010);
							DatagramPacket receivePacket = new DatagramPacket(receiveData,
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
							int inorder = 0;
							for(; inorder<s.length-2; inorder++)
							{
								String[] s1 = s[inorder+2].split(",");
								String[] s2 = s1[0].split(" ");
								int temp = Integer.parseInt(s2[1]);
								if(temp!=inorder)
									break;
							}
							if (s.length - 2 == inorder)
								continue;
							else { count += 1;
								bufsize[trial] = (j - 1);
								sum += bufsize[trial];
								if (min > bufsize[trial])
									min = bufsize[trial];
								if (max < bufsize[trial])
									max = bufsize[trial];
								if (datacollection)
									System.out.println(bufsize[trial]);
								break out;
							}
				}
		}
		clientSocket1.close();
		clientSocket2.close();
		double mean = ((double) sum) / count;
		double ss = 0;
		for (int i = 0; i < NUM_TRIALS; i++)
			if(bufsize[i]>0)
				ss += (bufsize[i] - mean) * (bufsize[i] - mean);
		double std = Math.sqrt(ss / count);
		System.out.println("BLOCK SIZE:\nAverage: " + mean
				+ "\nMinimum: " + min + "\nMaximum: " + max
				+ "\nStandard Deviation: " + std + "\n");
	}
}


/*

BLOCK SIZE:
Average: 13.833333333333334
Minimum: 12
Maximum: 18
Standard Deviation: 2.1147629234082532

*/
