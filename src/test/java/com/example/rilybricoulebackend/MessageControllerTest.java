package com.sbsolutions.rilybricoule.controllers;

import com.sbsolutions.rilybricoule.services.IMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IMessageService messageService;

    @Test
    void shouldReturnMessagesByChatId() throws Exception {

        mockMvc.perform(get("/messages/1"))
                .andExpect(status().isOk());
    }
}
