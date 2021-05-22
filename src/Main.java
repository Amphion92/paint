
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
    static JSlider slider = new JSlider(0, 30, 5);
    static JSlider rSlider = new JSlider(0, 255, 0);
    static JSlider gSlider = new JSlider(0, 255, 0);
    static JSlider bSlider = new JSlider(0, 255, 0);
    static JPanel colorIndicator = new JPanel();
    static JFrame frame = new JFrame();


    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(screenSize.width / 4, screenSize.height / 4, screenSize.width / 2, screenSize.height / 2);
        JPanel box = new JPanel();
        ImageEditor imageEditor = new ImageEditor(box);
        slider.addChangeListener(e -> {
            imageEditor.setWidth(slider.getValue());
        });
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(box, BorderLayout.CENTER);
        JPanel properties = new JPanel();
        properties.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        frame.getContentPane().add(properties, BorderLayout.EAST);
        properties.setLayout(new GridLayout(8, 1, 5, 5));
        properties.add(new JLabel("Размер:", SwingConstants.CENTER));
        properties.add(slider);
        JButton saveButt = new JButton("Сохранить");
        saveButt.addActionListener(e -> {
            try {
                FileDialog fileDialog = new FileDialog(frame);
                fileDialog.setMode(FileDialog.SAVE);
                fileDialog.setFile("*.jpg");
                fileDialog.setFilenameFilter((File dir, String name) -> name.endsWith(".jpg"));
                fileDialog.setVisible(true);
                String name = fileDialog.getFile();
                String dir = fileDialog.getDirectory();
                System.out.println("name " + name);
                System.out.println("dir " + dir);
                imageEditor.save(new File(dir, name).getPath());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (NullPointerException exception) {
                System.out.println("Нету файла.");
            }
        });
        properties.add(saveButt);
        JButton clearButt = new JButton("Очистить");
        ActionListener clearAct = e -> {
            imageEditor.clear();
            colorIndicator.setBackground(Color.BLACK);
            rSlider.setValue(0);
            gSlider.setValue(0);
            bSlider.setValue(0);
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
                Color color = new Color(
                        rSlider.getValue(),
                        gSlider.getValue(),
                        bSlider.getValue()
                );
                imageEditor.setColor(color);
                colorIndicator.setBackground(color);
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
                imageEditor.loadImage(new File(dir, name).getPath());
            } catch (NullPointerException ex) {
                clearAct.actionPerformed(e);
            }
        });
        properties.add(open);
        properties.setPreferredSize(new Dimension(frame.getWidth() / 4, frame.getHeight()));
        frame.setVisible(true);
    }
}


class ImageEditor {
    private List<List<Point>> drawnLines = new ArrayList<>();
    private List<Color> colors = new ArrayList<>();
    private List<Integer> widths = new ArrayList<>();
    private Set<Point> pointSet = new HashSet<>();
    private Color currColor = Color.BLACK;
    private boolean isPressed = false;
    private int width = 5;
    private double scale = 1.0;
    private ImageIcon sourceIcon = null;

    public void setColor(Color color) {
        currColor = color;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    private void setScale(double scale) {
        this.scale = scale;
        if (sourceIcon != null)
            pane.setIcon(new ImageIcon(sourceIcon.getImage().getScaledInstance((int) (sourceIcon.getIconWidth() * scale),
                    (int) (sourceIcon.getIconHeight() * scale)
                    , Image.SCALE_FAST)));
        pane.repaint();
        pane.setSize(pane.getParent().getSize());
    }

    private JLabel pane = new JLabel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            for (int j = 0; j < drawnLines.size(); j++) {
                List<Point> line = drawnLines.get(j);
                int w = (int)(widths.get(j) * scale);
                Color color = colors.get(j);
                ((Graphics2D) g).setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
                ((Graphics2D) g).setColor(color);
                for (int i = 0, pointsSize = line.size(); i < pointsSize - 1; i++) {
                    Point p = line.get(i);
                    Point nextP = line.get(i + 1);
                    g.drawLine((int) (scale * p.x), (int) (scale * p.y), (int) (scale * nextP.x), (int) (scale * nextP.y));
                }
            }
        }
    };


    public ImageEditor(Container parent) {
        pane.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!pointSet.contains(e.getPoint())) {
                    drawnLines.get(drawnLines.size() - 1).add(new Point((int) (e.getPoint().getX()/scale), (int) (e.getPoint().getY()/scale)));
                    pointSet.add(e.getPoint());
                    pane.repaint();
                }
                super.mouseDragged(e);
            }
        });
        pane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                drawnLines.add(new ArrayList<>());
                colors.add(currColor);
                widths.add(width);
                super.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                super.mouseReleased(e);
            }
        });
        pane.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0)
                    setScale(scale * 1.1);
                else setScale(scale * 0.8);
            }
        });
        parent.removeAll();
        parent.setLayout(new BorderLayout());
        parent.add(pane, BorderLayout.CENTER);
    }

    public void clear() {
        scale = 1.0;
        drawnLines = new ArrayList<>();
        pointSet = new HashSet<>();
        widths = new ArrayList<>();
        colors = new ArrayList<>();
        pane.repaint();
        pane.setIcon(null);
        sourceIcon = null;
    }

    public void loadImage(String path) {
        pane.setVerticalAlignment(SwingConstants.TOP);
        pane.setHorizontalAlignment(SwingConstants.LEFT);
        sourceIcon = new ImageIcon(path);
        setScale(scale);

    }

    public void save(String path) throws IOException {
        BufferedImage img = new BufferedImage(pane.getWidth(), pane.getHeight(), BufferedImage.TYPE_INT_RGB);
        pane.printAll(img.getGraphics());
        ImageIO.write(img, "JPEG", new File(path));
    }
}