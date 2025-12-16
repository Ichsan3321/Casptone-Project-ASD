package MazeApp;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Main entry point
 */
public class MazeApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MazeFrame frame = new MazeFrame();
            frame.setVisible(true);
        });
    }
}

/**
 * Jenis terrain + cost
 */
enum TerrainType {
    DEFAULT("Default (0)", 0),
    GRASS("Grass (1)", 1),
    MUD("Mud (5)", 5),
    WATER("Water (10)", 10);

    private final String label;
    private final int cost;

    TerrainType(String label, int cost) {
        this.label = label;
        this.cost = cost;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return label;
    }
}

/**
 * Satu cell di maze
 */
class MazeCell {
    public final int row;
    public final int col;

    // 0 = UP, 1 = RIGHT, 2 = DOWN, 3 = LEFT
    public boolean[] walls = new boolean[4];

    private TerrainType terrain = TerrainType.DEFAULT;

    public MazeCell(int row, int col) {
        this.row = row;
        this.col = col;
        resetWalls();
    }

    public void resetWalls() {
        Arrays.fill(walls, true);
        terrain = TerrainType.DEFAULT;
    }

    public TerrainType getTerrain() {
        return terrain;
    }

    public void setTerrain(TerrainType terrain) {
        this.terrain = terrain;
    }

    public int getTerrainCost() {
        return terrain.getCost();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MazeCell)) return false;
        MazeCell other = (MazeCell) o;
        return this.row == other.row && this.col == other.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}

/**
 * Frame utama dengan panel kontrol + maze + stopwatch
 */
class MazeFrame extends JFrame {

    private final MazePanel mazePanel;
    private final JComboBox<MazeGenerator.GenerationAlgorithm> genAlgoCombo;
    private final JComboBox<TerrainType> terrainCombo;
    private final JComboBox<MazeSolver.SolverAlgorithm> solverCombo;
    private final JCheckBox animateCheck;
    private final JLabel statusLabel;

    // Stats labels (timer & info)
    private final JLabel algoValueLabel;
    private final JLabel timeValueLabel;
    private final JLabel stopwatchValueLabel;
    private final JLabel visitedValueLabel;
    private final JLabel pathValueLabel;
    private final JLabel costValueLabel;

    // Stopwatch internal
    private javax.swing.Timer stopwatchTimer;
    private double stopwatchSeconds = 0.0;

