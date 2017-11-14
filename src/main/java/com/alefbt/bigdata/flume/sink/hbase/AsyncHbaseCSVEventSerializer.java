package com.alefbt.bigdata.flume.sink.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.conf.ComponentConfiguration;
import org.apache.flume.sink.hbase.AsyncHbaseEventSerializer;
import org.hbase.async.AtomicIncrementRequest;
import org.hbase.async.PutRequest;

/**
 * 
 * @author yehuda
 *
 */
public class AsyncHbaseCSVEventSerializer implements AsyncHbaseEventSerializer {
	private byte[] table;
	private byte[] columnFamily;

	private Event currentEvent;

	// private byte[][] columnNames;

	private final List<PutRequest> puts = new ArrayList<PutRequest>();
	private final List<AtomicIncrementRequest> incs = new ArrayList<AtomicIncrementRequest>();
	private String[] columnNames = null;
	private String[] columnKey = null;

	public void initialize(byte[] table, byte[] cf) {
		this.table = table;
		this.columnFamily = cf;
	}

	public void setEvent(Event event) {
		this.currentEvent = event;
	}

	public List<PutRequest> getActions() {
		String eventStr = new String(currentEvent.getBody());

		puts.clear();
		if (eventStr != null && !eventStr.isEmpty()) {
			try {
				CSVParser parser = CSVParser.parse(eventStr, CSVFormat.DEFAULT.withHeader(columnNames));

				for (CSVRecord record : parser) {
					byte[] recordKey = createRecordKey(record);
					for (Entry<String, String> recordEntry : record.toMap().entrySet()) {
						PutRequest req = new PutRequest(table, recordKey, columnFamily, recordEntry.getKey().getBytes(),
								recordEntry.getValue().getBytes());
						puts.add(req);
					}

					parser.close();
				}
			} catch (IOException e) {
				throw new FlumeException("ERROR: Cannot parse csv string ! csv=>>> " + eventStr);
			}
		}
		return puts;
	}

	private byte[] createRecordKey(CSVRecord record) {
		String retVal = "";

		if (columnKey.length == 0)
			retVal = UUID.randomUUID().toString();
		else {
			for (String col : columnKey) {
				retVal += record.get(col) + ":";
			}

			// Remove last ":"
			retVal = retVal.substring(0, retVal.length() - 1);
		}

		return retVal.getBytes();
	}

	public List<AtomicIncrementRequest> getIncrements() {
		incs.clear();
		// Increment the number of events received
		// incs.add(new AtomicIncrementRequest(table, "totalEvents".getBytes(),
		// columnFamily, eventCountCol));
		return incs;
	}

	public void cleanUp() {
		table = null;
		columnFamily = null;
		currentEvent = null;
		columnNames = null;
	}

	public void configure(Context context) {
		String cols = new String(context.getString("columns"));
		String key = new String(context.getString("key"));

		this.columnNames = cols.split(",");
		this.columnKey = key.split(",");
	}

	public void configure(ComponentConfiguration conf) {
	}
}
