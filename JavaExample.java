import java.util.*;

import com.microsoft.z3.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.*;

class JavaExample
{
	    int noOfConstraints = 100;	
	    @SuppressWarnings("serial")
	    class TestFailedException extends Exception
	    {
		public TestFailedException()
		{
		    super("Check FAILED");
		}
	    };
	public Optimize.Handle assertAllConstraints(Context ctx, Optimize opt,BoolExpr[] hardConstraints, BoolExpr[] softConstraints, int[] softConstraintWeights) throws TestFailedException
	{
		assertHardConstraints(ctx, opt, hardConstraints);
		log("Checking if the asserted hard constraints are satisfiable...");
		Status status = opt.Check();
		if(status!=Status.SATISFIABLE )
		{	
		        log("Hard constraints are not satisfiable or unkonwn.");
			return null;
		}
		log("Hard constraints are satisfiable. Applying weighted soft constraints.");
		Optimize.Handle mx = assertWeightedSoftConstraints(ctx, opt,softConstraints, softConstraintWeights);
			
		status = opt.Check();
		if(status!=Status.SATISFIABLE )
		{	
		        log("Soft constraints are not satisfiable or unkonwn.");
			return null;
		}
		log("Soft constraints are satisfiable");
		return mx;
	}

	public void assertHardConstraints(Context ctx, Optimize opt,BoolExpr[] constraints)
	{	
		log("Asserting hard constraints...");
		for (int i = 0; i < constraints.length; i++) 
		{
			opt.Add(constraints[i]);
			log("\tasserted hard contraint : " +constraints[i]);
	    	}
		log("Asserted all hard constraints.");
	}

	public Optimize.Handle assertWeightedSoftConstraints(Context ctx, Optimize opt, BoolExpr[] constraints, int[] weights)
	{       final int CONSTRAINT =0;
		final int WEIGHT = 1;
		Optimize.Handle mx = null;
		log("Asserting soft constraints...");
		for (int i = 0; i < constraints.length; i++) {
			mx = opt.AssertSoft(constraints[i], weights[i], "groupXYZ");
			log("\tasserted soft contraint : "+constraints[i]+" of weight : "+weights[i]);
		}
		log("Asserted all soft constraints.");
		return mx;
	}

	public void getTimeTable(Context ctx) throws TestFailedException
	{
		BoolExpr[] hardConstraints = getHardConstraints(ctx);
		BoolExpr[] softConstraints = getSoftConstraints(ctx); 
                int[] weights= getSoftConstraintWeights(noOfConstraints,10);
		int sumOfWeights = IntStream.of(weights).sum();
		Optimize opt = ctx.mkOptimize();
		Optimize.Handle mx =assertAllConstraints(ctx, opt,hardConstraints,softConstraints,weights);	
		if(mx != null)
		{	
                        log("Sum of weights of all soft constraints : " + sumOfWeights);
			log("Sum of weights of satisfiable soft Constraints : " + ( sumOfWeights -  Integer.parseInt(mx.toString()) ) );
                        log("\nDISPLAYING TIME TABLE"); 
			log("MODEL: \n" + opt.getModel());
			
		}
		else
		{
			log("Model cannot be obtained as constraints are not satisfiable");
		}
	}

	public BoolExpr[] getHardConstraints(Context ctx)
	{
		BoolExpr p1 = ctx.mkBoolConst("P1");
		BoolExpr p2 = ctx.mkBoolConst("P2");
		BoolExpr p3 = ctx.mkBoolConst("P3");
		BoolExpr p4 = ctx.mkBoolConst("P4");
		
                BoolExpr[] hardConstraints= new BoolExpr[2];
                hardConstraints[0] = ctx.mkAnd(p1,p2);
		hardConstraints[1] = ctx.mkAnd(p3,p4);
		return hardConstraints; 
	}
	public BoolExpr[] getSoftConstraints(Context ctx)
	{					
		int noOfConstants =50;	
		int minConstantsInConstraint=10;
		int maxConstantsInConstraint=15;
		return generateConstraints(ctx,noOfConstants, noOfConstraints, minConstantsInConstraint, maxConstantsInConstraint);
	}
	public int[] getSoftConstraintWeights(int noOfConstraints, int maxWeight)
	{ 	 
		int[] softConstraintWeights = new int[noOfConstraints];	
		Random random = new Random();
		for(int i=0;i<noOfConstraints;i++){
			softConstraintWeights[i]= random.nextInt(maxWeight)+1;
		}
		return softConstraintWeights;
	}

