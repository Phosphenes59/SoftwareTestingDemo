package com.demo.controller.admin;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AdminVenueController.class)
class AdminVenueControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private VenueService venueService;

    Venue venue;
    @BeforeEach
    void setUp() {
        // 新建测试venue
        int venueID = 1;
        String venue_name = "venue";
        String description = "this is description";
        int price = 100;
        String picture = "";
        String address = "address";
        String open_time = "08:00";
        String close_time = "18:00";
        venue = new Venue(venueID, venue_name, description, price, picture, address, open_time, close_time);
    }

    @Test
    public void venue_manage() throws Exception {
        // 新建测试venue_list (仅含一个测试venue)
        List<Venue> venues = new ArrayList<>();
        venues.add(venue);

        // 添加测试pageable
        Pageable pageable = PageRequest.of(0, 10, Sort.by("venueID").ascending());

        // given
        when(venueService.findAll(any())).thenReturn(new PageImpl<>(venues, pageable, 1));

        // when&then
        mockMvc.perform(get("/venue_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venue_manage"))
                .andExpect(model().attribute("total", 1));

        verify(venueService).findAll(any());
    }

    @Test
    public void editVenue() throws Exception {

        int venueID = 1;

        // given
        when(venueService.findByVenueID(venueID)).thenReturn(venue);

        // when & then
        mockMvc.perform(get("/venue_edit").param("venueID", String.valueOf(venueID)))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/venue_edit"))
                .andExpect(model().attribute("venue", venue));

        // verify
        verify(venueService).findByVenueID(venueID);
    }

    @Test
    public void venue_add() throws Exception{
        mockMvc.perform(get("/venue_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/venue_add"));
    }

    @Test
    public void getVenueList() throws Exception{
        // 设置模拟分页请求参数
        int page = 1;

        // 构造分页结果
        List<Venue> venues = new ArrayList<>();
        venues.add(venue);
        Page<Venue> pageResult = new PageImpl<>(venues);

        // 模拟venueService返回结果
        when(venueService.findAll(any())).thenReturn(pageResult);

        // 发起分页请求
        mockMvc.perform(get("/venueList.do"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].venueID").value(venue.getVenueID()))
                .andExpect(jsonPath("$.[0].venueName").value(venue.getVenueName()));

        // 验证venueService是否被调用
        verify(venueService).findAll(any());
    }

    @Test
    void addVenue_with_picture() throws Exception {
        // given
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(venue.getVenueID());

        // when & then
        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", venue.getVenueName())
                        .param("address", venue.getAddress())
                        .param("description", venue.getDescription())
                        .param("price", String.valueOf(venue.getPrice()))
                        .param("open_time", venue.getOpen_time())
                        .param("close_time", venue.getClose_time()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).create(any(Venue.class));
    }

    @Test
    void addVenue_without_picture() throws Exception {
        // given
        MockMultipartFile picture = new MockMultipartFile("picture", "", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(venue.getVenueID());

        // when & then
        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", venue.getVenueName())
                        .param("address", venue.getAddress())
                        .param("description", venue.getDescription())
                        .param("price", String.valueOf(venue.getPrice()))
                        .param("open_time", venue.getOpen_time())
                        .param("close_time", venue.getClose_time()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_manage"));

        verify(venueService, times(1)).create(any(Venue.class));
    }

    @Test
    void addVenue_failed() throws Exception {
        // given
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", "test image".getBytes());

        when(venueService.create(any(Venue.class))).thenReturn(-1);

        // when & then
        mockMvc.perform(multipart("/addVenue.do")
                        .file(picture)
                        .param("venueName", venue.getVenueName())
                        .param("address", venue.getAddress())
                        .param("description", venue.getDescription())
                        .param("price", String.valueOf(venue.getPrice()))
                        .param("open_time", venue.getOpen_time())
                        .param("close_time", venue.getClose_time()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_add"));

        verify(venueService, times(1)).create(any(Venue.class));
    }


    @Test
    public void testModifyVenue() throws Exception {
        String newVenueName = "new_venue";
        String picture = venue.getPicture();
        MockMultipartFile file = new MockMultipartFile("picture", "test.jpg", "text/plain", "test data".getBytes());

        when(venueService.findByVenueID(venue.getVenueID())).thenReturn(venue);
        doNothing().when(venueService).update(any());

        //when&then
        mockMvc.perform(multipart("/modifyVenue.do")
                        .file(file)
                        .param("venueID", String.valueOf(venue.getVenueID()))
                        .param("venueName", newVenueName)
                        .param("address", venue.getAddress())
                        .param("description", venue.getDescription())
                        .param("price", String.valueOf(venue.getPrice()))
                        .param("open_time", venue.getOpen_time())
                        .param("close_time", venue.getClose_time()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("venue_manage"));

        ArgumentCaptor<Venue> captor = ArgumentCaptor.forClass(Venue.class);
        verify(venueService).update(captor.capture());
        Venue capturedVenue = captor.getValue();
        assertEquals(newVenueName, capturedVenue.getVenueName());
        verify(venueService).findByVenueID(venue.getVenueID());
        verify(venueService).update(any());
    }

    @Test
    public void delVenue() throws Exception {
        mockMvc.perform(post("/delVenue.do")
                        .param("venueID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testCheckVenueName() throws Exception {
        //given
        String existVenueName = "existVenueName";
        String notExistVenueName = "notExistVenueName";
        when(venueService.countVenueName(existVenueName)).thenReturn(1);
        when(venueService.countVenueName(notExistVenueName)).thenReturn(0);

        //when&then
        mockMvc.perform(post("/checkVenueName.do")
                        .param("venueName", existVenueName))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
        mockMvc.perform(post("/checkVenueName.do")
                        .param("venueName", notExistVenueName))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(venueService, times(1)).countVenueName(existVenueName);
        verify(venueService, times(1)).countVenueName(notExistVenueName);


    }
}

