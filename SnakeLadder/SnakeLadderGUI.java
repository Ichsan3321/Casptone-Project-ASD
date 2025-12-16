package SnakeLadder;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Entry point
 */
public class SnakeLadderGUI {

    // ==== SESUAIKAN PATH ASSET DI SINI ====
    public static final String BGM_PATH =
            "C:\\Users\\Ichsan Ramadhan\\IdeaProjects\\Main\\src\\CapstoneProject\\SnakeLadderComponents\\SSurf.wav";

    public static final String[] AVATAR_PATHS = {
            "C:\\Users\\Ichsan Ramadhan\\IdeaProjects\\Main\\src\\CapstoneProject\\SnakeLadderComponents\\avatar1.png",
            "C:\\Users\\Ichsan Ramadhan\\IdeaProjects\\Main\\src\\CapstoneProject\\SnakeLadderComponents\\avatar2.png",
            "C:\\Users\\Ichsan Ramadhan\\IdeaProjects\\Main\\src\\CapstoneProject\\SnakeLadderComponents\\avatar3.png",
            "C:\\Users\\Ichsan Ramadhan\\IdeaProjects\\Main\\src\\CapstoneProject\\SnakeLadderComponents\\avatar4.png"
    };

    public static final String MOVE_SFX_PATH =
            "C:\\Users\\Ichsan Ramadhan\\IdeaProjects\\Main\\src\\CapstoneProject\\SnakeLadderComponents\\step.wav";
    
    public static final String DICE_SFX_PATH =
            "C:\\Users\\Ichsan Ramadhan\\IdeaProjects\\Main\\src\\CapstoneProject\\SnakeLadderComponents\\dice_roll.wav";

    public static final String BOARD_BG_PATH =
            "C:\\Users\\Ichsan Ramadhan\\IdeaProjects\\Main\\src\\CapstoneProject\\SnakeLadderComponents\\BackgroundMap.jpg";
    // =======================================

    public static class PlayerStats {
        public int wins;
        public int totalScore;

        public PlayerStats(int wins, int totalScore) {
            this.wins = wins;
            this.totalScore = totalScore;
        }
    }

    // ==== 2. UPDATE THE MAP ====
    // Key: Name, Value: Stats Object
    public static final Map<String, PlayerStats> PLAYER_HISTORY = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            Font uiFont = UITheme.PRIMARY_FONT;
            Enumeration<?> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object val = UIManager.get(key);
                if (val instanceof Font) {
                    UIManager.put(key, uiFont);
                }
            }

            LobbyFrame lobby = new LobbyFrame();
            lobby.setVisible(true);
        });
    }
}

/* ======================================================================
 *  THEME & REUSABLE COMPONENTS
 * ==================================================================== */

class UITheme {
    public static final Color BG_DARK = new Color(18, 24, 38);
    public static final Color BG_CARD = new Color(30, 39, 56);
    public static final Color ACCENT = new Color(88, 101, 242);
    public static final Color ACCENT_LIGHT = new Color(138, 151, 255);
    public static final Color ACCENT_DANGER = new Color(234, 84, 85);
    public static final Color TEXT_PRIMARY = new Color(236, 240, 241);
    public static final Color TEXT_SECONDARY = new Color(171, 178, 191);

    public static final Font PRIMARY_FONT  = new Font("Inter", Font.PLAIN, 14);
    public static final Font TITLE_FONT    = new Font("Inter", Font.BOLD, 26);
    public static final Font SUBTITLE_FONT = new Font("Inter", Font.PLAIN, 16);

    public static final int RADIUS_LARGE = 24;

    public static GameButton primaryButton(String text) {
        return new GameButton(text, ACCENT, ACCENT_LIGHT, Color.WHITE);
    }

    public static GameButton dangerButton(String text) {
        return new GameButton(text, ACCENT_DANGER, new Color(244, 143, 147), Color.WHITE);
    }

    public static RoundedPanel createCardPanel(int radius) {
        RoundedPanel panel = new RoundedPanel(radius);
        panel.setBackground(BG_CARD);
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        return panel;
    }
}

class RoundedPanel extends JPanel {
    private final int cornerRadius;

    public RoundedPanel(int radius) {
        this.cornerRadius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UIUtilities.enableHighQuality(g2);

        int x = 0;
        int y = 0;
        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRoundRect(x + 3, y + 5, w - 6, h - 6, cornerRadius + 6, cornerRadius + 6);

        Shape round = new RoundRectangle2D.Float(x, y, w - 6, h - 6, cornerRadius, cornerRadius);
        g2.setColor(getBackground() != null ? getBackground() : UITheme.BG_CARD);
        g2.fill(round);

        g2.dispose();
        super.paintComponent(g);
    }
}

class GameButton extends JButton {
    private final Color baseColor;
    private final Color hoverColor;
    private final Color textColor;
    private boolean hovered = false;

    public GameButton(String text, Color baseColor, Color hoverColor, Color textColor) {
        super(text);
        this.baseColor = baseColor;
        this.hoverColor = hoverColor;
        this.textColor = textColor;

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(UITheme.SUBTITLE_FONT);
        setBorder(new EmptyBorder(8, 18, 8, 18));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBorder(new EmptyBorder(9, 18, 7, 18));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setBorder(new EmptyBorder(8, 18, 8, 18));
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UIUtilities.enableHighQuality(g2);

        int w = getWidth();
        int h = getHeight();
        int arc = 18;

        Color c1 = hovered ? hoverColor : baseColor;
        Color c2 = hovered ? baseColor : hoverColor;

        GradientPaint gp = new GradientPaint(0, 0, c1, 0, h, c2);
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawRoundRect(1, 1, w - 2, h - 2, arc, arc);

        FontMetrics fm = g2.getFontMetrics(getFont());
        int textWidth = fm.stringWidth(getText());
        int textX = (w - textWidth) / 2;
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(textColor);
        g2.setFont(getFont());
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}

class UIUtilities {
    public static void enableHighQuality(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
}

/* ======================================================================
 *  MODEL: PLAYER, BOARD, DICE, SOUND
 * ==================================================================== */

class Player {
    private String name;
    private int position = 1;
    private int score = 0;
    private int wins = 0;

    // id pemain (slot ke-berapa di lobby)
    private final int id;

    // avatar
    private int avatarIndex;  // index di AVATAR_PATHS
    private Image avatar;     // cache gambar avatar

    private int lastClimbedLadderEnd = -1;

    public int getLastClimbedLadderEnd() { return lastClimbedLadderEnd; }
    public void setLastClimbedLadderEnd(int endTile) { this.lastClimbedLadderEnd = endTile; }

    public Player(String name, int id, int avatarIndex) {
        this.name = name;
        this.id = id;
        setAvatarIndex(avatarIndex); // sekalian load avatar
    }

    public String getName() { return name; }
    public void setName(String n) { this.name = n; }

    public int getPosition() { return position; }
    public void setPosition(int pos) {
        this.position = Math.max(1, Math.min(Board.SIZE, pos));
    }

    public int getScore() { return score; }
    public void addScore(int delta) { this.score += delta; }
    public void resetScore() { this.score = 0; }

    public int getWins() { return wins; }
    public void addWin() { this.wins++; }

    public void setWins(int wins) { this.wins = wins; }
    public void setScore(int score) { this.score = score; }

    public int getId() { return id; }

    // ==== avatar-related ====
    public int getAvatarIndex() { return avatarIndex; }

    public void setAvatarIndex(int avatarIndex) {
        this.avatarIndex = avatarIndex;
        this.avatar = AvatarLoader.loadAvatar(avatarIndex);
    }

    public Image getAvatar() { return avatar; }
}

class Cell {
    private final int index;
    private final int bonusPoints;
    private final boolean prime;
    private Ladder ladderStart;
    
    // [FIX 1] Add this missing field
    private Ladder ladderEnd; 

    public Cell(int index, int bonusPoints) {
        this.index = index;
        this.bonusPoints = bonusPoints;
        this.prime = isPrime(index);
    }

    public int getIndex() { return index; }
    public int getBonusPoints() { return bonusPoints; }
    public boolean isPrime() { return prime; }

    public Ladder getLadderStart() { return ladderStart; }
    public void setLadderStart(Ladder ladderStart) { this.ladderStart = ladderStart; }

