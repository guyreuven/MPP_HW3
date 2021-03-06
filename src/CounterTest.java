//import java.util.lang.*;
// This application launches a single worker who implements a counter with no locks
class SerialCounter {
  public static long main(String[] args) {  
    final int numMilliseconds = Integer.parseInt(args[0]);
    
    StopWatch timer = new StopWatch();
    PaddedPrimitive<CounterStruct> counter = new PaddedPrimitive<CounterStruct>(new CounterStruct());
    PaddedPrimitiveNonVolatile<Boolean> done = new PaddedPrimitiveNonVolatile<Boolean>(false);
    Thread workerThread = new Thread(new SoloCounterWorker(counter,done), new String("SoloWorker"));
    workerThread.start();
    timer.startTimer();
    try {
      Thread.sleep(numMilliseconds);
    } catch (InterruptedException ignore) {;}
    done.value = true;
    timer.stopTimer();
    final long totalCount = counter.value.counter;
    try {
      workerThread.join();
    } catch (InterruptedException ignore) {;}
//    System.out.println("count: " + totalCount);
//    System.out.println("time: " + timer.getElapsedTime());
//    System.out.println(totalCount/timer.getElapsedTime() + " inc / ms");
    
    printHelper.prettyPrint("Count", totalCount);
    printHelper.prettyPrint("Time", timer.getElapsedTime());
    printHelper.prettyPrint("Total increments/ms", totalCount/timer.getElapsedTime(), " inc / ms");
  
  	return totalCount/timer.getElapsedTime();
    
  }  
}

// This application launches numThreads workers who try to lock the counter and increment it
class ParallelCounter {
  public static String main(String[] args) {
    final int numMilliseconds = Integer.parseInt(args[0]);
    final int numThreads = Integer.parseInt(args[1]);
    final int lockType = Integer.parseInt(args[2]);
    final int returnType = Integer.parseInt(args[3]);
		
    PaddedPrimitive<CounterStruct> counter = new PaddedPrimitive<CounterStruct>(new CounterStruct());
    PaddedPrimitiveNonVolatile<Boolean> done = new PaddedPrimitiveNonVolatile<Boolean>(false);
    StopWatch timer = new StopWatch();
    Lock lock;
    LockAllocator la = new LockAllocator();
    lock = la.getLock(lockType);
    la.printLockType(lockType);
    
    lock.lock(); // I'll grab the lock and then later unlock as I release the workers

    Thread[] workerThread = new Thread[numThreads];
    CounterWorker[] workerData = new CounterWorker[numThreads];

    for( int i = 0; i < numThreads; i++ ) {
      workerData[i] = new CounterWorker(counter,done,lock);
      workerThread[i] = new Thread(workerData[i], new String("Worker"+i));
    }

    for( int i = 0; i < numThreads; i++ ) 
      workerThread[i].start();
      
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {;}
    
    lock.unlock(); // release the hounds
    timer.startTimer();
    try {
      Thread.sleep(numMilliseconds); // wait for a while
    } catch (InterruptedException e) {;}
    lock.lock(); // stop the madness
    timer.stopTimer(); // measure the throughput...
    done.value = true;
    final long totalCount = counter.value.counter;
//    System.out.println("count: " + totalCount);
//    System.out.println("time: " + timer.getElapsedTime());
//    System.out.println(totalCount/timer.getElapsedTime() + " inc / ms");
    printHelper.prettyPrint("Count", totalCount);
    printHelper.prettyPrint("Time", timer.getElapsedTime());
    printHelper.prettyPrint("Total packets/ms", totalCount/timer.getElapsedTime(), " inc / ms");

    lock.unlock(); // give the workers a chance to see done.value == true
    
    long[] count = new long[numThreads];
    for( int i = 0; i < numThreads; i++ ) {
      try {
        workerThread[i].join();
        count[i] = workerData[i].count; // collect their independent counts
      } catch (InterruptedException ignore) {;}      
    }
    System.out.println(Statistics.getStdDev(count)/timer.getElapsedTime());
    
    switch (returnType) {
	case 1:	// return throughput
		return String.valueOf(totalCount/timer.getElapsedTime());
	case 2:	// return Standard Deviation
		return String.valueOf(Statistics.getStdDev(count)/timer.getElapsedTime());
	case 3:	// return both throughput and Standard Deviation
//		String[] retArr = new String[2];
//		retArr[0] = String.valueOf(Statistics.getStdDev(count));
//		retArr[1] = String.valueOf(totalCount/timer.getElapsedTime());
		return String.valueOf(Statistics.getStdDev(count)/timer.getElapsedTime()) + ", " + String.valueOf(totalCount/timer.getElapsedTime());

	default:
		return String.valueOf(totalCount/timer.getElapsedTime());
	}
    
  }  
}
//This application launches numThreads workers who try to lock the counter and increment it
class ParallelCounterBackOffLockBenckmark {
	static BackoffLock lock = null;
	
