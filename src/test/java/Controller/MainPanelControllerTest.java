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
        controller.initializeContentPanels();
        controller.showPanel("Personal Center");

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
        Component[] compsBefore = contentPanel.getComponents();
        boolean hasLabel = false;
        for (Component comp : compsBefore) {
            if (comp instanceof JLabel && "Budget Management".equals(comp.getName())) {
                hasLabel = true;
            }
        }
        assertTrue(hasLabel, "Budget Management placeholder should exist");

        controller.showPanel("Budget Management");
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
        controller.showPanel("NotExistPanel");
        boolean hasPlaceholder = false;
        for (Component comp : contentPanel.getComponents()) {
            if ("NotExistPanel".equals(comp.getName())) {
                hasPlaceholder = true;
            }
        }
        assertTrue(hasPlaceholder, "Non-existent panel should be replaced with placeholder");
    }
}
