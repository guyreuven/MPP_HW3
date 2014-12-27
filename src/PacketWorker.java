import java.util.Random;

public interface PacketWorker extends Runnable {
	public void run();
}

class SerialPacketWorker implements PacketWorker {
	PaddedPrimitiveNonVolatile<Boolean> done;
	final PacketSource pkt;
	final Fingerprint residue = new Fingerprint();
	long fingerprint = 0;
	long totalPackets = 0;
	final int numSources;
	final boolean uniformBool;
	public SerialPacketWorker(
			PaddedPrimitiveNonVolatile<Boolean> done, 
			PacketSource pkt,
			boolean uniformBool,
			int numSources) {
		this.done = done;
		this.pkt = pkt;
		this.uniformBool = uniformBool;
		this.numSources = numSources;
	}

	public void run() {
		Packet tmp;
		while( !done.value ) {
			for( int i = 0; i < numSources; i++ ) {
				if( uniformBool )
					tmp = pkt.getUniformPacket(i);
				else
					tmp = pkt.getExponentialPacket(i);
				totalPackets++;
				fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);        
			}
		}
	}  
}

class SerialQueuePacketWorker implements PacketWorker {
	PaddedPrimitiveNonVolatile<Boolean> done;
	final PacketSource pkt;
	final Fingerprint residue = new Fingerprint();
	long fingerprint = 0;
	long totalPackets = 0;
	final int numSources;
	final boolean uniformBool;
	final LamportsQueue<Packet>[] queueBank;

	public SerialQueuePacketWorker(
			PaddedPrimitiveNonVolatile<Boolean> done, 
			PacketSource pkt,
			boolean uniformBool,
			int numSources,
			LamportsQueue<Packet>[] queueBank) {
		this.done = done;
		this.pkt = pkt;
		this.uniformBool = uniformBool;
		this.numSources = numSources;
		this.queueBank = queueBank;
	}

	public void run() {
		Packet tmp;
		while( !done.value ) {
			for( int i = 0; i < numSources; i++ ) {
				if( uniformBool )
					tmp = pkt.getUniformPacket(i);
				else
					tmp = pkt.getExponentialPacket(i);
				try {
					//enqueue tmp in the ith Lamport queue
					this.queueBank[i].enq(tmp);

				} catch (FullException e) {;}
				try {
					//dequeue the next packet from the ith Lamport queue into tmp
					tmp = this.queueBank[i].deq();
				} catch (EmptyException e) {;}
				totalPackets++;
				fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);        
			}
		}
	}  
}

class ParallelPacketWorker implements PacketWorker {

	PaddedPrimitiveNonVolatile<Boolean> done;
	final Fingerprint residue = new Fingerprint();
	long fingerprint = 0;
	long numOfPackets = 0;
	int numSources = 0;
	int workerId = 0;
	Strategy_e strategy = null;
	final LamportsQueue<Packet>[] lamportQueueBank;

	public ParallelPacketWorker(PaddedPrimitiveNonVolatile<Boolean> done, 
			LamportsQueue<Packet>[] lamportQueueBank,
			int workerId,
			int numSources,
			Strategy_e strategy) {
		this.done = done;
		this.lamportQueueBank = lamportQueueBank;
		this.workerId = workerId;
		this.numSources = numSources;
		this.strategy = strategy;
	}

	public void run() {
		Packet tmp 			= null;
		boolean deqSuccess 	= false;
		Random rand 		= new Random();
		Integer randSelection = null;

		while( !done.value )	{ // || !(lamportQueue.head == lamportQueue.tail)) {

			LamportsQueue<Packet> lamportQueue 	= null;
			deqSuccess 							= false;
			
			// first, choose and handle the relevant strategy
			switch (this.strategy) {		// assuming the strategies are: LockFree: 0, HomeQueue: 1, RandomQueue: 2, LastQueue: 3

			case LockFree:	// in this case - create a 1-to-1 mapping between queue and Worker
				lamportQueue = lamportQueueBank[workerId];
				try {
					//dequeue the next packet from the relevant Lamport queue into tmp
					tmp = lamportQueue.deq();
					numOfPackets++;
					fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed); 
				} catch (EmptyException e) {
					if(done.value)
					{
						break;
					}
				}

				break;
			case HomeQueue:	// in this case - fixed mapping as in LockFree, but with the added requirement that the worker grabs the (albeit uncontended) lock for the associated queue
				lamportQueue = lamportQueueBank[workerId];
				try {
					lamportQueue.lock.lock();

					//dequeue the next packet from the relevant Lamport queue into tmp
					tmp = lamportQueue.deq();
					deqSuccess = true;
					numOfPackets++;
				} catch (EmptyException e) {
					if(done.value)
					{
						break;
					}
				} finally	{
					lamportQueue.lock.unlock();
					if (deqSuccess)	{
						fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);
					}
				}
				
				break;

			case RandomQueue:	// in this case - The Worker picks a random queue to work on for each dequeue attempt
				randSelection = rand.nextInt(numSources);
				lamportQueue = lamportQueueBank[randSelection];
				try {
					lamportQueue.lock.lock();

					//dequeue the next packet from the relevant Lamport queue into tmp
					tmp = lamportQueue.deq();
					deqSuccess = true;
					numOfPackets++;
//					fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed); 
				} catch (EmptyException e) {
					if(done.value)
					{
						break;
					}
				} finally	{
					lamportQueue.lock.unlock();
					if (deqSuccess)	{
						fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);
					}
				}

				break;

			case LastQueue:	// in this case -
				Boolean successFlag = false;
				Boolean firstLock 	= true;
				while (!successFlag) {
					if (done.value)	{
						break;
					}
					randSelection = rand.nextInt(numSources);
					lamportQueue = lamportQueueBank[randSelection];
					successFlag = lamportQueue.lock.tryLock();
				}

				while (successFlag)	{	//dequeue the next packet from the relevant Lamport queue into tmp, until queue is empty
					deqSuccess = false;
					
					try 	{
						if (firstLock)	{
							firstLock = false;
						}
						else {
							lamportQueue.lock.lock();
						}
						tmp = lamportQueue.deq();
						deqSuccess = true;
						numOfPackets++;
//						fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);
					}	catch (EmptyException e) {
						break;
					} finally	{
						lamportQueue.lock.unlock();
						if (deqSuccess)	{
							fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);
						}
					}
					
				}
				
				break;

			default:
				break;
			}

		}
	}  

}

class Dispatcher implements Runnable {

	PaddedPrimitiveNonVolatile<Boolean> done;
	final LamportsQueue<Packet>[] lamportQueueBank;
	final PacketSource pkt;
	final int numSources;
	final boolean uniformBool;
	int[] pktCounter;

	public Dispatcher(PaddedPrimitiveNonVolatile<Boolean> done,
			LamportsQueue<Packet>[] lamportQueueBank,
			int numSources,
			boolean uniformBool,
			PacketSource pkt) {
		this.done = done;
		this.lamportQueueBank = lamportQueueBank;
		this.numSources = numSources;
		this.uniformBool = uniformBool;
		this.pkt = pkt;
		this.pktCounter = new int[numSources];
	}

	public void run() {
		Packet tmp;
		while( !done.value) {
			for( int i = 0; i < numSources; i++ ) {
				if( uniformBool )
					tmp = pkt.getUniformPacket(i);
				else
					tmp = pkt.getExponentialPacket(i);
				try {
					//enqueue tmp in the ith Lamport queue
					this.lamportQueueBank[i].enq(tmp);
					this.pktCounter[i]++;
				} catch (FullException e) {
					i--;
				}
			}
		}
	}
}  
