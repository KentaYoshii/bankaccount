import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {
    int curBalance;
    Lock transactionLock;
    Condition sufficientFundsCondition;
    public BankAccount(int initBalance){
        //initialize bank account to hold this balance
        this.curBalance = initBalance;
        //lock for managing transactions
        this.transactionLock = new ReentrantLock();
        //signal the transactions is over to all other waiting threads
        this.sufficientFundsCondition = transactionLock.newCondition();
    }

    public void deposit(float amount){
        transactionLock.lock();
        try {
            //add the amount to balance
            this.curBalance += amount;
            //signal the other threads since they might be waiting to withdraw
            this.sufficientFundsCondition.signalAll();
        } finally {
            transactionLock.unlock();
        }
    }

    public void withdraw(float amount){
        try {
            //if the current balance is smaller than the amount you are trying to withdraw
            while (this.curBalance < amount) {
                this.sufficientFundsCondition.await();
            }
            //if you have the amount in your account, then withdraw
            this.curBalance -= amount;
            //signal others
            this.sufficientFundsCondition.signalAll();
        } catch (InterruptedException e) {
            System.out.println("Exception: " + e);
        } finally {
            transactionLock.unlock();
        }
    }
}