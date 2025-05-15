package Controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.CardLayout;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JLabel;

import org.junit.jupiter.api.*;
import org.mockito.*;

import Service.BudgetService;
import Service.TransactionService;
import View.PersonalCenter.PersonalCenterPanel;

class MainPanelControllerTest {

    @Mock
    PersonCenterController personCenterController;

    @Mock
    BillController billController;

    @Mock
    BudgetService budgetService;

    @Mock
    TransactionService transactionService;

    JPanel contentPanel;
    CardLayout cardLayout;
    MainPanelController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cardLayout = new CardLayout();
        contentPanel = spy(new JPanel(cardLayout));
        controller = new MainPanelController(
                "testUser", contentPanel,
                personCenterController, billController,
                budgetService, transactionService, cardLayout
        );
    }

    @Test
    void testInitializeContentPanels() {
        controller.initializeContentPanels();

        // 检查是否初始化了 "Personal Center" 和其他5个占位符
        Component[] comps = contentPanel.getComponents();
        boolean hasPersonalCenter = false;
        int labelCount = 0;
        for (Component comp : comps) {
            if (comp instanceof PersonalCenterPanel) hasPersonalCenter = true;
            if (comp instanceof JLabel) labelCount++;
        }
        assertTrue(hasPersonalCenter, "Personal Center panel should be present");
        assertEquals(5, labelCount, "Should have 5 JLabel placeholders");
    }

    @Test
    void testShowPanel_loadsAndShowsPanel() {
        // 1. 先初始化
        controller.initializeContentPanels();
        // 2. 模拟点击“Personal Center”
        controller.showPanel("Personal Center");

        // 断言 cardLayout 正在显示“Personal Center”面板
        Component[] comps = contentPanel.getComponents();
        boolean found = false;
        for (Component comp : comps) {
            if (comp instanceof PersonalCenterPanel && "Personal Center".equals(comp.getName())) {
                found = true;
            }
        }
        assertTrue(found, "Personal Center should be loaded and named correctly");
    }

    @Test
    void testShowPanel_replacePlaceholderWithRealPanel() {
        controller.initializeContentPanels();
        // 先保证"Budget Management"还只是一个JLabel
        Component[] compsBefore = contentPanel.getComponents();
        boolean hasLabel = false;
        for (Component comp : compsBefore) {
            if (comp instanceof JLabel && "Budget Management".equals(comp.getName())) {
                hasLabel = true;
            }
        }
        assertTrue(hasLabel, "Budget Management placeholder should exist");

        // 触发showPanel加载
        controller.showPanel("Budget Management");
        // BudgetManagementPanel加载后，占位符会被panel取代
        boolean hasPanel = false;
        for (Component comp : contentPanel.getComponents()) {
            if ("Budget Management".equals(comp.getName()) && !(comp instanceof JLabel)) {
                hasPanel = true;
            }
        }
        assertTrue(hasPanel, "Budget Management real panel should be loaded and replace the placeholder");
    }

    @Test
    void testShowPanel_panelNotFoundShouldCreateNew() {
        controller.initializeContentPanels();
        // 触发一个不存在的面板
        controller.showPanel("NotExistPanel");
        // 结果应该有一个名为 NotExistPanel 的占位符面板
        boolean hasPlaceholder = false;
        for (Component comp : contentPanel.getComponents()) {
            if ("NotExistPanel".equals(comp.getName())) {
                hasPlaceholder = true;
            }
        }
        assertTrue(hasPlaceholder, "Non-existent panel should be replaced with placeholder");
    }
}
