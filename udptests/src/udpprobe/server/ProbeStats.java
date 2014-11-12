package udpprobe.server;

public class ProbeStats {
    String id;
    int count;
    long[] seqnos;
    long[] responseTimes;

    public ProbeStats(String id) {
	this.id = id;
	this.count = 0;
	this.seqnos = new long[24];
	this.responseTimes = new long[24];
    }

    public void addPacket(long seqno, long responseTime) {
	if(count < 24) {
	    seqnos[count] = seqno;
	    responseTimes[count] = responseTime;
	    count++;
	}
    }

    public String getStatsString() {
	StringBuilder str = new StringBuilder();
	for(int i = 0; i < count; i++) {
	    str.append("RESP: ");
	    str.append(seqnos[i]);
	    str.append(", ");
	    str.append(responseTimes[i]);
	    str.append("\n");
	}
	return str.toString();
    }

}