package udpprobe.server;

import gnu.getopt.Getopt;
import com.vipan.util.*;

import java.io.*;
import java.net.*;

class UDPServer {
    int port = 9010;  // default port
    public int ECHO = 1;
    public int STAT = 2;

    ExpiringCache cache;

    public UDPServer(int port) {
	if(port > 0) {
	    this.port = port;
	}

	// ttl, access timeout, max quantity, timer
	cache = new ExpiringCache(30*60*1000, 5*60*1000, 1000, 2*60*1000);	
    }

    public static void main(String args[]) throws Exception {
	Getopt g = new Getopt("server", args, "p:");
	int c;
	int port = -1;
	
	while((c = g.getopt()) != -1) {
	    switch(c) {
	    case 'p':
		port = Integer.parseInt(g.getOptarg());
		break;
	    }
	}

	(new UDPServer(port)).start();
    }

    public void start() throws Exception {
	DatagramSocket serverSocket = new DatagramSocket(port);
	byte[] receiveData = null;
	byte[] sendData = null;
	ProbeStats probeStats = null;
	long currentTimeMillis = -1;
	String id = null;

	while(true) {
	    receiveData = new byte[10240];
	    sendData = null;
	    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	    serverSocket.receive(receivePacket);
	    
	    InetAddress clientIpAddress = receivePacket.getAddress();
	    int clientPort = receivePacket.getPort();
	    UDPProbe udpProbe = parseRequest(receivePacket.getData());
	    id = Long.toString(udpProbe.id());
	    if(udpProbe.command() == ECHO) {
		if((probeStats = (ProbeStats)(cache.recover(id))) == null) {
		    probeStats = new ProbeStats(id);
		    cache.admit(id, probeStats);
		}
		currentTimeMillis = System.currentTimeMillis();
		probeStats.addPacket(udpProbe.seqno(), currentTimeMillis);
		if(udpProbe.length() > 0) {
		    sendData = createEchoResponse(udpProbe.id(), udpProbe.seqno(), udpProbe.length(), currentTimeMillis);
		}
	    } else if(udpProbe.command() == STAT) {
		sendData = createStatResponse(id);
	    }

	    if(sendData != null) {
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIpAddress, clientPort);
		serverSocket.send(sendPacket);
	    }
	}
    }

    protected UDPProbe parseRequest(byte[] receiveData) {
	int command = ECHO;
	long id = -1;
	long seqno = -1;
	int length = 1024;
	
	String[] headers = (new String(receiveData)).split("\n");
	for(int i = 0; i < headers.length; i++) {
	    String[] commandStr = headers[i].split(":");
	    if(commandStr.length == 2) {
		commandStr[0] = commandStr[0].trim();
		commandStr[1] = commandStr[1].trim();
		if(commandStr[0].equals("Method")) {
		    if(commandStr[1].equals("ECHO")) {
			command = ECHO;
		    } else if(commandStr[1].equals("STAT")) {
			command = STAT;
		    }
		} else if(commandStr[0].equals("Id")) {
		    try { id = Long.parseLong(commandStr[1]); } catch(Exception ex) { }
		} else if(commandStr[0].equals("Seqno")) {
		    try { seqno = Long.parseLong(commandStr[1]); } catch(Exception ex) { }
		} else if(commandStr[0].equals("Length")) {
		    try { length = Integer.parseInt(commandStr[1]); } catch(Exception ex) { } 
		}
	    }
	}

	System.out.println("Received UDP Probe: Method = " + command + 
			   ", Id = " + id + 
			   ", seqno = " + seqno + 
			   ", length = " + length);

	return new UDPProbe(command, id, seqno, length);
    }

    protected byte[] createEchoResponse(long id, long seqno, int length, long time) {
	String responseString = "Method: ECHO\nId: " + id + "\nSeqno: " + seqno + "\nTime: " + time + "\n";
	length = Math.max(responseString.length(), length);
	byte[] intResponseData = responseString.getBytes();
	byte[] responseData = new byte[length];
	for(int i = 0; i < intResponseData.length; i++) {
	    responseData[i] = intResponseData[i];
	}
	for(int i = intResponseData.length; i < length; i++) {
	    responseData[i] = ' ';
	}
	
	return responseData;
    }

    protected byte[] createStatResponse(String id) {
	String responseString = "Method: STAT\nId: " + id + "\n";
	ProbeStats probeStats = (ProbeStats)(cache.recover(id));
	if(probeStats != null) {
	    responseString += probeStats.getStatsString();
	}

	return responseString.trim().getBytes();
    }

    class UDPProbe {
	int command = ECHO;
	long id = -1;
	long seqno = -1;
	int length = 1024;

	public UDPProbe(int command, long id, long seqno, int length) {
	    this.command = command;
	    this.id = id;
	    this.seqno = seqno;
	    this.length = length;
	}

	public int command() {
	    return command;
	}

	public long id() {
	    return id;
	}
	
	public long seqno() {
	    return seqno;
	}

	public int length() {
	    return length;
	}
    }
}