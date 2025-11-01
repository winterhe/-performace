package space.wintersea.performace.service;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PerformanceService implements ApplicationRunner {

  @Override
  public void run(ApplicationArguments args) {
    try {
      performanceProcedure(args);
    } catch (Exception e) {
      System.out.println("\n\n" + e.getMessage());
    }

    exitApp();
  }

  private void performanceProcedure(ApplicationArguments args) {
    virtualThreadStartCostTest(1);
    virtualThreadStartCostTest(10);

    cpuIntensiveTaskTest(1);
    cpuIntensiveTaskTest(10);


    cpuIntensiveTaskTest02(1);
    cpuIntensiveTaskTest02(10);

  }

  private void virtualThreadStartCostTest(int loop) {
    int taskCount = 100_000;
    long used = 0;
    long start = 0;
    long end = 0;
    // test virtual thread start cost
    used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    System.out.println("-------loop: " + loop + " -----");
    System.out.println("Java test virtual thread start cost");
    // 1024 (kB)
    long usage = used >> 10;
    System.out.println("Java current memory usage: " + usage + " KB");

    start = Instant.now().toEpochMilli();

    for (int idx = 0; idx < loop; idx++) {
      CountDownLatch countDownLatch = new CountDownLatch(taskCount);
      try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
        for (int i = 0; i < taskCount; i++) {
          final int id = i;
          executor.submit(() -> {
            try {
              return id;
            } finally {
              countDownLatch.countDown();
            }
          });
        }
        countDownLatch.await();
      } catch (Exception e) {
        System.out.println("Exception: " + e.getMessage());
      }
    }

    end = Instant.now().toEpochMilli();
    System.out.println("Java create " + taskCount + " virtual thread time cost: " + (end - start)/loop + " ms");
    used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    usage = used >> 20;
    System.out.println("Java current memory usage: " + usage + " MB");
  }

  private void cpuIntensiveTaskTest(int loop) {
    int taskCount = 50_000;
    long used = 0;
    long start = 0;
    long end = 0;
    long usage = 0;

    AtomicLong total = new AtomicLong();
    start = Instant.now().toEpochMilli();

    System.out.println();
    System.out.println("Java CPU Intensive task Fibonacci recursion calculate");
    System.out.println("-------loop: " + loop + " -----");

    for (int idx = 0; idx < loop; idx++) {
      CountDownLatch countDownLatch = new CountDownLatch(taskCount);
      try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
        for (int i = 0; i < taskCount; i++) {
          int finalI1 = i % 33;
          executor.submit(() -> {
            try {
              total.getAndAdd(fibonacci(finalI1));
            } finally {
              countDownLatch.countDown();
            }
          });
        }
        countDownLatch.await();
      } catch (Exception e) {
        System.out.println("Exception: " + e.getMessage());
      }
    }

    end = Instant.now().toEpochMilli();
    System.out.println("Java calculate result: " + total.get()/loop);
    System.out.println("Java calculate cost: " + (end - start)/loop + " ms");
    used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    usage = used >> 20;
    System.out.println("Java current memory usage: " + usage + " MB");
    printGCCount();

  }

  private void cpuIntensiveTaskTest02(int loop) {
    int taskCount = 50_000;
    long used = 0;
    long start = 0;
    long end = 0;
    long usage = 0;

    AtomicLong total = new AtomicLong();
    start = Instant.now().toEpochMilli();

    System.out.println();
    System.out.println("Java CPU Intensive task Fibonacci iterate calculate");
    System.out.println("-------loop: " + loop + " -----");

    for (int idx = 0; idx < loop; idx++) {
      CountDownLatch countDownLatch = new CountDownLatch(taskCount);
      try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
        for (int i = 0; i < taskCount; i++) {
          int finalI1 = i % 34;
          executor.submit(() -> {
            try {
              total.getAndAdd(fibonacci02(finalI1));
            } finally {
              countDownLatch.countDown();
            }
          });
        }
        countDownLatch.await();
      } catch (Exception e) {
        System.out.println("Exception: " + e.getMessage());
      }
    }

    end = Instant.now().toEpochMilli();
    System.out.println("Java calculate result: " + total.get()/loop);
    System.out.println("Java calculate cost: " + (end - start)/loop + " ms");
    used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    usage = used >> 20;
    System.out.println("Java current memory usage: " + usage + " MB");
    printGCCount();

  }

  private long fibonacci(int n) {
    if (n <= 1) return n;
    return fibonacci(n - 1) + fibonacci(n - 2);
  }

  private long fibonacci02(int n) {
    if (n <= 1) return n;

    int a = 0;
    int b = 1;
    for (int i = 2; i <= n; i++) {
      int temp = a + b;
      a = b;
      b = temp;
    }
    return b;
  }

  public static void exitApp() {
    System.exit(0);
  }

  public static void printGCCount() {
    List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

    System.out.println("=== GC statistics ===");
    long totalCount = 0;
    long totalTime = 0;

    for (GarbageCollectorMXBean gcBean : gcBeans) {
      String name = gcBean.getName();
      long count = gcBean.getCollectionCount();
      long time = gcBean.getCollectionTime(); // 毫秒

      System.out.printf("%s: counts = %d, times = %d ms%n", name, count, time);
      totalCount += count;
      totalTime += time;
    }

    System.out.printf("total GC counts: %d, total time cost: %d ms%n", totalCount, totalTime);
  }
}
