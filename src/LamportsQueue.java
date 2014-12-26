
public class LamportsQueue<T> {
	
	
	volatile int head = 0;
	volatile int tail = 0;
	Lock lock = null;
	final T[] items;
	
	public LamportsQueue(int capacity) {
		items = (T[])new Object[capacity];
		head = 0; tail = 0;
	}
	
	public LamportsQueue(int capacity, int lockType) {
		items = (T[])new Object[capacity];
		head = 0; tail = 0;
		LockAllocator la = new LockAllocator();
	    this.lock = la.getLock(lockType);
	    la.printLockType(lockType);
	}
	
	
	public void enq(T x) throws FullException {
		if (tail - head == items.length)
			throw new FullException();
		items[tail % items.length] = x;
		tail++;
	}
	public T deq() throws EmptyException {
		if (tail - head == 0)
			throw new EmptyException();
		T x = items[head % items.length];
		head++;
		return x;
	}
}

