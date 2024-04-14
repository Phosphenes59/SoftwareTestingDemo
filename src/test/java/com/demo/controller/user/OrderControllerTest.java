package com.demo.controller.user;

import com.demo.entity.Order;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.entity.vo.OrderVo;
import com.demo.entity.vo.VenueOrder;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import com.demo.service.VenueService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import javax.servlet.http.HttpSession;
import javax.websocket.SessionException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private OrderService orderService;
    @MockBean
    private OrderVoService orderVoService;
    @MockBean
    private VenueService venueService;

    User user;

    Order order;

    Venue venue;

    @BeforeEach
    void setUp() {
        user = new User(1, "test1", "nickname",
                "pwd1", "222@qq.com", "123456",
                0, "picture");
        order = new Order(1, "test1", 1,
                1, LocalDateTime.now(), LocalDateTime.now(),
                1, 100);
        venue = new Venue(1, "venue", "good",
                100, "picture", "address",
                "open_time", "close_time");
    }

    @Test
    public void order_manage_with_login() throws Exception {
        //given
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("user", user);

        Page<Order> page = new PageImpl<>(Collections.singletonList(order));
        when(orderService.findUserOrder(any(), any())).thenReturn(page);

        //when&then
        mockMvc.perform(get("/order_manage").sessionAttrs(sessionAttrs))
                .andExpect(status().isOk())
                .andExpect(view().name("order_manage"))
                .andExpect(model().attribute("total", page.getTotalPages()));

        verify(orderService).findUserOrder(any(), any());
    }

    @Test
    public void order_manage_without_login() {
        NestedServletException thrown =
        Assertions.assertThrows(NestedServletException.class, ()->
                mockMvc.perform(get("/order_manage")));
        assertTrue(Objects.requireNonNull(thrown.getMessage())
                .contains("LoginException"));
    }

    @Test
    public void order_place_do() throws Exception {
        int venueID = 1;

        when(venueService.findByVenueID(venueID)).thenReturn(venue);

        mockMvc.perform(get("/order_place.do")
                        .param("venueID", String.valueOf(venueID)))
                .andExpect(status().isOk())
                .andExpect(view().name("order_place"))
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attribute("venue", venue));

        verify(venueService).findByVenueID(venueID);
    }

    @Test
    public void order_place() throws Exception {
        mockMvc.perform(get("/order_place"))
                .andExpect(status().isOk())
                .andExpect(view().name("order_place"));
    }

    @Test
    public void order_list_with_login() throws Exception {
        //given
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("user", user);

        Page<Order> page1 = new PageImpl<>(Collections.singletonList(order));
        when(orderService.findUserOrder(any(), any())).thenReturn(page1);

        List<OrderVo> orderVos = new ArrayList<>();
        OrderVo orderVo = new OrderVo(order.getOrderID(), order.getUserID(), order.getVenueID(), venue.getVenueName(),
                order.getState(), order.getOrderTime(), order.getStartTime(), order.getHours(), order.getTotal());
        orderVos.add(orderVo);

        when(orderVoService.returnVo(any())).thenReturn(orderVos);

        int page = 1;
        Pageable pageable = PageRequest.of(page - 1, 5, Sort.by("orderTime").descending());

        //when&then
        mockMvc.perform(get("/getOrderList.do")
                        .param("page", String.valueOf(page))
                        .sessionAttrs(sessionAttrs))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", Matchers.is(1)))
                .andExpect(jsonPath("$[0].orderID", Matchers.is(order.getOrderID())));


        verify(orderService).findUserOrder(user.getUserID(), pageable);
        verify(orderVoService).returnVo(page1.getContent());
    }

    @Test
    public void order_list_without_login() {
        NestedServletException thrown =
                Assertions.assertThrows(NestedServletException.class, ()->
                        mockMvc.perform(get("/getOrderList.do")));
        assertTrue(Objects.requireNonNull(thrown.getMessage())
                .contains("LoginException"));
    }

    @Test
    public void add_order_with_login() throws Exception {
        //given
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("user", user);

        String venueName = "venue1";
        String date = "2023-04-14";
        String startTime = "10:00";
        int hours = 2;

        doNothing().when(orderService).submit(eq(venueName), any(LocalDateTime.class), eq(hours), eq(user.getUserID()));

        mockMvc.perform(post("/addOrder.do")
                        .param("venueName", venueName)
                        .param("date", date)
                        .param("startTime", startTime)
                        .param("hours", String.valueOf(hours))
                        .sessionAttrs(sessionAttrs))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));
        verify(orderService, times(1)).submit(eq(venueName), any(LocalDateTime.class), eq(hours), eq(user.getUserID()));
    }

    @Test
    public void add_order_without_login() {
        NestedServletException thrown =
                Assertions.assertThrows(NestedServletException.class, ()->
                        mockMvc.perform(post("/addOrder.do")
                                .param("venueName", "venue1")
                                .param("date", "2023-04-14")
                                .param("startTime", "10:00")
                                .param("hours", "2"))
                );

        assertTrue(Objects.requireNonNull(thrown.getMessage())
                .contains("LoginException"));
    }


    @Test
    public void finishOrder() throws Exception {
        int orderID = 1;
        doNothing().when(orderService).finishOrder(orderID);

        mockMvc.perform(post("/finishOrder.do")
                        .param("orderID", String.valueOf(orderID)))
                .andExpect(status().isOk());

        verify(orderService).finishOrder(orderID);
    }

    @Test
    public void testEditOrder() throws Exception {
        // Prepare test data
        int orderID = 1;

        when(orderService.findById(orderID)).thenReturn(order);
        when(venueService.findByVenueID(order.getVenueID())).thenReturn(venue);

        // Perform the request
        mockMvc.perform(get("/modifyOrder.do")
                        .param("orderID", String.valueOf(orderID)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("order", order))
                .andExpect(model().attribute("venue", venue))
                .andExpect(view().name("order_edit"));

        // Verify that the orderService.findById() and venueService.findByVenueID() methods were called with the correct parameters
        verify(orderService).findById(orderID);
        verify(venueService).findByVenueID(order.getVenueID());
    }

    @Test
    public void modify_order_with_login() throws Exception {
        //given
        String venueName = "venue1";
        String date = "2023-04-14";
        String startTime = "10:00";
        int hours = 2;
        int orderID = 1;

        //simulate the logged-in user session
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("user", user);

        //mock the order service
        doNothing().when(orderService).updateOrder(eq(orderID), eq(venueName), any(LocalDateTime.class), eq(hours), eq(user.getUserID()));

        //perform the request and assert the response
        mockMvc.perform(post("/modifyOrder")
                        .param("venueName", venueName)
                        .param("date", date)
                        .param("startTime", startTime)
                        .param("hours", String.valueOf(hours))
                        .param("orderID", String.valueOf(orderID))
                        .sessionAttrs(sessionAttrs))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"))
                .andExpect(content().string("true"));

        //verify the order service is called with the correct arguments
        LocalDateTime ldt = LocalDateTime.of(2023, 4, 14, 10, 0);
        verify(orderService, times(1)).updateOrder(eq(orderID), eq(venueName), eq(ldt), eq(hours), eq(user.getUserID()));
    }

    @Test
    public void modify_order_without_login() {
        NestedServletException thrown =
                Assertions.assertThrows(NestedServletException.class, ()->
                        mockMvc.perform(post("/modifyOrder")
                                .param("venueName", "venue1")
                                .param("date", "2023-04-14")
                                .param("startTime", "10:00")
                                .param("hours", "2")
                                .param("orderID", String.valueOf(1)))
                );

        assertTrue(Objects.requireNonNull(thrown.getMessage())
                .contains("LoginException"));
    }

    @Test
    public void testDelOrder() throws Exception {
        int orderID = 123;

        doNothing().when(orderService).delOrder(orderID);
        mockMvc.perform(post("/delOrder.do")
                        .param("orderID", String.valueOf(orderID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderService, times(1)).delOrder(orderID);
    }

    @Test
    public void testGetOrder() throws Exception {
        // given
        LocalDateTime ldt = LocalDateTime.of(2023, 4, 14, 0, 0, 0);
        LocalDateTime ldt2 = ldt.plusDays(1);
        List<Order> orders = new ArrayList<>();
        orders.add(new Order());
        orders.add(new Order());
        orders.add(new Order());
        VenueOrder expectedVenueOrder = new VenueOrder();
        expectedVenueOrder.setVenue(venue);
        expectedVenueOrder.setOrders(orders);

        when(venueService.findByVenueName("venue")).thenReturn(venue);
        when(orderService.findDateOrder(venue.getVenueID(), ldt, ldt2)).thenReturn(orders);

        // when and then
        mockMvc.perform(get("/order/getOrderList.do")
                        .param("venueName", "venue")
                        .param("date", "2023-04-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venue.venueID", Matchers.is(1)))
                .andExpect(jsonPath("$.venue.venueName", Matchers.is("venue")))
                .andExpect(jsonPath("$.orders.length()", Matchers.is(3)));

        verify(venueService).findByVenueName("venue");
        verify(orderService).findDateOrder(venue.getVenueID(),ldt,ldt2);
    }
}