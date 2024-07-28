/**
 *
 */
package org.nexus.hadoop.mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import parquet.example.data.Group;
import parquet.hadoop.example.ExampleInputFormat;

import java.io.IOException;

/**
 * WARNING: This example doesn't try reading the schema for the data stored. Also, it doesn't handle for case where a
 * column may be missing in the data.
 *
 * @author naruto-kun
 *
 */
public class ReadParquetMR extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        try {
            args = new String[]{"data/input/parquet_io", "data/output/parquet_read/" + System.currentTimeMillis()};
            int res = ToolRunner.run(new Configuration(), new ReadParquetMR(), args);
            System.exit(res);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(255);
        }
    }

    @SuppressWarnings("deprecation")
    public int run(String[] args) throws Exception {
        getConf().set("mapred.textoutputformat.separator", ",");
        getConf().set("mapred.job.name", "test-parquet-mr");

        Job job = new Job(getConf());
        job.setJarByClass(getClass());

        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(ReadRequestMap.class);
        job.setNumReduceTasks(0);

        // [Saurabh] Either use this implemented InputFormatClass or set the following property
        // getConf().set("parquet.read.support.class", "parquet.hadoop.example.GroupReadSupport");
        job.setInputFormatClass(ExampleInputFormat.class); // [Saurabh] Can be changed to ParquetInputFormat
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);

        return 0;
    }

    /*
     * Read a Parquet record, write a CSV record
     */
    public static class ReadRequestMap extends Mapper<LongWritable, Group, NullWritable, Text> {
        // private static List<FieldDescription> expectedFields = null;

        @Override
        public void map(LongWritable key, Group value, Context context) throws IOException, InterruptedException {
            NullWritable outKey = NullWritable.get();

            String line = value.toString();
            String[] fields = line.split("\n");

            StringBuilder csv = new StringBuilder();
            // StringBuilder header = new StringBuilder();
            for (String field : fields) {
                String[] parts = field.split(": ");
                boolean mustQuote = (parts[1].contains(",") || parts[1].contains("'"));
                if (mustQuote) {
                    csv.append('"');
                }
                csv.append(parts[1]);
                // header.append(parts[0]).append(",");
                if (mustQuote) {
                    csv.append('"');
                }
            }
            // System.out.println(header.toString());
            context.write(outKey, new Text(csv.toString()));

        }
    }
}
