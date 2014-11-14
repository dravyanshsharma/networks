#!/usr/bin/bash
bash build.sh
java -cp "lib/java-getopt-1.0.13.jar:lib/commons-collections-3.2.1.jar:lib/log4j-1.2.16.jar:lib/expiringcache.jar:build" udpprobe.client.downlink
java -cp "lib/java-getopt-1.0.13.jar:lib/commons-collections-3.2.1.jar:lib/log4j-1.2.16.jar:lib/expiringcache.jar:build" udpprobe.client.uplink

