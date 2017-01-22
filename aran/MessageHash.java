package aran;

/*
 * To use MessageHash:
 * Create MessageHash Object
 * The message formats are stored in mranges0 and mranges1
 * Max Product of numbers in ranges0 must be less than 268435456
 * Max Product of numbers in ranges1 must be less than 4294967296
 * 
 * To hash a message, run hashMessage(messageType, the first list, second list)
 * 
 * After receiving the signal, get the MessageType by getMessageType(the '0' index of the message)
 * 
 * To unhash the message, do unhashMessage(array of 2 integers)
 * 
 */
public class MessageHash{
	private int[][] mranges0 = {{1000,1000,100,50,100}, 
			   {2,2,2},
			   {12,12,12}
			  };

	private int[][] mranges1 = {{4,4,4,50,100,50,100},
			   {2,2,2},
			   {23,23,2}
			  };
	private int[] ranges0Product;
	private int[] ranges1Product;
	
	private int maxX;
	
	public MessageHash(){
		ranges0Product = new int[mranges0.length];
		ranges1Product = new int[mranges1.length];
		for(int i=0;i<mranges0.length;i++){
			ranges0Product[i] = 16;
			ranges1Product[i] = 1;
			for(int j=1;j<mranges0[i].length;j++){ //!!!!!!!! j = 1
				ranges0Product[i] *= mranges0[i][j];				
			}
			for(int j=1;j<mranges1[i].length;j++){
				ranges1Product[i] *= mranges1[i][j];
			}
		}
		maxX = 500 + Constants.MAP_MAX_WIDTH + 10;
	}
	
	
	
	/*
	 * lalalala
	 */
    public int[] hashMessage(int msgType, int[] list0, int[] list1){
    	int[] ranges0 = mranges0[msgType];
    	int[] ranges1 = mranges1[msgType];
    	int[] reMsg = new int[2];
     	reMsg[0] = msgType - (Integer.MAX_VALUE - 1);
     	
     	int multiplier = 16;
     	for(int i=list0.length-1;i>-1;i--){
     		reMsg[0] += multiplier * list0[i];
     		multiplier *= ranges0[i];
     	}
     	
     	multiplier = 1;
     	reMsg[1] = -(Integer.MAX_VALUE - 1);
     	for(int i=list1.length-1;i>-1;i--){
     		reMsg[1] += multiplier * list1[i];
     		multiplier *= ranges1[i];
     	}
     	return reMsg;
    }
    
    public int getMessageType(int s){
    	long l0 = s;
        l0 += Integer.MAX_VALUE - 1;
        return (int) (l0%16);
    }
    /*
     * takes in an int list of length 2 (signal).
     * returns a 2d array list of size 2 containing 2 lists of ints
     */
    public int[][] unhashMessage(int[] s){
     	long l0 = s[0];
         l0 += Integer.MAX_VALUE - 1;
         long l1 = s[1];
         l1 += Integer.MAX_VALUE - 1;
         int msgType = (int) (l0%16);
         int[] ranges0 = mranges0[msgType];
     	int[] ranges1 = mranges1[msgType];
         int[] list0 = new int[ranges0.length];
     	int[] list1 = new int[ranges1.length];
     	
         int product0 = ranges0Product[msgType];
         for(int i=0;i<list0.length-1;i++){
         	list0[i] = (int)(l0/product0);
         	l0 -= list0[i] * product0;
         	product0 /= ranges0[i+1];
         }
         list0[list0.length-1] = (int)l0/product0;
         
         int product1 = ranges1Product[msgType];
         for(int i=0;i<list1.length-1;i++){
         	list1[i] = (int)(l1/product1);
         	l1 -= list1[i] * product1;
         	product1 /= ranges1[i+1];
         }
         list1[list1.length-1] = (int)l1/product1;
     	
     	int[][] rList = {list0, list1};
     	return rList;
     };
    
    public int[] fastHash(int type, int x1, int y1, int x2, int y2){
    	int m1 = type*maxX*maxX + x1*maxX + y1;
    	int m2 = x2*maxX + y2;
    	int[] ans = {m1,m2};
    	return ans;
    }
    
    public int fastHashType(int m1){
    	return m1/maxX/maxX;
    }
    
    public int[] fastUnHash(int[] m){
    	int[] ans = new int[5];
    	
    	ans[0] = m[0]/maxX/maxX;
    	m[0] -= ans[0]*maxX*maxX;
    	ans[1] = m[0]/maxX;
    	ans[2] = m[0]%maxX;
    	
    	ans[3] = m[1]/maxX;
    	ans[4] = m[1]%maxX;
    	
    	return ans;
    }
}