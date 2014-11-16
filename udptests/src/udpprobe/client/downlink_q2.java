	package udpprobe.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random;

class downlink_q2 {
	final static int NUM_TRIALS = 20;

	public static void main(String args[]) throws IOException {
		DatagramSocket clientSocket1 = new DatagramSocket();
		DatagramSocket clientSocket2 = new DatagramSocket();
		double tput = 1.2*1024*1024;
		int bufKB = 15;
		int bsize = bufKB*1024*8;
		double probe = bsize*1000/tput;
		boolean flip=false;
		clientSocket1.setSoTimeout((int)probe);
		InetAddress IPAddress = InetAddress
				.getByName("glenstorm.iitd.ernet.in");
		byte[] receiveData = new byte[10240];
		double[] bufdraintime = new double[NUM_TRIALS];
		double sum = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		int count = 0;
		for (int trial = 0; trial < NUM_TRIALS; trial++) {
			for (int j = 5; j<=24 ; j++) {
				flip = false;
				int id = new Random().nextInt(Integer.MAX_VALUE);
				for (int i = 0; i < j; i++) {
					String sentence = "Method: ECHO\nId: " + id + "\nSeqno: "
							+ i + "\nLength: 5120";
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
						clientSocket2.send(sendPacket);
						receivePacket = new DatagramPacket(receiveData,
								receiveData.length);
						clientSocket2.receive(receivePacket);
						String modifiedSentence = new String(
								receivePacket.getData(), 0,
								receivePacket.getLength());
						String[] s = modifiedSentence.split("\n");
						System.out.println("stat "+(s.length-2)+", recv "+i);
						flip = true;
						// if (s.length - 3 == j-1)
						// 	continue out;
						// else {
							bufdraintime[trial] = ((int)probe)*((double)(s.length-3)*5)/1000;
							count += 1;
							sum += bufdraintime[trial];
							if (min > bufdraintime[trial])
								min = bufdraintime[trial];
							if (max < bufdraintime[trial])
								max = bufdraintime[trial];
						//	break out;
						// }
					}
						//break out;
					if(flip)
						break;
				}

					if(flip)
						break;
			}
		}
		clientSocket1.close();
		clientSocket2.close();
		double mean = ((double) sum) / count;
		double ss = 0;
		for (int i = 0; i < NUM_TRIALS; i++)
			if(bufdraintime[i]>0)
				ss += (bufdraintime[i] - mean) * (bufdraintime[i] - mean);
		double std = Math.sqrt(ss / count);
		System.out.println("BUFFER DRAIN TIME:\nAverage: " + mean + "\nMinimum: "
				+ min + "\nMaximum: " + max + "\nStandard Deviation: " + std
				+ "\n");
	}
}

/*
BUFFER DRAIN TIME:
Average: 7.275
Minimum: 1.94
Maximum: 10.67
Standard Deviation: 3.182822123001326


1000000000000111110
0010001000100001100
0000001100101010000
0011101110101010000
0001010100000001000
1011000100000011000
0101010000011000101
1001000010000100101
1001000000000000101
1000000100100000010
0001000000000000000
0000100100001000000
0010110100001001100
0000100000011000011
0001110010010000010
0010100100100100000
0000000000011010111
1001000101100010000
1010100101000101100
1000001000100010010

8169744(10)32747476865

*/
