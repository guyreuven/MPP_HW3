//import prettyPrint;

public class MppRunner {

	public static void main(String[] args) {
		int runs 				= 4;
        int uniform_runs 		= 5;
        int exponential_runs 	= 11;
        String M				= "2000";	// test running time
        
        final int runningScenario = 0;
        
        switch (runningScenario) {
		case 0:	// Counter test
			System.out.println("Running Idle Lock Overhead test");
	        System.out.println("===============================\n");
	        
	        String[] numSourcesArgs = new String[]{"1"};	// num of threads
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
	            	String[] arguments = new String[]{M, "1", lock};
	            	parallelCounterResults[i] += ParallelCounter.main(arguments)/runs; 
	                i++;
	            }
	        }
	        
        }
	}
}
	        
//	        long[][] serialFirewallResults = new long[3][3];
//	        long[][] serialQueueFirewallResults = new long[3][3];
//	        for (int r = 0; r < runs; r++) {
//	            int i = 0;
//	            for (String numSources : numSourcesArgs) {
//	                int j = 0;
//	                for (String mean : meanInputsArgs) {
//	                    String[] arguments = new String[]{"2000", numSources, mean, "false", "8", "1"};
//	                    serialFirewallResults[i][j] += SerialFirewall.runSerialFirewall(arguments)/runs;
//	                    serialQueueFirewallResults[i][j] += SerialQueueFirewall.runSerialQueueFirewall(arguments)/runs;
//	                    j++;
//	                }
//	                i++;
//	            }
//	        }
//	        printHelper.csvPrinter("Serial (Test #1)", numSourcesArgs, serialFirewallResults);
//	        printHelper.csvPrinter("SerialQueue (Test #1)", numSourcesArgs, serialQueueFirewallResults);
//			break;
//
//		default:
//			break;
//		}
//        
//        
//       
//        System.out.println("Running Parallel Overhead test");
//        System.out.println("===============================\n");
//        String[] meanInputsArgs = new String[]{"25", "200", "800"};
//        String[] numSourcesArgs = new String[]{"1", "4", "10"};
//
//        long[][] serialFirewallResults = new long[3][3];
//        long[][] serialQueueFirewallResults = new long[3][3];
//        for (int r = 0; r < runs; r++) {
//            int i = 0;
//            for (String numSources : numSourcesArgs) {
//                int j = 0;
//                for (String mean : meanInputsArgs) {
//                    String[] arguments = new String[]{"2000", numSources, mean, "false", "8", "1"};
//                    serialFirewallResults[i][j] += SerialFirewall.runSerialFirewall(arguments)/runs;
//                    serialQueueFirewallResults[i][j] += SerialQueueFirewall.runSerialQueueFirewall(arguments)/runs;
//                    j++;
//                }
//                i++;
//            }
//        }
//        printHelper.csvPrinter("Serial (Test #1)", numSourcesArgs, serialFirewallResults);
//        printHelper.csvPrinter("SerialQueue (Test #1)", numSourcesArgs, serialQueueFirewallResults);
//        
//        
//        System.out.println("Running Dispatcher Rate test");
//        System.out.println("===============================\n");
//
//        String[] meanInputsArgs2 = {"1","25","100"};
//        String[] numSourcesArgs2 = {"1", "4", "10"};
//        long[][] ParallelFirewallResults = new long[3][3];
//
//        for (int r = 0; r < runs; r++) {
//            int i = 0;
//            for (String numSources : numSourcesArgs2) {
//                int j = 0;
//                for (String mean : meanInputsArgs2) {
//
//                    String[] arguments = {"2000", numSources, mean, "false", "8", "1"};
//                    ParallelFirewallResults[i][j] += ParallelFirewall.runParallelFirewall(arguments)/runs;
//
//                    j++;
//                }
//                i++;
//            }
//        }
//
//
//        printHelper.csvPrinter("Parallel", numSourcesArgs2, ParallelFirewallResults);
//        
//        
//        System.out.println("Running Speedup with Uniform Load test");
//        System.out.println("========================================\n");
//        String[] meanInputsArgs3 = new String[]{"1000", "6000"};
//        String[] numSourcesArgs3 = new String[]{"1", "4", "10"};
//        long[][] serialFirewallResults3 = new long[3][2];
//        long[][] ParallelFirewallResults3 = new long[3][2];
//        for (int r = 0; r < runs; r++) {
//            int i = 0;
//            for (String numSources : numSourcesArgs3) {
//                int j = 0;
//                for (String mean : meanInputsArgs3) {
//
//                    String[] arguments = {"2000", numSources, mean, "false", "8", "1"};
//                    serialFirewallResults3[i][j] += SerialFirewall.runSerialFirewall(arguments)/runs;
//                    ParallelFirewallResults3[i][j] += ParallelFirewall.runParallelFirewall(arguments)/runs;
//
//                    j++;
//                }
//                i++;
//            }
//        }
//
//        printHelper.csvPrinter("Serial", numSourcesArgs3, serialFirewallResults3);
//        printHelper.csvPrinter("Parallel", numSourcesArgs3, ParallelFirewallResults3);
//
//
//        System.out.println("Running Speedup with Exponentially Distributed Load test");
//        System.out.println("=========================================================\n");
//        
//        String[] meanInputsArgs4 = new String[]{"1000", "6000"};
//        String[] numSourcesArgs4 = new String[]{"1", "8", "64"};
//        long[][] serialFirewallResults4 = new long[3][2];
//        long[][] ParallelFirewallResults4 = new long[3][2];
//        for (int r = 0; r < runs; r++) {
//            int i = 0;
//            for (String numSources : numSourcesArgs4) {
//                int j = 0;
//                for (String mean : meanInputsArgs4) {
//
//                    String[] arguments = {"2000", numSources, mean, "true", "8", "1"};
//                    serialFirewallResults4[i][j] += SerialFirewall.runSerialFirewall(arguments)/runs;
//                    ParallelFirewallResults4[i][j] += ParallelFirewall.runParallelFirewall(arguments)/runs;
//
//                    j++;
//                }
//                i++;
//            }
//        }
//        
//        printHelper.csvPrinter("Serial (Test #4)", numSourcesArgs4, serialFirewallResults4);
//        printHelper.csvPrinter("Parallel (Test #4)", numSourcesArgs4, ParallelFirewallResults4);
//
//    }
//}