package lab4;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ResourceConditionFavorsNobody implements Resource {
  private int readers = 0;
  private final Lock lock = new ReentrantLock(true);
  private final Condition condition = lock.newCondition();

  public void write(Runnable block) throws InterruptedException {
    lock.lock();
    while(readers != 0) condition.await();
    readers = -1;
    lock.unlock();

    block.run();

    lock.lock();
    readers = 0;
    condition.signal();
    lock.unlock();
  }

  public void read(Runnable block) throws InterruptedException {
    lock.lock();
    while (readers == -1) condition.await();
    readers++;
    lock.unlock();

    block.run();

    lock.lock();
    readers--;
    condition.signal();
    lock.unlock();
  }
}
