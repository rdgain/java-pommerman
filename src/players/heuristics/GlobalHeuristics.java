package players.heuristics;

import core.ForwardModel;
import core.GameState;
import utils.EventsStatistics;
import utils.Types;

public class GlobalHeuristics extends StateHeuristic {
    private BoardStats rootBoardStats;

    // The order is: bombsTriggered, bombsPlaced, woodsDestroyed, powerUpsTaken, numKills, wastedBombs
    private double[] personalWeights = new double[]{0.1, 0.2, 0.05, 0.15, 0.5, -0.5};
    private double[] globalWeights = new double[]{0.1, 0.2, 0.05, 0.15, 0.5, -0.5};

    public GlobalHeuristics(GameState root) {
        rootBoardStats = new BoardStats(root);
    }

    @Override
    public double evaluateState(GameState gs) {


        boolean gameOver = gs.isTerminal();
        Types.RESULT win = gs.winner();

        // Compute a score relative to the root's state.
//        BoardStats lastBoardState = new BoardStats(gs);
//        double rawScore = rootBoardStats.score(lastBoardState);
        double rawScore = evaluateRaw(gs);

        if(gameOver && win == Types.RESULT.LOSS)
            rawScore = -1;

        if(gameOver && win == Types.RESULT.WIN)
            rawScore = 1;

        return rawScore;
    }

    @Override
    public double evaluateRaw(GameState gs) {
        ForwardModel fm = gs.getFM();
        EventsStatistics es = fm.getEventStats();
        int pid = gs.getPlayerId() - Types.TILETYPE.AGENT0.getKey();

        // calculate personalised heuristics
        double[] playerStats = es.getPlayerStats(pid);
        double playerScore = 0.0;
        for (int i = 0; i < personalWeights.length; i++){
            playerScore += personalWeights[i] * playerStats[i];
        }


        //  calculate global heuristics
        double globalScore = 0.0;
        // todo alive players?
        for (int id = 0; id < Types.NUM_PLAYERS; id++){
            double[] stats = es.getPlayerStats(id);
            for (int i = 0; i < globalWeights.length; i++){
                globalScore += globalWeights[i] * stats[i];
            }
        }
        double result =  playerScore - (globalScore/Types.NUM_PLAYERS);
        System.out.println("Heuristics = " + result + " playerScore = " + playerScore);
        return result;
    }

    public static class BoardStats
    {
        int tick, nTeammates, nEnemies, blastStrength;
        boolean canKick;
        int nWoods;
        static double maxWoods = -1;
        static double maxBlastStrength = 10;

        static double FACTOR_ENEMY;
        static double FACTOR_TEAM;
        static double FACTOR_WOODS = 0.1;
        static double FACTOR_CANKCIK = 0.15;
        static double FACTOR_BLAST = 0.15;

        BoardStats(GameState gs) {
            nEnemies = gs.getAliveEnemyIDs().size();

            // Init weights based on game mode
            if (gs.getGameMode() == Types.GAME_MODE.FFA) {
                FACTOR_TEAM = 0;
                FACTOR_ENEMY = 0.5;
            } else {
                FACTOR_TEAM = 0.1;
                FACTOR_ENEMY = 0.4;
                nTeammates = gs.getAliveTeammateIDs().size();  // We only need to know the alive teammates in team modes
                nEnemies -= 1;  // In team modes there's an extra Dummy agent added that we don't need to care about
            }

            // Save game state information
            this.tick = gs.getTick();
            this.blastStrength = gs.getBlastStrength();
            this.canKick = gs.canKick();

            // Count the number of wood walls
            this.nWoods = 1;
            for (Types.TILETYPE[] gameObjectsTypes : gs.getBoard()) {
                for (Types.TILETYPE gameObjectType : gameObjectsTypes) {
                    if (gameObjectType == Types.TILETYPE.WOOD)
                        nWoods++;
                }
            }
            if (maxWoods == -1) {
                maxWoods = nWoods;
            }
        }

        /**
         * Computes score for a game, in relation to the initial state at the root.
         * Minimizes number of opponents in the game and number of wood walls. Maximizes blast strength and
         * number of teammates, wants to kick.
         * @param futureState the stats of the board at the end of the rollout.
         * @return a score [0, 1]
         */
        double score(BoardStats futureState)
        {
            int diffTeammates = futureState.nTeammates - this.nTeammates;
            int diffEnemies = - (futureState.nEnemies - this.nEnemies);
            int diffWoods = - (futureState.nWoods - this.nWoods);
            int diffCanKick = futureState.canKick ? 1 : 0;
            int diffBlastStrength = futureState.blastStrength - this.blastStrength;

            return (diffEnemies / 3.0) * FACTOR_ENEMY + diffTeammates * FACTOR_TEAM + (diffWoods / maxWoods) * FACTOR_WOODS
                    + diffCanKick * FACTOR_CANKCIK + (diffBlastStrength / maxBlastStrength) * FACTOR_BLAST;
        }

        static double rawScore(BoardStats futureState) {
            int diffTeammates = futureState.nTeammates;
            int diffEnemies = - futureState.nEnemies;
            int diffWoods = - futureState.nWoods;
            int diffCanKick = futureState.canKick ? 1 : 0;
            int diffBlastStrength = futureState.blastStrength;

            return (diffEnemies / 3.0) * FACTOR_ENEMY + diffTeammates * FACTOR_TEAM + (diffWoods / maxWoods) * FACTOR_WOODS
                    + diffCanKick * FACTOR_CANKCIK + (diffBlastStrength / maxBlastStrength) * FACTOR_BLAST;
        }
    }
}
