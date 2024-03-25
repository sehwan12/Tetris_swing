package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


// MVC에서 View와 Controller의 상호작용
import controller.BoardController;

public class BoardView extends JFrame {
    private BoardController controller; // BoardController 참조 추가

    // board array size
    private static final int WIDTH = 10;
    private static final int HEIGHT = 20;

    public static final char BORDER_CHAR = 'X';

    private JTextPane pane;
    private SimpleAttributeSet styleSet;


    private JPanel glassPane; //게임 정지화면을 나타낼 glassPane

    public BoardView() {
        super("SeoulTech SE Tetris");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setBackground(Color.BLACK);
        setSize(400, 600);

        // GlassPane 초기화 by chatGPT
        glassPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();

                // Set the transparency (alpha) value
                float alpha = 0.5f;
                g2d.setColor(new Color(0, 0, 0, (int) (255 * alpha))); // Black color with transparency
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setFont(new Font("SansSerif", Font.BOLD, 40));

                String[] menuItems = {"일시정지", "메인메뉴", " 재시작 ", "게임종료"};
                Color[] menuColors = {new Color(200,200,200), Color.WHITE, Color.WHITE, Color.WHITE};

                for (int i = 0; i < menuItems.length; i++) {
                    // "일시정지" 메뉴 이후의 메뉴들에 대해 폰트 크기를 작게 설정
                    if (i > 0) g2d.setFont(new Font("SansSerif", Font.BOLD, 30));
                    FontMetrics fm = g2d.getFontMetrics();
                    int menuHeight = fm.getHeight();
                    int startY = (getHeight() - menuHeight * 4) / 4; //화면 상단에서 1/4부분부터 시작
                    String text = menuItems[i];
                    int textWidth = fm.stringWidth(text);
                    int x = (getWidth() - textWidth) / 2; //화면 가운데로
                    int interval_space = 20; //서로 20만큼 간격 넣기 추후 화면크기 조정할 때 수정 필요
                    int y = startY + i * (menuHeight+interval_space);

                    if (i == controller.getSelectedOption()) g2d.setColor(Color.YELLOW);
                    else g2d.setColor(menuColors[i]);
                    g2d.fillRect(x, y, textWidth, menuHeight);

                    g2d.setColor(Color.BLACK);
                    g2d.drawString(text, x, y + fm.getAscent());
                }
                g2d.dispose();
            }
        };
        glassPane.setOpaque(false);
        // JRootPane의 GlassPane 설정 by chatGPT
        JRootPane rootPane = this.getRootPane();
        rootPane.setGlassPane(glassPane);
        glassPane.setVisible(false); // 초기에는 보이지 않도록 설정

        //Board display setting.
        pane = new JTextPane();
        pane.setEditable(false);
        pane.setBackground(Color.BLACK);
        CompoundBorder border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 10),
                BorderFactory.createLineBorder(Color.DARK_GRAY, 5));
        pane.setBorder(border);
        this.getContentPane().add(pane, BorderLayout.CENTER);


        //Document default style.
        styleSet = new SimpleAttributeSet();
        StyleConstants.setFontSize(styleSet, 18);
        StyleConstants.setFontFamily(styleSet, "Courier New");
        StyleConstants.setBold(styleSet, true);
        StyleConstants.setForeground(styleSet, Color.WHITE);
        StyleConstants.setAlignment(styleSet, StyleConstants.ALIGN_CENTER);

    }

    public void addKeyListenerToFrame(KeyListener listener) {
        addKeyListener(listener);
        setFocusable(true);
        requestFocusInWindow();
    }



    public void setController(BoardController controller) {
        this.controller = controller;
    }

    public void showPauseScreen(boolean show) {
        glassPane.setVisible(show);
    }

    public int showGameOverDialog() {
        return JOptionPane.showConfirmDialog(this, "Game Over. 시작 메뉴로 돌아가시겠습니까?\n (No : 게임 종료)", "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    public int showGameExitDialog() {
        return JOptionPane.showConfirmDialog(this, "게임을 종료하시겠습니까?",
                "Game Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    public void drawBoard(int[][] board, Color[] board_color) {
        StringBuffer sb = new StringBuffer();
        for(int t=0; t<WIDTH+2; t++) sb.append(BORDER_CHAR);
        sb.append("\n");
        for(int i=0; i < board.length; i++) {
            sb.append(BORDER_CHAR);
            for(int j=0; j < board[i].length; j++) {
                if(board[i][j] == 1) {
                    sb.append("O");
                } else {
                    sb.append(" ");
                }
            }
            sb.append(BORDER_CHAR);
            sb.append("\n");
        }
        for(int t=0; t<WIDTH+2; t++) sb.append(BORDER_CHAR);
        pane.setText(sb.toString());
        StyledDocument doc = pane.getStyledDocument();

        doc.setParagraphAttributes(0, doc.getLength(), styleSet, false);

        //board_color에 저장된 값을 바탕으로 글자 색 칠하기
        for(int i=0; i<board_color.length; i++) {
            if (board_color[i] != null) {
                SimpleAttributeSet styles = new SimpleAttributeSet();
                StyleConstants.setForeground(styles, board_color[i]);
                doc.setCharacterAttributes(i, 1, styles, false);
            }
        }
        pane.setStyledDocument(doc);
    }

    public void glassRepaint() {
        glassPane.repaint();
    }


}