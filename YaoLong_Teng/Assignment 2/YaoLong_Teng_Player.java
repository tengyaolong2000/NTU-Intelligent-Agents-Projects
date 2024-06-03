class YaoLongTeng_Player extends Player {
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