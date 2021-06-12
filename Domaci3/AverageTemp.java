package mapreduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class AverageTemp 
{
	public static class AvgTuple implements Writable 
	{
		private double average = 0;
		private int count = 0;
		
		public AvgTuple() {}
		
		public AvgTuple(double temperature) 
		{
			this.average = temperature;
			this.count = 1;
		}
		
		public double getAverage() { return average; }
		public int getCount() { return count; }
		public void setAverage(double average) { this.average = average; }
		public void setCount(int count) { this.count = count; }
		
		@Override
		public void readFields(DataInput in) throws IOException 
		{
			average = in.readDouble();
			count = in.readInt();			
		}
		@Override
		public void write(DataOutput out) throws IOException 
		{
			out.writeDouble(average);
			out.writeInt(count);
		}
		
		public String toString() 
		{
			return "average: " + average/(double)count + ", count: " + count;
		}
		
	}
	
	public static class TupleMapper extends Mapper<Object, Text, Text, AvgTuple> 
	{
		private Text month = new Text();
		private AvgTuple outTuple = new AvgTuple();
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException 
		{
			String[] line = value.toString().split(",");
			month.set(line[1].substring(4,6));
			double temperature = (double) Integer.parseInt(line[3]);
			
			outTuple.setAverage(temperature);
			outTuple.setCount(1);
			
			context.write(month, outTuple);
		}
	}
	
	public static class TupleReducer extends Reducer<Text, AvgTuple, Text, AvgTuple> 
	{
		private AvgTuple result = new AvgTuple();		
		
		public void reduce(Text key, Iterable<AvgTuple> values, Context context) throws IOException, InterruptedException 
		{
			result.setCount(0);
			result.setAverage(0);
			
			int sum = 0;
			double average = 0;
			
			for (AvgTuple value : values) 
			{
				average += value.getAverage();
				sum += value.getCount();
			}
			
			result.setCount(sum);
			result.setAverage(average);
			context.write(key, result);
		}
	}
	
	public static void main(String[] args) throws Exception 
	{
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "average count temperature");
		job.setJarByClass(AverageTemp.class);
		job.setMapperClass(TupleMapper.class);
		job.setCombinerClass(TupleReducer.class);
		job.setReducerClass(TupleReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(AvgTuple.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job,  new Path(args[1]));
		System.exit(job.waitForCompletion(true)? 0 : 1);
	}

}
