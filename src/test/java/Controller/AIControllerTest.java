package Controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import Model.AIResponse;
import Model.ChatMessage;
import Service.AIService;
import View.AI.AIPanel;

@ExtendWith(MockitoExtension.class)
class AIControllerTest {

    @Mock
    private AIPanel mockView;

    @Mock
    private AIService mockAIService;

    private AIController controller;

    @BeforeEach
    void setUp() {
        controller = new AIController(mockView, mockAIService);
    }

    /**
     * 测试：AI 返回正常结果
     */
    @Test
    void testOnSendMessage_AISuccess() throws Exception {
        when(mockAIService.predictReply("hi")).thenReturn(new AIResponse("HI! I can tell you your balance, monthly spending, or record expense/income and give you some suggestion.", null));

        controller.onSendMessage("hi");

        // 校验流程
        verify(mockView).addMessage(argThat(msg -> msg.getContent().equals("hi")));
        verify(mockView).setLoadingState(true);
        // 由于SwingWorker异步，需要稍等
        Thread.sleep(100);
        verify(mockView).setLoadingState(false);
        verify(mockView).addMessage(argThat(msg -> msg.getContent().equals("HI! I can tell you your balance, monthly spending, or record expense/income and give you some suggestion.")));
        verify(mockView, never()).showError(anyString());
    }



    /**
     * 测试：AI服务抛异常
     */
    @Test
    void testOnSendMessage_AIThrows() throws Exception {
        when(mockAIService.predictReply(anyString())).thenThrow(new RuntimeException("崩溃"));

        controller.onSendMessage("crash");
        Thread.sleep(100);

        verify(mockView).addMessage(argThat(msg -> msg.getContent().equals("crash")));
        verify(mockView).setLoadingState(true);
        verify(mockView).setLoadingState(false);
        verify(mockView).showError(startsWith("Failed to process AI response:"));
    }
}
