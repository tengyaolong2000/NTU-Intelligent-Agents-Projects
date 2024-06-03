import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ThreePrisonersDilemma {
	
	/* 
	 This Java program models the two-player Prisoner's Dilemma game.
	 We use the integer "0" to represent cooperation, and "1" to represent 
	 defection. 
	 
	 Recall that in the 2-players dilemma, U(DC) > U(CC) > U(DD) > U(CD), where
	 we give the payoff for the first player in the list. We want the three-player game 
	 to resemble the 2-player game whenever one player's response is fixed, and we
	 also want symmetry, so U(CCD) = U(CDC) etc. This gives the unique ordering
	 
	 U(DCC) > U(CCC) > U(DDC) > U(CDC) > U(DDD) > U(CDD)
	 
	 The payoffs for player 1 are given by the following matrix: */
	
	static int[][][] payoff = {
		{{6,3},  //payoffs when first and second players cooperate
		 {3,0}}, //payoffs when first player coops, second defects
		{{8,5},  //payoffs when first player defects, second coops
	     {5,2}}};//payoffs when first and second players defect
	
	/* 
	 So payoff[i][j][k] represents the payoff to player 1 when the first
	 player's action is i, the second player's action is j, and the
	 third player's action is k.
	 
	 In this simulation, triples of players will play each other repeatedly in a
	 'match'. A match consists of about 100 rounds, and your score from that match
	 is the average of the payoffs from each round of that match. For each round, your
	 strategy is given a list of the previous plays (so you can remember what your 
	 opponent did) and must compute the next action.  */
	
	
	abstract class Player {
		// This procedure takes in the number of rounds elapsed so far (n), and 
		// the previous plays in the match, and returns the appropriate action.
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			throw new RuntimeException("You need to override the selectAction method.");
		}
		
		// Used to extract the name of this player class.
		final String name() {
			String result = getClass().getName();
			return result.substring(result.indexOf('$')+1);
		}
	}
	
	/* Here are four simple strategies: */
	
	class NicePlayer extends Player {
		//NicePlayer always cooperates
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return 0; 
		}
	}
	
	class NastyPlayer extends Player {
		//NastyPlayer always defects
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return 1; 
		}
	}
	
	class RandomPlayer extends Player {
		//RandomPlayer randomly picks his action each time
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (Math.random() < 0.5)
				return 0;  //cooperates half the time
			else
				return 1;  //defects half the time
		}
	}
	
	class TolerantPlayer extends Player {
		//TolerantPlayer looks at his opponents' histories, and only defects
		//if at least half of the other players' actions have been defects
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			int opponentCoop = 0;
			int opponentDefect = 0;
			for (int i=0; i<n; i++) {
				if (oppHistory1[i] == 0)
					opponentCoop = opponentCoop + 1;
				else
					opponentDefect = opponentDefect + 1;
			}
			for (int i=0; i<n; i++) {
				if (oppHistory2[i] == 0)
					opponentCoop = opponentCoop + 1;
				else
					opponentDefect = opponentDefect + 1;
			}
			if (opponentDefect > opponentCoop)
				return 1;
			else
				return 0;
		}
	}
	
	class FreakyPlayer extends Player {
		//FreakyPlayer determines, at the start of the match, 
		//either to always be nice or always be nasty. 
		//Note that this class has a non-trivial constructor.
		int action;
		FreakyPlayer() {
			if (Math.random() < 0.5)
				action = 0;  //cooperates half the time
			else
				action = 1;  //defects half the time
		}
		
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return action;
		}	
	}

	class T4TPlayer extends Player {
		//Picks a random opponent at each play, 
		//and uses the 'tit-for-tat' strategy against them 
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0; //cooperate by default
			if (Math.random() < 0.5)
				return oppHistory1[n-1];
			else
				return oppHistory2[n-1];
		}	
	}
	class T4TTolerantTakeAdvantagePlayer extends Player {
		private int numRoundsThreshold = 10;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// cooperate by default
			if (n == 0)
				return 0;

			if (n >= numRoundsThreshold) {
				int iDefect = 0;
				int oppDefect1 = 0;
				int oppDefect2 = 0;

				for (int index = n - 1; index > n - 1 - numRoundsThreshold; --index) {
					iDefect += myHistory[index];
					oppDefect1 += oppHistory1[index];
					oppDefect2 += oppHistory2[index];
				}

				if (iDefect == 0 && oppDefect1 == 0 && oppDefect2 == 0)
					return 1; // take advantage
			}

			if (oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0)
				return 0; // cooperate along

			if (oppHistory1[n-1] == 1 && oppHistory2[n-1] == 1 && myHistory[n-1] != 1)
				return 1; // both defect while i cooperate

			// TolerantPlayer
			int opponentCoop = 0;
			int opponentDefect = 0;

			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0)
					opponentCoop += 1;
				else
					opponentDefect += 1;

				if (oppHistory2[i] == 0)
					opponentCoop += 1;
				else
					opponentDefect += 1;
			}

			return (opponentDefect > opponentCoop) ? 1 : 0;
		}
	}

	class T4TDefectPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

			if (n == 0)
				return 1;

			// https://www.sciencedirect.com/science/article/abs/pii/S0096300316301011
			if (oppHistory1[n-1] == oppHistory2[n-1])
				return oppHistory1[n-1];

			return 1;
		}
	}
	//Cooperates if unsure.
	class T4TCoopPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// cooperate by default
			if (n == 0)
				return 0;


			if (oppHistory1[n-1] == oppHistory2[n-1])
				return oppHistory1[n-1];

			return 0;
		}
	}
	class T4TTolerantPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// cooperate by default
			if (n == 0)
				return 0;

			// https://www.sciencedirect.com/science/article/abs/pii/S0096300316301011
			if (oppHistory1[n-1] == oppHistory2[n-1])
				return oppHistory1[n-1];

			// TolerantPlayer
			int opponentCoop = 0;
			int opponentDefect = 0;

			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0)
					opponentCoop += 1;
				else
					opponentDefect += 1;

				if (oppHistory2[i] == 0)
					opponentCoop += 1;
				else
					opponentDefect += 1;
			}

			return (opponentDefect > opponentCoop) ? 1 : 0;
		}
	}
	class T4TTolerantPlayerThres extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			double thres = 0.6;
			// cooperate by default
			if (n == 0)
				return 0;

			// https://www.sciencedirect.com/science/article/abs/pii/S0096300316301011
			if (oppHistory1[n-1] == oppHistory2[n-1])
				return oppHistory1[n-1];

			// TolerantPlayer
			int opponentCoop = 0;
			int opponentDefect = 0;

			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0)
					opponentCoop += 1;
				else
					opponentDefect += 1;

				if (oppHistory2[i] == 0)
					opponentCoop += 1;
				else
					opponentDefect += 1;
			}

			return( (opponentDefect / (opponentCoop +opponentDefect)) >= thres) ? 1 : 0;
		}
	}
	//https://github.com/wilsonteng97/Intelligent-Agents-2-ThreePrisonersDilemma/blob/master/src/com/cz4046/ThreePrisonersDilemma_PlayerArena.java
	class WinStayLoseShift extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0;

			int r = n - 1;
			int myLA = myHistory[r];
			int oppLA1 = oppHistory1[r];
			int oppLA2 = oppHistory2[r];

			if (payoff[myLA][oppLA1][oppLA2]>=5) return myLA;
			return oppAction(myLA);
		}

		private int oppAction(int action) {
			if (action==1) return 0;
			return 1;
		}
	}
	//https://github.com/Javelin1991/CZ4046_Intelligent_Agents/blob/master/CZ4046_Assignment_2/ThreePrisonersDilemma.java
	class Naing_Htet_Player extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

			// Rule 1: our agent will cooperate in the first round
			if (n == 0)  {
				return 0;
			}

			// Rule 2: our agent will defect in the last few rounds, NastyPlayer mode is turned on
			if (n > 95) {
				return 1;
			}

			// Rule 3: if all players including our agent cooperated in the previous round,
			// then our agent will continue to cooperate
			if (myHistory[n-1] == 0 && oppHistory1[n-1] == 0 && oppHistory2[n-1] == 0) {
				return 0;
			}

			// Rule 4: check opponents history to see if they have defected before
			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 1 || oppHistory2[i] == 1) {
					// if either one of them defected before, our agent will always defect
					return 1;
				}
			}
			// Rule 5: Otherwise, by default nature, our agent will always cooperate
			return 0;
		}
	}
	//https://github.com/NgoJunHaoJason/CZ4046/blob/master/assignment_2/Ngo_Jason_Player.java
	public class Ngo_Jason_Player extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0)
				return 0; // cooperate by default

			if (n >= 109)
				return 1; // opponents cannot retaliate

			// https://www.sciencedirect.com/science/article/abs/pii/S0096300316301011
			if (oppHistory1[n-1] == oppHistory2[n-1])
				return oppHistory1[n-1];

			// n starts at 0, so compare history first

			if (n % 2 != 0) { // odd round - be tolerant
				// TolerantPlayer
				int opponentCoop = 0;
				int opponentDefect = 0;

				for (int i = 0; i < n; i++) {
					if (oppHistory1[i] == 0)
						opponentCoop += 1;
					else
						opponentDefect += 1;

					if (oppHistory2[i] == 0)
						opponentCoop += 1;
					else
						opponentDefect += 1;
				}

				return (opponentDefect > opponentCoop) ? 1 : 0;
			}
			// else: even round - compare history

			// HistoryPlayer
			int myNumDefections = 0;
			int oppNumDefections1 = 0;
			int oppNumDefections2 = 0;

			for (int index = 0; index < n; ++index) {
				myNumDefections += myHistory[index];
				oppNumDefections1 += oppHistory1[index];
				oppNumDefections2 += oppHistory2[index];
			}

			if (myNumDefections >= oppNumDefections1 && myNumDefections >= oppNumDefections2)
				return 0;
			else
				return 1;
		}
	}

	class ConservativePlayer  extends Player{
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

			if (n<=10) return 0;

			int opponentCoop = 0;
			int opponentDefect = 0;

			int opponentCoop1 = 0;
			int opponentCoop2 = 0;


			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0)
					opponentCoop += 1;
				else
					opponentDefect += 1;

				if (oppHistory2[i] == 0)
					opponentCoop += 1;
				else
					opponentDefect += 1;
			}

			for (int i = n-10; i < n; i++) { //last 10 window
				if (oppHistory1[i] == 0)
					opponentCoop1 += 1;


				if (oppHistory2[i] == 0)
					opponentCoop2 += 1;

			}

			if( (opponentCoop1 ==0 ) || (opponentCoop2 == 0))
				return 1;
			else{
				return (opponentDefect > opponentCoop) ? 1 : 0;
			}

		}
	}
	//https://github.com/wilsonteng97/Intelligent-Agents-2-ThreePrisonersDilemma/blob/master/src/com/cz4046/ThreePrisonersDilemma_PlayerArena.java
	class WILSON_TENG_Player extends Player {

		int[][][] payoff = {
				{{6, 3},     //payoffs when first and second players cooperate
						{3, 0}},     //payoffs when first player coops, second defects
				{{8, 5},     //payoffs when first player defects, second coops
						{5, 2}}};    //payoffs when first and second players defect

		int r;
		int this_round; int prev_round = 0;
		int[] myHist, opp1Hist, opp2Hist;
		int myLA, opp1LA, opp2LA;
		int opp1LLA, opp2LLA;
		int myScore=0, opp1Score=0, opp2Score=0;
		int opponent1Coop = 0; int opponent2Coop = 0;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0; // Always cooperate in first round!

			// Updating class variables for use in methods.
			this.prev_round = n - 1;
			this.myHist = myHistory;
			this.opp1Hist = oppHistory1;
			this.opp2Hist = oppHistory2;

			// Updating Last Actions (LA) for all players.
			this.r = prev_round;
			this.myLA = myHistory[r];
			this.opp1LA = oppHistory1[r];
			this.opp2LA = oppHistory2[r];

			// Updating Scores for all players
			this.myScore += payoff[myLA][opp1LA][opp2LA];
			this.opp1Score += payoff[opp1LA][opp2LA][myLA];
			this.opp2Score += payoff[opp2LA][opp1LA][myLA];

			// Update opponent's cooperate record.
			if (n>0) {
				opponent1Coop += oppAction(opp1Hist[r]);
				opponent2Coop += oppAction(opp2Hist[r]);
			}

			double opponent1Coop_prob = opponent1Coop / opp1Hist.length;
			double opponent2Coop_prob = opponent2Coop / opp2Hist.length;

			if ((n>100) && (opponent1Coop_prob<0.750 && opponent2Coop_prob<0.750)) {
				return actionWithNoise(1, 99);
			}

			if ((opp1LA+opp2LA ==0)&&(opponent1Coop_prob>0.705 && opponent2Coop_prob>0.705)) {
				return actionWithNoise(0, 99);
			}
			else
				return SoreLoser();
		}

		private boolean iAmLoser() {
			if (myScore>=opp1Score && myScore>=opp2Score) {
				return false;
			}
			return true;
		}

		private int SoreLoser() {
			if (iAmLoser()) return 1;
			return 0;
		}

		private int oppAction(int action) {
			if (action == 1) return 0;
			return 1;
		}
		private int actionWithNoise(int intendedAction, int percent_chance_for_intended_action) {
			Map<Integer, Integer> map = new HashMap<Integer, Integer>() {{
				put(intendedAction, percent_chance_for_intended_action);
				put(oppAction(intendedAction), 1-percent_chance_for_intended_action);
			}};
			LinkedList<Integer> list = new LinkedList<>();
			for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
				for (int i = 0; i < entry.getValue(); i++) {
					list.add(entry.getKey());
				}
			}
			Collections.shuffle(list);
			return list.pop();
		}
	}
	//https://github.com/wilsonteng97/Intelligent-Agents-2-ThreePrisonersDilemma/blob/master/src/com/cz4046/ThreePrisonersDilemma_PlayerArena.java
	class EncourageCoop2 extends Player {
		int myScore = 0;
		int opp1Score = 0;
		int opp2Score = 0;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// First Law: Always cooperate in first 2 rounds
			if (n < 2) return 0;

			// Second Law: Tolerate 2 consecutive defects from both opp
			// If 2 consecutive defects from both opp, then defect
			if (oppHistory1[n-1] == 1 && oppHistory1[n-2] == 1 &&
					oppHistory2[n-1] == 1 && oppHistory2[n-2] == 1)
				return 1;

			// Third Law: if one of the opponents is Nasty, then always defect
			boolean isOpp1Nasty, isOpp2Nasty;
			isOpp1Nasty = isNasty(n, oppHistory1);
			isOpp2Nasty = isNasty(n, oppHistory2);
			if (isOpp1Nasty || isOpp2Nasty) return 1;

			// Fourth Law: if one of the opponents is Random, then always defect
			boolean isOpp1Random, isOpp2Random;
			isOpp1Random = isRandom(n, oppHistory1);
			isOpp2Random = isRandom(n, oppHistory2);
			if (isOpp1Random || isOpp2Random) return 1;

			// Fifth Law: if my current score is lower than one of the opp, then always defect
			myScore += payoff[myHistory[n-1]][oppHistory1[n-1]][oppHistory2[n-1]];
			opp1Score += payoff[oppHistory1[n-1]][oppHistory2[n-1]][myHistory[n-1]];
			opp2Score += payoff[oppHistory2[n-1]][oppHistory1[n-1]][myHistory[n-1]];
			if (myScore < opp1Score || myScore < opp2Score) return 1;

			// Sixth Law: If above laws don't apply, then be a T4TPlayer
			if (Math.random() < 0.5) return oppHistory1[n-1];
			else return oppHistory2[n-1];
		}

		boolean isNasty(int n, int[] oppHistory) {
			int cnt = 0;
			for (int i=0; i<n; i++){
				if (oppHistory[i] == 1)
					cnt++;
			}
			if (cnt == n) return true;
			else return false;
		}

		boolean isRandom(int n, int[] oppHistory) {
			int sum = 0;
			double eps = 0.025;
			for (int i=0; i<n; i++) {
				sum += oppHistory[i];
			}
			// if ratio is roughly 0.5, then the opponent is highly likely to be random
			double ratio = (double) sum / n;
			if (Math.abs(ratio - 0.5) < eps) return true;
			else return false;
		}
	}
	//https://github.com/wilsonteng97/Intelligent-Agents-2-ThreePrisonersDilemma/blob/master/src/com/cz4046/ThreePrisonersDilemma_PlayerArena.java
	class Nice2 extends NicePlayer {

		// For tracking Defect/Cooperate probabilities
		private double opp1Def = 0;
		private double opp2Def = 0;

		// Thresholds
		private static final double FRIENDLY_THRESHOLD = 0.850;
		private static final double DEFENSIVE_THRESHOLD = 0.750;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

			// Start by cooperating
			if (n == 0) {

				return 0;
			}

			// Calculate probability for Def/Coop (Opponent 1)
			opp1Def += oppHistory1[n - 1];
			double opp1DefProb = opp1Def / oppHistory1.length;
			double opp1CoopProb = 1.000 - opp1DefProb;

			// Calculate probability for Def/Coop (Opponent 2)
			opp2Def += oppHistory2[n - 1];
			double opp2DefProb = opp2Def / oppHistory2.length;
			double opp2CoopProb = 1.000 - opp2DefProb;

            /*System.out.printf("Opponent 1: %.3f, %.3f, Opponent 2: %.3f, %.3f%n",
					opp1CoopProb, opp1DefProb, opp2CoopProb, opp2DefProb);*/
			if (opp1CoopProb >= FRIENDLY_THRESHOLD
					&& opp2CoopProb >= FRIENDLY_THRESHOLD
					&& oppHistory1[n - 1] == 0
					&& oppHistory2[n - 1] == 0) {

				// Good chance that both opponents will cooperate
				// Just cooperate so that everyone will be happy
				return 0;

			} else if ((opp1DefProb >= DEFENSIVE_THRESHOLD || opp2DefProb >= DEFENSIVE_THRESHOLD)
					&& (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1)) {

				// Given that one of the opponents have been relatively nasty,
				// and one of them has defected in the previous turn,
				// high prob that one of them will defect again,
				// defect to protect myself!
				return 1;

			} else if (n >= 2) {

				// Check if either opponent has defected in the last 2 turns
				if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1
						|| oppHistory1[n - 2] == 1 || oppHistory2[n - 2] == 1) {

					// DESTROY them!!
					return 1;
				} else {

					// Just be friendly!
					return 0;
				}
			} else {

				// At this moment, both players are not that friendly,
				// and yet neither of them are relatively nasty.
				// Just be friendly for now.
				return 0;
			}
		}
	}
	public class AngWaiKit_ANSON_Player2 extends Player{
		// Hyper-parameters
		private static final double COOPERATE_PROBABILITY = 0.90;
		private static final double DEFECT_LIMIT = 0.4;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// cooperate first
			if (n == 0)
				return 0;

			// last round so pick dominant strategy
			if (n >= 109)
				return 1;

			//tit for tat
			if (oppHistory1[n-1] == oppHistory2[n-1])
				return oppHistory1[n-1];

			//initialising variables
			int myNumDefections = 0;
			int oppNumDefections1 = 0;
			int oppNumDefections2 = 0;
			int oppNumCoop1 = 0;
			int oppNumCoop2 = 0;
			int oppNumDef1 = 0;
			int oppNumDef2 = 0;

			//get the number of times each opponent cooperated or defected
			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0)
					oppNumCoop1 += 1;
				else
					oppNumDef1 += 1;

				if (oppHistory2[i] == 0)
					oppNumCoop2 += 1;
				else
					oppNumDef2 += 1;
			}

			//calculate the cooperate and defect probabilities of past actions by opponent 1 and 2
			double oppProbOfDef1 = oppNumDef1 / oppHistory1.length;
			double oppProbOfDef2 = oppNumDef2 / oppHistory2.length;
			double oppProbOfCoop1 = oppNumCoop1 / oppHistory1.length;
			double oppProbOfCoop2 = oppNumCoop2 / oppHistory2.length;

			//check if opponents are likely to cooperate
			if (oppProbOfCoop1 >= COOPERATE_PROBABILITY && oppProbOfCoop2 >= COOPERATE_PROBABILITY) {
				//check if their probability of defecting is lower than our max threshold, if yes, cooperate, else defect
				if (oppProbOfDef1 <= DEFECT_LIMIT && oppProbOfDef2 <= DEFECT_LIMIT) return 0;
				else return 1;
			}
			else return 0;

			//else we compare history
            /*else{
                for (int i = 0; i < n; i++) {
                    myNumDefections += myHistory[i];
                    oppNumDefections1 += oppHistory1[i];
                    oppNumDefections2 += oppHistory2[i];
                }

                if (myNumDefections >= oppNumDefections1 && myNumDefections >= oppNumDefections2)
                    return 0;
                else
                    return 1;
            }*/
		}
	}
	public class AngWaiKit_ANSON_Player1 extends Player{
		// Hyper-parameters
		private static final double COOPERATE_PROBABILITY = 0.900;
		private static final double DEFECT_LIMIT = 0.050;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			// cooperate first
			if (n == 0)
				return 0;

			// last round so pick dominant strategy
			if (n >= 109)
				return 1;

			//tit for tat
			if (oppHistory1[n-1] == oppHistory2[n-1])
				return oppHistory1[n-1];

			//initialising variables
			int myNumDefections = 0;
			int oppNumDefections1 = 0;
			int oppNumDefections2 = 0;
			int oppNumCoop1 = 0;
			int oppNumCoop2 = 0;
			int oppNumDef1 = 0;
			int oppNumDef2 = 0;

			//get the number of times each opponent cooperated or defected
			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0)
					oppNumCoop1 += 1;
				else
					oppNumDef1 += 1;

				if (oppHistory2[i] == 0)
					oppNumCoop2 += 1;
				else
					oppNumDef2 += 1;
			}

			//calculate the cooperate and defect probabilities of past actions by opponent 1 and 2
			double oppProbOfDef1 = oppNumDef1 / oppHistory1.length;
			double oppProbOfDef2 = oppNumDef2 / oppHistory2.length;
			double oppProbOfCoop1 = oppNumCoop1 / oppHistory1.length;
			double oppProbOfCoop2 = oppNumCoop2 / oppHistory2.length;

			//check if opponents are likely to cooperate
			if (oppProbOfCoop1 >= COOPERATE_PROBABILITY && oppProbOfCoop2 >= COOPERATE_PROBABILITY){
				//check if their probability of defecting is lower than our max threshold, if yes, cooperate, else defect
				if (oppProbOfDef1 <= DEFECT_LIMIT && oppProbOfDef2 <= DEFECT_LIMIT) return 0;
				else return 1;
			}

			//else we compare history
			else{
				for (int i = 0; i < n; i++) {
					myNumDefections += myHistory[i];
					oppNumDefections1 += oppHistory1[i];
					oppNumDefections2 += oppHistory2[i];
				}

				if (myNumDefections >= oppNumDefections1 && myNumDefections >= oppNumDefections2)
					return 0;
				else
					return 1;
			}
		}
	}

	class T4TTolerantHistoryPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0)
				return 0; // cooperate by default

			if (n >= 109)
				return 1; // opponents cannot retaliate

			// https://www.sciencedirect.com/science/article/abs/pii/S0096300316301011
			if (oppHistory1[n-1] == oppHistory2[n-1])
				return oppHistory1[n-1];

			// n starts at 0, so compare history first
			if (n % 2 != 0) { // odd round - be tolerant
				// TolerantPlayer
				int opponentCoop = 0;
				int opponentDefect = 0;

				for (int i = 0; i < n; i++) {
					if (oppHistory1[i] == 0)
						opponentCoop += 1;
					else
						opponentDefect += 1;

					if (oppHistory2[i] == 0)
						opponentCoop += 1;
					else
						opponentDefect += 1;
				}

				return (opponentDefect > opponentCoop) ? 1 : 0;
			}
			// else: even round - compare history

			// HistoryPlayer
			int myNumDefections = 0;
			int oppNumDefections1 = 0;
			int oppNumDefections2 = 0;

			for (int index = 0; index < n; ++index) {
				myNumDefections += myHistory[index];
				oppNumDefections1 += oppHistory1[index];
				oppNumDefections2 += oppHistory2[index];
			}

			if (myNumDefections >= oppNumDefections1 && myNumDefections >= oppNumDefections2)
				return 0;
			else
				return 1;
		}
	}
	class YaoLongTeng extends Player{
		// This procedure takes in the number of rounds elapsed so far (n), and
		// the previous plays in the match, and returns the appropriate action.
		//We use the integer "0" to represent cooperation, and "1" to represent
		//defection
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {


			int threshold = 5;
			// cooperate by default for threshold rounds, Rule 1 and Rule 2
			if (n <= threshold)
				return 0;

			if (n>=108) return 1; // Rule 3: Exploit others when they cannot retaliate (on estimated last round)

			//Tit4tat backbone, Rule 4

			if (oppHistory1[n-1] == oppHistory2[n-1]){
				return oppHistory1[n-1];

			}
			//get total number of coop and defect actions from both opponents
			int opponentCoop = 0;
			int opponentDefect = 0;
			for (int i=0; i<n; i++) {
				if (oppHistory1[i] == 0)
					opponentCoop = opponentCoop + 1;
				else
					opponentDefect = opponentDefect + 1;
			}
			for (int i=0; i<n; i++) {
				if (oppHistory2[i] == 0)
					opponentCoop = opponentCoop + 1;
				else
					opponentDefect = opponentDefect + 1;
			}

			// Cooperative
			int opponentCoop1 = 0;
			int opponentCoop2 = 0;

			if (n>90) {
				for (int i = n - 20; i < n; i++) { //last 20 window
					if (oppHistory1[i] == 0)
						opponentCoop1 += 1;


					if (oppHistory2[i] == 0)
						opponentCoop2 += 1;
				}
				if ((opponentCoop1 ==0 ) || (opponentCoop2 == 0)) //if either did not coop in the last 20 rounds
					return 1;
				else{
					return (opponentDefect/opponentCoop)>1.2 ? 1 : 0; //rule 1 and 2, try to cooperate
				}
			}

			else{
				return (opponentDefect/(opponentCoop+0.001))>1.2 ? 1 : 0; //rule 1 and 2, try to cooperate
			}
		}

	}

	
	/* In our tournament, each pair of strategies will play one match against each other. 
	 This procedure simulates a single match and returns the scores. */
	float[] scoresOfMatch(Player A, Player B, Player C, int rounds) {
		int[] HistoryA = new int[0], HistoryB = new int[0], HistoryC = new int[0];
		float ScoreA = 0, ScoreB = 0, ScoreC = 0;
		
		for (int i=0; i<rounds; i++) {
			int PlayA = A.selectAction(i, HistoryA, HistoryB, HistoryC);
			int PlayB = B.selectAction(i, HistoryB, HistoryC, HistoryA);
			int PlayC = C.selectAction(i, HistoryC, HistoryA, HistoryB);
			ScoreA = ScoreA + payoff[PlayA][PlayB][PlayC];
			ScoreB = ScoreB + payoff[PlayB][PlayC][PlayA];
			ScoreC = ScoreC + payoff[PlayC][PlayA][PlayB];
			HistoryA = extendIntArray(HistoryA, PlayA);
			HistoryB = extendIntArray(HistoryB, PlayB);
			HistoryC = extendIntArray(HistoryC, PlayC);
		}
		float[] result = {ScoreA/rounds, ScoreB/rounds, ScoreC/rounds};
		return result;
	}
	
