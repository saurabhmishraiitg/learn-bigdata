/**
 *
 */
package org.nexus.hadoop.mr;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.mapred.AvroValue;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import parquet.avro.AvroParquetInputFormat;
import parquet.avro.AvroParquetOutputFormat;
import parquet.avro.AvroSchemaConverter;
import parquet.hadoop.Footer;
import parquet.hadoop.ParquetFileReader;
import parquet.schema.MessageType;

import java.io.IOException;
import java.util.List;

/**
 * Here we are Reading Parquet input data, as well as writing to Parquet output file. But the intermediate data model is
 * Avro based. Which is better for performance.
 *
 * @author naruto-kun
 *
 */
public class ReadParquetMRSchemaAvro extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        args = new String[]{"data/input/parquet_io", "data/output/parquet_read/" + System.currentTimeMillis()};
        int res = ToolRunner.run(new Configuration(), new ReadParquetMRSchemaAvro(), args);
        System.exit(res);
    }

    @SuppressWarnings("deprecation")
    public int run(String[] args) throws Exception {
        getConf().set("mapred.textoutputformat.separator", ",");
        getConf().set("mapred.job.name", "test-parquet-mr");

        Job job = Job.getInstance(getConf());

        // Parquet Schema
        // we assume a single schema for all files
        List<Footer> footers = ParquetFileReader.readFooters(getConf(), new Path(args[0]));
        MessageType schema = footers.get(0).getParquetMetadata().getFileMetaData().getSchema();

        // Avro Schema
        // convert the Parquet schema to an Avro schema
        AvroSchemaConverter avroSchemaConverter = new AvroSchemaConverter();
        Schema avroSchema = avroSchemaConverter.convert(schema);

        // Mapper
        job.setMapperClass(ParquetMapper.class);
        // Input
        job.setInputFormatClass(AvroParquetInputFormat.class);
        AvroParquetInputFormat.addInputPath(job, new Path(args[0]));
        AvroParquetInputFormat.setAvroReadSchema(job, avroSchema);

        // Intermediate Output
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(AvroValue.class);
        AvroJob.setMapOutputValueSchema(job, avroSchema);

        // Reducer
        job.setReducerClass(ParquetReducer.class);
        // Output
        job.setOutputFormatClass(AvroParquetOutputFormat.class);
        AvroParquetOutputFormat.setOutputPath(job, new Path(args[1]));
        AvroParquetOutputFormat.setSchema(job, avroSchema);
        job.setOutputKeyClass(Void.class);
        job.setOutputValueClass(GenericRecord.class);

        // Execute job and return status
        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static class ParquetMapper extends Mapper<LongWritable, GenericRecord, Text, AvroValue<GenericRecord>> {
        /**
         * The ASCII value of CTRL+A.
         */
        private static final char WRITE_DELIM = 1;

        // Reuse output objects.
        private final Text outputKey = new Text();
        private final AvroValue<GenericRecord> outputValue = new AvroValue<GenericRecord>();

        /**
         * Just creates a simple key using two fields and passes the rest of the value directly through. Do some more
         * processing here.
         *
         * @param key
         *            the mapper's key -- not used
         * @param value
         *            the Avro representation of this Parquet record
         * @param context
         *            the mapper's context
         */
        @Override
        protected void map(LongWritable key, GenericRecord value, Context context)
                throws IOException, InterruptedException {
            outputKey.set("" + value.get("field1") + WRITE_DELIM + value.get("field2"));
            outputValue.datum(value);
            context.write(outputKey, outputValue);
        }
    }

    public static class ParquetReducer extends Reducer<Text, AvroValue<GenericRecord>, Void, GenericRecord> {

        /**
         * Does nothing but pass the values through. Do some more processing here.
         *
         * @param key
         *            the reducer's key
         * @param values
         *            all of this key's records as an Avro representation of the Parquet record
         * @param context
         *            the reducer's context
         */
        @Override
        protected void reduce(Text key, Iterable<AvroValue<GenericRecord>> values, Context context)
                throws IOException, InterruptedException {
            for (AvroValue<GenericRecord> value : values) {
                context.write(null, value.datum());
            }
        }
    }
}
