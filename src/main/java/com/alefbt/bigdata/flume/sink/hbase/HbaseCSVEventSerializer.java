package com.alefbt.bigdata.flume.sink.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.flume.FlumeException;
import org.apache.flume.sink.hbase.HbaseEventSerializer;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HbaseCSVEventSerializer extends GenericEventSerializer implements HbaseEventSerializer {
	private static final Logger LOG = LoggerFactory.getLogger(HbaseCSVEventSerializer.class);
	private final List<Row> actions = new ArrayList<Row>();
	private final List<Increment> incs = new ArrayList<Increment>();

	public List<Row> getActions() {
		actions.clear();
	
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
				
				Put put = new Put(recordKeyString.getBytes());
				for (Entry<String, String> recordEntry : record.toMap().entrySet()) {
					put.addColumn(columnFamily, recordEntry.getKey().getBytes(), recordEntry.getValue().getBytes());
				}
				
		      actions.add(put);

			}

			try {
				parser.close();
			} catch (Exception e) {
				LOG.error("ERROR: Cannot create parser for csv string {}", eventStr, e);
				throw new FlumeException("ERROR: Cannot parse csv string ! csv=>>> " + eventStr);
			}

		}
		LOG.debug("Shoud do {} puts", actions.size());
	  	
		return actions;
	}

	public List<Increment> getIncrements() {
		return incs;
	}

}
