package com.alefbt.bigdata.flume.sink.tests;

import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.flume.FlumeException;

public class CSVReader {
	public static CSVRecord readCSVFirstLine(String inputCSV) {
		return readCSVFirstLine(inputCSV,null);
	}
	public static CSVRecord readCSVFirstLine(String inputCSV, String[] headerNames) {
		CSVRecord retVal = null;

		try {
			CSVParser parser = null;
			
			if(headerNames==null)
				parser = CSVParser.parse(inputCSV, CSVFormat.DEFAULT);
			else
				parser = CSVParser.parse(inputCSV, CSVFormat.DEFAULT.withHeader(headerNames));

			for (CSVRecord r : parser) {
				retVal = r;
				break;
			}
			
			parser.close();
			
		} catch (IOException e) {
			throw new FlumeException("Cannot parse csv string ! csv=" + inputCSV);
		}
		
		return retVal;
	}
}
