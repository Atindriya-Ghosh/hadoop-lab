package hadoop.cassandra;

import java.util.HashMap;
import java.util.Map;

import hadoop.cassandra.mapreduce.CassandraReadMapper;
import hadoop.cassandra.mapreduce.S3WriteReducer;

import org.apache.cassandra.hadoop.ColumnFamilyInputFormat;
import org.apache.cassandra.hadoop.ConfigHelper;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class HadoopCassandraDriver extends Configured implements Tool {

	private static Map<String, String> jobParameters;

	/**
	 * 
	 * @param args
	 *            <ol>
	 *            <li>Cassandra hostname. E.g. 192.168.1.4</li>
	 *            <li>Cassandra port no. E.g. 9160.</li>
	 *            <li>Cassandra keyspace. E.g. hadoop-lab.</li>
	 *            <li>Cassandra Column Family. E.g. test_cf. See README.md for
	 *            CF definition.</li>
	 *            <li>MapReduce Job output.</li>
	 *            <li>Cassandra Input Partitioner. Defaults to Random
	 *            Partitioner.</li>
	 *            </ol>
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new Configuration(), new HadoopCassandraDriver(), args);
	}

	@Override
	public int run(String[] args) throws Exception {
		createJobParametersMap(args);

		//setting up job
		Job job = new Job(getConf(), "Hadoop-Cassandra");
		Configuration conf = job.getConfiguration();
		
		job.setInputFormatClass(ColumnFamilyInputFormat.class);
		LazyOutputFormat.setOutputFormatClass(job, TextOutputFormat.class);
		
		job.setMapperClass(CassandraReadMapper.class);
		job.setReducerClass(S3WriteReducer.class);
		
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        
        FileOutputFormat.setOutputPath(job, new Path(jobParameters.get("output")));
        job.setJarByClass(HadoopCassandraDriver.class);
        
        //add astyanax jars to classpath
        //DistributedCache.addArchiveToClassPath(new Path("/jars/astyanax-cassandra-1.56.42.jar"), conf, FileSystem.get(conf));
       // DistributedCache.addArchiveToClassPath(new Path("/jars/astyanax-core-1.56.42.jar"), conf, FileSystem.get(conf));

        //setting up cassandra
        ConfigHelper.setInputRpcPort(conf, jobParameters.get("port"));
        ConfigHelper.setInputInitialAddress(conf, jobParameters.get("hostname"));
        ConfigHelper.setInputPartitioner(conf, jobParameters.get("partitioner"));
        ConfigHelper.setInputColumnFamily(conf, jobParameters.get("keyspace"), jobParameters.get("cf"), true);
        //get all records
        SlicePredicate predicate = new SlicePredicate().setSlice_range(new SliceRange(ByteBufferUtil.bytes(""), ByteBufferUtil.bytes(""), false, Integer.MAX_VALUE));
        ConfigHelper.setInputSlicePredicate(conf, predicate);
        
        job.waitForCompletion(true);
		return 0;
	}

	/**
	 * Sets the configuration parameters.
	 * 
	 * @param args
	 * @param configuration
	 */
	private static void createJobParametersMap(String[] args) {
		jobParameters = new HashMap<String, String>();
		jobParameters.put("hostname", args[0]);
		jobParameters.put("port", args[1]);
		jobParameters.put("keyspace", args[2]);
		jobParameters.put("cf", args[3]);
		jobParameters.put("output", args[4]);
		if (args.length == 6) {
			jobParameters.put("partitioner", args[5]);
		} else {
			jobParameters.put("partitioner",
					"org.apache.cassandra.dht.RandomPartitioner");
		}

	}

}
