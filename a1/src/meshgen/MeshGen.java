package meshgen;

public class MeshGen {
	
	public int divisionsU;
	public int divisionV;
	
    public static void main (String[] args) {
    	if (args[0].equals("-g")){
    		System.out.println("There is a geometry specifier");
    		if(args[1].equals("sphere")){
    			System.out.println("Make sphere mesh");
    		}
    		else if(args[1].equals("cylinder")){
    			System.out.println("Make cylinder mesh");
    		}
    		else{
    			System.out.println("Not valid input");
    		}
    	}
    	else {
    		System.out.println("We are working with an input file");
    	}
    	
//        for (String s: args) {
//            System.out.println(s);
//        }
    }
    
}