	public static long main(String[] args) {
		final int numMilliseconds = Integer.parseInt(args[0]);
		final int numThreads = Integer.parseInt(args[1]);

		
		lock = new BackoffLock();
		
		lock.setMinDelay(0);

		long lowResult = 0;
		long highResult = 1000000000;
		float minDeviation = (float) 0.0001;	// set the point where the change isn't relevant anymore
		long oldTestResult = 1;	// initialized value
		long testResult = ParallelCounterSingleIter(numThreads, numMilliseconds);
		long tmpTestResult = 0;
		
		for(long delay = 0; delay < 1000000; delay+=1000)
		{
			lock.setMinDelay(delay);
			for(int r = 0; r < 3; r++)
			{
				tmpTestResult += ParallelCounterSingleIter(numThreads, numMilliseconds)/3;
			}
			
			System.out.println("Delay = " + delay + " result = " + tmpTestResult);
		}
//		while ( Math.abs((testResult/oldTestResult)-1) > minDeviation ) 	{
//			int midResult = (int) ( lowResult + (highResult-lowResult)/2 );
//			System.out.println(midResult);
//			lock.setMinDelay(midResult);
//			
//			for (int r = 0; r < 5; r++)	{
//				tmpTestResult += (long) ParallelCounterSingleIter(numThreads, numMilliseconds)/5;
//			}
////			long tmpTestResult = ParallelCounterSingleIter(numThreads, numMilliseconds);
//			if (tmpTestResult > testResult)	{
//				lowResult = midResult;
//			}
//			else	{
//				highResult = midResult;
//			}
//			oldTestResult = testResult;
//			testResult = tmpTestResult;
//			tmpTestResult = 0;
//		}
		
		System.out.println("Low Result is : " + Long.toString(lowResult));
		System.out.println("High Result is: " + Long.toString(highResult));
		return lowResult;
	}
		
		
	private static long ParallelCounterSingleIter(int numThreads, int numMilliseconds)	{
		PaddedPrimitive<CounterStruct> counter = new PaddedPrimitive<CounterStruct>(new CounterStruct());
		PaddedPrimitiveNonVolatile<Boolean> done = new PaddedPrimitiveNonVolatile<Boolean>(false);
		StopWatch timer = new StopWatch();
		
		lock.lock(); // I'll grab the lock and then later unlock as I release the workers

		Thread[] workerThread = new Thread[numThreads];
		CounterWorker[] workerData = new CounterWorker[numThreads];

		for( int i = 0; i < numThreads; i++ ) {
			workerData[i] = new CounterWorker(counter,done,lock);
			workerThread[i] = new Thread(workerData[i], new String("Worker"+i));
		}

		for( int i = 0; i < numThreads; i++ ) 
			workerThread[i].start();

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {;}

		lock.unlock(); // release the hounds
		timer.startTimer();
		try {
			Thread.sleep(numMilliseconds); // wait for a while
		} catch (InterruptedException e) {;}
		lock.lock(); // stop the madness
		timer.stopTimer(); // measure the throughput...
		done.value = true;
		final long totalCount = counter.value.counter;
		printHelper.prettyPrint("Count", totalCount);
		printHelper.prettyPrint("Time", timer.getElapsedTime());
		printHelper.prettyPrint("Total increments/ms", totalCount/timer.getElapsedTime(), " inc / ms");


		lock.unlock(); // give the workers a chance to see done.value == true

		long[] count = new long[numThreads];
		for( int i = 0; i < numThreads; i++ ) {
			try {
				workerThread[i].join();
				count[i] = workerData[i].count; // collect their independent counts
			} catch (InterruptedException ignore) {;}      
		}
		System.out.println(Statistics.getStdDev(count)/timer.getElapsedTime());

		return totalCount/timer.getElapsedTime();
	} 
}