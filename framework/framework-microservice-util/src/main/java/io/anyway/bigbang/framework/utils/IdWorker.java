package io.anyway.bigbang.framework.utils;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Random;

class IdWorker {
    private final Long workerId;
    private final Long datacenterId;
    private final Long idepoch;

    private static final long workerIdBits = 5L;
    private static final long datacenterIdBits = 5L;
    private static final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private static final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    private static final long sequenceBits = 12L;
    private static final long workerIdShift = sequenceBits;
    private static final long datacenterIdShift = sequenceBits + workerIdBits;
    private static final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private static final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private Long lastTimestamp = -1L;
    private long sequence;
    private static final Random r = new Random();

    public IdWorker() {
        this(1344322705519L);
    }

    public IdWorker(long idepoch) {
        this(r.nextInt((int) maxWorkerId), r.nextInt((int) maxDatacenterId), 0, idepoch);
    }

    public IdWorker(long workerId, long datacenterId, long sequence) {
        this(workerId, datacenterId, sequence, 1344322705519L);
    }

    //
    public IdWorker(long workerId, long datacenterId, long sequence, long idepoch) {
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.sequence = sequence;
        this.idepoch = idepoch;
        if (workerId < 0 || workerId > maxWorkerId) {
            throw new IllegalArgumentException("workerId is illegal: " + workerId);
        }
        if (datacenterId < 0 || datacenterId > maxDatacenterId) {
            throw new IllegalArgumentException("datacenterId is illegal: " + workerId);
        }
        if (idepoch >= System.currentTimeMillis()) {
            throw new IllegalArgumentException("idepoch is illegal: " + idepoch);
        }
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public long getWorkerId() {
        return workerId;
    }

    public long getTime() {
        return System.currentTimeMillis();
    }

    public synchronized long getId() {
        return nextId();
    }

    public synchronized String getRadixId(){
        Long id= nextId();
//        return new StringBuffer()
//            .append(new BigInteger(workerId.toString()).toString(36).toUpperCase())
//            .append(sequence)
//            .append(new BigInteger(lastTimestamp.toString()).toString(36).toUpperCase()).toString();
        return new BigInteger(id.toString()).toString(36).toUpperCase(Locale.ROOT);
    }

    private long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards.");
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - idepoch) << timestampLeftShift)//
                | (datacenterId << datacenterIdShift)//
                | (workerId << workerIdShift)//
                | sequence;
        return id;
    }



    /**
     * get the timestamp (millis second) of id
     * @param id the nextId
     * @return the timestamp of id
     */
    public long getIdTimestamp(long id){
        return idepoch + (id >> timestampLeftShift);
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IdWorker{");
        sb.append("workerId=").append(workerId);
        sb.append(", datacenterId=").append(datacenterId);
        sb.append(", idepoch=").append(idepoch);
        sb.append(", lastTimestamp=").append(lastTimestamp);
        sb.append(", sequence=").append(sequence);
        sb.append('}');
        return sb.toString();
    }
}
