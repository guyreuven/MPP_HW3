import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

//import prettyPrint;

public class MppRunner {

	public static void main(String[] args) {
		int runs 				= 4;
		int uniform_runs 		= 5;
		int exponential_runs 	= 11;
		String M				= "2000";	// test running time

		String[] pickingQueueAlg = null;
		String[] meanInputsArgs  = null;

		final Tests_e runningScenario = Tests_e.PACKET_IdleLockOverhead;

		switch (runningScenario) {
		case COUNTER_IdleLockOverhead:	
			System.out.println("Running Idle Lock Overhead test");
			System.out.println("===============================\n");

			String[] numSourcesArgs = new String[]{"1"};				// num of threads
			String[] lockInputsArgs = new String[]{"0", "1", "4", "5"};	// choosing a lock 

			long serialCounterResults = 0;
			long[] parallelCounterResults = new long[4];

			for (int r = 0; r < runs; r++) {
				String[] arguments = new String[]{M};
				serialCounterResults += SerialCounter.main(arguments)/runs;
			}
			printHelper.prettyPrint("Serial Counter Result:", serialCounterResults );

			for (int r = 0; r < runs; r++) {
				int i = 0;
				for (String lock : lockInputsArgs) {
					String[] arguments = new String[]{M, "1", lock, "1"};
					parallelCounterResults[i] += Long.valueOf(ParallelCounter.main(arguments))/runs; 
					i++;
				}
			}
			printHelper.csvPrinter("Parallel Counter (Test #1)", parallelCounterResults);

			break;
		case COUNTER_LockScaling:	
			// Optimize the DELAY params

			System.out.println("Running Backoff Lock optimization test");
			System.out.println("======================================\n");
			String[] arguments = new String[]{M, "32", "1"};
			long optimizedMinResult = ParallelCounterBackOffLockBenckmark.main(arguments);
			System.out.println(optimizedMinResult);



			break;
		case COUNTER_Fairness:

			System.out.println("Running Fairness test");

			System.out.println("======================\n");

			//	        numSourcesArgs = new String[]{"32"};				// num of threads
			lockInputsArgs = new String[]{"0", "1", "4", "5"};	// choosing a lock 

			long[] parallelCounterThroughputResults = new long[4*runs];
			double[] parallelCounterDeviationResults = new double[4*runs];

			// we don't normalize scatter graph on the num of runs
			for (int r = 0; r < runs; r++) {
				int i = 0;
				for (String lock : lockInputsArgs) {
					arguments = new String[]{M, "32", lock, "3"};
					String result = ParallelCounter.main(arguments);
					final List<String> items = Arrays.asList(result.split("\\s*,\\s*"));

					parallelCounterDeviationResults[i+r] = Double.valueOf(items.get(0));
					parallelCounterThroughputResults[i+r] = Long.valueOf(items.get(1));

					i++;
				}
			}
			printHelper.csvPrinter("Parallel Counter Fairness (Test #3)", parallelCounterThroughputResults, parallelCounterDeviationResults);

			break;
		case PACKET_IdleLockOverhead:
			System.out.println("Running PACKET_IdleLockOverhead test");
			System.out.println("====================================\n");

			numSourcesArgs = new String[]{"1"};				// num of threads
			lockInputsArgs = new String[]{"0", "1", "4", "5"};	// choosing a lock
			pickingQueueAlg = new String[]{"0","1"};	// LockFree((short) 0, HomeQueue ((short) 1), RandomQueue ((short) 2), LastQueue((short) 3);
			meanInputsArgs = new String[]{"25", "200", "800"};

			long[][][] parallelPacketResults = new long[4][2][3];

			for (int r = 0; r < uniform_runs; r++)
			{
				int i = 0;
				int j = 0;
				int k = 0;
				for (String lock : lockInputsArgs)
				{
					for(String S : pickingQueueAlg)
					{
						for(String W : meanInputsArgs)
						{
							System.out.println("Lock: " + lock + "  S: " + S + "  W: " + W);
							arguments = new String[]{M, "1", W, "true","1","8",lock,S}; //M,n,W,Uniform flag,experimentNum,queue depth,lockType,S
							parallelPacketResults[k][j][i] += ParallelPacket.main(arguments)/uniform_runs; 
							i++;
						}
						i = 0;
						j++;
					}
					j = 0;
					k++;
				}
			}
			printHelper.csvPrinter("Packet (Test #1)", parallelPacketResults);
			System.out.println(Arrays.deepToString(parallelPacketResults));
			break;

		case PACKET_SpeedupWithUniformLoad_Phase1:
			System.out.println("Running PACKET_SpeedupWithUniformLoad test");
			System.out.println("==========================================\n");

			//			meanInputsArgs = new String[]{"1000", "1600"};
			numSourcesArgs = new String[]{"1", "4", "10"};
			lockInputsArgs = new String[]{"0", "1"};
			String[] strategyInputsArgs = new String[]{"0", "2", "3"};

			long[][][] parallelUniformPacketResults = new long [3][2][3];
			long[] serialUniformPacketResults = new long [3];

			for (int r = 0; r < uniform_runs; r++) {
				int j = 0;
				for (String numSources : numSourcesArgs) {
					int l = 0;
					for (String lock : lockInputsArgs)	{
						int k = 0;
						for (String strategy : strategyInputsArgs)	{
							System.out.println("Lock: " + lock + "\t\tStrategy: " + strategy + "\tMean: 1000" + "\tNumSources: " + numSources);
							arguments = new String[]{M, numSources, "1000", "true", "4", "8", lock, strategy};
							parallelUniformPacketResults[j][l][k] += ParallelPacket.main(arguments)/uniform_runs;

							k++;
						}
						l++;
					}
					String[] args2 = new String[]{M, numSources, "1000", "true", "4"};
					serialUniformPacketResults[j] += SerialPacket.main(args2)/uniform_runs;

					j++;
				}
			}
			printHelper.csvPrinter("Serial Uniform Packet Results (Test #5): Mean = 1000", serialUniformPacketResults);
			printHelper.csvPrinter("Parallel Uniform Packet Results (Test #5): Mean = 1000", parallelUniformPacketResults);

			break;
		case PACKET_SpeedupWithUniformLoad_Phase2:
			System.out.println("Running PACKET_SpeedupWithUniformLoad test");
			System.out.println("==========================================\n");

			//			meanInputsArgs = new String[]{"1000", "1600"};
			numSourcesArgs = new String[]{"1", "4", "10"};
			lockInputsArgs = new String[]{"0", "1"};
			strategyInputsArgs = new String[]{"0", "2", "3"};

			parallelUniformPacketResults = new long [3][2][3];
			serialUniformPacketResults = new long [3];

			for (int r = 0; r < uniform_runs; r++) {
				int j = 0;
				for (String numSources : numSourcesArgs) {
					int l = 0;
					for (String lock : lockInputsArgs)	{
						int k = 0;
						for (String strategy : strategyInputsArgs)	{
							System.out.println("Lock: " + lock + "\t\tStrategy: " + strategy + "\tMean: 1600" + "\tNumSources: " + numSources);
							arguments = new String[]{M, numSources, "1600", "true", "4", "8", lock, strategy};
							parallelUniformPacketResults[j][l][k] += ParallelPacket.main(arguments)/uniform_runs;

							k++;
						}
						l++;
					}
					String[] args2 = new String[]{M, numSources, "1600", "true", "4"};
					serialUniformPacketResults[j] += SerialPacket.main(args2)/uniform_runs;

					j++;
				}
			}
			printHelper.csvPrinter("Serial Uniform Packet Results (Test #5): Mean = 1600", serialUniformPacketResults);
			printHelper.csvPrinter("Parallel Uniform Packet Results (Test #5): Mean = 1600", parallelUniformPacketResults);

			break;

		case PACKET_SpeedupWithExponentialLoad_Phase1:
			System.out.println("Running PACKET_SpeedupWithUniformLoad test");
			System.out.println("==========================================\n");

			//			meanInputsArgs = new String[]{"1000", "1600"};
			numSourcesArgs = new String[]{"1", "4", "10"};
			//	        lockInputsArgs = new String[]{"0", "1"};
			strategyInputsArgs = new String[]{"0", "2", "3"};

			long[][] parallelUniformPacketResults_2d = new long [3][3];
			serialUniformPacketResults = new long [3];

			for (int r = 0; r < exponential_runs; r++) {
				int j = 0;
				for (String numSources : numSourcesArgs) {
					int l = 0;
					for (String strategy : strategyInputsArgs)	{
						System.out.println("Lock: " + "0" + "\t\tStrategy: " + strategy + "\tMean: 1000" + "\tNumSources: " + numSources);
						arguments = new String[]{M, numSources, "1000", "true", "4", "8", "0", strategy};
						parallelUniformPacketResults_2d[j][l] += ParallelPacket.main(arguments)/exponential_runs;
						l++;
					}
					String[] args2 = new String[]{M, numSources, "1000", "true", "4"};
					serialUniformPacketResults[j] += SerialPacket.main(args2)/exponential_runs;

					j++;
				}
			}
			printHelper.csvPrinter("Serial Uniform Packet Results (Test #6): Mean = 1000", serialUniformPacketResults);
			printHelper.csvPrinter("Parallel Uniform Packet Results (Test #6): Mean = 1000, lock = 0", parallelUniformPacketResults_2d);

			break;
		case PACKET_SpeedupWithExponentialLoad_Phase2:
			System.out.println("Running PACKET_SpeedupWithUniformLoad test");
			System.out.println("==========================================\n");

			//			meanInputsArgs = new String[]{"1000", "1600"};
			numSourcesArgs = new String[]{"1", "4", "10"};
			//	        lockInputsArgs = new String[]{"0", "1"};
			strategyInputsArgs = new String[]{"0", "2", "3"};

			parallelUniformPacketResults_2d = new long [3][3];
			serialUniformPacketResults = new long [3];

			for (int r = 0; r < exponential_runs; r++) {
				int j = 0;
				for (String numSources : numSourcesArgs) {
					int l = 0;
					for (String strategy : strategyInputsArgs)	{
						System.out.println("Lock: " + "1" + "\t\tStrategy: " + strategy + "\tMean: 1000" + "\tNumSources: " + numSources);
						arguments = new String[]{M, numSources, "1000", "true", "4", "8", "1", strategy};
						parallelUniformPacketResults_2d[j][l] += ParallelPacket.main(arguments)/exponential_runs;
						l++;
					}
					String[] args2 = new String[]{M, numSources, "1000", "true", "4"};
					serialUniformPacketResults[j] += SerialPacket.main(args2)/exponential_runs;

					j++;
				}
			}
			printHelper.csvPrinter("Serial Uniform Packet Results (Test #6): Mean = 1000", serialUniformPacketResults);
			printHelper.csvPrinter("Parallel Uniform Packet Results (Test #6): Mean = 1000, lock = 1", parallelUniformPacketResults_2d);

			break;
		case PACKET_SpeedupWithExponentialLoad_Phase3:
			System.out.println("Running PACKET_SpeedupWithUniformLoad test");
			System.out.println("==========================================\n");

			//			meanInputsArgs = new String[]{"1000", "1600"};
			numSourcesArgs = new String[]{"1", "4", "10"};
			//			lockInputsArgs = new String[]{"0", "1"};
			strategyInputsArgs = new String[]{"0", "2", "3"};

			parallelUniformPacketResults_2d = new long [3][3];
			serialUniformPacketResults = new long [3];

			for (int r = 0; r < exponential_runs; r++) {
				int j = 0;
				for (String numSources : numSourcesArgs) {
					int l = 0;
					for (String strategy : strategyInputsArgs)	{
						System.out.println("Lock: " + "0" + "\t\tStrategy: " + strategy + "\tMean: 1600" + "\tNumSources: " + numSources);
						arguments = new String[]{M, numSources, "1600", "true", "4", "8", "0", strategy};
						parallelUniformPacketResults_2d[j][l] += ParallelPacket.main(arguments)/exponential_runs;
						l++;
					}
					String[] args2 = new String[]{M, numSources, "1600", "true", "4"};
					serialUniformPacketResults[j] += SerialPacket.main(args2)/exponential_runs;

					j++;
				}
			}
			printHelper.csvPrinter("Serial Uniform Packet Results (Test #6): Mean = 1600", serialUniformPacketResults);
			printHelper.csvPrinter("Parallel Uniform Packet Results (Test #6): Mean = 1600, lock = 0", parallelUniformPacketResults_2d);

			break;
		case PACKET_SpeedupWithExponentialLoad_Phase4:
			System.out.println("Running PACKET_SpeedupWithUniformLoad test");
			System.out.println("==========================================\n");

			//			meanInputsArgs = new String[]{"1000", "1600"};
			numSourcesArgs = new String[]{"1", "4", "10"};
			//			lockInputsArgs = new String[]{"0", "1"};
			strategyInputsArgs = new String[]{"0", "2", "3"};

			parallelUniformPacketResults_2d = new long [3][3];
			serialUniformPacketResults = new long [3];

			for (int r = 0; r < exponential_runs; r++) {
				int j = 0;
				for (String numSources : numSourcesArgs) {
					int l = 0;
					for (String strategy : strategyInputsArgs)	{
						System.out.println("Lock: " + "1" + "\t\tStrategy: " + strategy + "\tMean: 1600" + "\tNumSources: " + numSources);
						arguments = new String[]{M, numSources, "1600", "true", "4", "8", "1", strategy};
						parallelUniformPacketResults_2d[j][l] += ParallelPacket.main(arguments)/exponential_runs;
						l++;
					}
					String[] args2 = new String[]{M, numSources, "1600", "true", "4"};
					serialUniformPacketResults[j] += SerialPacket.main(args2)/exponential_runs;

					j++;
				}
			}
			printHelper.csvPrinter("Serial Uniform Packet Results (Test #6): Mean = 1600", serialUniformPacketResults);
			printHelper.csvPrinter("Parallel Uniform Packet Results (Test #6): Mean = 1600 , lock = 1", parallelUniformPacketResults_2d);
			break;
		}
	}
}
