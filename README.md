# BigData-flume-hbase-serializer
BigData flume AsyncHbaseEventSerializer implemented with CSV reader and RowKey creator
Most of CSV handlers in flume thay split string with comma delimiter "," so the string for ex. `a,b,"c,c2",d` wil not parse correctly. 
This project uses `Apache common` lib for parsing CSV and handles thouse issues.

## Install

    # Build 
    mvn clean install
    
    # copy the jar to Lib in flume agent
    cp target/
    
## Example
When input CSV is like:

    ts,36882775649162,5610505046685714
    ti,3528885721650035,4903559682490813
    ti,30049400044021,6374608174602560


Using in flume configuration

	agent007.sinks.sink1.type = org.apache.flume.sink.hbase.AsyncHBaseSink
	agent007.sinks.sink1.channel = c1
	agent007.sinks.sink1.table = transactions
	agent007.sinks.sink1.columnFamily = clients
	agent007.sinks.sink1.batchSize = 5000
	
	# Add this
    agent007.sinks.sink1.serializer = com.alefbt.bigdata.flume.sink.hbase.AsyncHbaseCSVEventSerializer
    agent007.sinks.sink1.serializer.columns = type,id1,id2
    agent007.sinks.sink1.serializer.key = type,id2
    
It will Run `hbase put 'table', 'rowkey', 'column', 'value'`
So each row in example it will run 3 put commands

the RowKey will generated from `type,id2` fields

so, 

    # the config	 
    agent007.sinks.sink1.serializer.columns = type,id1,id2
    agent007.sinks.sink1.serializer.key = type,id2

	# csv value :
    ts,36882775649162,5610505046685714

	# row key:
	ts:5610505046685714
	
	# The actions on HBase will be:
	hbase put 'transactions', 'ts:5610505046685714', 'clients:type', 'ts'
	hbase put 'transactions', 'ts:5610505046685714', 'clients:id1', '36882775649162'
	hbase put 'transactions', 'ts:5610505046685714', 'clients:id2', '5610505046685714'

## Developers
Me: Yehuda Korotkin <yehuda@alefbt.com> 

## Tested

 * On cloudera 5.10

## See also

 * https://flume.apache.org/FlumeUserGuide.html#asynchbasesink
 * https://commons.apache.org/proper/commons-csv/
 * https://flume.apache.org/
 * http://hbase.apache.org/


## License 
MIT. (see LICENSE file)
 
