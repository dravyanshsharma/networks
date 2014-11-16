package udpprobe.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
//270 46 23 22 16 15 9 16 7 11 18 9 14 12 11 12 10 17 14 16 11 10 8 6
public class downlink_q4b {
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
						for (int c = 0; c < i; c++) {
							try {
								h.remove(Integer.parseInt(echoResponse[c]
										.split("\n")[2].split(" ")[1]));
							} catch (NullPointerException n) {
								continue trials_loop;
							}
						}
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
