package udpprobe.client;

import java.io.*;
import java.net.*;

class UDPClient {
    public static void main(String args[]) throws Exception {
	DatagramSocket clientSocket = new DatagramSocket();
	InetAddress IPAddress = InetAddress.getByName("localhost");
	byte[] receiveData = new byte[10240];

	for(int id = 100; id < 110; id++) {	
	    for(int i = 0; i < 5; i++) {
		String sentence = "Method: ECHO\nId: " + id + "\nSeqno: " + i + "\nLength: 128";
		byte[] sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9010);
		clientSocket.send(sendPacket);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		String modifiedSentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
		System.out.println("FROM SERVER:" + modifiedSentence.length() + "\n" + modifiedSentence + "\n");
	    }
	    
	    String sentence = "Method: STAT\nId: " + id;
	    byte[] sendData = sentence.getBytes();
	    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9010);
	    clientSocket.send(sendPacket);
	    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	    clientSocket.receive(receivePacket);
	    String modifiedSentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
	    System.out.println("FROM SERVER:" + modifiedSentence.length() + "\n" + modifiedSentence + "\n");
	}

	clientSocket.close();
   }
}

