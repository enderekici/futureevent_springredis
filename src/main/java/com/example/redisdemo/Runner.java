package com.example.redisdemo;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class Runner {

  private final SpringRedisFutureEventExecutor eventExecutor;
  static Random random = new Random();
  public Runner(SpringRedisFutureEventExecutor executor) {
    this.eventExecutor = executor;
    System.out.println("Runner created");

    Executors.newSingleThreadExecutor().submit(() -> {
      while (true) {
        executor.scheduleFutureEvent(generateRandomEvent());
      }
    });
    AtomicLong cnt = new AtomicLong();
    Executors.newSingleThreadExecutor().submit(() -> {
      while (true) {
        int size = executor.popDueEvents("j1").size();
        if (size > 0) {
          cnt.addAndGet(size);
        }
      }
    });

    Executors.newSingleThreadExecutor().submit(() -> {
      while (true) {
        System.out.println("processed: " + cnt.get() + " " + System.currentTimeMillis());
        cnt.set(0);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

  }
  private static FutureEvent generateRandomEvent() {
    HashMap<String, String> eventParams = new HashMap<>();
    eventParams.put(System.currentTimeMillis() + "", System.currentTimeMillis() + "");
    return new FutureEvent("a_" + UUID.randomUUID(),
        "j1",
        "e_" + random.nextInt(1000), eventParams,
        System.currentTimeMillis() + random.nextInt(30000));
  }
}
