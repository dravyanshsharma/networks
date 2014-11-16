	package udpprobe.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random;

class updown_q3 {
	final static int NUM_TRIALS = 50;

	public static void main(String args[]) throws IOException {
		DatagramSocket clientSocket1 = new DatagramSocket();
		DatagramSocket clientSocket2 = new DatagramSocket();
		double tput = 1.2*1024*1024;
		int bufKB = 30;
		int bsize = bufKB*1024*8;
		double probe = bsize*1000/tput;
		boolean flip=false;
		clientSocket1.setSoTimeout((int)probe);
		InetAddress IPAddress = InetAddress
				.getByName("glenstorm.iitd.ernet.in");
		byte[] receiveData = new byte[10240];
		double[] timediffup = new double[NUM_TRIALS];
		double[] timediffdown = new double[NUM_TRIALS];
		double sum = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		double sum1 = 0, min1 = Double.MAX_VALUE, max1 = Double.MIN_VALUE;
		int count = 0;
		for (int trial = 0; trial < NUM_TRIALS; trial++) {
			for (int j = bufKB/2; j<=bufKB/2 ; j++) {
				flip = false;
				int id = new Random().nextInt(Integer.MAX_VALUE);
				for (int i = 0; i < j; i++) {
					String sentence = "Method: ECHO\nId: " + id + "\nSeqno: "
							+ i + "\nLength: 1024";
					byte[] sendData = sentence.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, 9010);
					clientSocket1.send(sendPacket);
				}
				long start = System.nanoTime(), time;
				DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
				try {
						clientSocket1.receive(receivePacket);
					} catch (SocketTimeoutException e) {
						flip = true;
					}
				for (int i = 1; i < j ; i++) {
					try {
						clientSocket1.receive(receivePacket);
					} catch (SocketTimeoutException e) {
						flip = true;
						break;
					}
				}

				if(flip)
					break;
				time = System.nanoTime()-start;
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
				String[] s1 = s[2].split(" ");
				String[] s2 = s[s.length-1].split(" ");
				timediffup[trial] = (int)(Long.parseLong(s2[s2.length-1])-Long.parseLong(s1[s1.length-1]))*bufKB/(j-1);
				timediffdown[trial] = (((double)time)/1000/1000)*bufKB/(j-1)-timediffup[trial];			// down packets can get reordered; compare time diff of first and last sent from server
				if(timediffdown[trial]<0)
				{
					timediffdown[trial] = 0;
					timediffup[trial] = 0;
					break;
				}
				count += 1;
				sum += timediffup[trial];
				if (min > timediffup[trial])
					min = timediffup[trial];
				if (max < timediffup[trial])
					max = timediffup[trial];
				sum1 += timediffdown[trial];
				if (min1 > timediffdown[trial])
					min1 = timediffdown[trial];
				if (max1 < timediffdown[trial])
					max1 = timediffdown[trial];
			}
		}
		clientSocket1.close();
		clientSocket2.close();
		double mean = ((double) sum) / count;
		double ss = 0;
		for (int i = 0; i < NUM_TRIALS; i++)
			if(timediffup[i]>0)
				ss += (timediffup[i] - mean) * (timediffup[i] - mean);
		double std = Math.sqrt(ss / count);
		System.out.println("TIME DIFF UP:\nAverage: " + mean + "\nMinimum: "
				+ min + "\nMaximum: " + max + "\nStandard Deviation: " + std
				+ "\n");
		double mean1 = ((double) sum1) / count;
		double ss1 = 0;
		for (int i = 0; i < NUM_TRIALS; i++)
			if(timediffdown[i]>0)
				ss1 += (timediffdown[i] - mean1) * (timediffdown[i] - mean1);
		double std1 = Math.sqrt(ss1 / count);
		System.out.println("TIME DIFF DOWN:\nAverage: " + mean1 + "\nMinimum: "
				+ min1 + "\nMaximum: " + max1 + "\nStandard Deviation: " + std1
				+ "\n");
	}
}


/*
TIME DIFF UP:
Average: 20.127659574468087
Minimum: 17.0
Maximum: 45.0
Standard Deviation: 4.310388348385522

TIME DIFF DOWN:
Average: 3.239015714285714
Minimum: 0.763930714285717
Maximum: 7.597946428571429
Standard Deviation: 1.2967444835966078


*/
