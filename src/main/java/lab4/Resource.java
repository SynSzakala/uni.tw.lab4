package lab4;

public interface Resource {
  void write(Runnable block) throws InterruptedException;

  void read(Runnable block) throws InterruptedException;
}
