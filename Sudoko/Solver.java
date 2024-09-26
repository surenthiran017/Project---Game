public class Solver {

    public static boolean solve(int[][] board) {
        int[] empty = findEmpty(board);
        if (empty == null) return true;

        int row = empty[0];
        int col = empty[1];

        for (int i = 1; i <= 9; i++) {
            if (valid(board, i, new int[]{row, col})) {
                board[row][col] = i;

                if (solve(board)) {
                    return true;
                }

                board[row][col] = 0;
            }
        }

        return false;
    }

    public static boolean valid(int[][] board, int num, int[] pos) {
        // Check row
        for (int i = 0; i < board[0].length; i++) {
            if (board[pos[0]][i] == num && pos[1] != i) {
                return false;
            }
        }

        // Check column
        for (int i = 0; i < board.length; i++) {
            if (board[i][pos[1]] == num && pos[0] != i) {
                return false;
            }
        }

        // Check box
        int boxX = pos[1] / 3;
        int boxY = pos[0] / 3;

        for (int i = boxY * 3; i < boxY * 3 + 3; i++) {
            for (int j = boxX * 3; j < boxX * 3 + 3; j++) {
                if (board[i][j] == num && (i != pos[0] || j != pos[1])) {
                    return false;
                }
            }
        }

        return true;
    }

    private static int[] findEmpty(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 0) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }
}
