package com.alan.kafkastream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

@SpringBootApplication
public class KafkaStreamApplication {

    private static final Logger LOG = Logger.getLogger(KafkaStreamApplication.class.getSimpleName());

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamApplication.class, args);
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        //custom
        config.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, "3");

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream("streams");
        LOG.info("recuperando");

        KTable<String, Long> wordCounts = textLines
                .flatMapValues(textLine -> Arrays.asList(textLine.toLowerCase().split("\\W+")))
                .groupBy((k, v) -> v).count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("counts-store"));
        wordCounts.toStream().foreach((k, v) -> LOG.info(k + "=>" + Long.valueOf(v)));
        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

    }
}
