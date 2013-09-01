/**
 * 
 */
package hadoop.cassandra.mapreduce;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedMap;

import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.netflix.astyanax.serializers.TimeUUIDSerializer;
import com.netflix.astyanax.util.TimeUUIDUtils;


/**
 * @author atindriya
 * 
 */
public class CassandraReadMapper extends
		Mapper<ByteBuffer, SortedMap<ByteBuffer, IColumn>, Text, Text> {

	private static final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH");
	private static final TimeUUIDSerializer timeUUIDSerializer = new TimeUUIDSerializer();
	
	@Override
	protected void map(ByteBuffer rowKey, SortedMap<ByteBuffer, IColumn> columns,
			Context context) throws IOException, InterruptedException {
		for(IColumn column: columns.values()){
			Text hourBucket = new Text(formatDate(column.name()));
			Text event = new Text(ByteBufferUtil.string(column.value()));
			context.write(hourBucket, event);
		}
	}

	/**
	 * Takes a Bytebuffer TimeUUID and converts it into a String.
	 * @param dateTime
	 * @return
	 */
	private String formatDate(final ByteBuffer dateTime){
		String formattedDateString = null;
		//Taken from astyananx code.
		if (dateTime != null){
			ByteBuffer dup = dateTime.duplicate();
			long micros = TimeUUIDUtils.getMicrosTimeFromUUID(timeUUIDSerializer.fromByteBuffer(dup));
			formattedDateString = simpleDateFormat.format(new Date(micros / 1000));
		}
        return formattedDateString;
	}
	
	
	
	

}
