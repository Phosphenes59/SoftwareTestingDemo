package com.demo.service.impl;

import com.demo.dao.MessageDao;
import com.demo.dao.UserDao;
import com.demo.entity.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.Collections;

import static com.demo.service.MessageService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageDao messageDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private MessageServiceImpl messageService;

    Message message1,message2,message3;

    @BeforeEach
    void setUp(){
        LocalDateTime localDateTime1 = LocalDateTime.of(2023,4,13,11,11,11);
        LocalDateTime localDateTime2 = LocalDateTime.of(2023,4,11,12,12,12);
        message1 = new Message(1,"user1","这是一条未审核的消息",localDateTime1,STATE_NO_AUDIT);
        message2 = new Message(2,"user2","这是一条审核通过的消息",localDateTime2,STATE_PASS);
        message3 = new Message(3,"user3","这是一条拒绝留言发表",localDateTime1,STATE_REJECT);
    }


    @Test
        //Function: 通过ID找到message
        //Scenario: ID存在、ID不存在
        //Notice: 可能返回null
    void findById() {
        int correct_id = 1;
        int not_exist_id = 6;

        //given
        when(messageDao.getOne(eq(not_exist_id))).thenReturn(null);
        when(messageDao.getOne(eq(correct_id))).thenReturn(message1);

        //when
        Message not_exist_message = messageService.findById(not_exist_id);
        Message correct_message = messageService.findById(correct_id);

        //then
        verify(messageDao).getOne(eq(not_exist_id));
        verify(messageDao).getOne(eq(correct_id));
        assertNull(not_exist_message);
        assertNotNull(correct_message);
        assertEquals(correct_id,correct_message.getMessageID());
    }

    //Function: 通过userID找到该用户的所有message(分页形式)
    //Scenario: userID存在且查询到数据、userID存在且查询到尾页为空、userID不存在
    //Assume: 传入参数均非null
    //Notice: 返回的Page<Message>不会为null，但是可能为空
    @Test
    void findByUser() {
        String correct_user = "user1";
        String not_exist_user = "user6";
        Pageable pageable = PageRequest.of(0,1);

        when(messageDao.findAllByUserID(correct_user,pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(message1), pageable, 1))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 1));

        when(messageDao.findAllByUserID(not_exist_user,pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        // 存在的用户, 第一次调用分页
        Page<Message> result1 = messageService.findByUser(correct_user, pageable);
        assertEquals(1, result1.getNumberOfElements());
        assertEquals(message1, result1.getContent().get(0));

        // 存在的用户, 第二次调用分页
        Page<Message> result2 = messageService.findByUser(correct_user, pageable);
        assertEquals(0, result2.getNumberOfElements());
        assertTrue(result2.getContent().isEmpty());

        // 不存在的用户, 调用分页
        Page<Message> result3 = messageService.findByUser(not_exist_user, pageable);
        assertEquals(0, result3.getNumberOfElements());
        assertTrue(result3.getContent().isEmpty());

        verify(messageDao, times(2)).findAllByUserID(correct_user, pageable);
        verify(messageDao, times(1)).findAllByUserID(not_exist_user, pageable);

    }

    //Function: 创建message
    //Scenario: 创建成功
    //Assume: 传入参数不为null
    @Test
    void create_ok() {
        Message create_message = message1;
        // given
        when(messageDao.save(create_message)).thenReturn(create_message);
        //when
        int create_message_id = messageService.create(create_message);
        //then
        verify(messageDao, times(1)).save(create_message);
        assertEquals(create_message.getMessageID(), create_message_id);
    }

    //Function: 创建message
    //Scenario: message某项参数不合法(以userID不存在为例)
    //Assume: 传入参数不为null
    @Test
    void create_user_not_exist() {
        Message create_message = message1;
        String not_exist_user = "not_exist_user";
        create_message.setUserID(not_exist_user);
        // given
        when(userDao.countByUserID(not_exist_user)).thenReturn(0);
        //when
        int create_message_id = messageService.create(create_message);
        //then
        verify(userDao, times(1)).countByUserID(not_exist_user);
        verify(messageDao, never()).save(create_message);
        assertEquals(-1, create_message_id);
    }


    //Function: 删除message
    //Scenario: 删除成功(不论ID存在或不存在)
    @Test
    void delById() {
        int del_id = 1;
        int del_id_not_exist = 6;
        doNothing().when(messageDao).deleteById(del_id);
        doNothing().when(messageDao).deleteById(del_id_not_exist);
        messageService.delById(del_id);
        messageService.delById(del_id_not_exist);
        verify(messageDao, times(1)).deleteById(del_id);
        verify(messageDao, times(1)).deleteById(del_id_not_exist);
    }

    //Function: 更新message
    //Scenario: 更新成功
    //Assume: 传入参数非null
    @Test
    void update_ok() {
        Message update_message = message1;
        when(messageDao.save(update_message)).thenReturn(update_message);
        messageService.update(update_message);
        verify(messageDao,times(1)).save(update_message);
    }

    //Function: 更新message
    //Scenario: 要更新的message不在数据库中
    //Assume: 传入参数非null
    @Test
    void update_msg_not_exist() {
        Message update_message = message1;
        int not_exist_id = 6;
        update_message.setMessageID(not_exist_id);

        when(messageDao.findByMessageID(not_exist_id)).thenReturn(null);

        messageService.update(update_message);

        verify(messageDao,times(1)).findByMessageID(not_exist_id);
        verify(messageDao,never()).save(update_message);
    }

    //Function: 更新message
    //Scenario: message某项参数不合法(以userID不存在为例)
    //Assume: 传入参数非null
    @Test
    void update_user_not_exist() {
        Message update_message = message1;
        String not_exist_user = "not_exist_user";
        update_message.setUserID("not_exist_user");

        when(userDao.countByUserID(not_exist_user)).thenReturn(0);

        messageService.update(update_message);

        verify(userDao,times(1)).countByUserID(not_exist_user);
        verify(messageDao,never()).save(update_message);
    }

    //Function: 审核message通过
    //Scenario: 通过成功
    @Test
    void confirmMessage_ok() {
        Message confirm_msg = message1;
        int correct_id = 1;

        // given
        when(messageDao.findByMessageID(correct_id)).thenReturn(confirm_msg);
        doNothing().when(messageDao).updateState(STATE_PASS, correct_id);

        // when
        messageService.confirmMessage(correct_id);

        // then
        verify(messageDao, times(1)).findByMessageID(correct_id);
        verify(messageDao, times(1)).updateState(STATE_PASS, correct_id);
    }

    //Function: 审核message通过
    //Scenario: message不存在
    @Test
    void confirmMessage_not_exist() {
        int not_exist_id = 6;
        // given
        when(messageDao.findByMessageID(not_exist_id)).thenReturn(null);

        // when
        try {
            messageService.confirmMessage(not_exist_id);
            fail("Expect a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            //then
            verify(messageDao, times(1)).findByMessageID(not_exist_id);
            verify(messageDao, never()).updateState(STATE_PASS, not_exist_id);
            assertEquals("留言不存在", e.getMessage());
        }
    }

    //Function: 审核message通过
    //Scenario: message状态不为待审核
    @Test
    void confirmMessage_state_wrong() {
        Message confirm_msg = message1;
        int msg_id = 1;

        // given
        when(messageDao.findByMessageID(msg_id)).thenReturn(confirm_msg);

        // when
        try {
            messageService.confirmMessage(msg_id);
            fail("Expect a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            //then
            verify(messageDao, times(1)).findByMessageID(msg_id);
            verify(messageDao, never()).updateState(STATE_PASS, msg_id);
            assertEquals("状态不为待审核", e.getMessage());
        }
    }

    //Function: 审核message拒绝
    //Scenario: 拒绝成功
    @Test
    void rejectMessage_ok() {
        Message reject_msg = message1;
        int correct_id = 1;

        // given
        when(messageDao.findByMessageID(correct_id)).thenReturn(reject_msg);
        doNothing().when(messageDao).updateState(STATE_REJECT, correct_id);

        // when
        messageService.rejectMessage(correct_id);

        // then
        verify(messageDao, times(1)).findByMessageID(correct_id);
        verify(messageDao, times(1)).updateState(STATE_REJECT, correct_id);
    }

    //Function: 审核message拒绝
    //Scenario: message不存在
    @Test
    void rejectMessage_not_exist() {
        int not_exist_id = 6;
        // given
        when(messageDao.findByMessageID(not_exist_id)).thenReturn(null);
        // when
        try {
            messageService.rejectMessage(not_exist_id);
            fail("Expect a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            //then
            verify(messageDao, times(1)).findByMessageID(not_exist_id);
            verify(messageDao, never()).updateState(STATE_REJECT, not_exist_id);
            assertEquals("留言不存在", e.getMessage());
        }
    }

    //Function: 审核message拒绝
    //Scenario: message状态不为待审核
    @Test
    void rejectMessage_state_wrong() {
        Message reject_msg = message1;
        int msg_id = 1;

        // given
        when(messageDao.findByMessageID(msg_id)).thenReturn(reject_msg);

        // when
        try {
            messageService.rejectMessage(msg_id);
            fail("Expect a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            //then
            verify(messageDao, times(1)).findByMessageID(msg_id);
            verify(messageDao, never()).updateState(STATE_REJECT, msg_id);
            assertEquals("状态不为待审核", e.getMessage());
        }
    }

    //Function: 找到所有待审核的message(分页形式)
    //Scenario: 找到数据、找到尾页为空
    //Assume: 传入参数不为null
    //Notice: 返回的Page<Message>不会为null，但是可能为空
    @Test
    void findWaitState() {
        Pageable pageable = PageRequest.of(0,1);

        when(messageDao.findAllByState(STATE_NO_AUDIT, pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(message1), pageable, 1))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 1));

        // 第一次调用分页
        Page<Message> result1 = messageService.findWaitState(pageable);
        result1.getNumberOfElements();
        assertEquals(1, result1.getNumberOfElements());
        assertEquals(message1, result1.getContent().get(0));

        // 第二次调用分页
        Page<Message> result2 = messageService.findWaitState(pageable);
        assertEquals(0, result2.getNumberOfElements());
        assertTrue(result2.getContent().isEmpty());

        verify(messageDao, times(2)).findAllByState(STATE_NO_AUDIT, pageable);

    }

    //Function: 找到所有审核通过的message(分页形式)
    //Scenario: 找到数据、找到尾页为空
    //Assume: 传入参数不为null
    //Notice: 返回的Page<Message>不会为null，但是可能为空
    @Test
    void findPassState() {
        Pageable pageable = PageRequest.of(0,1);

        when(messageDao.findAllByState(STATE_PASS, pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(message2), pageable, 1))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 1));

        // 第一次调用分页
        Page<Message> result1 = messageService.findPassState(pageable);
        result1.getNumberOfElements();
        assertEquals(1, result1.getNumberOfElements());
        assertEquals(message2, result1.getContent().get(0));

        // 第二次调用分页
        Page<Message> result2 = messageService.findPassState(pageable);
        assertEquals(0, result2.getNumberOfElements());
        assertTrue(result2.getContent().isEmpty());

        verify(messageDao, times(2)).findAllByState(STATE_PASS, pageable);

    }
}
