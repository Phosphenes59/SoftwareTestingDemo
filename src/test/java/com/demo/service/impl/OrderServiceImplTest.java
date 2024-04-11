package com.demo.service.impl;

import com.demo.dao.OrderDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.demo.service.OrderService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private OrderServiceImpl orderService;

    Order order1, order2;

    Venue venue;

    static int VALID = 0, INVALID = 1;
    static int[] orderID = {1, 3};
    static String[] venueName = {"venue", "not exist venue"};
    static LocalDateTime[] startTime = {LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(1)};
    static int[] hours = {1, -1};
    static int[] venueID = {0, 1};
    static String[] userID = {"test", "not exist user"};
    static LocalDateTime orderTime = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        venue = new Venue(venueID[VALID], venueName[VALID], "good", 100, "picture", "address", "9:00", "20:00");
        int total = hours[VALID] * venue.getPrice();
        order1 = new Order(1,userID[VALID],venueID[VALID],STATE_NO_AUDIT,orderTime,startTime[VALID],hours[VALID],total);
        order2 = new Order(2,userID[VALID],venueID[VALID],STATE_FINISH,orderTime,startTime[VALID],hours[VALID],total);
    }

    //Function: 根据id返回订单
    //Scenario: id不存在, id存在
    //Notice: 可能返回null
    @Test
    void findById() {
        // given
        when(orderDao.getOne(eq(orderID[VALID]))).thenReturn(order1);
        when(orderDao.getOne(eq(orderID[INVALID]))).thenReturn(null);

        //when
        Order correct_order = orderService.findById(orderID[VALID]);
        Order not_exist_order = orderService.findById(orderID[INVALID]);

        //then
        verify(orderDao, times(1)).getOne(eq(orderID[VALID]));
        verify(orderDao, times(1)).getOne(eq(orderID[INVALID]));
        assertNotNull(correct_order);
        assertEquals(orderID[VALID], correct_order.getOrderID());
        assertNull(not_exist_order);
    }

    //Function: 根据VenueID和OrderStartTime返回订单列表
    //Scenario: VenueID存在, startTime正确
    //Assume: 传入参数非null
    //Notice: 返回的List<Order>不会为null，但是可能为空
    @Test
    void findDateOrder_success() {
        List<Order> expectedOrders = Arrays.asList(order1, order2);
        LocalDateTime correct_startTime = orderTime.minusHours(1);
        LocalDateTime correct_startTime2 = orderTime.plusHours(1);
        //given
        when(orderDao.findByVenueIDAndStartTimeIsBetween(venueID[VALID], correct_startTime, correct_startTime2)).thenReturn(expectedOrders);
        //when
        List<Order> correct_orders = orderService.findDateOrder(venueID[VALID], correct_startTime, correct_startTime2);
        //then
        verify(orderDao, times(1)).findByVenueIDAndStartTimeIsBetween(venueID[VALID], correct_startTime, correct_startTime2);
        assertEquals(2, correct_orders.size());
        assertEquals(order1, correct_orders.get(0));
        assertEquals(order2, correct_orders.get(1));
    }

    //Function: 根据VenueID和OrderStartTime返回订单列表
    //Scenario: VenueID不存在, startTime过早, startTime过晚
    //Assume: 传入参数非null
    //Notice: 返回的List<Order>不会为null，但是可能为空
    static List<Arguments> findDateOrder_failed_param(){
        LocalDateTime correct_startTime = orderTime.minusHours(1);
        LocalDateTime correct_startTime2 = orderTime.plusHours(1);
        LocalDateTime early_startTime = orderTime.minusHours(2);
        LocalDateTime early_startTime2 = orderTime.minusHours(1);
        LocalDateTime late_startTime = orderTime.plusHours(1);
        LocalDateTime late_startTime2 = orderTime.plusHours(2);

        return List.of(
                Arguments.of(venueID[INVALID], correct_startTime, correct_startTime2),
                Arguments.of(venueID[VALID], early_startTime, early_startTime2),
                Arguments.of(venueID[VALID], late_startTime, late_startTime2)
        );
    }
    @ParameterizedTest
    @MethodSource("findDateOrder_failed_param")
    void findDateOrder_failed(int venueID, LocalDateTime startTime, LocalDateTime startTime2) {
        List<Order> unexpectedOrders = new ArrayList<>();
        //given
        when(orderDao.findByVenueIDAndStartTimeIsBetween(venueID, startTime, startTime2)).thenReturn(unexpectedOrders);
        //when
        List<Order> not_exist_orders = orderService.findDateOrder(venueID, startTime, startTime2);
        //then
        verify(orderDao, times(1)).findByVenueIDAndStartTimeIsBetween(venueID, startTime, startTime2);
        assertEquals(0, not_exist_orders.size());
    }


    //Function: 根据userID返回该用户的所有订单列表
    //Scenario: userID存在且查询到数据、userID存在且查询到尾页为空、userID不存在
    //Assume: 传入参数均非null
    //Notice: 返回的Page<Order>不会为null，但是可能为空
    @Test
    void findUserOrder() {
        Pageable pageable = PageRequest.of(0,1);

        when(orderDao.findAllByUserID(userID[VALID],pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(order1), pageable, 1))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 1));

        when(orderDao.findAllByUserID(userID[INVALID], pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        // 存在的用户, 第一次调用分页
        Page<Order> result1 = orderService.findUserOrder(userID[VALID], pageable);
        assertEquals(1, result1.getNumberOfElements());
        assertEquals(order1, result1.getContent().get(0));

        // 存在的用户, 第二次调用分页
        Page<Order> result2 = orderService.findUserOrder(userID[VALID], pageable);
        assertEquals(0, result2.getNumberOfElements());
        assertTrue(result2.getContent().isEmpty());

        // 不存在的用户, 调用分页
        Page<Order> result3 = orderService.findUserOrder(userID[INVALID], pageable);
        assertEquals(0, result3.getNumberOfElements());
        assertTrue(result3.getContent().isEmpty());

        verify(orderDao, times(2)).findAllByUserID(userID[VALID], pageable);
        verify(orderDao, times(1)).findAllByUserID(userID[INVALID], pageable);
    }


    //Function: 更新订单
    //Scenario: 更新成功
    //Assume: 传入参数均非null
    @Test
    void updateOrder_success(){
        //given
        when(venueDao.findByVenueName(venueName[VALID])).thenReturn(venue);
        when(orderDao.findByOrderID(orderID[VALID])).thenReturn(order1);
        //when
        orderService.updateOrder(orderID[VALID], venueName[VALID], startTime[VALID], hours[VALID], userID[VALID]);
        //then
        verify(venueDao, times(1)).findByVenueName(venueName[VALID]);
        verify(orderDao, times(1)).findByOrderID(orderID[VALID]);
        verify(orderDao, times(1)).save(any());
    }

    //Function: 更新订单
    //Scenario: 订单ID不存在, 场馆名称不存在, 开始时间早于当前时间、时长不合法, userID不存在
    //Assume: 传入参数均非null
    static List<Arguments> updateOrder_failed_param(){
        return List.of(
                Arguments.of(orderID[INVALID], venueName[VALID], startTime[VALID], hours[VALID], userID[VALID]),
                Arguments.of(orderID[VALID], venueName[INVALID], startTime[VALID], hours[VALID], userID[VALID]),
                Arguments.of(orderID[VALID], venueName[VALID], startTime[INVALID], hours[INVALID], userID[VALID]),
                Arguments.of(orderID[VALID], venueName[VALID], startTime[VALID], hours[VALID], userID[INVALID])
        );
    }
    @ParameterizedTest
    @MethodSource("updateOrder_failed_param")
    void updateOrder_failed(int orderID, String venueName, LocalDateTime startTime, int hours, String userID){
        //given
        when(venueDao.findByVenueName(venueName)).thenReturn(venueName.equals(OrderServiceImplTest.venueName[VALID]) ? venue : null);
        when(orderDao.findByOrderID(orderID)).thenReturn(orderID == OrderServiceImplTest.orderID[VALID] ? order1 : null);
        //when
        orderService.updateOrder(orderID, venueName, startTime, hours, userID);
        //then
        verify(venueDao, times(1)).findByVenueName(venueName);
        verify(orderDao, times(1)).findByOrderID(orderID);
        verify(orderDao, never()).save(any());

    }

    //Function: 创建订单
    //Scenario: 创建成功
    //Assume: 传入参数非null
    @Test
    void submit_success() {
        //given
        when(venueDao.findByVenueName(venueName[VALID])).thenReturn(venue);
        //when
        orderService.submit(venueName[VALID], startTime[VALID], hours[VALID], userID[VALID]);
        //then
        verify(venueDao, times(1)).findByVenueName(venueName[VALID]);
        verify(orderDao, times(1)).save(any());
    }

    //Function: 创建订单
    //Scenario: 场馆名称不存在, 开始时间早于当前时间、时长不合法, userID不存在
    //Assume: 传入参数非null
    static List<Arguments> submit_failed_param(){
        return List.of(
                Arguments.of(venueName[INVALID], startTime[VALID], hours[VALID], userID[VALID]),
                Arguments.of(venueName[VALID], startTime[INVALID], hours[INVALID], userID[VALID]),
                Arguments.of(venueName[VALID], startTime[VALID], hours[VALID], userID[INVALID])
        );
    }
    @ParameterizedTest
    @MethodSource("submit_failed_param")
    void submit_failed(String venueName, LocalDateTime startTime, int hours, String userID){
        //given
        when(venueDao.findByVenueName(venueName)).thenReturn(venueName.equals(OrderServiceImplTest.venueName[VALID]) ? venue : null);
        //when
        orderService.submit(venueName, startTime, hours, userID);
        //then
        verify(venueDao, times(1)).findByVenueName(venueName);
        verify(orderDao, never()).save(any());
    }

    //Function: 创建订单
    //Scenario: 该场馆在该时间段已被预定
    //Assume: 传入参数非null
    @Test
    void submit_venueOccupied(){
        //given
        when(venueDao.findByVenueName(venueName[VALID])).thenReturn(venue);
//        when(orderDao.findAllByVenueIDAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(venue.getVenueID(),
//                startTime[VALID], startTime[VALID].plusHours(hours[VALID]))
//                .thenReturn(Collections.singletonList(order1)));
        //when
        orderService.submit(venueName[VALID], startTime[VALID], hours[VALID], userID[VALID]);
        //then
        verify(venueDao, times(1)).findByVenueName(venueName[VALID]);
//        verify(orderDao, times(1)).
//                findAllByVenueIDAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(venue.getVenueID(),
//                        startTime[VALID], startTime[VALID].plusHours(hours[VALID]));
        verify(orderDao, never()).save(any());
    }

    //Function: 删除订单
    //Scenario: 删除成功(不论ID存在或不存在)
    @Test
    void delOrder() {
        int del_id = 1;
        doNothing().when(orderDao).deleteById(del_id);
        orderService.delOrder(del_id);
        verify(orderDao, times(1)).deleteById(del_id);
    }

    //Function: 订单审核通过
    //Scenario: 审核通过成功
    @Test
    void confirmOrder() {
        when(orderDao.findByOrderID(orderID[VALID])).thenReturn(order1);
        doNothing().when(orderDao).updateState(STATE_WAIT, orderID[VALID]);
        orderService.confirmOrder(orderID[VALID]);
        verify(orderDao, times(1)).findByOrderID(orderID[VALID]);
        verify(orderDao, times(1)).updateState(STATE_WAIT, orderID[VALID]);
    }

    //Function: 订单审核通过
    //Scenario: 订单ID不存在
    @Test
    void confirmOrder_not_exist_id() {
        when(orderDao.findByOrderID(orderID[INVALID])).thenReturn(null);
        try{
            orderService.confirmOrder(orderID[INVALID]);
            fail("Expect a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            verify(orderDao, times(1)).findByOrderID(orderID[INVALID]);
            verify(orderDao, never()).updateState(STATE_WAIT, orderID[INVALID]);
            assertEquals("订单不存在", e.getMessage());
        }
    }

    //Function: 订单审核通过
    //Scenario: 订单状态不是待审核
    @Test
    void confirmOrder_state_not_noaudit() {
        int orderID = 2;
        when(orderDao.findByOrderID(orderID)).thenReturn(order2);
        try{
            orderService.confirmOrder(orderID);
            fail("Expect a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            verify(orderDao, times(1)).findByOrderID(orderID);
            verify(orderDao, never()).updateState(STATE_WAIT, orderID);
            assertEquals("订单状态不是待审核", e.getMessage());
        }
    }

    //Function: 完成订单
    //Scenario: 完成成功
    @Test
    void finishOrder() {
        when(orderDao.findByOrderID(orderID[VALID])).thenReturn(order1);
        doNothing().when(orderDao).updateState(STATE_FINISH, orderID[VALID]);
        orderService.finishOrder(orderID[VALID]);
        verify(orderDao, times(1)).findByOrderID(orderID[VALID]);
        verify(orderDao, times(1)).updateState(STATE_FINISH, orderID[VALID]);
    }

    //Function: 完成订单
    //Scenario: 订单ID不存在
    @Test
    void finishOrder_not_exist_id() {
        when(orderDao.findByOrderID(orderID[INVALID])).thenReturn(null);
        try{
            orderService.finishOrder(orderID[INVALID]);
            fail("Expect a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            verify(orderDao, times(1)).findByOrderID(orderID[INVALID]);
            verify(orderDao, never()).updateState(STATE_FINISH, orderID[INVALID]);
            assertEquals("订单不存在", e.getMessage());
        }
    }

    //Function: 完成订单
    //Scenario: 订单状态不是等待中
    @Test
    void finishOrder_state_not_wait() {
        int orderID = 2;
        when(orderDao.findByOrderID(orderID)).thenReturn(order2);
        try{
            orderService.finishOrder(orderID);
            fail("Expect a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            verify(orderDao, times(1)).findByOrderID(orderID);
            verify(orderDao, never()).updateState(STATE_FINISH, orderID);
            assertEquals("订单状态不是等待中", e.getMessage());
        }
    }

    //Function: 拒绝订单
    //Scenario: 拒绝成功
    @Test
    void rejectOrder() {
        when(orderDao.findByOrderID(orderID[VALID])).thenReturn(order1);
        doNothing().when(orderDao).updateState(STATE_REJECT, orderID[VALID]);
        orderService.rejectOrder(orderID[VALID]);
        verify(orderDao, times(1)).findByOrderID(orderID[VALID]);
        verify(orderDao, times(1)).updateState(STATE_REJECT, orderID[VALID]);
    }

    //Function: 拒绝订单
    //Scenario: 订单ID不存在
    @Test
    void rejectOrder_not_exist_id() {
        when(orderDao.findByOrderID(orderID[INVALID])).thenReturn(null);
        try{
            orderService.rejectOrder(orderID[INVALID]);
            fail("Expect a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            verify(orderDao, times(1)).findByOrderID(orderID[INVALID]);
            verify(orderDao, never()).updateState(STATE_REJECT, orderID[INVALID]);
            assertEquals("订单不存在", e.getMessage());
        }
    }

    //Function: 拒绝订单
    //Scenario: 订单状态不是待审核
    @Test
    void rejectOrder_state_not_noaudit() {
        int orderID = 2;
        when(orderDao.findByOrderID(orderID)).thenReturn(order2);
        try{
            orderService.rejectOrder(orderID);
            fail("Expect a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            verify(orderDao, times(1)).findByOrderID(orderID);
            verify(orderDao, never()).updateState(STATE_REJECT, orderID);
            assertEquals("订单状态不是待审核", e.getMessage());
        }
    }

    //Function: 返回所有未审核的订单(以分页形式)
    //Scenario: 查询第一页，查询第二页，查询到尾页为空
    //Assume: 传入参数非null
    //Notice: 返回的Page<Order>不会为null，但是可能为空
    @Test
    void findNoAuditOrder() {
        order2.setState(STATE_NO_AUDIT);
        Pageable pageable = PageRequest.of(0, 1);
        when(orderDao.findAllByState(STATE_NO_AUDIT, pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(order1), pageable, 2))
                .thenReturn(new PageImpl<>(Collections.singletonList(order2), pageable, 2))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 2));

        // 第一次调用分页
        Page<Order> result1 = orderService.findNoAuditOrder(pageable);
        assertEquals(1, result1.getNumberOfElements());
        assertEquals(order1, result1.getContent().get(0));

        // 第二次调用分页
        Page<Order> result2 = orderService.findNoAuditOrder(pageable);
        assertEquals(1, result2.getNumberOfElements());
        assertEquals(order2, result2.getContent().get(0));

        // 第三次调用分页
        Page<Order> result3 = orderService.findNoAuditOrder(pageable);
        assertEquals(0, result3.getNumberOfElements());

        // 验证findAllByState方法被调用了三次
        verify(orderDao, times(3)).findAllByState(STATE_NO_AUDIT, pageable);
    }

    //Function: 返回所有审核通过的订单(以列表形式)
    //Scenario: 成功查询
    //Notice: 返回的List<Order>不会为null，但是可能为空
    @Test
    void findAuditOrder() {
        order1.setState(STATE_WAIT);
        order2.setState(STATE_FINISH);
        List<Order> orders = Arrays.asList(order1, order2);

        when(orderDao.findAudit(STATE_WAIT, STATE_FINISH)).thenReturn(orders);

        List<Order> result = orderService.findAuditOrder();

        assertEquals(2, result.size());
        assertEquals(order1.getOrderID(), result.get(0).getOrderID());
        assertEquals(STATE_WAIT, result.get(0).getState());
        assertEquals(order2.getOrderID(), result.get(1).getOrderID());
        assertEquals(STATE_FINISH, result.get(1).getState());
        verify(orderDao, times(1)).findAudit(STATE_WAIT, STATE_FINISH);
    }
}