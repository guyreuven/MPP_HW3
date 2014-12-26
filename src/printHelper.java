
public class printHelper {
	
	public static void prettyPrint(String header, long value) {
		
		System.out.format("%-18s %10d%n", header, value); 
	}
	
	public static void prettyPrint(String header, double value) {
		
		System.out.format("%-18s %10f%n", header, value); 
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
	
	public static void csvPrinter(String header, long[] resultsArray)	{
		
		System.out.format("----------========== Showing results for %s scenario ==========----------%n", header);
		System.out.println(header + " CSV output: ");
		StringBuilder sb = new StringBuilder();
        int i = 0;
        for (long result : resultsArray) {
        	sb.append(result + ",");
        	
            System.out.println(sb.toString());
            sb.setLength(0);
            i++;
        }
        
        System.out.println("----------==========        End of " + header + " CSV output         ==========----------");
		
	}
	
	public static void csvPrinter(String header, long[] resultsArray, double[] resultsArray2)	{
		
		System.out.format("----------========== Showing results for %s scenario ==========----------%n", header);
		System.out.println(header + " CSV output: ");
		StringBuilder sb = new StringBuilder();
        
		for (int i=0; i < resultsArray.length; i++ )	{
			sb.append(resultsArray[i] + "," + resultsArray2[i] + ", ");
			System.out.println(sb.toString());
			sb.setLength(0);
		}
        
        System.out.println("----------==========        End of " + header + " CSV output         ==========----------");
		
	}

	
	public static void csvPrinter(String header, long[][][] results3dArray)	{
		
		System.out.format("----------========== Showing results for %s scenario ==========----------%n", header);
		System.out.println(header + " CSV output: ");
		StringBuilder sb = new StringBuilder();
        for (int k = 0; k < results3dArray[0].length; k++) { //for each L
        	sb.append("\n\n\n");
            for (int i = 0; i < results3dArray[0].length; i++)
            {
                for(int j = 0; j < results3dArray[0][0].length; j++)
                {
                	 sb.append(results3dArray[k][i][j] + ",");
                }
                sb.append("\n");
            }
            System.out.println(sb.toString());
            sb.setLength(0);
            k++;
        }
        
        System.out.println("----------==========        End of " + header + " CSV output         ==========----------");
		
	}

}
