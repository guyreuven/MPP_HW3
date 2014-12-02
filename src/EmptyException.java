
public class EmptyException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2552092792373569593L;

	public EmptyException()
	{
		super("Queue is empty.");
	}
}