    public MazeFrame() {
        setTitle("Maze Solver Visualizer - Prim, Kruskal, BFS, DFS, Dijkstra, A*");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1050, 650));
        setLocationRelativeTo(null);

        int rows = 20;
        int cols = 30;
        mazePanel = new MazePanel(rows, cols);

        // Listener animasi → kontrol stopwatch
        mazePanel.setAnimationListener(new MazePanel.AnimationListener() {
            @Override
            public void onAnimationStart() {
                startStopwatch();
            }

            @Override
            public void onAnimationEnd() {
                stopStopwatch();
            }
        });

        // ---------- Panel Kontrol (kanan) ----------
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        controlPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        controlPanel.setBackground(new Color(20, 22, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("Maze Controller");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 0;
        controlPanel.add(titleLabel, gbc);

        JSeparator sep1 = new JSeparator();
        sep1.setForeground(new Color(80, 80, 80));
        gbc.gridy = 1;
        controlPanel.add(sep1, gbc);

        // Maze generation
        JLabel genLabel = new JLabel("Generation Algorithm");
        genLabel.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 2;
        controlPanel.add(genLabel, gbc);

        genAlgoCombo = new JComboBox<>(MazeGenerator.GenerationAlgorithm.values());
        styleCombo(genAlgoCombo);
        gbc.gridy = 3;
        controlPanel.add(genAlgoCombo, gbc);

        JButton generateBtn = new JButton("Generate Maze");
        styleButton(generateBtn);
        gbc.gridy = 4;
        controlPanel.add(generateBtn, gbc);

        // Terrain
        JLabel terrainLabel = new JLabel("Terrain Paint");
        terrainLabel.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 5;
        controlPanel.add(terrainLabel, gbc);

        terrainCombo = new JComboBox<>(TerrainType.values());
        styleCombo(terrainCombo);
        gbc.gridy = 6;
        controlPanel.add(terrainCombo, gbc);

        JLabel terrainHint = new JLabel("Click on cells to paint terrain");
        terrainHint.setForeground(new Color(150, 150, 150));
        terrainHint.setFont(terrainHint.getFont().deriveFont(11f));
        gbc.gridy = 7;
        controlPanel.add(terrainHint, gbc);

        // Solver
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(80, 80, 80));
        gbc.gridy = 8;
        controlPanel.add(sep2, gbc);

        JLabel solverLabel = new JLabel("Solver Algorithm");
        solverLabel.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 9;
        controlPanel.add(solverLabel, gbc);

        solverCombo = new JComboBox<>(MazeSolver.SolverAlgorithm.values());
        styleCombo(solverCombo);
        gbc.gridy = 10;
        controlPanel.add(solverCombo, gbc);

        animateCheck = new JCheckBox("Animate steps");
        animateCheck.setForeground(Color.WHITE);
        animateCheck.setBackground(controlPanel.getBackground());
        animateCheck.setSelected(true);
        gbc.gridy = 11;
        controlPanel.add(animateCheck, gbc);

        JButton solveBtn = new JButton("Solve Maze");
        styleButton(solveBtn);
        gbc.gridy = 12;
        controlPanel.add(solveBtn, gbc);

        // ---------- Stats Panel (Timer & info) ----------
        JLabel statsHeader = new JLabel("Last Run Stats");
        statsHeader.setForeground(new Color(190, 190, 190));
        statsHeader.setFont(statsHeader.getFont().deriveFont(Font.BOLD, 14f));
        gbc.gridy = 13;
        controlPanel.add(statsHeader, gbc);

        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setBackground(new Color(28, 30, 40));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 90)),
                new EmptyBorder(8, 10, 8, 10)
        ));

        GridBagConstraints sg = new GridBagConstraints();
        sg.gridx = 0;
        sg.gridy = 0;
        sg.anchor = GridBagConstraints.WEST;
        sg.insets = new Insets(2, 2, 2, 2);

        JLabel algoLabel = new JLabel("Algorithm:");
        algoLabel.setForeground(new Color(180, 180, 180));
        statsPanel.add(algoLabel, sg);

        algoValueLabel = new JLabel("-");
        algoValueLabel.setForeground(Color.WHITE);
        algoValueLabel.setFont(algoValueLabel.getFont().deriveFont(Font.BOLD, 12f));
        sg.gridx = 1;
        statsPanel.add(algoValueLabel, sg);

        sg.gridx = 0;
        sg.gridy++;
        JLabel timeLabel = new JLabel("Execution Time:");
        timeLabel.setForeground(new Color(180, 180, 180));
        statsPanel.add(timeLabel, sg);

        timeValueLabel = new JLabel("-");
        timeValueLabel.setForeground(Color.WHITE);
        sg.gridx = 1;
        statsPanel.add(timeValueLabel, sg);

        sg.gridx = 0;
        sg.gridy++;
        JLabel stopwatchLabel = new JLabel("Stopwatch:");
        stopwatchLabel.setForeground(new Color(180, 180, 180));
        statsPanel.add(stopwatchLabel, sg);

        stopwatchValueLabel = new JLabel("-");
        stopwatchValueLabel.setForeground(new Color(0, 255, 180));
        stopwatchValueLabel.setFont(new Font("Consolas", Font.BOLD, 12));
        sg.gridx = 1;
        statsPanel.add(stopwatchValueLabel, sg);

        sg.gridx = 0;
        sg.gridy++;
        JLabel visitedLabel = new JLabel("Visited Nodes:");
        visitedLabel.setForeground(new Color(180, 180, 180));
        statsPanel.add(visitedLabel, sg);

        visitedValueLabel = new JLabel("-");
        visitedValueLabel.setForeground(Color.WHITE);
        sg.gridx = 1;
        statsPanel.add(visitedValueLabel, sg);

        sg.gridx = 0;
        sg.gridy++;
        JLabel pathLabel = new JLabel("Path Length:");
        pathLabel.setForeground(new Color(180, 180, 180));
        statsPanel.add(pathLabel, sg);

        pathValueLabel = new JLabel("-");
        pathValueLabel.setForeground(Color.WHITE);
        sg.gridx = 1;
        statsPanel.add(pathValueLabel, sg);

        sg.gridx = 0;
        sg.gridy++;
        JLabel costLabel = new JLabel("Total Cost:");
        costLabel.setForeground(new Color(180, 180, 180));
        statsPanel.add(costLabel, sg);

        costValueLabel = new JLabel("-");
        costValueLabel.setForeground(Color.WHITE);
        sg.gridx = 1;
        statsPanel.add(costValueLabel, sg);

        gbc.gridy = 14;
        controlPanel.add(statsPanel, gbc);

        // Status
        statusLabel = new JLabel("Ready.");
        statusLabel.setForeground(new Color(200, 200, 200));
        gbc.gridy = 15;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        controlPanel.add(statusLabel, gbc);

        add(mazePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        // ---------- Listeners ----------
        terrainCombo.addActionListener(e ->
                mazePanel.setCurrentTerrain((TerrainType) terrainCombo.getSelectedItem()));

        generateBtn.addActionListener(e -> {
            MazeGenerator.GenerationAlgorithm algo =
                    (MazeGenerator.GenerationAlgorithm) genAlgoCombo.getSelectedItem();
            mazePanel.generateMaze(algo);

            // Reset stats & stopwatch
            algoValueLabel.setText("-");
            timeValueLabel.setText("-");
            visitedValueLabel.setText("-");
            pathValueLabel.setText("-");
            costValueLabel.setText("-");
            stopwatchValueLabel.setText("-");
            stopStopwatch();

            statusLabel.setText("Maze generated with " + algo + ". Paint terrain, then choose a solver.");
        });

        solveBtn.addActionListener(e -> {
            MazeSolver.SolverAlgorithm algo =
                    (MazeSolver.SolverAlgorithm) solverCombo.getSelectedItem();
            boolean animate = animateCheck.isSelected();

            // Pastikan stopwatch lama berhenti
            stopStopwatch();

            MazeSolver.Result result = mazePanel.solveMaze(algo, animate);

            if (result == null || result.path == null || result.path.isEmpty()) {
                statusLabel.setText("No path found with " + algo + ".");
                algoValueLabel.setText(algo.toString());
                timeValueLabel.setText("-");
                visitedValueLabel.setText("-");
                pathValueLabel.setText("0");
                costValueLabel.setText("-");
                stopwatchValueLabel.setText("-");
            } else {
                int pathLen = (result.path != null ? result.path.size() : 0);
                int visitedCount = (result.visitedOrder != null ? result.visitedOrder.size() : 0);
                double ms = result.executionTimeNanos / 1_000_000.0;

                statusLabel.setText(String.format(
                        "Solved with %s | Path length: %d | Total cost: %d | Time: %.3f ms",
                        algo, pathLen, result.totalCost, ms
                ));

                algoValueLabel.setText(algo.toString());
                timeValueLabel.setText(String.format("%.3f ms", ms));
                visitedValueLabel.setText(String.valueOf(visitedCount));
                pathValueLabel.setText(String.valueOf(pathLen));
                costValueLabel.setText(String.valueOf(result.totalCost));

                // Kalau tidak animasi → stopwatch = waktu eksekusi algonya
                if (!animate) {
                    double secs = result.executionTimeNanos / 1_000_000_000.0;
                    stopwatchValueLabel.setText(String.format("%.6f s", secs));
                }
                // Kalau animate = true → stopwatch dihandle oleh animation listener
            }
        });

        pack();
    }

    private void styleButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setBackground(new Color(76, 110, 245));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 13f));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setBackground(new Color(35, 38, 48));
        combo.setForeground(Color.WHITE);
        combo.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
    }

    // ---------- Stopwatch logic ----------
    private void startStopwatch() {
        stopStopwatch();
        stopwatchSeconds = 0.0;
        stopwatchValueLabel.setText("0.000 s");
        stopwatchTimer = new javax.swing.Timer(50, e -> {
            stopwatchSeconds += 0.05;
            stopwatchValueLabel.setText(String.format("%.3f s", stopwatchSeconds));
        });
        stopwatchTimer.start();
    }

    private void stopStopwatch() {
        if (stopwatchTimer != null) {
            stopwatchTimer.stop();
            stopwatchTimer = null;
        }
    }
}

