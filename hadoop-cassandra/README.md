# hadoop-cassandra

This module contains sample Hadoop MapReduce code that accesses the data that resides in a Cassandra DB.

## Cassandra Setup

To set-up the test Column Families in Cassandra, run the follwoing commands in cassandra-cli

    create keyspace hadoop_lab with placement_strategy = 'SimpleStrategy' and strategy_options = {replication_factor : 1};
    create column family test_cf with comparator = 'TimeUUIDType' and default_validation_class = 'BytesType'  and key_validation_class = 'UTF8Type' and compression_options = {sstable_compression:SnappyCompressor, chunk_length_kb:64};

## Objective
The specific example in this project performs the following steps

* Read from a Cassandra DB.
* Format the TimeUUID column name to generate the output file location and filename. If TimeUUID points to a date `2013-09-01 22:26:51` and the output path is `/output`, then the file will be `/output/2013/09/01/22/part-r-00000`.
* Write the values in the blob to the file.

## Run

* Build the uber-jar by running `mvn clean install`. The uber-jar will be created in the target directory with the name hadoop-cassandra-`<version>`-jar-with-dependencies.jar. 
* Copy this uber-jar to your Hadoop environment and run the following command
   
    `dse hadoop jar` `<jar-name>` `<hostname>` `<port>` `<keyspace>` `<columnfamily>` `<output_path>` `<cassandra_cluster_partitioner>`
    
    For example
    
    `dse hadoop jar hadoop-cassandra-0.0.1-SNAPSHOT-jar-with-dependencies.jar 192.168.1.4 9160 hadoop_lab test_cf /output org.apache.cassandra.dht.RandomPartitioner`




