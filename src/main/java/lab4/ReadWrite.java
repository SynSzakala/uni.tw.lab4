package lab4;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

class Actor extends Thread {
  private final Resource resource;

  private final int id;

  private final int rounds;
  private final boolean read;


  public Actor(int id, Resource resource, int rounds, boolean read) {
    this.id = id;
    this.resource = resource;
    this.rounds = rounds;
    this.read = read;
  }

  @Override public void run() {
    try {
      for (int i = 0; i < rounds; i++) {
        int finalI = i;
        if (read) {
          resource.read(() -> iterate(finalI));
        } else {
          resource.write(() -> iterate(finalI));
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private void iterate(int i) {
    try {
      sleep(read ? 10 : 20);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}

class ProducerConsumerTest {

  private final int writers;
  private final int readers;

  private final int writerRounds;

  private final int readerRounds;
  private final Resource resource;

  public ProducerConsumerTest(int writers, int readers, int writerRounds, int readerRounds, Resource resource) {
    this.writers = writers;
    this.readers = readers;
    this.writerRounds = writerRounds;
    this.readerRounds = readerRounds;
    this.resource = resource;
  }

  public void run(FileWriter file) {
    var startTime = System.nanoTime();

    var actors = new ArrayList<Actor>();

    for (int i = 0; i < this.readers; i++) {
      var reader = new Actor(i, resource, readerRounds / readers + (i == 0 ? readerRounds % readers : 0), true);
      reader.start();
      actors.add(reader);
    }

    for (int i = 0; i < this.writers; i++) {
      var writer = new Actor(i, resource, writerRounds / writers + (i == 0 ? writerRounds % writers : 0), false);
      writer.start();
      actors.add(writer);
    }

    for (var actor : actors) {
      try {
        actor.join();
      } catch (Exception e) {
        e.printStackTrace();
      }

    }

    var stopTime = System.nanoTime();
    var duration = stopTime - startTime;
    var durationMs = duration / 1000 / 1000;

    System.out.printf("Writers: %d | Readers: %d | Duration: %d\n", writers, readers, durationMs);
    try {
      file.write(String.format("%d %d %d\n", writers, readers, durationMs));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}


public class ReadWrite {
  public static void main(String[] args) {

    var buffers = new Resource[]{
        new ResourceSemaphoreFavorsNobody(),
        new ResourceSemaphoreFavorsReaders(),
        new ResourceSemaphoreFavorsWriters(),
    };
    var readerCounts = new int[]{10, 30, 60, 100};
    var writerCounts = new int[]{1, 3, 6, 10};


    // writes are slower than reads

    for (var buffer : buffers) {
      var name = buffer.getClass().getSimpleName();
      var file = new File(name + ".txt");
      try (var writer = new FileWriter(file)) {
        for (int readerCount : readerCounts) {
          for (int writerCount : writerCounts) {
            (new ProducerConsumerTest(writerCount, readerCount, 100, 500, buffer)).run(writer);
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      try {
        Runtime.getRuntime().exec(String.format("gnuplot -e \"set terminal png size 1000,800; set output '%s.png'; splot '%s.txt'\"", name, name));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
