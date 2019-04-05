import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Optimize;

public class Optimizer {
	HashMap <String,List<String>> timeslotAndparallelLabsmap= new HashMap<String,List<String>>(); // hashmap generated to to store parallel labs
	HashMap <String,Map<String,IntExpr>> labWiseCandidate_CONSTANTSmap=new HashMap<String,Map<String,IntExpr>>(); //hashmap to store constants for each demonstrator for each lab
	HashMap <String,Map<String,Integer>> labWiseCandidateRankingMap=new HashMap <String,Map<String,Integer>>();
	HashMap <String,Integer> labAndNumberOfDemonstratorsmap=new HashMap <String,Integer> ();
	
	
	public Optimizer(HashMap<String,List<String>>timeslotAndparallelLabsmap,HashMap<String,Map<String,IntExpr>> labWiseDemonstrator_CONSTANTSmap,HashMap <String,Map<String,Integer>>labWiseDemonstratorRankingMap,HashMap <String,Integer>labAndNumberOfDemonstratorsmap) {
		timeslotAndparallelLabsmap=this.timeslotAndparallelLabsmap;
		labWiseDemonstrator_CONSTANTSmap=this.labWiseCandidate_CONSTANTSmap;
		labWiseDemonstratorRankingMap=this.labWiseCandidateRankingMap;
		labAndNumberOfDemonstratorsmap=this.labAndNumberOfDemonstratorsmap;
	}
	
	
	public void optimizeToGlobalMaxima(Context ctx,Optimize opt) {
		for(Entry<String, List<String>> timeslot: timeslotAndparallelLabsmap.entrySet() ) {
			ArithExpr GlobalDemonstratorRankAverage=getDemonstratorRankAverage(timeslot.getKey(),ctx);
			for (Entry<String, Map<String, IntExpr>> labWiseDemonstrator_CONSTANTS : labWiseCandidate_CONSTANTSmap.entrySet())
			{	
				String labId=labWiseDemonstrator_CONSTANTS.getKey();
				if (!timeslotAndparallelLabsmap.get(timeslot.getKey()).contains(labId))
					continue;
				ArithExpr sumDemonstratorRanks=ctx.mkInt(0);
				for (Entry<String,IntExpr> Demonstrator_Constants :labWiseDemonstrator_CONSTANTS.getValue().entrySet())
				{
					ArithExpr effectiveRank;
					int demonstratorRank=labWiseCandidateRankingMap.get(labId).get(Demonstrator_Constants.getKey());
					effectiveRank=ctx.mkMul(ctx.mkInt(demonstratorRank),Demonstrator_Constants.getValue());
					sumDemonstratorRanks=ctx.mkAdd(sumDemonstratorRanks,effectiveRank);
				}
				ArithExpr demonstratorRankAvg=ctx.mkDiv(sumDemonstratorRanks,ctx.mkInt(labAndNumberOfDemonstratorsmap.get(labId)));
				ArithExpr variance=ctx.mkPower(ctx.mkSub(demonstratorRankAvg,GlobalDemonstratorRankAverage), ctx.mkInt(2));
				opt.MkMinimize(variance);
			}
		}
	}
	
	
	private ArithExpr getDemonstratorRankAverage(String timeslot,Context ctx) {
		
		
		ArithExpr sumOfLabwiseAverage=ctx.mkInt(0);
		for (Entry<String, Map<String, IntExpr>> labWiseDemonstrator_CONSTANTS : labWiseCandidate_CONSTANTSmap.entrySet())
		{	
			String labId=labWiseDemonstrator_CONSTANTS.getKey();
			if (!timeslotAndparallelLabsmap.get(timeslot).contains(labId))
				continue;
			ArithExpr sumDemonstratorRanks=ctx.mkInt(0);
			
			for (Entry<String,IntExpr> Demonstrator_Constants :labWiseDemonstrator_CONSTANTS.getValue().entrySet())
			{
				ArithExpr effectiveRank;
				int demonstratorRank=labWiseCandidateRankingMap.get(labId).get(Demonstrator_Constants.getKey());
				effectiveRank=ctx.mkMul(ctx.mkInt(demonstratorRank),Demonstrator_Constants.getValue());
				sumDemonstratorRanks=ctx.mkAdd(sumDemonstratorRanks,effectiveRank);
			}
			ArithExpr demonstratorRankAvg=ctx.mkDiv(sumDemonstratorRanks,ctx.mkInt(labAndNumberOfDemonstratorsmap.get(labId)));
			sumOfLabwiseAverage=ctx.mkAdd(sumOfLabwiseAverage,demonstratorRankAvg);
		}
		return ctx.mkDiv(sumOfLabwiseAverage, ctx.mkInt(labAndNumberOfDemonstratorsmap.size()));
	}
}