	public void log(String text)
	{
		System.out.println(text);
	}

	public static void main(String[] args)
	{
		JavaExample p = new JavaExample();
		try
		{
			    com.microsoft.z3.Global.ToggleWarningMessages(true);
			    Log.open("test.log");

			    System.out.print("Z3 Major Version: ");
			    System.out.println(Version.getMajor());
			    System.out.print("Z3 Full Version: ");
			    System.out.println(Version.getString());
			    System.out.print("Z3 Full Version String: ");
			    System.out.println(Version.getFullVersion());
                            System.out.println("\nTIME TABLE GENERATION");
			    {	
				HashMap<String, String> cfg = new HashMap<String, String>();
               			cfg.put("model", "true");
                		Context ctx = new Context(cfg);
				p.getTimeTable(ctx);
			    }
			    Log.close();
		    	    if (Log.isOpen())
		            System.out.println("Log is still open!");
		} 
		catch (Z3Exception ex)
		{
		    System.out.println("Z3 Managed Exception: " + ex.getMessage());
		    System.out.println("Stack trace: ");
		    ex.printStackTrace(System.out);
		}
		catch (TestFailedException ex)
		{
		    System.out.println("TEST CASE FAILED: " + ex.getMessage());
		    System.out.println("Stack trace: ");
		    ex.printStackTrace(System.out);
		} 
		catch (Exception ex)
		{
		    System.out.println("Unknown Exception: " + ex.getMessage());
		    System.out.println("Stack trace: ");
		    ex.printStackTrace(System.out);
		}
	 }


	public BoolExpr[] generateConstraints(Context ctx, int noOfConstants, int noOfConstraints, int minContantsInConstraint, int maxConstantsInConstraint)
	{	
		Random random = new Random();
		char[] word = new char[random.nextInt(6)+4]; 
        	for(int j = 0; j < word.length; j++)
        	{
           		word[j] = (char)('a' + random.nextInt(26));
        	}
        	String constantName = new String(word);	
		BoolExpr[] constraints= new BoolExpr[noOfConstraints];
                BoolExpr[] constants= new BoolExpr[noOfConstants];   	
		for(int i =0;i<noOfConstants;i++)
		{
			constants[i] = ctx.mkBoolConst(constantName + "_" + (i+1));	
		}
                
		for(int i =0;i<noOfConstraints;i++)
		{
			BoolExpr temp=constants[random.nextInt(noOfConstants)];			
			int noOfConstantsInConstraint= random.nextInt(maxConstantsInConstraint-minContantsInConstraint)+minContantsInConstraint;
			
			for(int j =0;j<noOfConstantsInConstraint;j++)
			{
				temp = generateExpr(ctx,temp,constants[random.nextInt(noOfConstants)]);	
			}
			constraints[i]=temp;
		}	
		return constraints;
	}
	public BoolExpr generateExpr(Context ctx,BoolExpr expr,BoolExpr constant )
	{	
		String[] options = {"or","and","not"};
		Random random = new Random();
		switch (options[random.nextInt(3)]) {
		 case "or":
		        expr= ctx.mkOr(expr,constant);
		        break;
		 case "and":
			expr= ctx.mkAnd(expr,constant);
			break;	
		case "not":
			expr = ctx.mkNot(expr);
		}
		return expr;
	}		
}




