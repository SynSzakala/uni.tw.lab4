package lab4;

import java.util.concurrent.Semaphore;

// favors the writer
public class ResourceSemaphoreFavorsWriters implements Resource {
  private int readers = 0;
  private int writers = 0;

  private Semaphore resourceSemaphore = new Semaphore(1);
  private Semaphore tryResourceSemaphore = new Semaphore(1);
  private Semaphore readerSemaphore = new Semaphore(1);
  private Semaphore writerSemaphore = new Semaphore(1);

  public void write(Runnable block) throws InterruptedException {
    writerSemaphore.acquire();
    writers++;
    if (writers == 1) {
      tryResourceSemaphore.acquire(); // If there are no other writers lock the readers out
    }
    writerSemaphore.release();

    resourceSemaphore.acquire();
    block.run();
    resourceSemaphore.release();

    writerSemaphore.acquire();
    writers--;
    if (writers == 0) {
      tryResourceSemaphore.release(); // If there are no writers left unlock the readers
    }
    writerSemaphore.release();
  }

  public void read(Runnable block) throws InterruptedException {
    tryResourceSemaphore.acquire();
    readerSemaphore.acquire();
    readers++;
    if (readers == 1) {
      resourceSemaphore.acquire();
    }
    readerSemaphore.release();
    tryResourceSemaphore.release();

    block.run();

    readerSemaphore.acquire();
    readers--;
    if (readers == 0) {
      resourceSemaphore.release();
    }
    readerSemaphore.release();
  }
}