/**
 * Panel yang menggambar maze + handle animasi + klik terrain
 */
class MazePanel extends JPanel implements MouseListener {

    public interface AnimationListener {
        void onAnimationStart();
        void onAnimationEnd();
    }

    private AnimationListener animationListener;

    private final int rows;
    private final int cols;
    private MazeCell[][] grid;

    private final int cellSize = 25;
    private final int padding = 20;

    private MazeCell startCell;
    private MazeCell goalCell;

    private TerrainType currentTerrain = TerrainType.GRASS;

    // Animasi
    private List<MazeCell> visitedOrder = new ArrayList<>();
    private List<MazeCell> finalPath = new ArrayList<>();
    private int animationIndex = 0;
    private javax.swing.Timer animationTimer;

    public MazePanel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        initGrid();
        setBackground(new Color(15, 17, 25));
        addMouseListener(this);
    }

    private void initGrid() {
        grid = new MazeCell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new MazeCell(r, c);
            }
        }
        startCell = grid[0][0];
        goalCell = grid[rows - 1][cols - 1];
    }

    public void setAnimationListener(AnimationListener listener) {
        this.animationListener = listener;
    }

    public void setCurrentTerrain(TerrainType terrain) {
        this.currentTerrain = terrain;
    }

    public void generateMaze(MazeGenerator.GenerationAlgorithm algo) {
        stopAnimation();
        MazeGenerator.generate(grid, algo);
        // Reset path & visited
        visitedOrder = new ArrayList<>();
        finalPath = new ArrayList<>();
        animationIndex = 0;
        repaint();
    }

    public MazeSolver.Result solveMaze(MazeSolver.SolverAlgorithm algorithm, boolean animate) {
        stopAnimation();
        MazeSolver.Result result = MazeSolver.solve(grid, startCell, goalCell, algorithm);
        if (result == null) {
            return null;
        }
        this.visitedOrder = result.visitedOrder;
        this.finalPath = result.path;
        this.animationIndex = 0;

        if (animate) {
            if (animationListener != null) {
                animationListener.onAnimationStart();
            }
            animationTimer = new javax.swing.Timer(25, e -> {
                int totalSteps = (visitedOrder != null ? visitedOrder.size() : 0)
                        + (finalPath != null ? finalPath.size() : 0);
                if (animationIndex < totalSteps) {
                    animationIndex++;
                    repaint();
                } else {
                    stopAnimation();
                }
            });
            animationTimer.start();
        } else {
            // Langsung tunjukkan final path saja
            animationIndex = (visitedOrder != null ? visitedOrder.size() : 0)
                    + (finalPath != null ? finalPath.size() : 0);
            repaint();
        }
        return result;
    }

    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
            if (animationListener != null) {
                animationListener.onAnimationEnd();
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int w = cols * cellSize + padding * 2;
        int h = rows * cellSize + padding * 2;
        return new Dimension(w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (grid == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // Draw background card
        int cardX = padding / 2;
        int cardY = padding / 2;
        int cardW = cols * cellSize + padding;
        int cardH = rows * cellSize + padding;

        g2.setColor(new Color(24, 26, 36));
        g2.fillRoundRect(cardX, cardY, cardW, cardH, 20, 20);

        // Hitung set cell visited & path yg sudah muncul sesuai animationIndex
        Set<MazeCell> visitedSoFar = new HashSet<>();
        Set<MazeCell> pathSoFar = new HashSet<>();

        int visitedCount = visitedOrder != null ? visitedOrder.size() : 0;
        int pathCount = finalPath != null ? finalPath.size() : 0;

        if (visitedOrder != null) {
            int limit = Math.min(animationIndex, visitedCount);
            for (int i = 0; i < limit; i++) {
                visitedSoFar.add(visitedOrder.get(i));
            }
        }

        if (finalPath != null) {
            int pathStepsToShow = Math.max(0, animationIndex - visitedCount);
            pathStepsToShow = Math.min(pathStepsToShow, pathCount);
            for (int i = 0; i < pathStepsToShow; i++) {
                pathSoFar.add(finalPath.get(i));
            }
        }

        // Gambar cell background (terrain + visited + path)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                MazeCell cell = grid[r][c];

                int x = padding + c * cellSize;
                int y = padding + r * cellSize;

                Color baseColor;

                // Terrain
                switch (cell.getTerrain()) {
                    case GRASS:
                        baseColor = new Color(46, 139, 87); // hijau
                        break;
                    case MUD:
                        baseColor = new Color(139, 69, 19); // coklat
                        break;
                    case WATER:
                        baseColor = new Color(30, 144, 255); // biru
                        break;
                    case DEFAULT:
                    default:
                        baseColor = new Color(40, 42, 56);
                        break;
                }

                g2.setColor(baseColor);
                g2.fillRect(x, y, cellSize, cellSize);

                // Visited overlay
                if (visitedSoFar.contains(cell)) {
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillRect(x, y, cellSize, cellSize);
                }

                // Path overlay
                if (pathSoFar.contains(cell)) {
                    g2.setColor(new Color(255, 215, 0, 140));
                    g2.fillRect(x, y, cellSize, cellSize);
                }

                // Start & Goal highlight
                if (cell.equals(startCell)) {
                    g2.setColor(new Color(144, 238, 144));
                    g2.fillOval(x + 6, y + 6, cellSize - 12, cellSize - 12);
                } else if (cell.equals(goalCell)) {
                    g2.setColor(new Color(255, 99, 71));
                    g2.fillOval(x + 6, y + 6, cellSize - 12, cellSize - 12);
                }
            }
        }

        // Gambar walls
        g2.setColor(new Color(230, 230, 230));
        g2.setStroke(new BasicStroke(2f));

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                MazeCell cell = grid[r][c];
                int x = padding + c * cellSize;
                int y = padding + r * cellSize;

                if (cell.walls[0]) {
                    g2.drawLine(x, y, x + cellSize, y);
                }
                if (cell.walls[1]) {
                    g2.drawLine(x + cellSize, y, x + cellSize, y + cellSize);
                }
                if (cell.walls[2]) {
                    g2.drawLine(x, y + cellSize, x + cellSize, y + cellSize);
                }
                if (cell.walls[3]) {
                    g2.drawLine(x, y, x, y + cellSize);
                }
            }
        }
    }

    // ---- Mouse untuk paint terrain ----
    @Override
    public void mouseClicked(MouseEvent e) {
        int mx = e.getX() - padding;
        int my = e.getY() - padding;
        if (mx < 0 || my < 0) return;

        int c = mx / cellSize;
        int r = my / cellSize;

        if (r >= 0 && r < rows && c >= 0 && c < cols) {
            MazeCell cell = grid[r][c];
            cell.setTerrain(currentTerrain);
            repaint();
        }
    }

    // Unused mouse events
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}

