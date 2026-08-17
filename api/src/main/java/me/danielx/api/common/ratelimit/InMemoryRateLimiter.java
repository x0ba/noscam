package me.danielx.api.common.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRateLimiter {
  private final Map<String, Deque<Instant>> windows = new ConcurrentHashMap<>();

  public boolean tryAcquire(String key, int maxRequests, Duration window) {
    Instant now = Instant.now();
    Instant cutoff = now.minus(window);
    Deque<Instant> times = windows.computeIfAbsent(key, ignored -> new ArrayDeque<>());
    synchronized (times) {
      while (!times.isEmpty() && times.peekFirst().isBefore(cutoff)) {
        times.removeFirst();
      }
      if (times.size() >= maxRequests) {
        return false;
      }
      times.addLast(now);
      return true;
    }
  }
}
