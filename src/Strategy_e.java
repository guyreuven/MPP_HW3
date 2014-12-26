
public enum Strategy_e {
	
	LockFree((short) 0), HomeQueue ((short) 1), RandomQueue ((short) 2), LastQueue((short) 3);
	
    private short val;
    
    private Strategy_e(short val){
        this.val = val;
    }
    
}
