//import prettyPrint;

public class MppRunner {

	public static void main(String[] args) {

        int runs = 3;
       
        System.out.println("Running Parallel Overhead test");
        System.out.println("===============================\n");
        String[] meanInputsArgs = new String[]{"25", "200", "800"};
        String[] numSourcesArgs = new String[]{"1", "4", "10"};

        long[][] serialFirewallResults = new long[3][3];
        long[][] serialQueueFirewallResults = new long[3][3];
        for (int r = 0; r < runs; r++) {
            int i = 0;
            for (String numSources : numSourcesArgs) {
                int j = 0;
                for (String mean : meanInputsArgs) {
                    String[] arguments = new String[]{"2000", numSources, mean, "false", "8", "1"};
                    serialFirewallResults[i][j] += SerialFirewall.main(arguments)/runs;
                    serialQueueFirewallResults[i][j] += SerialQueueFirewall.main(arguments)/runs;
                    j++;
                }
                i++;
            }
        }
        printHelper.csvPrinter("Serial", numSourcesArgs, serialFirewallResults);
        printHelper.csvPrinter("SerialQueue", numSourcesArgs, serialQueueFirewallResults);
        
//        excel_print("Serial", numSourcesArgs, serialFirewallResults);
//        excel_print("SerialQueue", numSourcesArgs, serialQueueFirewallResults);

        
        System.out.println("Running Dispatcher Rate test");
        System.out.println("===============================\n");
        String[] meanInputsArgs2 = {"1"};
        String[] numSourcesArgs2 = {"1", "4", "10"};
        long[][] ParallelFirewallResults = new long[3][1];
        for (int r = 0; r < runs; r++) {
            int i = 0;
            for (String numSources : numSourcesArgs2) {
                int j = 0;
                for (String mean : meanInputsArgs2) {
                    String[] arguments = {"2000", numSources, mean, "false", "8", "1"};
                    ParallelFirewallResults[i][j] += ParallelFirewall.main(arguments)/runs;
                    j++;
                }
                i++;
            }
        }

//        excel_print("Parallel", numSourcesArgs2, parallel_res2);
        printHelper.csvPrinter("Parallel", numSourcesArgs2, ParallelFirewallResults);
        
        
        System.out.println("Running Speedup with Uniform Load test");
        System.out.println("========================================\n");
        String[] meanInputsArgs3 = {"1000", "6000"};
        String[] numSourcesArgs3 = {"1", "4", "10"};
        long[][] serialFirewallResults3 = new long[3][2];
        long[][] ParallelFirewallResults3 = new long[3][2];
        for (int r = 0; r < runs; r++) {
            int i = 0;
            for (String numSources : numSourcesArgs3) {
                int j = 0;
                for (String mean : meanInputsArgs3) {
                    String[] arguments = {"2000", numSources, mean, "false", "8", "1"};
                    serialFirewallResults3[i][j] += SerialFirewall.main(arguments)/runs;
                    ParallelFirewallResults3[i][j] += ParallelFirewall.main(arguments)/runs;
                    j++;
                }
                i++;
            }
        }
//        excel_print("Serial", numSourcesArgs3, serialFirewallResults3);
//        excel_print("Parallel", numSourcesArgs3, ParallelFirewallResults3);
        printHelper.csvPrinter("Serial", numSourcesArgs3, serialFirewallResults3);
        printHelper.csvPrinter("Parallel", numSourcesArgs3, ParallelFirewallResults3);


        System.out.println("%n%n%nSpeedup with Exponentially Distributed Load");
        System.out.println("------------------%n");
        System.out.println("Running Speedup with Exponentially Distributed Load test");
        System.out.println("=========================================================\n");
        
        String[] meanInputsArgs4 = {"1000", "6000"};
        String[] numSourcesArgs4 = {"1", "8", "64"};
        long[][] serialFirewallResults4 = new long[3][2];
        long[][] ParallelFirewallResults4 = new long[3][2];
        for (int r = 0; r < runs; r++) {
            int i = 0;
            for (String numSources : numSourcesArgs4) {
                int j = 0;
                for (String mean : meanInputsArgs4) {
                    String[] arguments = {"2000", numSources, mean, "true", "8", "1"};
                    serialFirewallResults4[i][j] += SerialFirewall.main(arguments)/runs;
                    ParallelFirewallResults4[i][j] += ParallelFirewall.main(arguments)/runs;
                    j++;
                }
                i++;
            }
        }
//        excel_print("Serial", numSourcesArgs4, serialFirewallResults4);
//        excel_print("Parallel", numSourcesArgs4, ParallelFirewallResults4);
        
        printHelper.csvPrinter("Serial", numSourcesArgs4, serialFirewallResults4);
        printHelper.csvPrinter("Parallel", numSourcesArgs4, ParallelFirewallResults4);

    }

//    private static void excel_print(String name, String[] Ns, long[][] serial_queue_results) {
//        System.out.println("Name: " + name);
//        StringBuilder sb = new StringBuilder();
//        int i = 0;
//        for (long[] serial_queue_result : serial_queue_results) {
//            sb.append(Ns[i] + ",");
//            for (long l : serial_queue_result) {
//                sb.append(l + ",");
//            }
//            System.out.println(sb.toString());
//            sb.setLength(0);
//            i++;
//        }
//    }
}