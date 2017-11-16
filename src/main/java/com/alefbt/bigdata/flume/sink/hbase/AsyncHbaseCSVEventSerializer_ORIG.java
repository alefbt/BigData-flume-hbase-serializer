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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author yehuda
 *
 */
public class AsyncHbaseCSVEventSerializer_ORIG implements AsyncHbaseEventSerializer {
	private static final Logger LOG = LoggerFactory.getLogger(AsyncHbaseCSVEventSerializer_ORIG.class);
	protected byte[] table;
	protected byte[] columnFamily;

	protected Event currentEvent;

	// private byte[][] columnNames;
	private final byte[] eventCountCol = "eventCount".getBytes();
	private final List<PutRequest> puts = new ArrayList<PutRequest>();
	private final List<AtomicIncrementRequest> incs = new ArrayList<AtomicIncrementRequest>();
	protected String[] columnNames = null;
	protected String[] columnKey = null;

	public void initialize(byte[] table, byte[] cf) {
		LOG.debug("initialize");

		this.table = table;
		this.columnFamily = cf;
	}

	public void configure(Context context) {
		String cols = new String(context.getString("columns"));
		String key = new String(context.getString("key"));

		this.columnNames = cols.split(",");
		this.columnKey = key.split(",");
		LOG.debug("Config columnNames = {}", cols);
		LOG.debug("Config columnKey = {}", key);
	}

	public void configure(ComponentConfiguration conf) {
	}

	public void setEvent(Event event) {
		LOG.debug("Got event header={}", event.getHeaders().toString());
		this.currentEvent = event;
	}

	public List<PutRequest> getActions() {
		// List<PutRequest> puts = new ArrayList<PutRequest>();
		puts.clear();

		String eventStr = new String(currentEvent.getBody());

		LOG.debug("getActions() ... EventBody: {}", eventStr);

		CSVParser parser = null;
		try {
			parser = CSVParser.parse(eventStr, CSVFormat.DEFAULT.withHeader(columnNames));
		} catch (IOException e) {
			LOG.error("ERROR: Cannot create parser for csv string {}", eventStr, e);
			throw new FlumeException("ERROR: Cannot create parser for csv string ! csv=>>> " + eventStr);
		}
		
		
		if (parser != null) {
			for (CSVRecord record : parser) {

				String recordKeyString = createRecordKey(record);

				LOG.debug("Create key: {}", recordKeyString);
				byte[] recordKey = recordKeyString.getBytes();

				for (Entry<String, String> recordEntry : record.toMap().entrySet()) {
					PutRequest req = new PutRequest(table, recordKey, columnFamily, recordEntry.getKey().getBytes(),
							recordEntry.getValue().getBytes());
					puts.add(req);
				}

			}

			try {
				parser.close();
			} catch (IOException e) {
				LOG.error("ERROR: Cannot create parser for csv string {}", eventStr, e);
				throw new FlumeException("ERROR: Cannot parse csv string ! csv=>>> " + eventStr);
			}

		}
		LOG.debug("Shoud do {} puts", puts.size());
		return puts;
	}

	private String createRecordKey(CSVRecord record) {
		String retVal = "";

		if (columnKey.length == 0)
			retVal = UUID.randomUUID().toString();
		else {
			for (String col : columnKey) {
				retVal += record.get(col) + ":";
			}
			retVal = retVal.substring(0, retVal.length() - 1);
		}

		return retVal;
	}

	public List<AtomicIncrementRequest> getIncrements() {
		incs.clear();
		incs.add(new AtomicIncrementRequest(table, "totalEvents".getBytes(), columnFamily, eventCountCol));
		return incs;
	}

	public void cleanUp() {
		table = null;
		columnFamily = null;
		currentEvent = null;
		columnNames = null;
	}

}
