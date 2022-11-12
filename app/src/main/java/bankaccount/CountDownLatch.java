package bankaccount;

import java.util.concurrent.atomic.AtomicInteger;

public class CountDownLatch {
    private final AtomicInteger counter;
    private final Object condition = new Object();

    public CountDownLatch(int n) {
        this.counter = new AtomicInteger(n);
    }

    public void countDown() {
        //get the old val of counter
        int n = this.counter.decrementAndGet();
        //if it is 0
        if (n == 0) {
            //notify the passive threads
            synchronized (this.condition) {
                this.condition.notifyAll();
            }
        }
    }

    public void await() {
        //if there is still active threads around
        while (this.counter.get() > 0) {
            synchronized (this.condition) {
                try {
                    //await on the condition
                    condition.wait();
                } catch (InterruptedException e) {
                    System.out.println("Exception: " + e);
                }
            }
        }
    }
}