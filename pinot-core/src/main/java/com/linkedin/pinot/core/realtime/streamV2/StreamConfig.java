package com.linkedin.pinot.core.realtime.streamV2;

import com.linkedin.pinot.common.utils.time.TimeUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;


/**
 * Wrapper around stream configs map from table config.
 */
public class StreamConfig {

  public enum StreamConsumerType {
    HIGH_LEVEL,
    SIMPLE
  }

  private int DEFAULT_FLUSH_THRESHOLD_ROWS = 5_000_000;
  private long DEFAULT_FLUSH_THRESHOLD_TIME = TimeUnit.MILLISECONDS.convert(6, TimeUnit.HOURS);

  private String _type;
  private String _name;
  private List<StreamConsumerType> _consumerTypes;
  private int _flushThresholdRows = DEFAULT_FLUSH_THRESHOLD_ROWS;
  private long _flushThresholdTimeMillis = DEFAULT_FLUSH_THRESHOLD_TIME;

  private Map<String, String> _streamConfigs;

  public StreamConfig(@Nonnull Map<String, String> streamConfigs) {

    _streamConfigs = streamConfigs;
    _type = streamConfigs.get(StreamConfigProperties.STREAM_TYPE);

    String streamNameProperty = StreamConfigProperties.constructStreamProperty(_type, StreamConfigProperties.NAME);
    _name = streamConfigs.get(streamNameProperty);
    String consumerTypes = StreamConfigProperties.constructStreamProperty(_type, StreamConfigProperties.CONSUMER_TYPES);
    for (String consumerType : consumerTypes.split(",")) {
      _consumerTypes.add(StreamConsumerType.valueOf(consumerType));
    }

    String flushThresholdRows = streamConfigs.get(StreamConfigProperties.STREAM_FLUSH_THRESHOLD_ROWS);
    if (flushThresholdRows != null) {
      _flushThresholdRows = Integer.parseInt(flushThresholdRows);
    }

    String flushThresholdTime = streamConfigs.get(StreamConfigProperties.STREAM_FLUSH_THRESHOLD_TIME);
    if (flushThresholdTime != null) {
      _flushThresholdTimeMillis = TimeUtils.convertPeriodToMillis(flushThresholdTime);
    }
  }

  public String getType() {
    return _type;
  }

  public String getName() {
    return _name;
  }

  public int getFlushThresholdRows() {
    return _flushThresholdRows;
  }

  public long getFlushThresholdTimeMillis() {
    return _flushThresholdTimeMillis;
  }

  public boolean hasHighLevelConsumer() {
    return _consumerTypes.contains(StreamConsumerType.HIGH_LEVEL);
  }

  public boolean hasSimpleConsumer() {
    return _consumerTypes.contains(StreamConsumerType.SIMPLE);
  }

  public String getValue(String key) {
    return _streamConfigs.get(key);
  }

  public Map<String, String> getAllProperties() {
    return _streamConfigs;
  }
}
