package hadoop.cassandra.mapreduce;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

public class S3WriteReducer extends Reducer<Text, Text, NullWritable, Text> {
	
	private MultipleOutputs<NullWritable, Text> mos;
	
	private static final String NEW_LINE_CHAR = System.getProperty("line.separator");
	private static final String PATH_SEPARATOR = File.separator;
	
	@Override
	protected void setup(Context context)
			throws IOException, InterruptedException {
		mos = new MultipleOutputs<NullWritable, Text>(context);
	}

	@Override
	protected void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		StringBuilder builder = new StringBuilder();
		for(Text text: values){
			builder.append(text.toString()).append(NEW_LINE_CHAR);
		}		
		String fileName = generateFileName(key);
		mos.write(NullWritable.get(), new Text(builder.toString()), fileName);
	}

	/**
	 * Generates the filename.
	 * @param key
	 * @param parentDir
	 */
	private String generateFileName(Text key) {
		StringBuilder pathBuilder = new StringBuilder(key.toString());
		pathBuilder.append(PATH_SEPARATOR).append("part");
		return pathBuilder.toString();
	}
	
	@Override
	protected void cleanup(Context context)
			throws IOException, InterruptedException {
		mos.close();
	}

}
