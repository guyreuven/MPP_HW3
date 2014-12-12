
public class printHelper {
	
	public static void prettyPrint(String header, long value) {
		
		System.out.format("%-18s %10d%n", header, value); 
	}
	
	public static void prettyPrint(String header, long value, String units) {
		
		System.out.format("%-18s %10d %10s%n", header, value, units);
	}

	public static void prettyPrint(String str1, String str2, String str3, String str4, String str5) {
		
		System.out.format("%-18s %10d %10s%n", str1, str2, str3); 
	}
	
	
	public static void csvPrinter(String header, String[] runInputs, long[][] results2dArray)	{
		
		System.out.format("----------========== Showing results for %s scenario ==========----------%n", header);
		System.out.println(header + " CSV output: ");
		StringBuilder sb = new StringBuilder();
        int i = 0;
        for (long[] resultArr : results2dArray) {
            sb.append(runInputs[i] + ",");
            for (long result : resultArr) {
                sb.append(result + ",");
            }
            System.out.println(sb.toString());
            sb.setLength(0);
            i++;
        }
        
        System.out.println("----------==========        End of " + header + " CSV output         ==========----------");
		
	}

}