package bankaccount;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PreferredBankAccount {
    int curBalance;
    Lock transactionLock;
    Lock lock;
    int  numPreferredWaiting;
    Condition sufficientFundsCondition;
    Condition priorityCondition;
    public PreferredBankAccount(int initBalance){
        //initialize bank account to hold this balance
        this.curBalance = initBalance;
        //lock for managing transactions
        this.transactionLock = new ReentrantLock();
        //lock for managing preference
        this.lock = new ReentrantLock();
        //number of threads that want to conduct 'preferred withdrawals'
        this.numPreferredWaiting = 0;
        //signal the transactions is over to all other waiting threads
        this.sufficientFundsCondition = this.transactionLock.newCondition();
        //signal the transaction is over to preferred threads
        this.priorityCondition = this.transactionLock.newCondition();
    }

    public void deposit(float amount){
        transactionLock.lock();
        try {
            //add the amount to balance
            this.curBalance += amount;
            //signal the other threads since they might be waiting to withdraw
            this.notifyThread();
        } finally {
            transactionLock.unlock();
        }
    }

    public void notifyThread(){
        if (this.numPreferredWaiting == 0) {
            //handle the non-preferred transactions
            this.sufficientFundsCondition.signalAll();
        } else {
            //notify its kind
            this.priorityCondition.signalAll();
        }
    }

    public void transfer(float k, PreferredBankAccount reserve) {
        this.lock.lock ();
        try {
            reserve.withdraw(k, false);
            deposit (k);
        } finally {
            this.lock.unlock();
        }
    }


    public void withdraw(float amount, boolean isPreferred){
        transactionLock.lock();
        try {
            if (isPreferred) {
                //if the transaction is preferred
                this.numPreferredWaiting++;
                while (this.curBalance < amount) {
                    //await on the priority condition
                    this.priorityCondition.await();
                }
                this.numPreferredWaiting--;
                this.curBalance -= amount;
                //if no other preferred transactions there
                this.notifyThread();
            } else {
                while (this.curBalance < amount) {
                    this.sufficientFundsCondition.await();
                }
                this.curBalance -= amount;
                this.notifyThread();
            }
        } catch (InterruptedException e){
            System.out.println("Exception: " + e);
        } finally {
            this.transactionLock.unlock();
        }
    }
}