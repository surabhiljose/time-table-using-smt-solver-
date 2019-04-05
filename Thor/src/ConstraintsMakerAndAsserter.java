import java.util.*;
import java.util.Map.Entry;


import com.microsoft.z3.*;


public class ConstraintsMakerAndAsserter {
	
	Context ctx;
	Optimize opt;
	Optimize.Handle mx;
	
	
	//provided hashmaps
	HashMap <String,Map<String,Integer>> labWiseDemonstratorRankingMap=new HashMap <String,Map<String,Integer>>();
	HashMap <String,Integer> labAndNumberOfDemonstratorsmap=new HashMap <String,Integer> ();
	HashMap <String,String> labAndTimeslotmap=new HashMap <String,String>() ;
	
	
	//generated hashmaps for easy coding
	HashMap <String,Map<String,IntExpr>> labWiseDemonstrator_CONSTANTSmap=new HashMap<String,Map<String,IntExpr>>(); //hashmap to store constants for each demonstrator for each lab
	HashMap <String,List<String>> demonstratorAndlabsmap= new HashMap <String,List<String>>(); //hashmap generated for easy access to demonstrator and his lab
	HashMap <String,List<String>> timeslotAndparallelLabsmap= new HashMap<String,List<String>>(); // hashmap generated to to store parallel labs
	
	
	ArithExpr demonstratorCount;
	BoolExpr currentExpr;
	int currentDemonstratorRating;
	String currentLabId;
	String currentDemonstrator;
	Calendar timebeforeAssetion = Calendar.getInstance();
	
		
	public ConstraintsMakerAndAsserter(HashMap <String,Map<String,Integer>> labWiseDemonstratorRankingMap, HashMap <String,Integer> labAndNumberOfDemonstratorsmap,HashMap <String,String> labAndTimeslotmap ) {
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		ctx = new Context(cfg);
		opt=ctx.mkOptimize();
		this.labWiseDemonstratorRankingMap=labWiseDemonstratorRankingMap;
		this.labAndTimeslotmap=labAndTimeslotmap;
		this.labAndNumberOfDemonstratorsmap=labAndNumberOfDemonstratorsmap;
		for (Entry<String, String> labAndTimeslot : labAndTimeslotmap.entrySet())
		{
			if(timeslotAndparallelLabsmap.containsKey(labAndTimeslot.getValue())) {
				timeslotAndparallelLabsmap.get(labAndTimeslot.getValue()).add(labAndTimeslot.getKey());
			}
			else {
				List<String> list =new ArrayList<String>();
				list.add(labAndTimeslot.getKey());
				timeslotAndparallelLabsmap.put(labAndTimeslot.getValue(), list);
			}
		}
	}

		
	public void makeAndsetConstants() {
		for (Entry<String, Map<String, Integer>> labEntry : labWiseDemonstratorRankingMap.entrySet())
		{		
			String labId = labEntry.getKey();
			HashMap<String,IntExpr> tempDemonstrators_CONSTANTmap=new HashMap<String,IntExpr>();
			for (Entry<String,Integer> demonstratorAndRankingEntry :labEntry.getValue().entrySet())
			{
				String demonstratorID=demonstratorAndRankingEntry.getKey();
				updateDemonstratorAndlabsMap(demonstratorID,labId);
				IntExpr demonstrator=ctx.mkIntConst(labId+"-"+demonstratorID);
				opt.Add(ctx.mkAnd(ctx.mkLe(ctx.mkInt(0), demonstrator),
                        ctx.mkLe(demonstrator, ctx.mkInt(1))));
				tempDemonstrators_CONSTANTmap.put(demonstratorID, demonstrator);
			}
			labWiseDemonstrator_CONSTANTSmap.put(labId, tempDemonstrators_CONSTANTmap);	
		}
		
	}
	
	public void updateDemonstratorAndlabsMap(String demonstratorID,String labId) {
		if(demonstratorAndlabsmap.containsKey(demonstratorID)) {		
			demonstratorAndlabsmap.get(demonstratorID).add(labId);
		}
		else {
			List<String> tempList = new ArrayList<String>(); 
			tempList.add(labId);
			demonstratorAndlabsmap.put(demonstratorID, tempList);
		}
	}
	
	public void makeAndAssertConstraints() {
		if(!labWiseDemonstrator_CONSTANTSmap.isEmpty())
		{
			for (Entry<String, Map<String, IntExpr>> demonstratorsmap : labWiseDemonstrator_CONSTANTSmap.entrySet())
			{		
				String labId = demonstratorsmap.getKey();
				demonstratorCount=ctx.mkInt(0);
				Map <String,IntExpr>temp = demonstratorsmap.getValue(); 
				for (Entry<String, IntExpr> demonstratorConstant : temp.entrySet())
				{
					String demonstratorID=demonstratorConstant.getKey();
					int demonstratorRating=labWiseDemonstratorRankingMap.get(labId).get(demonstratorID);
					assertConstraintsForDemonstrator(demonstratorID,labId,demonstratorRating);
					demonstratorCount=ctx.mkAdd(demonstratorCount,demonstratorConstant.getValue());
				}
				opt.Add(ctx.mkEq(demonstratorCount, ctx.mkInt(labAndNumberOfDemonstratorsmap.get(labId))));
			}
		}
	}
	public void assertConstraintsForDemonstrator(String demonstratorID,String labId,int demonstratorRating) {
		BoolExpr expr= ctx.mkEq(ctx.mkInt(1), labWiseDemonstrator_CONSTANTSmap.get(labId).get(demonstratorID));
		String timeSlot=labAndTimeslotmap.get(labId);
		List<String> parallelLabs=timeslotAndparallelLabsmap.get(timeSlot);
		parallelLabs.remove(labId);
		for(String parallelLab :parallelLabs ){
			if(demonstratorAndlabsmap.get(demonstratorID).contains(parallelLab)) {
				if(labWiseDemonstrator_CONSTANTSmap.get(parallelLab).containsKey(demonstratorID)) {
					expr=ctx.mkAnd(expr,ctx.mkEq(ctx.mkInt(0),labWiseDemonstrator_CONSTANTSmap.get(parallelLab).get(demonstratorID)));
				}
			}
		}
		System.out.println("Weight = "+demonstratorRating);
		System.out.println(expr.toString());
		System.out.println("...........................................");
		opt.AssertSoft(expr,demonstratorRating , labId);
	}
	
	public void getSolution()  {
		makeAndsetConstants();
		makeAndAssertConstraints();
		Optimizer optimizer = new Optimizer(timeslotAndparallelLabsmap, labWiseDemonstrator_CONSTANTSmap,labWiseDemonstratorRankingMap,labAndNumberOfDemonstratorsmap);
		optimizer.optimizeToGlobalMaxima(ctx, opt);			
		outputHandler();
	}

	public void outputHandler() {
		switch(opt.Check()) {
		case UNSATISFIABLE:
			System.out.println("constraints are not satisfyable");
			break;
		case UNKNOWN:
			System.out.println("constraints are not known");
			break;
		case SATISFIABLE:
			System.out.println("time taken: " + (Calendar.getInstance().getTimeInMillis()-timebeforeAssetion.getTimeInMillis()));
			new OutputParser().parseModel(opt.getModel(),labWiseDemonstratorRankingMap);
		}
	}
}
