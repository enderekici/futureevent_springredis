package com.example.redisdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SpringRedisFutureEventExecutor implements
    FutureEventExecutor {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public SpringRedisFutureEventExecutor(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
   ObjectMapper o= new ObjectMapper();
   o.findAndRegisterModules();
   objectMapper=o;
  }

  private String getStoreKeyName(String journeyName) {
    return "future_k_" + journeyName;
  }

  private String getStoreValueName(String journeyName) {
    return "future_v_" + journeyName;
  }

  @Override
  public void deployJourney(String journeyName) {

  }

  @Override
  public void unDeployJourney(String journeyName) {
    redisTemplate.delete(getStoreKeyName(journeyName));
    redisTemplate.delete(getStoreValueName(journeyName));
  }

  @SneakyThrows
  @Override
  public void scheduleFutureEvent(FutureEvent futureEvent) {
    String futureEventKey = futureEvent.getKey();
    redisTemplate.boundZSetOps(getStoreKeyName(futureEvent.getJourneyName()))
        .add(futureEventKey, futureEvent.getDueDate());
    redisTemplate.boundHashOps(getStoreValueName(futureEvent.getJourneyName()))
        .put(futureEventKey, objectMapper.writeValueAsString(futureEvent));

  }

  @SneakyThrows
  @Override
  public Collection<FutureEvent> popDueEvents(String journeyName) {
    BoundZSetOperations<String, String> set = redisTemplate.boundZSetOps(getStoreKeyName(
        journeyName));
    long currentTimeMillis = System.currentTimeMillis();
    Collection<String> keys = set.rangeByScore(0, currentTimeMillis);
    set.removeRangeByScore(0, currentTimeMillis);

    BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(
        getStoreValueName(journeyName));
    List<FutureEvent> list = new ArrayList<>(keys.size());
    for (String key : keys) {
      Object o = ops.get(key);
      if (o != null) {
        list.add(objectMapper.readValue((String) o, FutureEvent.class));
      }
    }
    return list;
  }
}
