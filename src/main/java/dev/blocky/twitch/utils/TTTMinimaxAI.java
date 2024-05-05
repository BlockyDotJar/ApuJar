/**
 * ApuJar - A useful bot for Twitch with many cool utility features.
 * Copyright (C) 2024 BlockyDotJar (aka. Dominic R.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.blocky.twitch.utils;

import java.util.Random;

public class TTTMinimaxAI
{
    private static final Random random = new Random();

    private final double strength;

    public TTTMinimaxAI(int strength)
    {
        this.strength = strength;
    }

    public int doMove(int[] board, int player)
    {
        double next = random.nextDouble();

        if (strength != 1 && next >= strength)
        {
            int emptyCells = 0;

            for (int i : board)
            {
                if (i == 0)
                {
                    emptyCells++;
                }
            }

            int j = random.nextInt(emptyCells);

            for (int i = 0; i < board.length; i++)
            {
                if (board[i] == 0)
                {
                    if (j == 0)
                    {
                        return i;
                    }

                    j--;
                }
            }
        }

        int bestMove = 0;
        int bestMoveScore = Integer.MIN_VALUE;

        for (int i = 0; i < board.length; i++)
        {
            if (board[i] == 0)
            {
                board[i] = player;

                int score = min(board, player % 2 + 1, 1, bestMoveScore);

                if (score > bestMoveScore)
                {
                    bestMoveScore = score;
                    bestMove = i;
                }

                board[i] = 0;
            }
        }
        return bestMove;
    }

    private int min(int[] board, int player, int depth, int currentMaxScore)
    {
        int winner = checkForWinner(board);

        if (winner != 0)
        {
            int score = 100 - depth;
            return winner == player ? -score : score;
        }

        if (isFull(board))
        {
            return 0;
        }

        int bestMoveScore = Integer.MAX_VALUE;

        for (int i = 0; i < board.length; i++)
        {
            if (board[i] == 0)
            {
                board[i] = player;

                int score = max(board, player % 2 + 1, depth + 1, bestMoveScore);

                if (score < bestMoveScore)
                {
                    bestMoveScore = score;
                }

                board[i] = 0;

                if (bestMoveScore <= currentMaxScore)
                {
                    return bestMoveScore;
                }
            }
        }
        return bestMoveScore;
    }

    private int max(int[] board, int player, int depth, int currentMinScore)
    {
        int winner = checkForWinner(board);

        if (winner != 0)
        {
            int score = 100 - depth;
            return winner == player ? score : -score;
        }

        if (isFull(board))
        {
            return 0;
        }

        int bestMoveScore = Integer.MIN_VALUE;

        for (int i = 0; i < board.length; i++)
        {
            if (board[i] == 0)
            {
                board[i] = player;

                int score = min(board, player % 2 + 1, depth + 1, bestMoveScore);

                if (score > bestMoveScore)
                {
                    bestMoveScore = score;
                }

                board[i] = 0;

                if (bestMoveScore >= currentMinScore)
                {
                    return bestMoveScore;
                }
            }
        }
        return bestMoveScore;
    }

    public static int checkForWinner(int[] board)
    {
        int winner = checkHorizontally(board);

        if (winner != 0)
        {
            return winner;
        }

        winner = checkVertically(board);

        if (winner != 0)
        {
            return winner;
        }

        winner = checkDiagonally(board, 1);

        if (winner != 0)
        {
            return winner;
        }

        winner = checkDiagonally(board, 2);

        return winner;
    }

    private static int checkHorizontally(int[] board)
    {
        for (int i = 0; i < 3; i++)
        {
            boolean x = true;
            boolean o = true;

            for (int j = 0; j < 3; j++)
            {
                int k = i * 3 + j;

                if (board[k] != 1)
                {
                    x = false;
                }

                if (board[k] != 2)
                {
                    o = false;
                }
            }

            if (x)
            {
                return 1;
            }

            if (o)
            {
                return 2;
            }
        }
        return 0;
    }

    private static int checkVertically(int[] board)
    {
        for (int i = 0; i < 3; i++)
        {
            boolean x = true;
            boolean o = true;

            for (int j = 0; j < 3; j++)
            {
                int k = j * 3 + i;

                if (board[k] != 1)
                {
                    x = false;
                }

                if (board[k] != 2)
                {
                    o = false;
                }
            }

            if (x)
            {
                return 1;
            }

            if (o)
            {
                return 2;
            }
        }
        return 0;
    }

    private static int checkDiagonally(int[] board, int player)
    {
        boolean rigthToLeft = true;
        boolean leftToRight = true;

        for (int i = 0; i < 3; i++)
        {
            int j = i * 3 + i;

            if (board[j] != player)
            {
                rigthToLeft = false;
            }

            j = (2 - i) * 3 + i;

            if (board[j] != player)
            {
                leftToRight = false;
            }
        }

        if (rigthToLeft || leftToRight)
        {
            return player;
        }

        return 0;
    }

    private static boolean isFull(int[] board)
    {
        for (int i : board)
        {
            if (i == 0)
            {
                return false;
            }
        }
        return true;
    }
}
