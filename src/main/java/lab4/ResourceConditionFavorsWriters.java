package lab4;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ResourceConditionFavorsWriters implements Resource {
  private int reading = 0;
  private int writers = 0;
  private int writing = 0;
  private final Lock lock = new ReentrantLock();
  private final Condition condition = lock.newCondition();

  public void write(Runnable block) throws InterruptedException {
    lock.lock();
    writers++;
    while (reading > 0 || writing > 0)
      condition.await();

    writing++;
    lock.unlock();

    block.run();

    lock.lock();
    writing--;
    writers--;
    condition.signalAll();
    lock.unlock();
  }

  public void read(Runnable block) throws InterruptedException {
    lock.lock();
    if (writers > 0)
      condition.await();
    while (writing > 0)
      condition.await();
    reading++;
    lock.unlock();

    block.run();

    lock.lock();
    reading--;
    condition.signalAll();
    lock.unlock();
  }
}