//	This is a helper function needed by scoresOfMatch.
	int[] extendIntArray(int[] arr, int next) {
		int[] result = new int[arr.length+1];
		for (int i=0; i<arr.length; i++) {
			result[i] = arr[i];
		}
		result[result.length-1] = next;
		return result;
	}
	
	/* The procedure makePlayer is used to reset each of the Players 
	 (strategies) in between matches. When you add your own strategy,
	 you will need to add a new entry to makePlayer, and change numPlayers.*/
	
	int numPlayers = 34;
	Player makePlayer(int which) {

		switch (which) {
			case 33: return new RandomPlayer();
			case 32: return new FreakyPlayer();
			case 31: return new T4TPlayer();
			case 30: return new NastyPlayer();
			case 29: return new NicePlayer();
			case 28: return new TolerantPlayer();
			case 27: return new T4TTolerantHistoryPlayer();
			case 26: return new T4TTolerantHistoryPlayer();
			case 25 : return new AngWaiKit_ANSON_Player2();
			//case 25 : return new AngWaiKit_ANSON_Player2();
			case 24: return new Naing_Htet_Player();
			case 23: return new YaoLongTeng();
			case 22: return new T4TTolerantTakeAdvantagePlayer();
			case 21: return new T4TTolerantTakeAdvantagePlayer();
			case 20: return new T4TDefectPlayer();
			case 19: return new T4TDefectPlayer();
			case 18: return new T4TTolerantPlayer();
			case 17: return new T4TTolerantPlayer();
			case 16: return new T4TCoopPlayer();
			case 15: return new T4TCoopPlayer();
			case 14: return new T4TTolerantPlayerThres();
			case 13: return new T4TTolerantPlayerThres();
			case 12: return new Naing_Htet_Player();
			case 11: return new WinStayLoseShift();
			case 10: return new WinStayLoseShift();
			case 9: return new ConservativePlayer();
			case 8: return new ConservativePlayer();
			case 7: return new Ngo_Jason_Player();
			case 6: return new Ngo_Jason_Player();
			case 5: return new Nice2();
			case 4: return new Nice2();
			case 3: return new EncourageCoop2();
			case 2: return new EncourageCoop2();
			case 1: return new WILSON_TENG_Player();
			case 0: return new WILSON_TENG_Player();
		}
		throw new RuntimeException("Bad argument passed to makePlayer");
		/*


		switch (which) {
			case 6: return new FreakyPlayer();
			case 5: return new NastyPlayer();
			case 4: return new RandomPlayer();
			case 3: return new NicePlayer();
			case 2: return new TolerantPlayer();
			case 1: return new T4TPlayer();
			case 0: return new YaoLongTeng();


		}
		throw new RuntimeException("Bad argument passed to makePlayer"); */



	}
	
	/* Finally, the remaining code actually runs the tournament. */
	
	public static void main (String[] args) {
		ThreePrisonersDilemma instance = new ThreePrisonersDilemma();
		instance.runTournament();

	}
	
	boolean verbose = true; // set verbose = false if you get too much text output
	
	void runTournament() {
		float[] totalScore = new float[numPlayers];

		// This loop plays each triple of players against each other.
		// Note that we include duplicates: two copies of your strategy will play once
		// against each other strategy, and three copies of your strategy will play once.

		for (int i=0; i<numPlayers; i++) for (int j=i; j<numPlayers; j++) for (int k=j; k<numPlayers; k++) {

			Player A = makePlayer(i); // Create a fresh copy of each player
			Player B = makePlayer(j);
			Player C = makePlayer(k);
			int rounds = 90 + (int)Math.rint(20 * Math.random()); // Between 90 and 110 rounds
			float[] matchResults = scoresOfMatch(A, B, C, rounds); // Run match
			totalScore[i] = totalScore[i] + matchResults[0];
			totalScore[j] = totalScore[j] + matchResults[1];
			totalScore[k] = totalScore[k] + matchResults[2];
			if (verbose)
				System.out.println(A.name() + " scored " + matchResults[0] +
						" points, " + B.name() + " scored " + matchResults[1] + 
						" points, and " + C.name() + " scored " + matchResults[2] + " points.");
		}
		int[] sortedOrder = new int[numPlayers];
		// This loop sorts the players by their score.
		for (int i=0; i<numPlayers; i++) {
			int j=i-1;
			for (; j>=0; j--) {
				if (totalScore[i] > totalScore[sortedOrder[j]]) 
					sortedOrder[j+1] = sortedOrder[j];
				else break;
			}
			sortedOrder[j+1] = i;
		}
		
		// Finally, print out the sorted results.
		if (verbose) System.out.println();
		System.out.println("Tournament Results");
		for (int i=0; i<numPlayers; i++) 
			System.out.println(makePlayer(sortedOrder[i]).name() + ": " 
				+ totalScore[sortedOrder[i]] + " points.");

	} // end of runTournament()
	
} // end of class PrisonersDilemma

