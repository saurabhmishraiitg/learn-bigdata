package org.nexus.hadoop.wordcount;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.Iterator;

/**
 * Basic WordCount Job using new MR API.
 *
 * @author smishra
 */
@SuppressWarnings("deprecation")
public class BasicWordCount {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration config = new Configuration();

        String inputPath = "data/input";
        String outputPath = "data/output";

        Job job = new Job(config, "Saurabh's Word Count");

        job.setJarByClass(BasicWordCount.class);
        FileInputFormat.addInputPath(job, new Path(inputPath));
        job.setInputFormatClass(TextInputFormat.class);
        job.setMapperClass(BasicMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setReducerClass(BasicReducer.class);
        job.setNumReduceTasks(10);

        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        FileSystem fs = FileSystem.get(config);
        if (fs.exists(new Path(outputPath))) {
            fs.delete(new Path(outputPath));
        }

        job.waitForCompletion(true);
    }

    /**
     * Basic Mapper Class
     *
     * @author smishra
     */
    public static class BasicMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            for (String word : value.toString().trim().split(" ")) {
                context.write(new Text(word), new IntWritable(1));
            }
        }
    }

    /**
     * Basic Reducer Class
     *
     * @author smishra
     */
    public static class BasicReducer extends Reducer<Text, IntWritable, Text, LongWritable> {

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {

            Iterator<IntWritable> iterator = values.iterator();
            long count = 0L;
            while (iterator.hasNext()) {
                iterator.next();
                count++;
            }

            context.write(key, new LongWritable(count));
        }
    }
}
