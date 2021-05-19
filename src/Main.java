
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;


public class Main {
    static List<List<Point>> drawnLines = new ArrayList<>();
    volatile static List<Color> colors = new ArrayList<>();
    volatile static List<Integer> widths = new ArrayList<>();
    static Set<Point> pointSet = new HashSet<>();
    static boolean isPressed = false;
    static JSlider slider = new JSlider(0, 30, 5);
    static JSlider rSlider = new JSlider(0, 255, 0);
    static JSlider gSlider = new JSlider(0, 255, 0);
    static JSlider bSlider = new JSlider(0, 255, 0);
    static JPanel colorIndicator = new JPanel();
    static JFrame frame = new JFrame();
    static Color currColor = Color.BLACK;

    public static void save(JLabel panel) throws IOException {
        BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        FileDialog fileDialog = new FileDialog(frame);
        fileDialog.setMode(FileDialog.SAVE);
        fileDialog.setFile("*.jpg");
        fileDialog.setFilenameFilter((File dir, String name) -> name.endsWith(".jpg"));
        fileDialog.setVisible(true);
        String name = fileDialog.getFile();
        String dir = fileDialog.getDirectory();
        System.out.println("name " + name);
        System.out.println("dir " + dir);
        panel.printAll(img.getGraphics());
        ImageIO.write(img, "JPEG", new File(dir, name));
    }

    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(screenSize.width / 4, screenSize.height / 4, screenSize.width / 2, screenSize.height / 2);
        JLabel pane = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                for (int j = 0; j < drawnLines.size(); j++) {
                    List<Point> line = drawnLines.get(j);
                    int w = widths.get(j);
                    Color color = colors.get(j);
                    ((Graphics2D) g).setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
                    ((Graphics2D) g).setColor(color);
                    for (int i = 0, pointsSize = line.size(); i < pointsSize - 1; i++) {
                        Point p = line.get(i);
                        Point nextP = line.get(i + 1);
                        g.drawLine(p.x, p.y, nextP.x, nextP.y);
                    }
                }
            }
        };

        pane.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!pointSet.contains(e.getPoint())) {
                    drawnLines.get(drawnLines.size() - 1).add(e.getPoint());
                    pointSet.add(e.getPoint());
                    pane.repaint();
                }
                super.mouseDragged(e);
            }
        });
        pane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Main.isPressed = true;
                Main.drawnLines.add(new ArrayList<>());
                Main.colors.add(currColor);
                Main.widths.add(slider.getValue());
                super.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Main.isPressed = false;
                super.mouseReleased(e);
            }
        });
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(pane, BorderLayout.CENTER);
        JPanel properties = new JPanel();
        properties.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        frame.getContentPane().add(properties, BorderLayout.EAST);
        properties.setLayout(new GridLayout(8, 1, 5, 5));
        properties.add(new JLabel("Размер:", SwingConstants.CENTER));
        properties.add(slider);
        JButton saveButt = new JButton("Сохранить");
        saveButt.addActionListener(e -> {
            try {
                save(pane);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        properties.add(saveButt);
        JButton clearButt = new JButton("Очистить");
        ActionListener clearAct = e -> {
            drawnLines = new ArrayList<>();
            pointSet = new HashSet<>();
            widths = new ArrayList<>();
            colors = new ArrayList<>();
            pane.repaint();
            colorIndicator.setBackground(Color.BLACK);
            rSlider.setValue(0);
            gSlider.setValue(0);
            bSlider.setValue(0);
            pane.setIcon(null);
        };
        clearButt.addActionListener(clearAct);
        properties.add(clearButt);
        JPanel colorPnl = new JPanel();
        colorPnl.setLayout(new GridLayout(3, 2));
        colorPnl.add(new JLabel("R:", SwingConstants.CENTER));
        colorPnl.add(rSlider);
        colorPnl.add(new JLabel("G:", SwingConstants.CENTER));
        colorPnl.add(gSlider);
        colorPnl.add(new JLabel("B:", SwingConstants.CENTER));
        colorPnl.add(bSlider);
        ChangeListener act = e -> {
            try {
                currColor = new Color(
                        rSlider.getValue(),
                        gSlider.getValue(),
                        bSlider.getValue()
                );
                colorIndicator.setBackground(currColor);
                colorIndicator.repaint();
            } catch (IllegalArgumentException ex) {
                rSlider.setValue(0);
                gSlider.setValue(0);
                bSlider.setValue(0);
            }
        };
        rSlider.addChangeListener(act);
        gSlider.addChangeListener(act);
        bSlider.addChangeListener(act);
        colorIndicator.setBackground(Color.BLACK);
        colorIndicator.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        properties.add(colorPnl);
        properties.add(colorIndicator);
        JButton open = new JButton("Открыть");
        open.addActionListener(e -> {
            try {
                clearAct.actionPerformed(e);
                FileDialog fileDialog = new FileDialog(frame);
                fileDialog.setVisible(true);
                String name = fileDialog.getFile();
                String dir = fileDialog.getDirectory();
                System.out.println("name " + name);
                System.out.println("dir " + dir);
                pane.setVerticalAlignment(SwingConstants.TOP);
                pane.setHorizontalAlignment(SwingConstants.LEFT);
                pane.setIcon(new ImageIcon(new File(dir, name).getPath()));
            } catch (NullPointerException ex) {
                clearAct.actionPerformed(e);
            }
        });
        properties.add(open);
        properties.setPreferredSize(new Dimension(frame.getWidth() / 4, frame.getHeight()));
        frame.setVisible(true);
    }
}