/**
 * Generate maze dengan Prim & Kruskal
 */
class MazeGenerator {

    public enum GenerationAlgorithm {
        PRIM, KRUSKAL
    }

    private static class Edge {
        MazeCell a, b;
        int weight;

        Edge(MazeCell a, MazeCell b, int weight) {
            this.a = a;
            this.b = b;
            this.weight = weight;
        }
    }

    public static void generate(MazeCell[][] grid, GenerationAlgorithm algo) {
        int rows = grid.length;
        int cols = grid[0].length;

        // Reset walls & terrain
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c].resetWalls();
            }
        }

        if (algo == GenerationAlgorithm.PRIM) {
            generatePrim(grid);
        } else {
            generateKruskal(grid);
        }

        // Tambah beberapa extra connection untuk multiple path
        addExtraConnections(grid, (rows * cols) / 5);

        // --- ADD THIS LINE HERE ---
        assignRandomTerrain(grid);
    }

    private static void generatePrim(MazeCell[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;
        Random rand = new Random();

        Set<MazeCell> inMaze = new HashSet<>();
        List<Edge> frontier = new ArrayList<>();

        MazeCell start = grid[rand.nextInt(rows)][rand.nextInt(cols)];
        inMaze.add(start);
        frontier.addAll(getNeighborEdges(grid, start, rand));

        while (!frontier.isEmpty()) {
            int idx = rand.nextInt(frontier.size());
            Edge edge = frontier.remove(idx);

            MazeCell next = edge.b;
            if (inMaze.contains(next)) {
                continue;
            }

            inMaze.add(next);
            removeWall(edge.a, edge.b);

            frontier.addAll(getNeighborEdges(grid, next, rand));
        }
    }

    private static void generateKruskal(MazeCell[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;
        Random rand = new Random();

        List<Edge> edges = new ArrayList<>();

        // Buat edge antar cell tetangga
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                MazeCell cell = grid[r][c];
                // kanan
                if (c + 1 < cols) {
                    edges.add(new Edge(cell, grid[r][c + 1], rand.nextInt(1000)));
                }
                // bawah
                if (r + 1 < rows) {
                    edges.add(new Edge(cell, grid[r + 1][c], rand.nextInt(1000)));
                }
            }
        }

        // Sort edge by weight
        edges.sort(Comparator.comparingInt(e -> e.weight));

        UnionFind uf = new UnionFind(rows * cols);

        for (Edge edge : edges) {
            int idA = edge.a.row * cols + edge.a.col;
            int idB = edge.b.row * cols + edge.b.col;
            if (uf.find(idA) != uf.find(idB)) {
                uf.union(idA, idB);
                removeWall(edge.a, edge.b);
            }
        }
    }

    private static List<Edge> getNeighborEdges(MazeCell[][] grid, MazeCell cell, Random rand) {
        int rows = grid.length;
        int cols = grid[0].length;
        List<Edge> result = new ArrayList<>();

        int r = cell.row;
        int c = cell.col;

        if (r > 0) result.add(new Edge(cell, grid[r - 1][c], rand.nextInt(1000)));
        if (r < rows - 1) result.add(new Edge(cell, grid[r + 1][c], rand.nextInt(1000)));
        if (c > 0) result.add(new Edge(cell, grid[r][c - 1], rand.nextInt(1000)));
        if (c < cols - 1) result.add(new Edge(cell, grid[r][c + 1], rand.nextInt(1000)));

        return result;
    }

    private static void removeWall(MazeCell a, MazeCell b) {
        int dr = b.row - a.row;
        int dc = b.col - a.col;

        if (dr == -1 && dc == 0) { // b di atas a
            a.walls[0] = false;
            b.walls[2] = false;
        } else if (dr == 1 && dc == 0) { // b di bawah a
            a.walls[2] = false;
            b.walls[0] = false;
        } else if (dr == 0 && dc == 1) { // b di kanan a
            a.walls[1] = false;
            b.walls[3] = false;
        } else if (dr == 0 && dc == -1) { // b di kiri a
            a.walls[3] = false;
            b.walls[1] = false;
        }
    }

    private static void addExtraConnections(MazeCell[][] grid, int extraCount) {
        int rows = grid.length;
        int cols = grid[0].length;
        Random rand = new Random();

        int attempts = extraCount * 5;
        int added = 0;

        while (added < extraCount && attempts-- > 0) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            MazeCell cell = grid[r][c];

            List<MazeCell> neighbors = new ArrayList<>();
            if (r > 0) neighbors.add(grid[r - 1][c]);
            if (r < rows - 1) neighbors.add(grid[r + 1][c]);
            if (c > 0) neighbors.add(grid[r][c - 1]);
            if (c < cols - 1) neighbors.add(grid[r][c + 1]);

            if (neighbors.isEmpty()) continue;

            MazeCell nb = neighbors.get(rand.nextInt(neighbors.size()));

            // Kalau masih ada wall antara cell & neighbor → remove untuk buat loop
            if (hasWallBetween(cell, nb)) {
                removeWall(cell, nb);
                added++;
            }
        }
    }

    private static boolean hasWallBetween(MazeCell a, MazeCell b) {
        int dr = b.row - a.row;
        int dc = b.col - a.col;

        if (dr == -1 && dc == 0) {
            return a.walls[0] || b.walls[2];
        } else if (dr == 1 && dc == 0) {
            return a.walls[2] || b.walls[0];
        } else if (dr == 0 && dc == 1) {
            return a.walls[1] || b.walls[3];
        } else if (dr == 0 && dc == -1) {
            return a.walls[3] || b.walls[1];
        }
        return false;
    }

    // Union-Find untuk Kruskal
    private static class UnionFind {
        private final int[] parent;
        private final int[] rank;

        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        void union(int a, int b) {
            int ra = find(a);
            int rb = find(b);
            if (ra == rb) return;
            if (rank[ra] < rank[rb]) {
                parent[ra] = rb;
            } else if (rank[ra] > rank[rb]) {
                parent[rb] = ra;
            } else {
                parent[rb] = ra;
                rank[ra]++;
            }
        }
    }

    private static void assignRandomTerrain(MazeCell[][] grid) {
        Random rand = new Random();
        int rows = grid.length;
        int cols = grid[0].length;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Keep Start and Goal cells easy (Default terrain)
                if ((r == 0 && c == 0) || (r == rows - 1 && c == cols - 1)) {
                    grid[r][c].setTerrain(TerrainType.DEFAULT);
                    continue;
                }

                double chance = rand.nextDouble(); // 0.0 to 1.0

                // Adjust probabilities here:
                if (chance < 0.60) { 
                    // 60% chance for Default (Cost 0)
                    grid[r][c].setTerrain(TerrainType.DEFAULT);
                } else if (chance < 0.80) { 
                    // 20% chance for Grass (Cost 1)
                    grid[r][c].setTerrain(TerrainType.GRASS);
                } else if (chance < 0.95) { 
                    // 15% chance for Mud (Cost 5)
                    grid[r][c].setTerrain(TerrainType.MUD);
                } else { 
                    // 5% chance for Water (Cost 10)
                    grid[r][c].setTerrain(TerrainType.WATER);
                }
            }
        }
    }
}

