package udpprobe.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random;

class UDPClient {
	final static int NUM_TRIALS = 20;

	public static void main(String args[]) throws IOException {
		DatagramSocket clientSocket1 = new DatagramSocket();
		DatagramSocket clientSocket2 = new DatagramSocket();
		double tput = 1.2*1024*1024;
		int bufKB = 26;
		int bsize = bufKB*1024*8;
		double probe = 2*bsize*1000/tput;
		boolean flip=false;
		clientSocket1.setSoTimeout(1000);
		int quantum = (int)(probe/200);
		int base = (int)(probe/100)+quantum;
		InetAddress IPAddress = InetAddress
				.getByName("glenstorm.iitd.ernet.in");
		byte[] receiveData = new byte[10240];
		int c = 0;
		for(int t = (int)probe; t>=base ; t -= quantum)
			c++;
		int[][] bufdraintime = new int[NUM_TRIALS][c];
		double[] estimate = new double[NUM_TRIALS];
		double sum = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		int count = 0;
		for (int trial = 0; trial < NUM_TRIALS; trial++) {
		for (int trial1 = 0; trial1 < NUM_TRIALS; trial1++) {
			for (int t = (int)probe, jj=0; t>=base ; t -= quantum, jj++) {
				//clientSocket1.setSoTimeout(t);
				flip = false;
				int id = new Random().nextInt(Integer.MAX_VALUE);
				for (int i = 0; i < bufKB; i++) {
					String sentence = "Method: ECHO\nId: " + id + "\nSeqno: "
							+ i + "\nLength: 1024";
					byte[] sendData = sentence.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, 9010);
					clientSocket1.send(sendPacket);
				}
				try {
    				// to sleep 10 seconds
				    Thread.sleep(t);
				} catch (InterruptedException e) {
    				// recommended because catching InterruptedException clears interrupt flag
    				Thread.currentThread().interrupt();
    				// you probably want to quit if the thread is interrupted
    				return;
				}
				for (int i = 0; i < bufKB; i++) {
					String sentence = "Method: ECHO\nId: " + id + "\nSeqno: "
							+ i + "\nLength: 1024";
					byte[] sendData = sentence.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, 9010);
					clientSocket1.send(sendPacket);
				}
				int recv = 0;
				for (int i = 0; i < 2*bufKB; i++) {
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);
					try {
						clientSocket1.receive(receivePacket);
					} catch (SocketTimeoutException e) {
						flip = true;
						recv = i+1;
						break;
					}
				}

					if(flip) // some packet lost
					{
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
						if(s.length-2!=recv)
							bufdraintime[trial][jj] += 1;
						//System.out.println("stat "+(s.length-2)+", recv "+i);
					}
			}
		}
		double bestr = 0;
		int bestarg = -1;
		for(int j=1; j<c; j++)
		{
			double meanl = 0, meanr = 0;
			for(int k=0; k<j; k++)
				meanl += bufdraintime[trial][k];
			for(int k=j; k<c; k++)
				meanr += bufdraintime[trial][k];
			double ratio = meanr*j/meanl/(c-j);
			if(ratio>0 && ratio>bestr)
			{
				bestr = ratio;
				bestarg = j;
			}
		}
		System.out.println("barg = "+bestarg);
		estimate[trial] = probe - bestarg*quantum;
		if(bestarg==0)
			count += 1;
		sum += probe - bestarg*quantum;
		if (min > probe - bestarg*quantum)
			min = probe - bestarg*quantum;
		if (max < probe - bestarg*quantum)
			max = probe - bestarg*quantum;
		}
		clientSocket1.close();
		clientSocket2.close();
		//System.out.println("count = "+count+"/"+NUM_TRIALS);
		double mean = ((double) sum) / NUM_TRIALS;
		double ss = 0;
		for (int i = 0; i < NUM_TRIALS; i++)
			ss += (estimate[i] - mean) * (estimate[i] - mean);
		double std = Math.sqrt(ss / NUM_TRIALS);
		System.out.println("BUFFER DRAIN TIME:\nAverage: " + mean + "\nMinimum: "
				+ min + "\nMaximum: " + max + "\nStandard Deviation: " + std
				+ "\n");
		for(int i=0; i<NUM_TRIALS; i++)
		{
			for(int j=0; j<c; j++)
			{
				System.out.print(""+bufdraintime[i][j]+" ");
			}
			System.out.println("");
		}
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
