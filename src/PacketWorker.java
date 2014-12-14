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
	final LamportsQueue<Packet> lamportQueue;

	public ParallelPacketWorker(PaddedPrimitiveNonVolatile<Boolean> done, LamportsQueue<Packet> lamportQueue) {
		this.done = done;
		this.lamportQueue = lamportQueue;
	}

	public void run() {
		Packet tmp;
		while( !done.value || !(lamportQueue.head == lamportQueue.tail)) {
			try {
				//dequeue the next packet from the ith Lamport queue into tmp
				tmp = this.lamportQueue.deq();
				numOfPackets++;
				fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed); 
			} catch (EmptyException e) {
				if(done.value)
				{
					break;
				}
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