/**
 * Solver: BFS, DFS (stack), Dijkstra, A*
 */
class MazeSolver {

    public enum SolverAlgorithm {
        BFS, DFS, DIJKSTRA, ASTAR
    }

    public static class Result {
        public List<MazeCell> path;
        public List<MazeCell> visitedOrder;
        public int totalCost;
        public long executionTimeNanos; // timer algonya

        public Result(List<MazeCell> path, List<MazeCell> visitedOrder, int totalCost) {
            this.path = path;
            this.visitedOrder = visitedOrder;
            this.totalCost = totalCost;
            this.executionTimeNanos = 0L;
        }
    }

    public static Result solve(MazeCell[][] grid, MazeCell start, MazeCell goal, SolverAlgorithm algo) {
        long startTime = System.nanoTime();
        Result res;

        switch (algo) {
            case BFS:
                res = bfs(grid, start, goal);
                break;
            case DFS:
                res = dfs(grid, start, goal);
                break;
            case DIJKSTRA:
                res = dijkstra(grid, start, goal);
                break;
            case ASTAR:
                res = aStar(grid, start, goal);
                break;
            default:
                res = null;
        }

        long endTime = System.nanoTime();
        if (res != null) {
            res.executionTimeNanos = endTime - startTime;
        }
        return res;
    }

