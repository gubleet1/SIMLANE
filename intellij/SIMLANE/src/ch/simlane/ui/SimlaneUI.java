package ch.simlane.ui;

import ch.simlane.editor.Editor;
import ch.simlane.editor.event.StateChangeEvent;
import ch.simlane.editor.event.StateChangeListener;
import ch.simlane.editor.tools.SimulationControls;
import ch.simlane.ui.components.EditorLayersPanel;
import ch.simlane.ui.components.EditorToolsPanel;
import ch.simlane.utils.JSONScenarios;
import ch.simlane.utils.SimlaneUIListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SimlaneUI implements StateChangeListener {

    // the values below always assume that the tile has a size of 1.0 x 1.0
    // this defines the distance of all lanes from the border of the tile
    public static final double LANE_SPACING = 0.235;
    public static final double CAR_RADIUS = 0.1;
    public static final float LANE_THICKNESS = 3;

    public static final String MENU_ACTION_COMMAND_FILE = "FILE";
    public static final String MENU_ACTION_COMMAND_SCENARIOS = "SCENARIOS";

    public static final String MENU_ITEM_ACTION_COMMAND_LOAD_MAP = "LOAD_MAP";
    public static final String MENU_ITEM_ACTION_COMMAND_SAVE_MAP = "SAVE_MAP";
    public static final String MENU_ITEM_ACTION_COMMAND_EXIT = "EXIT";

    public static final Dimension PREFERRED_MAP_SIZE = new Dimension(650, 650);
    public static final Dimension MINIMUM_MAP_SIZE = new Dimension(300, 300);

    private static final int PREFERRED_TOOLS_WIDTH = 350;
    private static final int MINIMUM_TOOLS_WIDTH = 350;

    private static final Dimension MINIMUM_FRAME_SIZE = new Dimension(750, 500);

    private static final String ICON_32 = "/simlane/icon/icon32.png";
    private static final String ICON_64 = "/simlane/icon/icon64.png";
    private static final String ICON_128 = "/simlane/icon/icon128.png";

    private static final String FRAME_TITLE = "SIMLANE";
    private static final String DESIGN_AREA_TITLE = "Design Area";
    private static final String TOOLS_AREA_TITLE = "Tools";

    private static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    private static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);

    private Editor editor;

    private JFrame frame;
    private JPanel contentPane;
    private JSplitPane splitPane;
    private EditorLayersPanel editorLayersPanel;
    private EditorToolsPanel editorToolsPanel;

    private JMenu fileMenu;
    private List<JMenuItem> fileMenuItems;

    private JMenu scenarioMenu;
    private List<JMenuItem> scenarioMenuItems;

    public SimlaneUI(Editor editor) {
        this.editor = editor;
        initialize();
    }

    private void initialize() {
        editor.addStateChangeListener(this);
        editor.getTools().getSimulationControls().addStateChangeListener(this);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(this::createFrame);
    }

    private void createFrame() {
        frame = new JFrame();
        frame.setIconImages(getIconImages());
        frame.setTitle(FRAME_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setMinimumSize(MINIMUM_FRAME_SIZE);
        createContentPane();
        createMenuBar();
        frame.setContentPane(contentPane);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        createFileMenu();
        createScenarioMenu();
        menuBar.add(fileMenu);
        menuBar.add(scenarioMenu);
        frame.setJMenuBar(menuBar);
    }

    private void createFileMenu() {
        fileMenu = new JMenu("File");
        fileMenu.setActionCommand(MENU_ACTION_COMMAND_FILE);
        fileMenu.setMnemonic(KeyEvent.VK_F);
        createFileMenuItems();
        fileMenuItems.forEach(item -> fileMenu.add(item));
        fileMenu.insertSeparator(2);
    }

    private void createFileMenuItems() {
        JMenuItem loadMap = new JMenuItem("Load Map");
        loadMap.setActionCommand(MENU_ITEM_ACTION_COMMAND_LOAD_MAP);
        JMenuItem saveMap = new JMenuItem("Save Map");
        saveMap.setActionCommand(MENU_ITEM_ACTION_COMMAND_SAVE_MAP);
        JMenuItem exit = new JMenuItem("Exit");
        exit.setActionCommand(MENU_ITEM_ACTION_COMMAND_EXIT);
        fileMenuItems = new LinkedList<>(Arrays.asList(loadMap, saveMap, exit));
    }

    private void createScenarioMenu() {
        scenarioMenu = new JMenu("Scenario");
        scenarioMenu.setActionCommand(MENU_ACTION_COMMAND_SCENARIOS);
        scenarioMenu.setMnemonic(KeyEvent.VK_S);
        createScenarioMenuItems();
        scenarioMenuItems.forEach(item -> scenarioMenu.add(item));
    }

    private void createScenarioMenuItems() {
        JSONScenarios jsonScenarios = editor.getJSONScenarios();
        String[] scenarios = jsonScenarios.getAvailableScenarios();
        scenarioMenuItems = new LinkedList<>();
        for (String scenario : scenarios) {
            String name = jsonScenarios.getScenarioDisplayName(scenario);
            JMenuItem menuItem = new JMenuItem(name);
            menuItem.setActionCommand(scenario);
            scenarioMenuItems.add(menuItem);
        }
    }

    private void createContentPane() {
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        createEditorLayersPanel();
        createEditorToolsPanel();
        createSplitPane();
        contentPane.add(splitPane, BorderLayout.CENTER);
    }

    private void createEditorLayersPanel() {
        editorLayersPanel = new EditorLayersPanel(editor);
        editorLayersPanel.setBorder(createEditorPanelBorder(DESIGN_AREA_TITLE));
    }

    private void createEditorToolsPanel() {
        editorToolsPanel = new EditorToolsPanel(editor.getTools());
        editorToolsPanel.setBorder(createEditorPanelBorder(TOOLS_AREA_TITLE));
    }

    private void createSplitPane() {
        JScrollPane scrollPane = new JScrollPane(editorToolsPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setMinimumSize(new Dimension(MINIMUM_TOOLS_WIDTH, 0));
        scrollPane.setPreferredSize(new Dimension(PREFERRED_TOOLS_WIDTH, PREFERRED_MAP_SIZE.height));
        scrollPane.setBorder(null);
        scrollPane.setLayout(new ScrollPaneLayout() {
            private boolean scrollBar;

            @Override
            public void layoutContainer(Container parent) {
                int width = scrollPane.getWidth();
                int height = scrollPane.getHeight();
                int scrollBarWidth = UIManager.getInt("ScrollBar.width");
                editorToolsPanel.adjustSize(width - (scrollBar ? scrollBarWidth : 0));
                if (scrollBar) {
                    if (height >= editorToolsPanel.getPreferredSize().height + scrollBarWidth) {
                        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                        scrollBar = false;
                        editorToolsPanel.adjustSize(width);
                    }
                } else {
                    if (height < editorToolsPanel.getPreferredSize().height) {
                        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                        scrollBar = true;
                        editorToolsPanel.adjustSize(width - scrollBarWidth);
                    }
                }
                super.layoutContainer(parent);
            }
        });
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorLayersPanel, scrollPane);
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(1);
        splitPane.setBorder(null);
    }

    private Border createEditorPanelBorder(String text) {
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(etchedBorder, text);
        titledBorder.setTitleFont(new Font("Arial", Font.PLAIN, 15));
        Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        return BorderFactory.createCompoundBorder(emptyBorder, titledBorder);
    }

    private List<Image> getIconImages() {
        List<Image> icons = new LinkedList<>();
        icons.add(new ImageIcon(getClass().getResource(ICON_32)).getImage());
        icons.add(new ImageIcon(getClass().getResource(ICON_64)).getImage());
        icons.add(new ImageIcon(getClass().getResource(ICON_128)).getImage());
        return icons;
    }

    public JFrame getFrame() {
        return frame;
    }

    private void updateMenuBar() {
        int state = editor.getTools().getSimulationControls().getState();
        boolean enabled = state == SimulationControls.STATE_DISABLED;
        // update scenario menu
        scenarioMenu.setEnabled(enabled);
        // update file menu
        fileMenuItems.forEach(item -> {
            if (item.getActionCommand().equals(MENU_ITEM_ACTION_COMMAND_EXIT)) {
                return;
            }
            item.setEnabled(enabled);
        });
    }

    private void updateCursor() {
        int state = editor.getTools().getSimulationControls().getState();
        boolean wait = state == SimulationControls.STATE_VALIDATING;
        if (wait) {
            if (!frame.getCursor().equals(WAIT_CURSOR)) {
                frame.setCursor(WAIT_CURSOR);
            }
        } else {
            if (!frame.getCursor().equals(DEFAULT_CURSOR)) {
                frame.setCursor(DEFAULT_CURSOR);
            }
        }
    }

    public void addSimlaneUIListener(SimlaneUIListener listener) {
        EventQueue.invokeLater(() -> {
            // add listener to the menu items
            for (List<JMenuItem> menuItems : Arrays.asList(fileMenuItems, scenarioMenuItems)) {
                for (JMenuItem menuItem : menuItems) {
                    menuItem.addActionListener(listener);
                }
            }
            // add listener to the editor layers panel
            editorLayersPanel.addSimlaneUIListener(listener);
            // add listener to the tools panel
            editorToolsPanel.addSimlaneUIListener(listener);
        });
    }

    public void stateChange(StateChangeEvent event) {
        if (event.getType().equals(SimulationControls.STATE_CHANGED_EVENT)) {
            EventQueue.invokeLater(() -> {
                updateMenuBar();
                updateCursor();
                frame.repaint();
            });
        }
    }
}
