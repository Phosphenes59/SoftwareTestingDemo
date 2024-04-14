package com.demo.service.impl;

import com.demo.dao.MessageDao;
import com.demo.dao.UserDao;
import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.demo.service.OrderService.STATE_NO_AUDIT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MessageVoServiceImplTest {

    @Mock
    private MessageDao messageDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private MessageVoServiceImpl messageVoService;

    List<Message> messagesList;
    List<MessageVo> messageVosList;
    User[] users = new User[3];

    @BeforeEach
    void setup() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 4, 11, 13, 14, 14);
        messagesList = new ArrayList<>();
        messageVosList = new ArrayList<>();

        for(int i = 0; i < 3; i++){
            users[i] = new User(i, "user"+i, "user_name", "pwd", "email", "phone", 0, "picture");
            Message msg = new Message(i, "user"+i, "第"+i+"条", localDateTime, STATE_NO_AUDIT);
            messagesList.add(msg);
            messageVosList.add(new MessageVo(msg.getMessageID(), users[i].getUserID(), msg.getContent(),
                    msg.getTime(), users[i].getUserName(), users[i].getPicture(), msg.getState()));
        }
    }

    //Function: 根据msgID返回MessageVo
    //Scenario: 存在该msgID并返回成功
    @Test
    void returnMessageVoByMessageID_ok() {
        int correct_id = 0;
        Message msg = messagesList.get(correct_id);
        MessageVo msgVo = messageVosList.get(correct_id);
        //given
        when(messageDao.findByMessageID(correct_id)).thenReturn(msg);
        when(userDao.findByUserID(msg.getUserID())).thenReturn(users[correct_id]);

        //when
        MessageVo correct = messageVoService.returnMessageVoByMessageID(correct_id);

        //then
        verify(messageDao, times(1)).findByMessageID(correct_id);
        verify(userDao, times(1)).findByUserID(msg.getUserID());
        assertNotNull(correct);
        assertEquals(msgVo, correct);
    }

    //Function: 根据msgID返回MessageVo
    //Scenario: msgID不存在或msg的UserID不存在(以msgID不存在为例)
    @Test
    void returnMessageVoByMessageID_not_exist_id() {
        int wrong_id = 4;
        //given
        when(messageDao.findByMessageID(eq(wrong_id))).thenReturn(null);
        //when
        MessageVo wrong = messageVoService.returnMessageVoByMessageID(wrong_id);
        //then
        assertNull(wrong);
    }

    //Function: 根据msg列表返回MessageVo的列表
    //Scenario: 列表中所有msg均存在并返回成功
    @Test
    void returnVo_ok() {
        //given
        for(int i = 0; i < 3; i++){
            when(messageDao.findByMessageID(i)).thenReturn(messagesList.get(i));
            when(userDao.findByUserID(messagesList.get(i).getUserID())).thenReturn(users[i]);
        }

        //when
        List<MessageVo> result = messageVoService.returnVo(messagesList);

        //then
        for(int i = 0; i < 3; i++){
            verify(messageDao, times(1)).findByMessageID(i);
            verify(userDao, times(1)).findByUserID(messagesList.get(i).getUserID());
        }
        assertNotNull(result);
        assertEquals(result, messageVosList);
    }

    //Function: 根据msg列表返回MessageVo的列表
    //Scenario: 列表中某项msg为null
    @Test
    void returnVo_some_msg_null() {
        //given
        for(int i = 0; i < 3; i++){
            when(messageDao.findByMessageID(i)).thenReturn(messagesList.get(i));
            when(userDao.findByUserID(messagesList.get(i).getUserID())).thenReturn(users[i]);
        }
        messagesList.set(0, null);

        //when
        List<MessageVo> result = messageVoService.returnVo(messagesList);

        //then
        assertNotNull(result);
        assertNull(result.get(0));
        assertEquals(result.get(1), messageVosList.get(1));
        assertEquals(result.get(2), messageVosList.get(2));
    }

}