    // [FIX 2] Add these missing getter/setter methods
    public Ladder getLadderEnd() { return ladderEnd; }
    public void setLadderEnd(Ladder ladderEnd) { this.ladderEnd = ladderEnd; }

    private boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }
}

class Ladder {
    private final int start;
    private final int end;
    private boolean visible = true;
    private boolean locked = true;

    public Ladder(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() { return start; }
    public int getEnd() { return end; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
}

enum DiceColor { GREEN, RED }

class DiceResult {
    private final int value;
    private final DiceColor color;

    public DiceResult(int value, DiceColor color) {
        this.value = value;
        this.color = color;
    }

    public int getValue() { return value; }
    public DiceColor getColor() { return color; }
}

class Dice {
    private final Random random = new Random();

    public DiceResult roll() {
        int value = random.nextInt(6) + 1;
        double p = random.nextDouble();
        DiceColor color = (p < 0.7) ? DiceColor.GREEN : DiceColor.RED;
        return new DiceResult(value, color);
    }
}

class Board {
    public static final int SIZE = 64;

    private final Cell[] cells = new Cell[SIZE + 1];
    private final List<Ladder> ladders = new ArrayList<>();
    private final Random random = new Random();

    public Board() {
        initCells();
        generateLadders(5);
    }

    private void initCells() {
        for (int i = 1; i <= SIZE; i++) {
            int bonus = random.nextInt(11);
            cells[i] = new Cell(i, bonus);
        }
    }

    private void generateLadders(int count) {
        int generated = 0;
        // Safety counter to prevent infinite loops if the board gets crowded
        int attempts = 0; 

        while (generated < count && attempts < 1000) {
            attempts++;

            // 1. Random Start (2..61)
            // We removed the "!cells[start].isPrime()" check so it is now fully random.
            int start = random.nextInt(SIZE - 3) + 2; 

            // 2. Random End
            int end = random.nextInt(SIZE - start) + (start + 1);
            
            if (start >= SIZE || end > SIZE) continue;

            // 3. Conflict Check
            boolean conflict = false;
            for (Ladder lad : ladders) {
                if (lad.getStart() == start || lad.getEnd() == end || 
                    lad.getStart() == end || lad.getEnd() == start) {
                    conflict = true;
                    break;
                }
            }
            if (conflict) continue;

            // 4. Create and add the ladder
            Ladder ladder = new Ladder(start, end);
            ladder.setVisible(true);
            ladder.setLocked(true);
            ladders.add(ladder);
            
            // Set Start Reference
            cells[start].setLadderStart(ladder);
            
            // [CRITICAL FIX] Set End Reference logic!
            // This is required for the "Slide Down" logic to work.
            if (end <= SIZE) {
                cells[end].setLadderEnd(ladder); 
            }

            generated++;
        }
    }

    public Cell getCell(int index) {
        if (index < 1) index = 1;
        if (index > SIZE) index = SIZE;
        return cells[index];
    }

    public List<Ladder> getLadders() { return ladders; }

    public Ladder findNearestLadderUsingDijkstra(int fromIndex) {
        int[] dist = new int[SIZE + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[fromIndex] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.offer(new int[]{fromIndex, 0});
        boolean[] visited = new boolean[SIZE + 1];

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int node = cur[0];
            int d = cur[1];
            if (visited[node]) continue;
            visited[node] = true;

            if (node - 1 >= 1 && !visited[node - 1]) {
                int nd = d + 1;
                if (nd < dist[node - 1]) {
                    dist[node - 1] = nd;
                    pq.offer(new int[]{node - 1, nd});
                }
            }
            if (node + 1 <= SIZE && !visited[node + 1]) {
                int nd = d + 1;
                if (nd < dist[node + 1]) {
                    dist[node + 1] = nd;
                    pq.offer(new int[]{node + 1, nd});
                }
            }
        }

        Ladder best = null;
        int bestDist = Integer.MAX_VALUE;
        for (Ladder ladder : ladders) {
            int s = ladder.getStart();
            if (dist[s] < bestDist) {
                bestDist = dist[s];
                best = ladder;
            }
        }
        return best;
    }
}

class SoundManager {
    private Clip bgmClip;
    private Clip moveSfxClip; // Reuse this clip for steps
    
    public SoundManager() {
        // Preload the Move SFX immediately when SoundManager is created
        loadMoveSound();
    }

    private void loadMoveSound() {
        try {
            File f = new File(SnakeLadderGUI.MOVE_SFX_PATH);
            if (!f.exists()) return;

            AudioInputStream ais = AudioSystem.getAudioInputStream(f);
            moveSfxClip = AudioSystem.getClip();
            moveSfxClip.open(ais);

            // Lower volume slightly for the step sound
            try {
                FloatControl control = (FloatControl) moveSfxClip.getControl(FloatControl.Type.MASTER_GAIN);
                control.setValue(-5.0f); 
            } catch (Exception ignored) {}
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playMoveSound() {
        // FAST REPLAY: No new threads, no file loading.
        if (moveSfxClip != null) {
            if (moveSfxClip.isRunning()) {
                moveSfxClip.stop(); // Stop if currently playing
            }
            moveSfxClip.setFramePosition(0); // Rewind to start
            moveSfxClip.start(); // Play
        }
    }

    public void playDiceSound() {
        // Keep dice sound in a thread since it happens rarely (once per turn)
        new Thread(() -> {
            try {
                File f = new File(SnakeLadderGUI.DICE_SFX_PATH);
                if (!f.exists()) return;
                
                AudioInputStream ais = AudioSystem.getAudioInputStream(f);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.start();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    public void playLadderSound() {
        Toolkit.getDefaultToolkit().beep();
    }

    // --- BGM Logic (unchanged) ---
    public void playBackgroundMusic(String filePath) {
        try {
            stopBackgroundMusic();
            File audioFile = new File(filePath);
            if (!audioFile.exists()) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            setBgmVolume(0.8f);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void stopBackgroundMusic() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }

    public void setBgmVolume(float volume) {
        if (bgmClip == null) return;
        try {
            FloatControl control = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = control.getMinimum();
            float max = control.getMaximum();
            float dB = min + (max - min) * volume;
            control.setValue(dB);
        } catch (Exception e) { /* ignore */ }
    }
}

/* ======================================================================
 *  LOBBY + PLAYER SETUP
 * ==================================================================== */

class LobbyFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(cardLayout);

    public LobbyFrame() {
        setTitle("Snake & Ladders Game - Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(16, 16, 16, 16));

        rootPanel.setOpaque(false);

        JPanel startCard = buildStartCard();
        JPanel setupCard = buildSetupCard();

        rootPanel.add(startCard, "start");
        rootPanel.add(setupCard, "setup");

        add(rootPanel, BorderLayout.CENTER);
        cardLayout.show(rootPanel, "start");
    }

    private JPanel buildStartCard() {
        RoundedPanel card = UITheme.createCardPanel(UITheme.RADIUS_LARGE);
        card.setLayout(new BorderLayout(16, 16));

        JLabel title = new JLabel("Snake & Ladders Game", SwingConstants.CENTER);
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Prime Ladder · 64 Tiles · Smart Rules", SwingConstants.CENTER);
        subtitle.setForeground(UITheme.TEXT_SECONDARY);
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        card.add(subtitle, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 8));

        JLabel hint = new JLabel("Tip: Tiles 5, 10, 15, ... grant extra turns!");
        hint.setForeground(UITheme.TEXT_SECONDARY);
        hint.setFont(UITheme.PRIMARY_FONT.deriveFont(Font.ITALIC, 12f));
        bottom.add(hint);

        GameButton playButton = UITheme.primaryButton("Start Playing");
        bottom.add(playButton);

        card.add(bottom, BorderLayout.SOUTH);

        playButton.addActionListener(e -> showLoadingThenSetup());

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        wrapper.add(card, gbc);

        return wrapper;
    }

    private JPanel buildSetupCard() {
        return new PlayerSetupPanel(this);
    }

    private void showLoadingThenSetup() {
        JDialog loading = new JDialog(this, "Loading", true);
        loading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loading.setSize(260, 120);
        loading.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.setBackground(UITheme.BG_CARD);

        JLabel lbl = new JLabel("Preparing board...", SwingConstants.CENTER);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        p.add(lbl, BorderLayout.NORTH);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        p.add(bar, BorderLayout.CENTER);

        loading.setContentPane(p);

        javax.swing.Timer t = new javax.swing.Timer(900, e -> {
            loading.dispose();
            cardLayout.show(rootPanel, "setup");
        });
        t.setRepeats(false);
        t.start();
        loading.setVisible(true);
    }

    public void startGameWithPlayers(List<Player> players) {
        GameFrame frame = new GameFrame(players);
        frame.setVisible(true);
        dispose();
    }
}

/* ======================================================================
 *  AVATAR PREVIEW
 * ==================================================================== */

class AvatarPreview extends JLabel {

    public AvatarPreview() {
        this(false);
    }

    public AvatarPreview(boolean ignoreRingFlag) {
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UIUtilities.enableHighQuality(g2);

        int size = Math.min(getWidth(), getHeight());
        if (size <= 0) {
            g2.dispose();
            return;
        }

        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        Shape circle = new Ellipse2D.Double(x, y, size, size);

        Image img = null;
        Icon icon = getIcon();
        if (icon instanceof ImageIcon) {
            img = ((ImageIcon) icon).getImage();
        }

        g2.setClip(circle);
        if (img != null) {
            g2.drawImage(img, x, y, size, size, this);
        } else {
            g2.setColor(new Color(30, 39, 56));
            g2.fill(circle);
        }
        g2.setClip(null);
        g2.dispose();
    }
}

/* ======================================================================
 *  PLAYER SETUP PANEL
 * ==================================================================== */

class PlayerSetupPanel extends RoundedPanel {
    private final LobbyFrame parent;
    private final JSpinner playerSpinner;
    private final JPanel playersListPanel;
    private final List<PlayerSetupPanel.PlayerSlot> slots = new ArrayList<>();

    public PlayerSetupPanel(LobbyFrame parent) {
        super(UITheme.RADIUS_LARGE);
        this.parent = parent;
        setBackground(UITheme.BG_CARD);
        setLayout(new BorderLayout(16, 16));

        JLabel title = new JLabel("Set Up Players", SwingConstants.LEFT);
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_PRIMARY);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);
        JLabel lbl = new JLabel("Number of Players:");
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        playerSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 4, 1));
        ((JSpinner.NumberEditor) playerSpinner.getEditor()).getTextField().setColumns(2);
        ((JSpinner.NumberEditor) playerSpinner.getEditor()).getTextField().setBackground(new Color(22, 27, 44));
        ((JSpinner.NumberEditor) playerSpinner.getEditor()).getTextField().setForeground(UITheme.TEXT_PRIMARY);
        top.add(lbl);
        top.add(playerSpinner);

        center.add(top, BorderLayout.NORTH);

        playersListPanel = new JPanel();
        playersListPanel.setOpaque(false);
        playersListPanel.setLayout(new GridLayout(4, 1, 6, 6));
        center.add(playersListPanel, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        GameButton startBtn = UITheme.primaryButton("Start Game");
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(startBtn, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        playerSpinner.addChangeListener(e -> rebuildSlots());
        startBtn.addActionListener(e -> createPlayersAndStart());

        rebuildSlots();
    }

    private void rebuildSlots() {
        playersListPanel.removeAll();
        slots.clear();

        int count = (Integer) playerSpinner.getValue();
        for (int i = 0; i < count; i++) {
            PlayerSlot slot = new PlayerSlot(i);
            slots.add(slot);
            playersListPanel.add(slot);
        }

        revalidate();
        repaint();
    }

    private void createPlayersAndStart() {
        List<Player> players = new ArrayList<>();
        for (PlayerSlot slot : slots) {
            String name = slot.getPlayerName();
            int avatarIndex = slot.getSelectedAvatarIndex();

            if (name == null || name.trim().isEmpty()) {
                name = "Player " + (slot.index + 1);
            }
            name = name.trim(); // Normalize name

            Player p = new Player(name, slot.index, avatarIndex);

            if (SnakeLadderGUI.PLAYER_HISTORY.containsKey(name)) {
                SnakeLadderGUI.PlayerStats stats = SnakeLadderGUI.PLAYER_HISTORY.get(name);
                
                p.setWins(stats.wins);
                p.setScore(stats.totalScore); // Restore the score!
                
                System.out.println("Restored " + name + ": " + stats.wins + " wins, " + stats.totalScore + " score.");
            }

            // ==== NEW LOGIC: CHECK HISTORY ====
            // ==================================

            players.add(p);
        }
        parent.startGameWithPlayers(players);
    }

    static class PlayerSlot extends RoundedPanel {
        private final JTextField nameField;
        private final JComboBox<String> avatarCombo;
        private final AvatarPreview avatarPreview;
        private final int index;

        public PlayerSlot(int idx) {
            super(UITheme.RADIUS_LARGE);
            this.index = idx;
            setBackground(new Color(40, 50, 72, 220));
            setLayout(new BorderLayout(8, 8));
            setBorder(new EmptyBorder(8, 8, 8, 8));

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

            JLabel lbl = new JLabel("Player " + (idx + 1));
            lbl.setForeground(UITheme.TEXT_PRIMARY);
            lbl.setFont(UITheme.SUBTITLE_FONT);
            left.add(lbl);

            nameField = new JTextField("Player " + (idx + 1));
            nameField.setBackground(new Color(22, 27, 44));
            nameField.setForeground(UITheme.TEXT_PRIMARY);
            nameField.setCaretColor(UITheme.TEXT_PRIMARY);
            left.add(Box.createVerticalStrut(4));
            left.add(nameField);

            add(left, BorderLayout.CENTER);

            JPanel right = new JPanel(new BorderLayout(4, 4));
            right.setOpaque(false);

            avatarPreview = new AvatarPreview();
            avatarPreview.setPreferredSize(new Dimension(72, 72));
            right.add(avatarPreview, BorderLayout.CENTER);

            avatarCombo = new JComboBox<>();
            for (int i = 0; i < SnakeLadderGUI.AVATAR_PATHS.length; i++) {
                avatarCombo.addItem("Avatar " + (i + 1));
            }
            avatarCombo.setSelectedIndex(idx % SnakeLadderGUI.AVATAR_PATHS.length);
            updateAvatarPreview();
            avatarCombo.addActionListener(e -> updateAvatarPreview());
            right.add(avatarCombo, BorderLayout.SOUTH);

            add(right, BorderLayout.EAST);
        }

        private void updateAvatarPreview() {
            Image avatar = AvatarLoader.loadAvatar(avatarCombo.getSelectedIndex());
            if (avatar != null) {
                Image scaled = avatar.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                avatarPreview.setIcon(new ImageIcon(scaled));
            } else {
                avatarPreview.setIcon(null);
            }
        }

        public String getPlayerName() {
            return nameField.getText();
        }

        public int getSelectedAvatarIndex() {
            return avatarCombo.getSelectedIndex();
        }
    }
}

/* ======================================================================
 *  AVATAR LOADER (OPTIMIZED)
 * ==================================================================== */

class AvatarLoader {
    private static final Map<Integer, Image> baseCache = new HashMap<>();
    private static final Map<String, Image> scaledCache = new HashMap<>();

    public static Image loadAvatar(int index) {
        if (baseCache.containsKey(index)) return baseCache.get(index);
        if (index < 0 || index >= SnakeLadderGUI.AVATAR_PATHS.length) return null;

        String path = SnakeLadderGUI.AVATAR_PATHS[index];
        File f = new File(path);
        if (!f.exists()) {
            System.err.println("Avatar not found: " + path);
            return null;
        }
        Image img = new ImageIcon(path).getImage();
        baseCache.put(index, img);
        return img;
    }

    /**
     * Versi avatar yang sudah di-scale untuk pawn.
     * Akan di-cache per (index,size) supaya tidak di-scale ulang tiap frame.
     */
    public static Image loadAvatarScaled(int index, int size) {
        String key = index + "_" + size;
        if (scaledCache.containsKey(key)) {
            return scaledCache.get(key);
        }

        Image base = loadAvatar(index);
        if (base == null) return null;

        Image scaled = base.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        scaledCache.put(key, scaled);
        return scaled;
    }

    public static void clearScaledCache() {
        scaledCache.clear();
    }
}

/* ======================================================================
 *  GAME FRAME
 * ==================================================================== */

class GameFrame extends JFrame {
    private Board board;
    private final Dice dice = new Dice();
    private final SoundManager soundManager = new SoundManager();

    private final List<Player> players;
    private int currentPlayerIndex = 0;

    private final BoardPanel boardPanel;
    private final GameButton rollButton;
    private final GameButton resetButton;
    private final JLabel infoLabel;          // hanya nama player
    private final AvatarPreview turnAvatar;  // avatar di CURRENT TURN card
    private final JLabel scoreLabel;
    private final JLabel topWinsLabel;
    private final JLabel topScoresLabel;
    private final DicePanel dicePanel;
    private final JTextArea historyArea;
    private final JSlider volumeSlider;

    public GameFrame(List<Player> players) {
        setTitle("Snake & Ladders Game - Prime Ladder 64");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 780); // sedikit lebih kecil biar enteng
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        this.players = players;

        this.board = new Board();
        soundManager.playBackgroundMusic(SnakeLadderGUI.BGM_PATH);

        boardPanel = new BoardPanel(board, players);
        boardPanel.setCurrentPlayerIndex(currentPlayerIndex);

        rollButton = UITheme.primaryButton("Roll Dice");
        resetButton = UITheme.dangerButton("Back to Lobby");
        infoLabel = new JLabel("", SwingConstants.LEFT);
        scoreLabel = new JLabel("", SwingConstants.LEFT);
        topWinsLabel = new JLabel("", SwingConstants.LEFT);
        topScoresLabel = new JLabel("", SwingConstants.LEFT);
        dicePanel = new DicePanel();
        historyArea = new JTextArea();
        volumeSlider = new JSlider(0, 100, 80);
        turnAvatar = new AvatarPreview();   // tanpa ring, hanya PNG
        turnAvatar.setPreferredSize(new Dimension(44, 44));

        styleComponents();
        layoutComponents();
        attachListeners();
        updateScoreLabel();
        updateLeaderboards();
        updateCurrentPlayerHeader();

        log("Game started with players:");
        for (Player p : players) {
            log(" - " + p.getName());
        }
    }

    private void styleComponents() {
        infoLabel.setFont(UITheme.TITLE_FONT.deriveFont(18f));
        infoLabel.setForeground(UITheme.ACCENT_LIGHT);

        scoreLabel.setFont(UITheme.PRIMARY_FONT.deriveFont(12f));
        scoreLabel.setForeground(UITheme.TEXT_SECONDARY);

        topWinsLabel.setFont(UITheme.PRIMARY_FONT.deriveFont(12f));
        topWinsLabel.setForeground(UITheme.TEXT_SECONDARY);

        topScoresLabel.setFont(UITheme.PRIMARY_FONT.deriveFont(12f));
        topScoresLabel.setForeground(UITheme.TEXT_SECONDARY);

        historyArea.setEditable(false);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setFont(UITheme.PRIMARY_FONT.deriveFont(12f));
        historyArea.setBackground(new Color(15, 20, 32));
        historyArea.setForeground(UITheme.TEXT_SECONDARY);
        historyArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        volumeSlider.setOpaque(false);
        volumeSlider.setBackground(UITheme.BG_CARD);
        volumeSlider.setForeground(UITheme.ACCENT);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(12, 12));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        RoundedPanel boardCard = UITheme.createCardPanel(UITheme.RADIUS_LARGE);
        boardCard.setLayout(new BorderLayout(8, 8));
        boardCard.add(boardPanel, BorderLayout.CENTER);
        add(boardCard, BorderLayout.CENTER);

        RoundedPanel sideCard = UITheme.createCardPanel(UITheme.RADIUS_LARGE);
        sideCard.setPreferredSize(new Dimension(340, 0));
        sideCard.setLayout(new BorderLayout(10, 10));

        // === TOP: CURRENT TURN + global wins ===
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        RoundedPanel turnCard = new RoundedPanel(18);
        turnCard.setOpaque(false);
        turnCard.setBackground(new Color(30, 39, 56, 220));
        turnCard.setLayout(new BorderLayout());
        turnCard.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel turnLabel = new JLabel("CURRENT TURN");
        turnLabel.setForeground(UITheme.TEXT_SECONDARY);
        turnLabel.setFont(UITheme.PRIMARY_FONT.deriveFont(Font.BOLD, 11f));

        JPanel turnContent = new JPanel(new BorderLayout(8, 0));
        turnContent.setOpaque(false);
        turnContent.add(turnAvatar, BorderLayout.WEST);
        turnContent.add(infoLabel, BorderLayout.CENTER);

        turnCard.add(turnLabel, BorderLayout.NORTH);
        turnCard.add(turnContent, BorderLayout.CENTER);

        infoPanel.add(turnCard);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(scoreLabel);

        sideCard.add(infoPanel, BorderLayout.NORTH);

        // === CENTER: Game Feed + Leaderboard ===
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JLabel logTitle = new JLabel("Game Feed");
        logTitle.setForeground(UITheme.TEXT_SECONDARY);
        logTitle.setFont(UITheme.PRIMARY_FONT.deriveFont(Font.BOLD, 12f));
        centerPanel.add(logTitle);

        RoundedPanel logCard = new RoundedPanel(18);
        logCard.setOpaque(false);
        logCard.setLayout(new BorderLayout());
        logCard.setBackground(new Color(30, 39, 56, 220));

        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setPreferredSize(new Dimension(320, 220));
        historyScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        historyScroll.setBorder(BorderFactory.createEmptyBorder());

        logCard.add(historyScroll, BorderLayout.CENTER);
        centerPanel.add(logCard);
        centerPanel.add(Box.createVerticalStrut(10));

        JPanel leaderboardRow = new JPanel(new GridLayout(1, 2, 8, 0));
        leaderboardRow.setOpaque(false);

        JPanel winsPanel = new JPanel();
        winsPanel.setOpaque(false);
        winsPanel.setLayout(new BoxLayout(winsPanel, BoxLayout.Y_AXIS));
        JLabel winsTitle = new JLabel("Top Wins");
        winsTitle.setForeground(UITheme.TEXT_SECONDARY);
        winsTitle.setFont(UITheme.PRIMARY_FONT.deriveFont(Font.BOLD, 12f));
        winsPanel.add(winsTitle);
        winsPanel.add(Box.createVerticalStrut(2));
        winsPanel.add(topWinsLabel);

        JPanel scoresPanel = new JPanel();
        scoresPanel.setOpaque(false);
        scoresPanel.setLayout(new BoxLayout(scoresPanel, BoxLayout.Y_AXIS));
        JLabel scoresTitle = new JLabel("Top Scores");
        scoresTitle.setForeground(UITheme.TEXT_SECONDARY);
        scoresTitle.setFont(UITheme.PRIMARY_FONT.deriveFont(Font.BOLD, 12f));
        scoresPanel.add(scoresTitle);
        scoresPanel.add(Box.createVerticalStrut(2));
        scoresPanel.add(topScoresLabel);

        leaderboardRow.add(winsPanel);
        leaderboardRow.add(scoresPanel);

        centerPanel.add(leaderboardRow);

        sideCard.add(centerPanel, BorderLayout.CENTER);

        // === BOTTOM: Dice + buttons + volume ===
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JPanel diceRow = new JPanel(new BorderLayout());
        diceRow.setOpaque(false);
        dicePanel.setPreferredSize(new Dimension(120, 120));
        diceRow.add(dicePanel, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(rollButton);
        btnRow.add(resetButton);
        diceRow.add(btnRow, BorderLayout.SOUTH);

        bottomPanel.add(diceRow);
        bottomPanel.add(Box.createVerticalStrut(8));

        JPanel volRow = new JPanel(new BorderLayout());
        volRow.setOpaque(false);
        JLabel volLabel = new JLabel("BGM Volume");
        volLabel.setForeground(UITheme.TEXT_SECONDARY);
        volLabel.setFont(UITheme.PRIMARY_FONT.deriveFont(11f));
        volRow.add(volLabel, BorderLayout.WEST);
        volRow.add(volumeSlider, BorderLayout.CENTER);

        bottomPanel.add(volRow);

        sideCard.add(bottomPanel, BorderLayout.SOUTH);

        add(sideCard, BorderLayout.EAST);
    }

    private void attachListeners() {
        rollButton.addActionListener(this::onRollDice);

        resetButton.addActionListener(e -> {
            soundManager.stopBackgroundMusic();
            new LobbyFrame().setVisible(true);
            dispose();
        });

        volumeSlider.addChangeListener(e -> {
            float v = volumeSlider.getValue() / 100f;
            soundManager.setBgmVolume(v);
        });
    }

    private void updateCurrentPlayerHeader() {
        if (players.isEmpty()) return;
        Player current = players.get(currentPlayerIndex);
        infoLabel.setText(current.getName());

        Image avatarImg = current.getAvatar();
        if (avatarImg != null) {
            Image scaled = avatarImg.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            turnAvatar.setIcon(new ImageIcon(scaled));
        } else {
            turnAvatar.setIcon(null);
        }

        boardPanel.setCurrentPlayerIndex(currentPlayerIndex);
    }

    /* ==================== GAME LOGIC ==================== */

    private void onRollDice(ActionEvent e) {
        if (players.isEmpty()) return;

        Player current = players.get(currentPlayerIndex);
        rollButton.setEnabled(false);

        // --- PERBAIKAN DI SINI ---
        // Mainkan suara HANYA SEKALI di awal, sebelum animasi visual mulai
        soundManager.playDiceSound(); 
        // -------------------------

        final int[] ticks = {0};
        javax.swing.Timer shakeTimer = new javax.swing.Timer(80, ev -> {
            ticks[0]++;
            
            // Update visual dadu (tanpa suara)
            DiceResult temp = dice.roll();
            dicePanel.setDiceResult(temp);
            
            // HAPUS baris soundManager.playDiceSound() dari sini!

            if (ticks[0] >= 10) {
                ((javax.swing.Timer) ev.getSource()).stop();

                DiceResult result = dice.roll();
                dicePanel.setDiceResult(result);
                applyDiceResult(current, result);
            }
        });
        shakeTimer.start();
    }

    private void applyDiceResult(Player current, DiceResult result) {
        int from = current.getPosition(); // Current position (Start of this turn)
        
        // --- DELETE THE OLD "SPECIAL BACKWARD LOGIC" BLOCK HERE ---
        // We removed the block that checked 'isJustClimbedLadder' 
        // because that logic is now handled inside 'handleAfterMovement'.

        // --- NORMAL MOVEMENT ---
        
        int to;
        if (result.getColor() == DiceColor.GREEN) {
            to = Math.min(Board.SIZE, from + result.getValue());
        } else {
            to = Math.max(1, from - result.getValue());
        }

        List<Integer> path = buildPath(from, to);
        
        log(current.getName() + " rolled " + result.getColor() + " " + result.getValue() +
                ": " + from + " → " + to);

        boardPanel.setHighlightedPath(path, currentPlayerIndex);
        
        // Pass 'from' so handleAfterMovement knows where we started
        animateMovement(current, path, () -> handleAfterMovement(current, result, from));
    }

    private List<Integer> buildPath(int from, int to) {
        List<Integer> path = new ArrayList<>();
        if (from == to) return path;
        int step = (to > from) ? 1 : -1;
        for (int p = from + step; p != to + step; p += step) {
            path.add(p);
        }
        return path;
    }

    private void animateMovement(Player player, List<Integer> path, Runnable after) {
        if (path.isEmpty()) {
            after.run();
            return;
        }

        List<Integer> fullPath = new ArrayList<>();
        fullPath.add(player.getPosition());
        fullPath.addAll(path);
        int finalCell = path.get(path.size() - 1);

        // Call the updated method
        boardPanel.startPathAnimation(player, fullPath, 
            // 1. Action to do on every step (PLAY SOUND)
            () -> soundManager.playMoveSound(), 
            
            // 2. Action to do when destination reached (FINISH)
            () -> {
                player.setPosition(finalCell);
                // (Optional) Play one last sound for landing if you want, 
                // but the step loop usually covers it.
                after.run();
            }
        );
    }

    private void handleAfterMovement(Player current, DiceResult lastRoll, int startPosition) {
        try {
            int currentPos = current.getPosition();
            Cell currentCell = board.getCell(currentPos);
            
            // --- 1. UNLOCK LOGIC ---
            if (currentCell.isPrime()) {
                Ladder nearest = board.findNearestLadderUsingDijkstra(currentPos);
                if (nearest != null && nearest.isLocked()) {
                    nearest.setLocked(false);
                    String msg = current.getName() + " landed on PRIME " + currentPos +
                            " → unlocked ladder " + nearest.getStart() + " → " + nearest.getEnd();
                    log(msg);
                    boardPanel.repaint();
                }
            }

            // --- 2. BACKWARD LOGIC ---
            Ladder ladderEndHere = currentCell.getLadderEnd(); // This will work now!
            
            if (lastRoll.getColor() == DiceColor.RED && ladderEndHere != null) {
                if (current.getLastClimbedLadderEnd() == currentPos) {
                    
                    int top = ladderEndHere.getEnd();
                    int bottom = ladderEndHere.getStart();

                    current.setPosition(bottom); 
                    current.setLastClimbedLadderEnd(-1); 

                    String msg = current.getName() + " moved BACKWARD to ladder top (" + top + 
                                 ") & previously climbed it → Sliding down!";
                    log(msg);

                    boardPanel.startLadderAnimation(current, top, bottom, () -> {
                        current.setPosition(bottom);
                        soundManager.playLadderSound();
                        boardPanel.repaint();
                        awardBonusAndCheckFinish(current);
                    });
                    return;
                }
            }

            // --- 3. CLIMB LOGIC ---
            Ladder ladderStartHere = currentCell.getLadderStart();

            if (ladderStartHere != null && !ladderStartHere.isLocked()) {
                boolean startedOnPrime = board.getCell(startPosition).isPrime();

                if (startedOnPrime) {
                    int start = ladderStartHere.getStart();
                    int end = ladderStartHere.getEnd();

                    current.setPosition(start);
                    current.setLastClimbedLadderEnd(end); 

                    String msg = current.getName() + " found unlocked ladder & started on Prime → CLIMBING!";
                    log(msg);

                    boardPanel.startLadderAnimation(current, start, end, () -> {
                        current.setPosition(end);
                        soundManager.playLadderSound();
                        boardPanel.repaint();
                        awardBonusAndCheckFinish(current);
                    });
                    return;
                } else {
                     log(current.getName() + " is at ladder base, but started at " + 
                        startPosition + " (Not Prime) → Cannot climb.");
                }
            }

            // 4. Normal Processing
            awardBonusAndCheckFinish(current);

        } catch (Exception e) {
            // [FIX 4] EMERGENCY RECOVERY
            // If anything crashes, print error and FORCE ENABLE the button
            System.err.println("CRASH IN MOVEMENT LOGIC: " + e.getMessage());
            e.printStackTrace();
            rollButton.setEnabled(true); 
        }
    }

    private void awardBonusAndCheckFinish(Player current) {
        int pos = current.getPosition();
        Cell c = board.getCell(pos);
        int bonus = c.getBonusPoints();
        current.addScore(bonus);

        String baseText = current.getName() + " stopped at " + pos +
                " (bonus +" + bonus + ", total " + current.getScore() + ")";
        log(baseText);
        boardPanel.repaint();

        if (pos >= Board.SIZE) {
            endGame(current);
        } else {
            boolean extraTurn = (pos % 5 == 0);
            if (extraTurn) {
                String msg = current.getName() + " landed on " + pos +
                        " (multiple of 5) → EXTRA TURN!";
                log(msg);
            } else {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
            rollButton.setEnabled(true);
        }

        updateScoreLabel();
        updateLeaderboards();
        updateCurrentPlayerHeader();
    }

    private void endGame(Player winnerByPosition) {
        rollButton.setEnabled(false);
        soundManager.stopBackgroundMusic();

        winnerByPosition.addWin();
        updateScoreLabel();
        updateLeaderboards();

        log("GAME OVER. Winner: " + winnerByPosition.getName() +
                " (total wins: " + winnerByPosition.getWins() + ")");

        // 1. Prepare Ranking Data
        PriorityQueue<Player> pq = new PriorityQueue<>(
                Comparator.comparingInt(Player::getScore).reversed()
        );
        pq.addAll(players);

        // 2. Build the Message String
        StringBuilder sb = new StringBuilder();
        sb.append("Winner (reach cell 64): ").append(winnerByPosition.getName())
                .append(" (total wins: ").append(winnerByPosition.getWins()).append(")\n\n");
        sb.append("Ranking by score this game:\n");

        // --- MOVED UP: Build the ranking list BEFORE showing the dialog ---
        int rank = 1;
        while (!pq.isEmpty()) {
            Player p = pq.poll();
            sb.append(rank).append(". ").append(p.getName())
                    .append(" - Score: ").append(p.getScore())
                    .append(" (pos ").append(p.getPosition())
                    .append(", wins ").append(p.getWins()).append(")\n");
            rank++;
        }
        // ----------------------------------------------------------------

        // 3. Show the Options Dialog
        Object[] options = {"Play Again", "Back to Menu", "Exit Game"};

        int choice = JOptionPane.showOptionDialog(
                this,
                sb.toString(), // Now contains the full ranking
                "Game Over",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        // 4. Handle Choice
        if (choice == 0) {
            // CASE 1: Play Again (Instant Rematch)
            resetForNextRound();

        } else if (choice == 1) {
            // CASE 2: Back to Menu
            savePlayerDataToGlobalMap(); // Ensure this method exists in GameFrame!

            new LobbyFrame().setVisible(true);
            dispose(); // Close current game window

        } else {
            // CASE 3: Exit (or closed dialog)
            System.exit(0);
        }    
    }

    private void savePlayerDataToGlobalMap() {
        for (Player p : players) {
            // Save BOTH wins and score into the custom object
            SnakeLadderGUI.PlayerStats stats = new SnakeLadderGUI.PlayerStats(p.getWins(), p.getScore());
            
            SnakeLadderGUI.PLAYER_HISTORY.put(p.getName(), stats);
        }
    }

    private void resetForNextRound() {
        log("\n=== NEW GAME STARTED (same players, wins carried over) ===\n");
        board = new Board();
        boardPanel.setBoard(board);
        boardPanel.clearHighlightedPath();
        AvatarLoader.clearScaledCache();
        for (Player p : players) {
            p.setPosition(1);
        }
        currentPlayerIndex = 0;
        rollButton.setEnabled(true);

        soundManager.playBackgroundMusic(SnakeLadderGUI.BGM_PATH);
        updateScoreLabel();
        updateLeaderboards();
        updateCurrentPlayerHeader();
    }

    private void updateScoreLabel() {
        StringBuilder sb = new StringBuilder("Wins: ");
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (i > 0) sb.append(" | ");
            sb.append(p.getName()).append("=").append(p.getWins());
        }
        scoreLabel.setText(sb.toString());
    }

    private void updateLeaderboards() {
        // Top Wins
        List<Player> byWins = new ArrayList<>(players);
        byWins.sort(Comparator.comparingInt(Player::getWins).reversed());

        StringBuilder winsSb = new StringBuilder("<html>");
        for (int i = 0; i < Math.min(3, byWins.size()); i++) {
            Player p = byWins.get(i);
            winsSb.append(i + 1).append(". ").append(p.getName())
                    .append(" - ").append(p.getWins()).append(" wins<br>");
        }
        winsSb.append("</html>");
        topWinsLabel.setText(winsSb.toString());

        // Top Scores (current game)
        List<Player> byScore = new ArrayList<>(players);
        byScore.sort(Comparator.comparingInt(Player::getScore).reversed());

        StringBuilder scoreSb = new StringBuilder("<html>");
        for (int i = 0; i < Math.min(3, byScore.size()); i++) {
            Player p = byScore.get(i);
            scoreSb.append(i + 1).append(". ").append(p.getName())
                    .append(" - ").append(p.getScore()).append(" pts<br>");
        }
        scoreSb.append("</html>");
        topScoresLabel.setText(scoreSb.toString());
    }

    private void log(String msg) {
        historyArea.append("• " + msg + "\n");

        // Batasi panjang log supaya JTextArea tidak terlalu besar (anti lag)
        String text = historyArea.getText();
        int maxChars = 4000;
        if (text.length() > maxChars) {
            historyArea.setText(text.substring(text.length() - maxChars));
        }

        historyArea.setCaretPosition(historyArea.getDocument().getLength());
    }
}

/* ======================================================================
 *  BOARD PANEL (MAP BACKGROUND + COORDINATE TILES) - OPTIMIZED
 * ==================================================================== */

class BoardPanel extends JPanel {

    // ✅ KOORDINAT YANG SUDAH DIPERBAIKI - SEMUA DI DARATAN
    private static final double[][] CELL_POS = new double[Board.SIZE + 1][2];

    static {
        // Format: CELL_POS[index] = {x, y} dalam range 0.0 - 1.0
        // Disesuaikan dengan peta di gambar
        
        // Mulai di Kiri Bawah & Menyusuri Bawah ke Kanan (1-11)
        CELL_POS[1] = new double[]{0.21, 0.71}; 
        CELL_POS[2] = new double[]{0.24, 0.74};
        CELL_POS[3] = new double[]{0.28, 0.77};
        CELL_POS[4] = new double[]{0.25, 0.80};
        CELL_POS[5] = new double[]{0.30, 0.81};
        CELL_POS[6] = new double[]{0.34, 0.80};
        CELL_POS[7] = new double[]{0.57, 0.73};
        CELL_POS[8] = new double[]{0.60, 0.76};
        CELL_POS[9] = new double[]{0.64, 0.76}; // Masuk Pulau Kanan
        CELL_POS[10] = new double[]{0.67, 0.73};
        CELL_POS[11] = new double[]{0.66, 0.68};

        // Menuju ke Tengah & Naik (12-28)
        CELL_POS[12] = new double[]{0.70, 0.68};
        CELL_POS[13] = new double[]{0.73, 0.70};
        CELL_POS[14] = new double[]{0.75, 0.67};
        CELL_POS[15] = new double[]{0.76, 0.63};
        CELL_POS[16] = new double[]{0.78, 0.59};
        CELL_POS[17] = new double[]{0.82, 0.60};
        CELL_POS[18] = new double[]{0.85, 0.62};
        CELL_POS[19] = new double[]{0.87, 0.59}; 
        CELL_POS[20] = new double[]{0.85, 0.56}; // Masuk P. Kanan Bawah
        CELL_POS[21] = new double[]{0.83, 0.53}; // Di P. Kanan Bawah
        CELL_POS[22] = new double[]{0.86, 0.52}; // Putaran P. Kanan Bawah
        CELL_POS[23] = new double[]{0.87, 0.48}; // Keluar P. Kanan Bawah

        // Lanjutan ke Tengah dan ke Es
        CELL_POS[24] = new double[]{0.84, 0.45};
        CELL_POS[25] = new double[]{0.83, 0.40};
        CELL_POS[26] = new double[]{0.76, 0.39};
        CELL_POS[27] = new double[]{0.71, 0.40};
        CELL_POS[28] = new double[]{0.64, 0.39};

        // Menuju Kiri Atas & Melintasi Tengah (29-57)
        CELL_POS[28] = new double[]{0.61, 0.38};
        CELL_POS[29] = new double[]{0.57, 0.38}; // Masuk Pulau Kiri Atas
        CELL_POS[30] = new double[]{0.54, 0.40};
        CELL_POS[31] = new double[]{0.49, 0.41};
        CELL_POS[32] = new double[]{0.49, 0.48};
        CELL_POS[33] = new double[]{0.50, 0.52};
        CELL_POS[34] = new double[]{0.50, 0.56};
        CELL_POS[35] = new double[]{0.51, 0.60};
        CELL_POS[36] = new double[]{0.48, 0.63};
        CELL_POS[37] = new double[]{0.46, 0.57};
        CELL_POS[38] = new double[]{0.43, 0.56};
        CELL_POS[39] = new double[]{0.41, 0.58};
        CELL_POS[40] = new double[]{0.41, 0.58};
        CELL_POS[41] = new double[]{0.39, 0.61};
        CELL_POS[42] = new double[]{0.37, 0.65};
        CELL_POS[43] = new double[]{0.36, 0.61};
        CELL_POS[44] = new double[]{0.33, 0.60};
        CELL_POS[45] = new double[]{0.27, 0.58};
        CELL_POS[46] = new double[]{0.24, 0.57};
        CELL_POS[47] = new double[]{0.19, 0.56};
        CELL_POS[48] = new double[]{0.20, 0.52};
        CELL_POS[49] = new double[]{0.18, 0.49};
        CELL_POS[50] = new double[]{0.13, 0.47};
        CELL_POS[51] = new double[]{0.08, 0.47};
        CELL_POS[52] = new double[]{0.14, 0.42};
        CELL_POS[53] = new double[]{0.25, 0.33};
        CELL_POS[54] = new double[]{0.20, 0.26};
        CELL_POS[55] = new double[]{0.30, 0.25};
        CELL_POS[56] = new double[]{0.41, 0.25};
        CELL_POS[57] = new double[]{0.49, 0.24};

        // Menyusuri Puncak Pulau Kanan Atas (58-64)
        CELL_POS[58] = new double[]{0.54, 0.19};
        CELL_POS[59] = new double[]{0.60, 0.18};
        CELL_POS[60] = new double[]{0.66, 0.20};
        CELL_POS[61] = new double[]{0.74, 0.21};
        CELL_POS[62] = new double[]{0.80, 0.22};
        CELL_POS[63] = new double[]{0.84, 0.18};
        CELL_POS[64] = new double[]{0.86, 0.14}; // Titik Akhir
    }

    private Board board;
    private final List<Player> players;

    private final Color[] playerColors = {
            new Color(231, 76, 60),
            new Color(52, 152, 219),
            new Color(46, 204, 113),
            new Color(155, 89, 182)
    };

    private BoardPanel.LadderAnimation ladderAnimation;
    private BoardPanel.MovementAnimation movementAnimation;

    private int currentPlayerIndex = 0;
    private List<Integer> highlightedPath = new ArrayList<>();
    private int highlightedPlayerIndex = -1;

    private final Image boardTexture;
    private Image scaledBoardTexture;
    private int lastBoardW = -1;
    private int lastBoardH = -1;

    public BoardPanel(Board board, List<Player> players) {
        this.board = board;
        this.players = players;
        setOpaque(false);
        setDoubleBuffered(true);

        Image tex = null;
        File f = new File(SnakeLadderGUI.BOARD_BG_PATH);
        if (f.exists()) {
            tex = new ImageIcon(SnakeLadderGUI.BOARD_BG_PATH).getImage();
        } else {
            System.err.println("Board background not found: " + SnakeLadderGUI.BOARD_BG_PATH);
        }
        this.boardTexture = tex;
        
        // ===== TAMBAHKAN KODE INI =====
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int margin = 16;
                int boardW = getWidth() - margin * 2;
                int boardH = getHeight() - margin * 2;
                
                // Koordinat relatif terhadap board (dikurangi margin)
                int clickX = e.getX() - margin;
                int clickY = e.getY() - margin;
                
                // Hitung koordinat normalisasi (0.0 - 1.0)
                double normalizedX = (double) clickX / boardW;
                double normalizedY = (double) clickY / boardH;
                
                // Format output untuk copy-paste langsung
                System.out.println("===== COORDINATE TRACKER =====");
                System.out.println("Click position: (" + e.getX() + ", " + e.getY() + ")");
                System.out.println("Board size: " + boardW + " x " + boardH);
                System.out.println("Normalized: (" + String.format("%.2f", normalizedX) + ", " + String.format("%.2f", normalizedY) + ")");
                System.out.println("For CELL_POS array:");
                System.out.println("CELL_POS[X] = new double[]{" + String.format("%.2f", normalizedX) + ", " + String.format("%.2f", normalizedY) + "};");
                System.out.println("==============================\n");
            }
        });
    }

    public void setBoard(Board board) {
        this.board = board;
        repaint();
    }

    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index;
        repaint();
    }

    public void setHighlightedPath(List<Integer> path, int playerIndex) {
        this.highlightedPath = new ArrayList<>(path);
        this.highlightedPlayerIndex = playerIndex;
        repaint();
    }

    public void clearHighlightedPath() {
        this.highlightedPath.clear();
        this.highlightedPlayerIndex = -1;
        repaint();
    }

    public void startLadderAnimation(Player player, int startCell, int endCell, Runnable onFinish) {
        ladderAnimation = new LadderAnimation(player, startCell, endCell, onFinish);

        javax.swing.Timer timer = new javax.swing.Timer(60, ev -> {
            ladderAnimation.progress += 0.05;
            if (ladderAnimation.progress >= 1.0) {
                ladderAnimation.progress = 1.0;
                ((javax.swing.Timer) ev.getSource()).stop();
                LadderAnimation finished = ladderAnimation;
                ladderAnimation = null;
                repaint();
                if (finished.onFinish != null) {
                    finished.onFinish.run();
                }
            }
            repaint();
        });
        timer.setRepeats(true);
        timer.start();
    }

    public void startPathAnimation(Player player, List<Integer> fullPath, Runnable onStep, Runnable onFinish) {
        if (fullPath == null || fullPath.size() < 2) {
            if (onFinish != null) onFinish.run();
            return;
        }
        movementAnimation = new MovementAnimation(player, fullPath, onFinish);

        javax.swing.Timer timer = new javax.swing.Timer(35, ev -> {
            
            movementAnimation.segmentProgress += 0.08; 

            // ==== FIX: LATENCY COMPENSATION ====
            // Trigger sound slightly BEFORE (at 85%) the pawn lands.
            // This covers the tiny delay the OS takes to play audio.
            if (movementAnimation.segmentProgress >= 0.85 && !movementAnimation.soundPlayedForThisStep) {
                if (onStep != null) {
                    onStep.run();
                }
                movementAnimation.soundPlayedForThisStep = true; // Mark as played
            }
            // ===================================

            if (movementAnimation.segmentProgress >= 1.0) {
                movementAnimation.segmentProgress = 0.0;
                movementAnimation.segmentIndex++;
                
                // Reset the flag for the NEXT step
                movementAnimation.soundPlayedForThisStep = false; 

                if (movementAnimation.segmentIndex >= movementAnimation.path.size() - 1) {
                    ((javax.swing.Timer) ev.getSource()).stop();
                    MovementAnimation finished = movementAnimation;
                    movementAnimation = null;
                    repaint();
                    if (finished.onFinish != null) {
                        finished.onFinish.run();
                    }
                    return;
                }
            }
            repaint();
        });
        timer.setRepeats(true);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        UIUtilities.enableHighQuality(g2);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(19, 26, 45));
        g2.fillRect(0, 0, w, h);

        int margin = 16;
        int boardW = w - margin * 2;
        int boardH = h - margin * 2;
        int x0 = margin;
        int y0 = margin;

        // Background image
        if (boardTexture != null) {
            if (scaledBoardTexture == null || boardW != lastBoardW || boardH != lastBoardH) {
                scaledBoardTexture = boardTexture.getScaledInstance(boardW, boardH, Image.SCALE_SMOOTH);
                lastBoardW = boardW;
                lastBoardH = boardH;
            }
            g2.drawImage(scaledBoardTexture, x0, y0, this);
        } else {
            g2.setColor(new Color(44, 62, 80));
            g2.fillRoundRect(x0, y0, boardW, boardH, 24, 24);
        }

        g2.translate(x0, y0);

        drawBoard(g2, boardW, boardH);
        drawLadders(g2, boardW, boardH);
        drawPlayers(g2, boardW, boardH);

        g2.dispose();
    }

    private void drawBoard(Graphics2D g2, int boardW, int boardH) {
        for (int i = 1; i <= Board.SIZE; i++) {
            Point center = cellCenter(i, boardW, boardH);
            Cell cell = board.getCell(i);

            int r = 22;
            int x = center.x - r / 2;
            int y = center.y - r / 2;

            if (highlightedPath.contains(i) && highlightedPlayerIndex >= 0) {
                Color c = playerColors[highlightedPlayerIndex % playerColors.length];
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 140));
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(x - 4, y - 4, r + 8, r + 8);
            }

            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(x + 2, y + 4, r, r);

            Color fill = cell.isPrime()
                    ? new Color(46, 204, 113, 240)
                    : new Color(255, 255, 255, 230);

            g2.setColor(fill);
            g2.fillOval(x, y, r, r);

            g2.setColor(new Color(52, 73, 94, 220));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x, y, r, r);

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
            String text = String.valueOf(i);
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(new Color(44, 62, 80));
            g2.drawString(text,
                    center.x - fm.stringWidth(text) / 2,
                    center.y + fm.getAscent() / 3);

            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 9f));
            String bonus = "+" + cell.getBonusPoints();
            FontMetrics fm2 = g2.getFontMetrics();
            g2.setColor(new Color(127, 140, 141));
            g2.drawString(bonus,
                    center.x - fm2.stringWidth(bonus) / 2,
                    y + r + fm2.getAscent());
        }
    }

    private void drawLadders(Graphics2D g2, int boardW, int boardH) {
        for (Ladder ladder : board.getLadders()) {
            if (!ladder.isVisible()) continue;

            Point startCenter = cellCenter(ladder.getStart(), boardW, boardH);
            Point endCenter = cellCenter(ladder.getEnd(), boardW, boardH);

            int x1 = startCenter.x;
            int y1 = startCenter.y;
            int x2 = endCenter.x;
            int y2 = endCenter.y;

            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(ladder.isLocked()
                    ? new Color(189, 195, 199)
                    : new Color(211, 84, 0));
            g2.drawLine(x1, y1, x2, y2);

            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(0, 0, 0, 40));
            g2.drawLine(x1 + 2, y1 + 2, x2 + 2, y2 + 2);

            if (ladder.isLocked()) {
                int lockSize = 16;
                int lx = x1 - lockSize / 2;
                int ly = y1 - lockSize / 2;

                g2.setColor(new Color(44, 62, 80, 220));
                g2.fillRoundRect(lx, ly, lockSize, lockSize, 6, 6);
                g2.setColor(new Color(236, 240, 241));
                g2.drawRoundRect(lx, ly, lockSize, lockSize, 6, 6);
            }
        }
    }

    private void drawPlayers(Graphics2D g2, int boardW, int boardH) {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);

            Point base;

            if (ladderAnimation != null && ladderAnimation.player == p) {
                Point startCenter = cellCenter(ladderAnimation.startCell, boardW, boardH);
                Point endCenter = cellCenter(ladderAnimation.endCell, boardW, boardH);
                double t = ladderAnimation.progress;
                int x = (int) (startCenter.x + (endCenter.x - startCenter.x) * t);
                int y = (int) (startCenter.y + (endCenter.y - startCenter.y) * t);
                base = new Point(x, y);
            } else if (movementAnimation != null && movementAnimation.player == p) {
                List<Integer> path = movementAnimation.path;
                int segIdx = movementAnimation.segmentIndex;
                if (segIdx >= path.size() - 1) {
                    base = cellCenter(p.getPosition(), boardW, boardH);
                } else {
                    Point fromCenter = cellCenter(path.get(segIdx), boardW, boardH);
                    Point toCenter = cellCenter(path.get(segIdx + 1), boardW, boardH);
                    double t = movementAnimation.segmentProgress;
                    int x = (int) (fromCenter.x + (toCenter.x - fromCenter.x) * t);
                    int y = (int) (fromCenter.y + (toCenter.y - fromCenter.y) * t);
                    base = new Point(x, y);
                }
            } else {
                base = cellCenter(p.getPosition(), boardW, boardH);
            }

            int r = (int) (boardW / 16.0);
            int cx = base.x;
            int cy = base.y;

            int offX = (i % 2 == 0) ? -r / 4 : r / 4;
            int offY = (i / 2 == 0) ? -r / 4 : r / 4;
            cx += offX;
            cy += offY;

            if (i == currentPlayerIndex) {
                g2.setColor(new Color(255, 215, 0, 160));
                g2.setStroke(new BasicStroke(4f));
                g2.drawOval(cx - r / 2 - 4, cy - r / 2 - 4, r + 8, r + 8);
            }

            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(cx - r / 2, cy - r / 2 + 3, r, r);

            Image pawnImg = AvatarLoader.loadAvatarScaled(p.getAvatarIndex(), r);
            Shape pawnCircle = new Ellipse2D.Double(cx - r / 2.0, cy - r / 2.0, r, r);

            if (pawnImg != null) {
                g2.setClip(pawnCircle);
                g2.drawImage(pawnImg, cx - r / 2, cy - r / 2, r, r, this);
                g2.setClip(null);

                g2.setColor(new Color(255, 255, 255, 200));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(pawnCircle);
            } else {
                Color pc = playerColors[i % playerColors.length];
                g2.setColor(pc);
                g2.fill(pawnCircle);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(pawnCircle);
            }
        }
    }

    private Point cellCenter(int index, int boardW, int boardH) {
        if (index < 1) index = 1;
        if (index > Board.SIZE) index = Board.SIZE;
        double[] pos = CELL_POS[index];
        int x = (int) (boardW * pos[0]);
        int y = (int) (boardH * pos[1]);
        return new Point(x, y);
    }

    static class LadderAnimation {
        Player player;
        int startCell;
        int endCell;
        double progress;
        Runnable onFinish;

        LadderAnimation(Player player, int startCell, int endCell, Runnable onFinish) {
            this.player = player;
            this.startCell = startCell;
            this.endCell = endCell;
            this.onFinish = onFinish;
            this.progress = 0.0;
        }
    }

    static class MovementAnimation {
        Player player;
        List<Integer> path;
        int segmentIndex;
        double segmentProgress;
        Runnable onFinish;
        
        // NEW: Flag to prevent double sounds
        boolean soundPlayedForThisStep = false; 

        MovementAnimation(Player player, List<Integer> path, Runnable onFinish) {
            this.player = player;
            this.path = path;
            this.onFinish = onFinish;
            this.segmentIndex = 0;
            this.segmentProgress = 0.0;
        }
    }
}

