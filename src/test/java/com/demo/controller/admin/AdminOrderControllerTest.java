package com.demo.controller.admin;

import com.demo.controller.user.OrderController;
import com.demo.entity.Order;
import com.demo.entity.Venue;
import com.demo.entity.vo.OrderVo;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import com.demo.service.VenueService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private OrderService orderService;
    @MockBean
    private OrderVoService orderVoService;
    @MockBean
    private VenueService venueService;

    Venue venue;
    Order order;
    OrderVo orderVo;

    @BeforeEach
    void setUp() {
        venue = new Venue(1, "venue", "good", 100,
                "picture", "address", "9:00", "20:00");
        order = new Order(1, "test1", venue.getVenueID(),
                1, LocalDateTime.now(), LocalDateTime.now(),
                1, 100);
        orderVo = new OrderVo(order.getOrderID(), order.getUserID(), order.getVenueID(), venue.getVenueName(),
                order.getState(), order.getOrderTime(), order.getStartTime(), order.getHours(), order.getTotal());
    }

    @Test
    public void reservation_manage() throws Exception {
        List<Order> orders = new ArrayList<>();
        orders.add(order);
        List<OrderVo> orderVos = new ArrayList<>();
        orderVos.add(orderVo);
        Pageable order_pageable = PageRequest.of(0, 10, Sort.by("orderTime").descending());
        when(orderService.findAuditOrder()).thenReturn(orders);
        when(orderVoService.returnVo(orders)).thenReturn(orderVos);
        when(orderService.findNoAuditOrder(order_pageable)).thenReturn(new PageImpl<>(orders));

        mockMvc.perform(get("/reservation_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservation_manage"))
                .andExpect(model().attribute("order_list", orderVos))
                .andExpect(model().attribute("total", 1));

        verify(orderService, times(1)).findAuditOrder();
        verify(orderVoService, times(1)).returnVo(anyList());
        verify(orderService, times(1)).findNoAuditOrder(order_pageable);
    }

    @Test
    public void getNoAuditOrder() throws Exception {
        List<Order> orders = new ArrayList<>();
        orders.add(order);
        List<OrderVo> orderVos = new ArrayList<>();
        orderVos.add(orderVo);

        when(orderService.findNoAuditOrder(any(Pageable.class)))
                .thenReturn(new PageImpl<>(orders));

        when(orderVoService.returnVo(anyList()))
                .thenReturn(orderVos);

        mockMvc.perform(get("/admin/getOrderList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", Matchers.is(1)))
                .andExpect(jsonPath("$[0].orderID", Matchers.is(orderVo.getOrderID())));

        verify(orderService, times(1)).findNoAuditOrder(any(Pageable.class));
        verify(orderVoService, times(1)).returnVo(anyList());

    }

    @Test
    public void confirmOrder() throws Exception {
        int orderID = 1;
        doNothing().when(orderService).confirmOrder(orderID);

        mockMvc.perform(post("/passOrder.do")
                        .param("orderID", String.valueOf(orderID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderService, times(1)).confirmOrder(orderID);
    }

    @Test
    public void rejectOrder() throws Exception {
        int orderId = 1;
        doNothing().when(orderService).rejectOrder(orderId);
        mockMvc.perform(post("/rejectOrder.do")
                        .param("orderID", String.valueOf(orderId)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        verify(orderService, times(1)).rejectOrder(orderId);
    }
}