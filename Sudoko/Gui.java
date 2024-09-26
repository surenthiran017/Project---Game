import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class Gui extends JPanel implements KeyListener, MouseListener {
    private Grid board;
    private int key;
    private boolean run;
    private long start;
    private int strikes;

    public Gui() {
        board = new Grid(9, 9, 540, 540);
        key = -1;
        run = true;
        start = System.currentTimeMillis();
        strikes = 0;
        setPreferredSize(new Dimension(540, 600));
        addKeyListener(this);
        addMouseListener(this);
        setFocusable(true);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        long playTime = (System.currentTimeMillis() - start) / 1000;
        redrawWindow(g, playTime, strikes);
    }

    private void redrawWindow(Graphics g, long playTime, int strikes) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 540, 600);
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 40));

        // Draw time
        String timeString = "Time: " + formatTime(playTime);
        g.drawString(timeString, 380, 580);

        // Draw strikes
        String strikeString = "X ".repeat(strikes);
        g.setColor(Color.RED);
        g.drawString(strikeString, 20, 580);

        // Draw grid and board
        board.draw(g);
    }

    private String formatTime(long secs) {
        long minute = secs / 60;
        long sec = secs % 60;
        return " " + minute + ":" + sec;
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_9) {
            key = code - KeyEvent.VK_0; // Store the pressed number
            if (board.selected != null) {
                Cube selectedCube = board.getSelectedCube();
                if (selectedCube != null && selectedCube.value == 0) {
                    selectedCube.temp = key; // Set the temp value to the key
                }
            }
        } else if (code == KeyEvent.VK_DELETE) {
            board.clear(); // Clear the selected cube
            key = -1;
        } else if (code == KeyEvent.VK_ENTER) {
            if (board.selected != null) {
                Cube selectedCube = board.getSelectedCube();
                if (selectedCube != null && selectedCube.temp != 0) {
                    if (board.place(selectedCube.temp)) {
                        System.out.println("Success");
                    } else {
                        System.out.println("Wrong");
                        strikes++;
                    }
                    key = -1;
                    if (board.isFinished()) {
                        System.out.println("Game over");
                        run = false;
                    }
                }
            }
        }
        repaint(); // Repaint after handling input
    }
    

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (x < 540 && y < 540) {
            int[] clicked = board.click(x, y);
            if (clicked != null) {
                board.select(clicked[0], clicked[1]);
                key = -1;
            }
        }
        repaint();
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Sudoku");
        Gui gamePanel = new Gui();
        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class Grid {
    private Cube[][] cubes;
    private int rows, cols, width, height;
    public int[] selected;

    public Grid(int rows, int cols, int width, int height) {
        this.rows = rows;
        this.cols = cols;
        this.width = width;
        this.height = height;
        this.selected = null;
        cubes = new Cube[rows][cols];

        int[][] board = {
            {7, 8, 0, 4, 0, 0, 1, 2, 0},
            {6, 0, 0, 0, 7, 5, 0, 0, 9},
            {0, 0, 0, 6, 0, 1, 0, 7, 8},
            {0, 0, 7, 0, 4, 0, 2, 6, 0},
            {0, 0, 1, 0, 5, 0, 9, 3, 0},
            {9, 0, 4, 0, 6, 0, 0, 0, 5},
            {0, 7, 0, 3, 0, 0, 0, 1, 2},
            {1, 2, 0, 0, 0, 7, 4, 0, 0},
            {0, 4, 9, 2, 0, 6, 0, 0, 7}
        };

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cubes[i][j] = new Cube(board[i][j], i, j, width, height);
            }
        }
    }

    public void draw(Graphics g) {
        int gap = width / 9;
        for (int i = 0; i <= rows; i++) {
            if (i % 3 == 0) {
                g.setColor(Color.BLACK);
                g.fillRect(0, i * gap, width, 4);
                g.fillRect(i * gap, 0, 4, height);
            } else {
                g.setColor(Color.GRAY);
                g.fillRect(0, i * gap, width, 1);
                g.fillRect(i * gap, 0, 1, height);
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cubes[i][j].draw(g);
            }
        }
    }

    public int[] click(int x, int y) {
        int gap = width / 9;
        return new int[]{y / gap, x / gap};
    }

    public void select(int row, int col) {
        if (selected != null) {
            cubes[selected[0]][selected[1]].selected = false;
        }
        cubes[row][col].selected = true;
        selected = new int[]{row, col};
    }

    public void clear() {
        if (selected != null && cubes[selected[0]][selected[1]].value == 0) {
            cubes[selected[0]][selected[1]].temp = 0;
        }
    }

    public boolean place(int val) {
        int row = selected[0], col = selected[1];
        if (cubes[row][col].value == 0) {
            cubes[row][col].set(val);
            if (Solver.valid(getModel(), val, new int[]{row, col}) && Solver.solve(getModel())) {
                return true;
            } else {
                cubes[row][col].set(0);
                return false;
            }
        }
        return false;
    }

    public boolean isFinished() {
        for (Cube[] cubeRow : cubes) {
            for (Cube cube : cubeRow) {
                if (cube.value == 0) return false;
            }
        }
        return true;
    }

    private int[][] getModel() {
        int[][] model = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                model[i][j] = cubes[i][j].value;
            }
        }
        return model;
    }

    public Cube getSelectedCube() {
        if (selected != null) {
            return cubes[selected[0]][selected[1]];
        }
        return null;
    }
}

class Cube {
    public int value, temp;
    public boolean selected;
    private int row, col, width, height;

    public Cube(int value, int row, int col, int width, int height) {
        this.value = value;
        this.temp = 0;
        this.row = row;
        this.col = col;
        this.width = width;
        this.height = height;
        this.selected = false;
    }

    public void draw(Graphics g) {
        int gap = width / 9;
        int x = col * gap;
        int y = row * gap;
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 40));

        if (temp != 0 && value == 0) {
            g.setColor(Color.GRAY);
            g.drawString(String.valueOf(temp), x + 5, y + 40);
        } else if (value != 0) {
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(value), x + gap / 2 - 15, y + gap / 2 + 15);
        }

        if (selected) {
            g.setColor(Color.RED);
            g.drawRect(x, y, gap, gap);
        }
    }

    public void set(int val) {
        this.value = val;
    }
}
