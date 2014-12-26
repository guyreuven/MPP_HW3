
class SerialPacket {
  public static void main(String[] args) {

    final int numMilliseconds = Integer.parseInt(args[0]);    
    final int numSources = Integer.parseInt(args[1]);
    final long mean = Long.parseLong(args[2]);
    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
    final short experimentNumber = Short.parseShort(args[4]);

    @SuppressWarnings({"unchecked"})
    StopWatch timer = new StopWatch();
    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
    PaddedPrimitiveNonVolatile<Boolean> done = new PaddedPrimitiveNonVolatile<Boolean>(false);
    PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);
        
    SerialPacketWorker workerData = new SerialPacketWorker(done, pkt, uniformFlag, numSources);
    Thread workerThread = new Thread(workerData);
    
    workerThread.start();
    timer.startTimer();
    try {
      Thread.sleep(numMilliseconds);
    } catch (InterruptedException ignore) {;}
    done.value = true;
    memFence.value = true;  // memFence is a 'volatile' forcing a memory fence
    try {                   // which means that done.value is visible to the workers
      workerThread.join();
    } catch (InterruptedException ignore) {;}      
    timer.stopTimer();
    final long totalCount = workerData.totalPackets;
    System.out.println("count: " + totalCount);
    System.out.println("time: " + timer.getElapsedTime());
    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
  }
}

class ParallelPacket {
  public static void main(String[] args) {

    final int numMilliseconds = Integer.parseInt(args[0]);    
    final int numSources = Integer.parseInt(args[1]);
    final long mean = Long.parseLong(args[2]);
    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
    final short experimentNumber = Short.parseShort(args[4]);
    final int queueDepth = Integer.parseInt(args[5]);
    final int lockType = Integer.parseInt(args[6]);
    final short strategy = Short.parseShort(args[7]);

    @SuppressWarnings({"unchecked"})
    //
    // Allocate and initialize your Lamport queues
    //
    LamportsQueue<Packet>[] lamportQbank = new LamportsQueue[numSources];
    for (int i = 0; i < numSources ; i++ )	{
    	lamportQbank[i] = new LamportsQueue<Packet>(queueDepth, lockType);
    }
    
    StopWatch timer = new StopWatch();
    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
    // 
    // Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
    // 
    PaddedPrimitiveNonVolatile<Boolean>[] doneArr = new PaddedPrimitiveNonVolatile[numSources+1];
    for (int i = 0; i < numSources ; i++ )	{
    	lamportQbank[i] = new LamportsQueue<Packet>(queueDepth);
    	doneArr[i] = new PaddedPrimitiveNonVolatile<Boolean>(false);
    }
    
    doneArr[numSources] = new PaddedPrimitiveNonVolatile<Boolean>(false); // the dispatcher doesn't need a queue
    
    // Allocate and initialize Dispatcher and Worker threads
    //
    Dispatcher dispatcher = new Dispatcher(doneArr[numSources], lamportQbank, numSources, uniformFlag, pkt); 
    Thread dispatcherThread = new Thread(dispatcher);
    
    // Allocate and initialize an array of Worker classes (ParallelPacketWorker),
    ParallelPacketWorker[] parallelPacketWorkers = new ParallelPacketWorker[numSources];
    
    // implementing Runnable and the corresponding Worker Threads
    Thread[] parallelPacketWorkerThreads = new Thread[numSources];
    for (int i = 0; i < numSources; i++)	{
    	ParallelPacketWorker parallelPacketWorker = new ParallelPacketWorker(doneArr[i], 
															    			lamportQbank,
															    			i,
															    			numSources,
															    			Strategy_e.values()[strategy]);
    	parallelPacketWorkers[i] = parallelPacketWorker;
    	parallelPacketWorkerThreads[i] = new Thread(parallelPacketWorker);
    }
    
    
    // call .start() on your Workers
    //
    for(Thread tread : parallelPacketWorkerThreads)
    {
    	tread.start();
    }
    
    timer.startTimer();
    // 
    // call .start() on your Dispatcher
    // 
    dispatcherThread.start();
    
    try {
      Thread.sleep(numMilliseconds);
    } catch (InterruptedException ignore) {;}
    // 
    // assert signals to stop Dispatcher - remember, Dispatcher needs to deliver an 
    // equal number of packets from each source
    //
    doneArr[numSources].value = true;
    
    // call .join() on Dispatcher
    //
    try {
		dispatcherThread.join();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    // assert signals to stop Workers - they are responsible for leaving the queues
    // empty - use whatever protocol you like, but one easy one is to have each
    // worker verify that it's corresponding queue is empty after it observes the
    // done signal set to true
    //
    for(int i = 0; i < numSources; i++)
    {
    	doneArr[i].value = true;
    }
    
    // call .join() for each Worker
    for(Thread tread : parallelPacketWorkerThreads)
    {
    	try {
			tread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    timer.stopTimer();
    
//    final long totalCount = dispatchData.totalPackets;
    
    // Get the total number of enq packets
    long totalCount = 0;
    for (ParallelPacketWorker parallelPacketWorker : parallelPacketWorkers) {
    	totalCount += parallelPacketWorker.numOfPackets;
    }
    
    System.out.println("count: " + totalCount);
    System.out.println("time: " + timer.getElapsedTime());
    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
  }
}