    private static Result bfs(MazeCell[][] grid, MazeCell start, MazeCell goal) {
        Queue<MazeCell> queue = new ArrayDeque<>();
        Map<MazeCell, MazeCell> parent = new HashMap<>();
        Set<MazeCell> visited = new HashSet<>();
        List<MazeCell> visitedOrder = new ArrayList<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            MazeCell current = queue.poll();
            visitedOrder.add(current);

            if (current.equals(goal)) {
                break;
            }

            for (MazeCell nb : getNeighbors(grid, current)) {
                if (!visited.contains(nb)) {
                    visited.add(nb);
                    parent.put(nb, current);
                    queue.add(nb);
                }
            }
        }

        List<MazeCell> path = reconstructPath(parent, start, goal);
        int cost = computePathCost(path);
        return new Result(path, visitedOrder, cost);
    }

    private static Result dfs(MazeCell[][] grid, MazeCell start, MazeCell goal) {
        Deque<MazeCell> stack = new ArrayDeque<>();
        Map<MazeCell, MazeCell> parent = new HashMap<>();
        Set<MazeCell> visited = new HashSet<>();
        List<MazeCell> visitedOrder = new ArrayList<>();

        stack.push(start);
        visited.add(start);

        while (!stack.isEmpty()) {
            MazeCell current = stack.pop();
            visitedOrder.add(current);

            if (current.equals(goal)) {
                break;
            }

            for (MazeCell nb : getNeighbors(grid, current)) {
                if (!visited.contains(nb)) {
                    visited.add(nb);
                    parent.put(nb, current);
                    stack.push(nb);
                }
            }
        }

        List<MazeCell> path = reconstructPath(parent, start, goal);
        int cost = computePathCost(path);
        return new Result(path, visitedOrder, cost);
    }

    private static Result dijkstra(MazeCell[][] grid, MazeCell start, MazeCell goal) {
        Map<MazeCell, Integer> dist = new HashMap<>();
        Map<MazeCell, MazeCell> parent = new HashMap<>();
        List<MazeCell> visitedOrder = new ArrayList<>();

        Comparator<MazeCell> cmp = Comparator.comparingInt(dist::get);
        PriorityQueue<MazeCell> pq = new PriorityQueue<>(cmp);

        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                dist.put(grid[r][c], Integer.MAX_VALUE);
            }
        }
        dist.put(start, 0);
        pq.add(start);

        while (!pq.isEmpty()) {
            MazeCell current = pq.poll();
            if (visitedOrder.contains(current)) continue; // simple visited check
            visitedOrder.add(current);

            if (current.equals(goal)) break;

            int currentDist = dist.get(current);
            for (MazeCell nb : getNeighbors(grid, current)) {
                int edgeCost = nb.getTerrainCost(); // cost masuk ke cell neighbor
                int newDist = currentDist + edgeCost;
                if (newDist < dist.get(nb)) {
                    dist.put(nb, newDist);
                    parent.put(nb, current);
                    pq.add(nb);
                }
            }
        }

        List<MazeCell> path = reconstructPath(parent, start, goal);
        int cost = computePathCost(path);
        return new Result(path, visitedOrder, cost);
    }

    private static Result aStar(MazeCell[][] grid, MazeCell start, MazeCell goal) {
        Map<MazeCell, Integer> gScore = new HashMap<>();
        Map<MazeCell, Integer> fScore = new HashMap<>();
        Map<MazeCell, MazeCell> parent = new HashMap<>();
        List<MazeCell> visitedOrder = new ArrayList<>();

        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                MazeCell cell = grid[r][c];
                gScore.put(cell, Integer.MAX_VALUE);
                fScore.put(cell, Integer.MAX_VALUE);
            }
        }

        gScore.put(start, 0);
        fScore.put(start, heuristic(start, goal));

        PriorityQueue<MazeCell> openSet =
                new PriorityQueue<>(Comparator.comparingInt(fScore::get));
        openSet.add(start);

        Set<MazeCell> inOpen = new HashSet<>();
        inOpen.add(start);

        while (!openSet.isEmpty()) {
            MazeCell current = openSet.poll();
            inOpen.remove(current);
            visitedOrder.add(current);

            if (current.equals(goal)) {
                break;
            }

            for (MazeCell nb : getNeighbors(grid, current)) {
                int tentativeG = gScore.get(current) + nb.getTerrainCost();
                if (tentativeG < gScore.get(nb)) {
                    parent.put(nb, current);
                    gScore.put(nb, tentativeG);
                    fScore.put(nb, tentativeG + heuristic(nb, goal));
                    if (!inOpen.contains(nb)) {
                        openSet.add(nb);
                        inOpen.add(nb);
                    }
                }
            }
        }

        List<MazeCell> path = reconstructPath(parent, start, goal);
        int cost = computePathCost(path);
        return new Result(path, visitedOrder, cost);
    }

    private static int heuristic(MazeCell a, MazeCell b) {
        // Manhattan distance
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    private static List<MazeCell> getNeighbors(MazeCell[][] grid, MazeCell cell) {
        List<MazeCell> result = new ArrayList<>();
        int rows = grid.length;
        int cols = grid[0].length;
        int r = cell.row;
        int c = cell.col;

        // UP
        if (!cell.walls[0] && r > 0) {
            result.add(grid[r - 1][c]);
        }
        // RIGHT
        if (!cell.walls[1] && c < cols - 1) {
            result.add(grid[r][c + 1]);
        }
        // DOWN
        if (!cell.walls[2] && r < rows - 1) {
            result.add(grid[r + 1][c]);
        }
        // LEFT
        if (!cell.walls[3] && c > 0) {
            result.add(grid[r][c - 1]);
        }

        return result;
    }

    private static List<MazeCell> reconstructPath(
            Map<MazeCell, MazeCell> parent,
            MazeCell start,
            MazeCell goal
    ) {
        List<MazeCell> path = new ArrayList<>();
        if (!start.equals(goal) && !parent.containsKey(goal)) {
            return path; // kosong, no path
        }

        MazeCell current = goal;
        path.add(current);
        while (!current.equals(start)) {
            current = parent.get(current);
            if (current == null) {
                return new ArrayList<>(); // no path
            }
            path.add(current);
        }
        Collections.reverse(path);
        return path;
    }

    private static int computePathCost(List<MazeCell> path) {
        int cost = 0;
        for (MazeCell cell : path) {
            cost += cell.getTerrainCost();
        }
        return cost;
    }
}
