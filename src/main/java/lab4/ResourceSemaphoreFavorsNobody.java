package lab4;

import java.util.concurrent.Semaphore;

// favors the writer
public class ResourceSemaphoreFavorsNobody implements Resource {
  private int readers = 0;

  private Semaphore resourceSemaphore = new Semaphore(1);
  private Semaphore readerSemaphore = new Semaphore(1);

  private Semaphore serviceSemaphore = new Semaphore(1);

  public void write(Runnable block) throws InterruptedException {
    serviceSemaphore.acquire();
    resourceSemaphore.acquire();
    serviceSemaphore.release();

    block.run();

    resourceSemaphore.release();
  }

  public void read(Runnable block) throws InterruptedException {
    serviceSemaphore.acquire();
    readerSemaphore.acquire();
    readers++;
    if (readers == 1) resourceSemaphore.acquire();
    serviceSemaphore.release();
    readerSemaphore.release();

    block.run();

    readerSemaphore.acquire();
    readers--;
    if (readers == 0) resourceSemaphore.release();
    readerSemaphore.release();
  }
}