/* ======================================================================
 *  DICE PANEL
 * ==================================================================== */

class DicePanel extends JPanel {
    private int value = 0;
    private DiceColor color = DiceColor.GREEN;

    public DicePanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(120, 120));
        setDoubleBuffered(true);
    }

    public void setDiceResult(DiceResult result) {
        this.value = result.getValue();
        this.color = result.getColor();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (value == 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        UIUtilities.enableHighQuality(g2);

        int size = Math.min(getWidth(), getHeight()) - 30;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(x + 4, y + 8, size, size, 28, 28);

        Color border = (color == DiceColor.GREEN)
                ? new Color(46, 204, 113)
                : new Color(231, 76, 60);

        GradientPaint bodyGp = new GradientPaint(
                x, y, new Color(252, 252, 252),
                x, y + size, new Color(220, 221, 225)
        );
        g2.setPaint(bodyGp);
        g2.fillRoundRect(x, y, size, size, 26, 26);

        g2.setStroke(new BasicStroke(3f));
        g2.setColor(border.darker());
        g2.drawRoundRect(x, y, size, size, 26, 26);

        g2.setColor(new Color(border.getRed(), border.getGreen(), border.getBlue(), 90));
        g2.drawRoundRect(x + 4, y + 4, size - 8, size - 8, 22, 22);

        g2.setColor(border.darker());
        int dot = size / 7;
        int cx = x + size / 2;
        int cy = y + size / 2;

        int left = x + size / 4;
        int right = x + size - size / 4;
        int top = y + size / 4;
        int bottom = y + size - size / 4;

        switch (value) {
            case 1 -> drawDot(g2, cx, cy, dot);
            case 2 -> { drawDot(g2, left, top, dot); drawDot(g2, right, bottom, dot); }
            case 3 -> {
                drawDot(g2, left, top, dot);
                drawDot(g2, cx, cy, dot);
                drawDot(g2, right, bottom, dot);
            }
            case 4 -> {
                drawDot(g2, left, top, dot);
                drawDot(g2, right, top, dot);
                drawDot(g2, left, bottom, dot);
                drawDot(g2, right, bottom, dot);
            }
            case 5 -> {
                drawDot(g2, left, top, dot);
                drawDot(g2, right, top, dot);
                drawDot(g2, cx, cy, dot);
                drawDot(g2, left, bottom, dot);
                drawDot(g2, right, bottom, dot);
            }
            case 6 -> {
                drawDot(g2, left, top, dot);
                drawDot(g2, right, top, dot);
                drawDot(g2, left, cy, dot);
                drawDot(g2, right, cy, dot);
                drawDot(g2, left, bottom, dot);
                drawDot(g2, right, bottom, dot);
            }
        }

        g2.dispose();
    }

    private void drawDot(Graphics2D g2, int cx, int cy, int size) {
        GradientPaint gp = new GradientPaint(
                cx, cy - size / 2f, g2.getColor().brighter(),
                cx, cy + size / 2f, g2.getColor().darker()
        );
        Paint old = g2.getPaint();
        g2.setPaint(gp);
        g2.fillOval(cx - size / 2, cy - size / 2, size, size);
        g2.setPaint(old);
    }
}

