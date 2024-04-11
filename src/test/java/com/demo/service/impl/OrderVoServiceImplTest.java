package com.demo.service.impl;

import com.demo.dao.OrderDao;
import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import com.demo.entity.vo.OrderVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class OrderVoServiceImplTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private VenueDao venueDao;

    @InjectMocks
    private OrderVoServiceImpl orderVoServiceImpl;

    List<Order> ordersList;
    List<OrderVo> orderVosList;
    Venue[] venues = new Venue[3];

    @BeforeEach
    void setUp() {
        LocalDateTime orderTime = LocalDateTime.of(2023, 4, 13, 10, 0);
        LocalDateTime startTime = LocalDateTime.of(2023, 4, 13, 12, 0);
        ordersList = new ArrayList<>();
        orderVosList = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            venues[i] = new Venue(i, "venue"+i, "good", 100, "picture", "address", "9:00", "20:00");
            Order order = new Order(i, "user"+i, i, 1, orderTime, startTime, 1, 100);
            ordersList.add(order);
            orderVosList.add(new OrderVo(order.getOrderID(), order.getUserID(), order.getVenueID(), venues[i].getVenueName(),
                    order.getState(), order.getOrderTime(), order.getStartTime(), order.getHours(), order.getTotal()));
        }
    }

    //Function: 根据orderID返回OrderVo
    //Scenario: 存在该orderID并返回成功
    @Test
    void returnOrderVoByOrderID_ok() {
        int correct_id = 0;
        Order order = ordersList.get(correct_id);
        OrderVo orderVo = orderVosList.get(correct_id);
        //given
        when(orderDao.findByOrderID(correct_id)).thenReturn(order);
        when(venueDao.findByVenueID(order.getVenueID())).thenReturn(venues[correct_id]);

        //when
        OrderVo correct = orderVoServiceImpl.returnOrderVoByOrderID(correct_id);

        //then
        verify(orderDao, times(1)).findByOrderID(correct_id);
        verify(venueDao, times(1)).findByVenueID(order.getVenueID());
        assertNotNull(correct);
        assertEquals(orderVo, correct);
    }

    //Function: 根据orderID返回OrderVo
    //Scenario: orderID不存在或order的VenueID不存在(以orderID不存在为例)
    @Test
    void returnOrderVoByOrderID_not_exist_id() {
        int wrong_id = 4;
        //given
        when(orderDao.findByOrderID(eq(wrong_id))).thenReturn(null);
        //when
        OrderVo wrong = orderVoServiceImpl.returnOrderVoByOrderID(wrong_id);
        //then
        verify(orderDao, times(1)).findByOrderID(eq(wrong_id));
        assertNull(wrong);
    }


    //Function: 根据order列表返回OrderVo的列表
    //Scenario: 列表中所有order均存在并返回成功
    @Test
    void returnVo_ok() {
        //given
        for(int i = 0; i < 3; i++){
            when(orderDao.findByOrderID(i)).thenReturn(ordersList.get(i));
            when(venueDao.findByVenueID(ordersList.get(i).getVenueID())).thenReturn(venues[i]);
        }

        //when
        List<OrderVo> result = orderVoServiceImpl.returnVo(ordersList);

        //then
        for(int i = 0; i < 3; i++){
            verify(orderDao, times(1)).findByOrderID(i);
            verify(venueDao, times(1)).findByVenueID(ordersList.get(i).getVenueID());
        }
        assertNotNull(result);
        assertEquals(result, orderVosList);
    }

    //Function: 根据order列表返回OrderVo的列表
    //Scenario: 列表中某项order为null
    @Test
    void returnVo_some_order_null() {
        //given
        for(int i = 0; i < 3; i++){
            when(orderDao.findByOrderID(i)).thenReturn(ordersList.get(i));
            when(venueDao.findByVenueID(ordersList.get(i).getVenueID())).thenReturn(venues[i]);
        }
        ordersList.set(0, null);

        //when
        List<OrderVo> result = orderVoServiceImpl.returnVo(ordersList);

        //then
        assertNotNull(result);
        assertNull(result.get(0));
        assertEquals(result.get(1), orderVosList.get(1));
        assertEquals(result.get(2), orderVosList.get(2));
    }

}