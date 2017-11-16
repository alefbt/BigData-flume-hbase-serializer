package com.alefbt.bigdata.flume.sink.hbase;

import java.util.UUID;

import org.apache.commons.csv.CSVRecord;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.conf.ComponentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericEventSerializer {
	private static final Logger LOG = LoggerFactory.getLogger(GenericEventSerializer.class);
	protected byte[] table;
	protected byte[] columnFamily;

	protected Event currentEvent;

	protected String[] columnNames = null;
	protected String[] columnKey = null;

	public void initialize(byte[] table, byte[] cf) {
		LOG.debug("initialize");

		setTable(table);
		setColumnFamily(cf);

	}

	public void initialize(Event event, byte[] columnFamily) {
		setEvent(event);
		setColumnFamily(columnFamily);

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

	public void setTable(byte[] table) {
		this.table = table;
	}

	public void setColumnFamily(byte[] cf) {
		this.columnFamily = cf;
	}

	protected String createRecordKey(CSVRecord record) {
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

	public void close() {
		cleanUp();
	}

	public void cleanUp() {
		table = null;
		columnFamily = null;
		currentEvent = null;
		columnNames = null;
	}
}
