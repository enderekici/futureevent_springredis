package com.example.redisdemo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FutureEvent implements
  Comparable<FutureEvent> {

  private String key;
  private Map<String, String> eventParams;
  private long dueDate;

  @JsonIgnore
  public FutureEvent(String actorId,String journeyName, String eventName, Map<String, String> eventParams, long dueDate) {
    this.key = actorId + "~" + journeyName + "~" + eventName;
    this.eventParams = eventParams;
    this.dueDate = dueDate;
  }

  @Override
  public int compareTo(FutureEvent o) {
    return Long.compare(this.dueDate, o.dueDate);
  }

  @JsonIgnore
  public String getJourneyName() {
    return key.split("~")[1];
  }
}
