package lab4;
import java.util.concurrent.Semaphore;

public class ResourceSemaphoreFavorsReaders implements Resource {
  private int readers = 0; // counts readers
  private Semaphore readerSemaphore = new Semaphore(1);
  private Semaphore resourceSemaphore = new Semaphore(1);


  public void write(Runnable block) throws InterruptedException {
    resourceSemaphore.acquire();
    block.run();
    resourceSemaphore.release();
  }

  public void read(Runnable block) throws InterruptedException {
    readerSemaphore.acquire();
    readers++;
    if (readers == 1)
      resourceSemaphore.acquire();
    readerSemaphore.release();
    block.run();
    readerSemaphore.acquire();
    readers--;
    if (readers == 0)
      resourceSemaphore.release();
    readerSemaphore.release();
  }
}
