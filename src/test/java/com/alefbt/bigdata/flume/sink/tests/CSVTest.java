package com.alefbt.bigdata.flume.sink.tests;

import java.util.Map.Entry;

import org.apache.commons.csv.CSVRecord;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CSVTest {

	@Test
	public void test_simple_csv() {
		String inputCSV = "a,b,c,d";
		CSVRecord r = CSVReader.readCSVFirstLine(inputCSV);
		Assert.assertEquals(r.get(3), "d");
	}

	@Test
	public void test_simple_csv_with_headers_foreach() {
		String inputCSV = "a,b,c,d";
		String[] headers = {
				"COL_A","COL_B","COL_C","COL_D"
		};
		
		CSVRecord r = CSVReader.readCSVFirstLine(inputCSV,headers);
		
		for(Entry<String, String> s : r.toMap().entrySet())
			System.out.println(s.getKey() + " - " + s.getValue());
	}

	@Test
	public void test_simple_csv_with_headers() {
		String inputCSV = "a,b,c,d";
		String[] s = {
				"COL_A","COL_B","COL_C","COL_D"
		};
		CSVRecord r = CSVReader.readCSVFirstLine(inputCSV,s);
		Assert.assertEquals(r.get(s[2]), "c");
	}
	
	@Test
	public void test_complex1_csv() {
		String inputCSV = "a,\"b,b1\",c,d";
		CSVRecord r = CSVReader.readCSVFirstLine(inputCSV);
		Assert.assertEquals(r.get(3), "d");
		Assert.assertEquals(r.get(1), "b,b1");
	}

	@Test
	public void test_complex2_csv() {
		String inputCSV = "a,b,c\"c,\"d,d\"";
		CSVRecord r = CSVReader.readCSVFirstLine(inputCSV);
		Assert.assertEquals(r.get(3), "d,d");
		Assert.assertEquals(r.get(2), "c\"c");
	}
	
	
}
