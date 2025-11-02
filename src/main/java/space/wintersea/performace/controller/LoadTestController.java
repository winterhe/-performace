package space.wintersea.performace.controller;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoadTestController {
  private static final AtomicLong reqCount = new AtomicLong(0);

  @GetMapping("/starttest")
  public Map<String, Object> startTest() {
    reqCount.set(0);
    long count = 10;

    // 1, wait I/O wait
    ioBoundTask();

    // 2, simulate cpu calculation
    long tmpRandN = count%50;
    long result = cpuIntensiveTask((int) tmpRandN);

    System.out.println("start test");
    // 3, response result
    return Map.of(
        "message", "Java start test",
        "count", count,
        "hash", result
    );

  }


  @GetMapping("/test")
  public Map<String, Object> handleTest() {
    long count = reqCount.getAndAdd(1);

    // 1, wait I/O wait
    ioBoundTask();

    // 2, simulate cpu calculation
    long tmpRandN = count%50;
    long result = cpuIntensiveTask((int) tmpRandN);

    // 3, response result
    return Map.of(
        "message", "Processed successful by Java",
        "count", count,
        "hash", result
    );

  }


  @GetMapping("/endtest")
  public Map<String, Object> endTest() {
    reqCount.set(0);
    long count = 1;

    // 1, wait I/O wait
    ioBoundTask();

    System.out.println("end test");
    // 3, response result
    return Map.of(
        "message", "Java end test",
        "count", count,
        "hash", 1
    );

  }


  // 计算第 N 个斐波那契数 (迭代/非递归，避免栈溢出，专注于CPU循环)
  private static long cpuIntensiveTask(int n) {
    if (n <= 1) return n;

    int limit = n * 100000+1;
    long a = 0;
    long b = 1;
    long c = 1;

    for (int i = 2; i < limit; i++) {
      c = a + b;
      a = b;
      b = c;
    }
    return c;
  }
  // 模拟I/O密集型任务
  private static void ioBoundTask() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
