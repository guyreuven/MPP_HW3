import java.util.concurrent.atomic.*;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public interface Lock {
	public void lock();
	public boolean tryLock();
	public void unlock();  
}

class TASLock implements Lock {
	AtomicBoolean state = new AtomicBoolean(false);

	public void lock() {
		while( state.getAndSet(true) ) {;} // keep trying to get the lock
	}

	public boolean tryLock() {
		if( !state.get() )
		{ // check first if it the lock is free
			this.lock(); // grab it
			return true;
		}
		else
			return false;
	}

	public void unlock() {
		state.set(false);
	}
}

class Backoff { // helper class for the BackoffLock
	volatile int tmp = 100;
	final long minDelay, maxDelay;
	long limit;
	int seed;

	public Backoff(long minDelay, long maxDelay) {
		this.minDelay = minDelay;
		this.maxDelay = maxDelay;
		this.limit = minDelay;
	}

	public void backoff() throws InterruptedException {
		for( int i = 0; i < limit; i++ ) { // simple 'local' delay - replace if you like...
			tmp += i;
		}
		if( 2*limit <= maxDelay )
			limit = 2*limit;
	}
}

class BackoffLock implements Lock {
	private AtomicBoolean state = new AtomicBoolean(false);
	private static long MIN_DELAY = 6193; // You should tune these parameters...
	private static final long MAX_DELAY = 100000000;

	public void lock() {
		while(state.get()) {;} // try to get the lock once before allocating a new Backoff(...)
		if(!state.getAndSet(true)) {
			return;
		} else {
			Backoff backoff = new Backoff(MIN_DELAY,MAX_DELAY);
			try {
				backoff.backoff();        
			} catch (InterruptedException ignore) {;}
			while(true) {
				while(state.get()) {;}
				if(!state.getAndSet(true)) {
					return;
				} else {
					try {
						backoff.backoff();
					} catch (InterruptedException ignore) {;}
				}
			}    
		}
	}

	public boolean tryLock() {
		if( !state.get() ) {
			this.lock();
			return true;
		} 
		else
			return false;
	}

	public void unlock() {
		state.set(false);
	}
	
	public void setMinDelay (long delay) {
		this.MIN_DELAY = delay;
	}
}



class CLHLock implements Lock {
	AtomicBoolean state = new AtomicBoolean(false);
	private final ThreadLocal<QNode> pred;  
	private final ThreadLocal<QNode> node;  
	private final AtomicReference<QNode> tail = new AtomicReference<QNode>(new QNode());  
	
	private static final class QNode {  
		volatile boolean locked;  
	}  
	
	public CLHLock() {  
		this.node = new ThreadLocal<QNode>() {  
			protected QNode initialValue() {  
				return new QNode();  
			}  
		};  

		this.pred = new ThreadLocal<QNode>() {  
			protected QNode initialValue() {  
				return null;  
			}  
		};  
	}  

	public void lock() {  
		while( state.getAndSet(true) ) {;}  // change the lock state.
		final QNode node = this.node.get();  
		node.locked = true;  
		QNode pred = this.tail.getAndSet(node);  
		this.pred.set(pred);  
		while (pred.locked) {}  
	}  

	public boolean tryLock() {

		if(!state.get())
		{ 
			this.lock(); 
			return true;
		}
		else
		{
			return false;
		}
	}

	public void unlock() {
		final QNode node = this.node.get();  
		node.locked = false;  
		this.node.set(this.pred.get());  
		state.set(false);
	}
}



class MCSLock implements Lock {
	AtomicBoolean state = new AtomicBoolean(false);
	AtomicReference<QNode> tail;
	ThreadLocal<QNode> MyNode;
	
	private static final class QNode {  
		volatile boolean locked;
		QNode next = null;
	} 
	
	public MCSLock()
	{   
		tail = new AtomicReference<>(null);    
		MyNode = new ThreadLocal<QNode>() {                  
			
			protected QNode initialValue()
			{
				return new QNode();
			}      
		};    
	}

	@Override
	public void lock()
	{
		while( state.getAndSet(true) ) {;} // change the lock state.
		QNode qNode = MyNode.get();        
		QNode pred = tail.getAndSet(qNode);

		if(pred != null)
		{
			qNode.locked = true;
			pred.next = qNode;

			while(qNode.locked) {}
		}
	}

	@Override
	public void unlock()
	{
		
		QNode qNode = MyNode.get();
		  if(qNode.next == null)
		  {
		   if(tail.compareAndSet(qNode, null))
		   {
		    state.set(false);
		    return;
		   }
		   while(qNode.next == null) {}
		  }

		  qNode.next.locked = false;
		  qNode.next = null;
		  state.set(false);
	}

	public boolean tryLock() {
		if( !state.get() ) { // check first if it the lock is free
			this.lock(); // grab it
			return true;
		}
		else
			return false;
	}
}


class LockAllocator {
	public Lock getLock(int lockType) {
		return getLock(lockType, 128);
	}

	public Lock getLock(int lockType, int maxThreads) {
		Lock lock = null;
		if( lockType == 0 ) {
			lock = new TASLock();
		}
		else if( lockType == 1 ) {
			lock = new BackoffLock();
		}
		else if( lockType == 4 ) {  
			lock = new CLHLock();           // You need to write these...
		}
		else if( lockType == 5 ) {
			lock = new MCSLock();           // You need to write these...
		}
		else {
			System.out.println("This is not a valid lockType:");
			System.out.println(lockType);
		}
		return lock;
	}


	public void printLockType(int lockType) {
		if( lockType == 0 ) {
			System.out.println("TASLock");
		}
		else if( lockType == 1 ) {
			System.out.println("BackoffLock");
		}
		
		else if( lockType == 4 ) {
			System.out.println("CLHLock");
		}
		else if( lockType == 5 ) {
			System.out.println("MCSLock");
		}
		else {
			System.out.println("This is not a valid lockType:");
			System.out.println(lockType);
		}
	}
	public void printLockTypes() {
		for( int i = 0; i < 6; i++ ) {
			System.out.println(i + ":");
			printLockType(i);
		}
	}
}
