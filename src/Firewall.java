//class SerialFirewall {
//  public static long runSerialFirewall(String[] args) {
//    final int numMilliseconds = Integer.parseInt(args[0]);   
//    final int numSources = Integer.parseInt(args[1]);
//    final long mean = Long.parseLong(args[2]);
//    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
//    final short experimentNumber = Short.parseShort(args[5]);
//    
//    StopWatch timer = new StopWatch();
//    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
//    PaddedPrimitiveNonVolatile<Boolean> done = new PaddedPrimitiveNonVolatile<Boolean>(false);
//    PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);
//        
//    SerialPacketWorker workerData = new SerialPacketWorker(done, pkt, uniformFlag, numSources);
//    Thread workerThread = new Thread(workerData);
//    
//    workerThread.start();
//    timer.startTimer();
//    try {
//      Thread.sleep(numMilliseconds);
//    } catch (InterruptedException ignore) {;}
//    done.value = true;
//    memFence.value = true;  // memFence is a 'volatile' forcing a memory fence
//    try {                   // which means that done.value is visible to the workers
//      workerThread.join();
//    } catch (InterruptedException ignore) {;}      
//    timer.stopTimer();
//    final long totalCount = workerData.totalPackets;
////    System.out.println("count: " + totalCount);
////    System.out.println("time: " + timer.getElapsedTime());
////    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
////    
//    printHelper.prettyPrint("Count", totalCount);
//    printHelper.prettyPrint("Time", timer.getElapsedTime());
//    printHelper.prettyPrint("Total packets/ms", totalCount/timer.getElapsedTime(), " pkts / ms");
//    
//    return totalCount/timer.getElapsedTime();
//  }
//}
//
//class SerialQueueFirewall {
//  public static long runSerialQueueFirewall(String[] args) {
//    final int numMilliseconds = Integer.parseInt(args[0]);   
//    final int numSources = Integer.parseInt(args[1]);
//    final long mean = Long.parseLong(args[2]);
//    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
//    final int queueDepth = Integer.parseInt(args[4]);
//    final short experimentNumber = Short.parseShort(args[5]);
//   
//    StopWatch timer = new StopWatch();
//    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
//    PaddedPrimitiveNonVolatile<Boolean> done = new PaddedPrimitiveNonVolatile<Boolean>(false);
//    PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);
//
//    // ...
//    // allocate and initialize bank of numSources Lamport queues
//    // each with depth queueDepth
//    // they should throw FullException and EmptyException upon those conditions
//    
//	LamportsQueue<Packet>[] lamportQbank = new LamportsQueue[numSources];
//    for (int i = 0; i < numSources ; i++ )	{
//    	lamportQbank[i] = new LamportsQueue<Packet>(queueDepth);
//    }
//	
////	for(LamportsQueue<Packet> lQ : lamportQbank)
////    {
////    	LamportsQueue<Packet> tmp = new LamportsQueue<Packet>(queueDepth);
//////    	lQ = new LamportsQueue<Packet>(queueDepth);
////    	lQ = tmp;
////    }
//    
//    // Create a SerialQueuePackerWorker workerData 
//    // as SerialPackerWorker, but be sure to Pass the lamport queues
//    SerialQueuePacketWorker workerData = new SerialQueuePacketWorker(done, pkt, uniformFlag, numSources, lamportQbank); 
//    
//    // The rest of the code looks as in Serial Firewall
//    Thread workerThread = new Thread(workerData);
//    
//    workerThread.start();
//    timer.startTimer();
//    try {
//      Thread.sleep(numMilliseconds);
//    } catch (InterruptedException ignore) {;}
//    done.value = true;
//    memFence.value = true;  // memFence is a 'volatile' forcing a memory fence
//    try {                   // which means that done.value is visible to the workers
//      workerThread.join();
//    } catch (InterruptedException ignore) {;}      
//    timer.stopTimer();
//    final long totalCount = workerData.totalPackets;
//    
////    System.out.println("count: " + totalCount);
////    System.out.println("time: " + timer.getElapsedTime());
////    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
//    
//    printHelper.prettyPrint("Count", totalCount);
//    printHelper.prettyPrint("Time", timer.getElapsedTime());
//    printHelper.prettyPrint("Total packets/ms", totalCount/timer.getElapsedTime(), " pkts / ms");
//    
//    return totalCount/timer.getElapsedTime();
//    
//  }
//}
//
//class ParallelFirewall {
//  public static long runParallelFirewall(String[] args) {
//    final int numMilliseconds = Integer.parseInt(args[0]);     
//    final int numSources = Integer.parseInt(args[1]);
//    final long mean = Long.parseLong(args[2]);
//    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
//    final int queueDepth = Integer.parseInt(args[4]);
//    final short experimentNumber = Short.parseShort(args[5]);
//
//    StopWatch timer = new StopWatch();
//    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
//    
//   
//    // Allocate and initialize bank of Lamport queues, as in SerialQueueFirewall
//    LamportsQueue<Packet>[] lamportQbank = new LamportsQueue[numSources];
////    for(LamportsQueue<Packet> lQ : lamportQbank)
////    {
////    	lQ = new LamportsQueue<Packet>(queueDepth);
////    }
//    
//    for (int i = 0; i < numSources ; i++ )	{
//    	lamportQbank[i] = new LamportsQueue<Packet>(queueDepth);
//    }
//    
//    // Allocate and initialize any signals used to marshal threads (eg. done signals)
//    PaddedPrimitiveNonVolatile<Boolean>[] doneArr = new PaddedPrimitiveNonVolatile[numSources+1];
//    for (int i = 0; i < numSources ; i++ )	{
//    	lamportQbank[i] = new LamportsQueue<Packet>(queueDepth);
//    	doneArr[i] = new PaddedPrimitiveNonVolatile<Boolean>(false);
//    }
//    
//    doneArr[numSources] = new PaddedPrimitiveNonVolatile<Boolean>(false); // the dispatcher doesn't need a queue
//    
////    for(PaddedPrimitiveNonVolatile<Boolean> doneSignal:doneArr)
////    {
////    	doneSignal = new PaddedPrimitiveNonVolatile<Boolean>(false);
////    }
//
//    // Allocate and initialize a Dispatcher class implementing Runnable
//    // and a corresponding Dispatcher Thread
//    Dispatcher dispatcher = new Dispatcher(doneArr[numSources], lamportQbank, numSources, uniformFlag, pkt); 
//    Thread dispatcherThread = new Thread(dispatcher);
//    
//    // Allocate and initialize an array of Worker classes (ParallelPacketWorker),
//    ParallelPacketWorker[] parallelPacketWorkers = new ParallelPacketWorker[numSources];
//    
//    // implementing Runnable and the corresponding Worker Threads
//    Thread[] parallelPacketWorkerThreads = new Thread[numSources];
//    for(int i=0; i < numSources; i++)
//    {
//    	ParallelPacketWorker parallelPacketWorker = new ParallelPacketWorker(doneArr[i], lamportQbank[i]);
//    	parallelPacketWorkers[i] = parallelPacketWorker;
//    	parallelPacketWorkerThreads[i] = new Thread(parallelPacketWorker);
//    }
//
//    
//    // Call start() for each worker
//    for(Thread tread : parallelPacketWorkerThreads)
//    {
//    	tread.start();
//    }
//
//    timer.startTimer();
//    
//    // Call start() for the Dispatcher thread
//    dispatcherThread.start();
//    try {
//      Thread.sleep(numMilliseconds);
//    } catch (InterruptedException ignore) {;}
//    
//    // assert signals to stop Dispatcher - remember, Dispatcher needs to deliver an
//    // equal number of packets from each source
//    doneArr[numSources].value = true;
//    // call .join() on Dispatcher
//    try {
//		dispatcherThread.join();
//	} catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//    // assert signals to stop Workers - they are responsible for leaving the queues
//    // empty - use whatever protocol you like, but one easy one is to have each
//    // worker verify that it's corresponding queue is empty after it observes the
//    // done signal set to true
//    for(int i = 0; i < numSources; i++)
//    {
//    	doneArr[i].value = true;
//    }
//    
//    
//    // call .join() for each Worker
//    for(Thread tread : parallelPacketWorkerThreads)
//    {
//    	try {
//			tread.join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
//    timer.stopTimer();
//    
//    // Get the total number of enq packets
//    long totalPackets = 0;
//    for (ParallelPacketWorker parallelPacketWorker : parallelPacketWorkers) {
//        totalPackets += parallelPacketWorker.numOfPackets;
//    }
//    
//    // Output the statistics
////    System.out.println("count: " + totalPackets);
////    System.out.println("time: " + timer.getElapsedTime());
//    printHelper.prettyPrint("Count", totalPackets);
//    printHelper.prettyPrint("Time", timer.getElapsedTime());
//    printHelper.prettyPrint("Total packets/ms", totalPackets/timer.getElapsedTime(), " pkts / ms");
//    long retVal = totalPackets / timer.getElapsedTime();
////    System.out.println(retVal + " pkts / ms");
//    return retVal;
//  }
//}
