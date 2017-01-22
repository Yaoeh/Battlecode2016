package aran;

public interface MessageHashConstants {
	int[][] mranges0 = {{16,3000}, 
			   {2,2,2},
			   {12,12,12}
			  };

	int[][] mranges1 = {{100,100,100,100},
			   {2,2,2},
			   {23,23,2}
			  };
	
	public static boolean isMessageValid(int type, int[] arg0, int []arg1){ //Can call this method to check for message errors
		boolean answer= false;
		//If the type ranges is within bounds of both message types, and if the length of the int array is the same for the specific types in both ranges
		if (type>= 0 && type < mranges0.length && type < mranges1.length && arg0.length== mranges0[type].length && arg1.length== mranges1[type].length){
			boolean arg0Verified= true;
			boolean arg1Verified= true;
			
			for (int n = 0; n< arg0.length; n++){
				if (arg0[n]> 0 && arg0[n] > mranges0[type][n]){ //If one of the value exceeds the bound, the message is false
					arg0Verified= false;
					break;
				}
			}
			for (int n2= 0; n2< arg1.length; n2++){
				if (arg1[n2]> 0 && arg1[n2] > mranges0[type][n2]){ //If one of the value exceeds the bound, the message is false
					arg1Verified= false;
					break;
				}			
			}
			
			if (arg0Verified && arg1Verified){
				answer= true;
			}else{
				System.out.println("Message invald: " + "int[0]: "+ arg0Verified + " int[1]"+ arg1Verified);
			}
		}
		return answer;
	}
}