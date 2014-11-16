	package udpprobe.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random;

class pairpacket_a_b {
	final static int NUM_TRIALS = 50;

	public static void main(String args[]) throws IOException {
		DatagramSocket clientSocket1 = new DatagramSocket();
		DatagramSocket clientSocket2 = new DatagramSocket();
		clientSocket1.setSoTimeout(1000);
		InetAddress IPAddress = InetAddress
				.getByName("glenstorm.iitd.ernet.in");
		byte[] receiveData = new byte[10240];
		double capacity = 1;
		double[] estimate = new double[NUM_TRIALS*10];
		double[] estimate1 = new double[NUM_TRIALS];
		double sum = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		int count = 0;
		for (int trial = 0; trial < NUM_TRIALS; trial++) {
			for (int jj=1; jj<11 ; jj++) {
				//clientSocket1.setSoTimeout(t);
				int id = new Random().nextInt(Integer.MAX_VALUE);
				for (int i = 0; i < jj; i++) {
					String sentence = "Method: ECHO\nId: " + id + "\nSeqno: "
							+ i + "\nLength: 1024";
					byte[] sendData = sentence.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, 9010);
					clientSocket1.send(sendPacket);
				}
				try {
				    Thread.sleep(100);
				} catch (InterruptedException e) {
    				// recommended because catching InterruptedException clears interrupt flag
    				Thread.currentThread().interrupt();
    				// you probably want to quit if the thread is interrupted
    				return;
				}
				for (int i = 0; i < jj; i++) {
					String sentence = "Method: ECHO\nId: " + (id+1) + "\nSeqno: "
							+ i + "\nLength: 1024";
					byte[] sendData = sentence.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, 9010);
					clientSocket1.send(sendPacket);
				}

						String sentence = "Method: STAT\nId: " + id;
						byte[] sendData = sentence.getBytes();
						DatagramPacket sendPacket = new DatagramPacket(
								sendData, sendData.length, IPAddress, 9010);
						clientSocket2.send(sendPacket);
						DatagramPacket receivePacket = new DatagramPacket(receiveData,
								receiveData.length);
						clientSocket2.receive(receivePacket);
						String modifiedSentence = new String(
								receivePacket.getData(), 0,
								receivePacket.getLength());
						String[] s = modifiedSentence.split("\n");
						String[] s1 = s[s.length-1].split(" ");

						String sentence1 = "Method: STAT\nId: " + (id+1);
						byte[] sendData1 = sentence1.getBytes();
						DatagramPacket sendPacket1 = new DatagramPacket(
								sendData1, sendData1.length, IPAddress, 9010);
						clientSocket2.send(sendPacket1);
						DatagramPacket receivePacket1 = new DatagramPacket(receiveData,
								receiveData.length);
						clientSocket2.receive(receivePacket1);
						String modifiedSentence1 = new String(
								receivePacket1.getData(), 0,
								receivePacket1.getLength());
						String[] ss = modifiedSentence1.split("\n");
						String[] ss1 = ss[ss.length-1].split(" ");

						int delta = (int)(Long.parseLong(ss1[2])-Long.parseLong(s1[2]));
						
						if(capacity*(2-delta/100.0)>0)
						{	count++; estimate[trial*10+jj-1] = capacity*(2-delta/100.0);
						if(jj==1)
							estimate1[trial] = delta;
						sum += estimate[trial*10+jj-1];
						if (min > estimate[trial*10+jj-1])
							min = estimate[trial*10+jj-1];
						if (max < estimate[trial*10+jj-1])
							max = estimate[trial*10+jj-1];
						}

						
						//System.out.println("stat "+(s.length-2)+", recv "+i);
			}
		}
		clientSocket1.close();
		clientSocket2.close();
		//System.out.println("count = "+count+"/"+NUM_TRIALS);
		double mean = ((double) sum) / count;
		double ss = 0;
		for (int i = 0; i < NUM_TRIALS*10; i++)
			if(estimate[i]>0)
				ss += (estimate[i] - mean) * (estimate[i] - mean);
		double std = Math.sqrt(ss / count);	
		System.out.println("RELATIVE AVAILABLE CAPACITY:\nAverage: " + mean + "\nMinimum: "
				+ min + "\nMaximum: " + max + "\nStandard Deviation: " + std
				+ "\n");
		System.out.print("Variation for fixed inter-dispatch time: ");
		for(int i=0; i<NUM_TRIALS;i++)
		{
			System.out.print(estimate1[i]+" ");
		}System.out.println("");

	}
}


/*
RELATIVE AVAILABLE CAPACITY:
Average: 0.9899500000000009
Minimum: 0.79
Maximum: 1.08
Standard Deviation: 0.030487661438686982

Variation for fixed inter-dispatch time: 100.0 101.0 101.0 101.0 101.0 100.0 101.0 100.0 100.0 100.0 99.0 101.0 101.0 97.0 101.0 101.0 100.0 100.0 100.0 100.0
*/
