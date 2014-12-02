
public class LamportsQueue<T> {
	
	
	volatile int head = 0;
	volatile int tail = 0;
	final T[] items;
	
	public LamportsQueue(int capacity) {
		items = (T[])new Object[capacity];
		head = 0; tail = 0;
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

