package com.demo.controller.admin;

import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.demo.service.MessageService.STATE_PASS;
import static com.demo.service.MessageService.STATE_REJECT;
import static com.demo.service.OrderService.STATE_NO_AUDIT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@WebMvcTest(AdminMessageController.class)
class AdminMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MessageService messageService;
    @MockBean
    private MessageVoService messageVoService;

    Message[] messageArray = new Message[3];
    MessageVo[] messageVoArray = new MessageVo[4];
    List<Message> messages_pass = new ArrayList<>();
    List<Message> messages = new ArrayList<>();
    Message message1, message2, message3;
    User user1;

    @BeforeEach
    void setup() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 4, 11, 13, 14, 14);
        messages = new ArrayList<>();
        message1 = new Message(0,"user1","这是一条未审核的消息",localDateTime, STATE_NO_AUDIT);
        message2 = new Message(1,"user2","这是一条审核通过的消息",localDateTime,STATE_PASS);
        message3 = new Message(2,"user3","这是一条拒绝留言发表",localDateTime,STATE_REJECT);
        user1 = new User(1, "user1", "user_name", "pwd", "email", "phone", 0, "picture");

        for(int i = 0; i < 3; i++){
            messageArray[i] = new Message(i, "user1", "第"+i+"条", localDateTime, STATE_PASS);
            messages.add(messageArray[i]);
            if(i<2){
                messages_pass.add(messageArray[i]);
            }
            messageVoArray[i] = new MessageVo(messageArray[i].getMessageID(),"user1",messageArray[i].getContent(),messageArray[i].getTime(),user1.getUserName(),user1.getPicture(),messageArray[i].getState());
        }
    }

    @Test
    void message_manage() throws Exception {
        Pageable message_pageable = PageRequest.of(0,10, Sort.by("time").descending());
        Page<Message> messagePage = new PageImpl<>(messages,message_pageable,3);

        //given
        when(messageService.findWaitState(message_pageable)).thenReturn(messagePage);
        //when&then
        mockMvc.perform(get("/message_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/message_manage"))
                .andExpect(model().attribute("total",messagePage.getTotalPages()));
        verify(messageService, times(1)).findWaitState(message_pageable);
    }

    @Test
    void messageList() throws Exception{
        int page = 1;
        Pageable message_pageable= PageRequest.of(page-1,10, Sort.by("time").descending());
        List<Message> messages_test = new ArrayList<>();
        messages_test.add(message1);
        List<MessageVo> messageVos = new ArrayList<>();
        messageVos.add(new MessageVo(message1.getMessageID(),message1.getUserID(),message1.getContent(),message1.getTime(),user1.getUserName(),user1.getPicture(),message1.getState()));
        //given
        when(messageService.findWaitState(message_pageable)).thenReturn(new PageImpl<>(messages_test,message_pageable,1));
        when(messageVoService.returnVo(messages_test)).thenReturn(messageVos);
        //when&then
        mockMvc.perform(get("/messageList.do")
                        .param("page", String.valueOf(page))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].messageID").value(messages_test.get(0).getMessageID()))
                .andExpect(jsonPath("$[0].userID").value(messages_test.get(0).getUserID()))
                .andExpect(jsonPath("$[0].content").value(messages_test.get(0).getContent()))
                .andExpect(jsonPath("$[0].state").value(messages_test.get(0).getState()))
                .andExpect(jsonPath("$[0].userName").value(messageVos.get(0).getUserName()));

        verify(messageService, times(1)).findWaitState(message_pageable);
        verify(messageVoService, times(1)).returnVo(messages_test);

    }

    @Test
    void passMessage()throws Exception {
        int messageID = 6;

        //given
        doNothing().when(messageService).confirmMessage(messageID);

        //when&then
        mockMvc.perform(post("/passMessage.do")
                        .param("messageID", String.valueOf(messageID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService, times(1)).confirmMessage(messageID);
    }

    @Test
    void rejectMessage() throws Exception {
        int messageID = 1;

        // given
        doNothing().when(messageService).rejectMessage(messageID);

        //when&then
        mockMvc.perform(post("/rejectMessage.do")
                        .param("messageID", String.valueOf(messageID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService, times(1)).rejectMessage(messageID);
    }


    @Test
    void delMessage() throws Exception {
        int messageID = 1;

        //given
        doNothing().when(messageService).delById(messageID);

        // when&then
        mockMvc.perform(post("/delMessage.do")
                        .param("messageID", String.valueOf(messageID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(messageService, times(1)).delById(messageID);

    }
}
