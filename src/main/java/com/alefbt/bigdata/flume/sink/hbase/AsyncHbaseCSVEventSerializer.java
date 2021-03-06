package com.alefbt.bigdata.flume.sink.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.flume.FlumeException;
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
public class AsyncHbaseCSVEventSerializer extends GenericEventSerializer implements AsyncHbaseEventSerializer {
	private static final Logger LOG = LoggerFactory.getLogger(AsyncHbaseCSVEventSerializer.class);
	private final byte[] eventCountCol = "eventCount".getBytes();
	private final List<PutRequest> puts = new ArrayList<PutRequest>();
	private final List<AtomicIncrementRequest> incs = new ArrayList<AtomicIncrementRequest>();

	public List<PutRequest> getActions() {
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

	public List<AtomicIncrementRequest> getIncrements() {
		incs.clear();
		incs.add(new AtomicIncrementRequest(table, "totalEvents".getBytes(), columnFamily, eventCountCol));
		return incs;
	}

}
