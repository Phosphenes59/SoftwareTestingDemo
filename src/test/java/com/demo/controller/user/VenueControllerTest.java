package com.demo.controller.user;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenueController.class)
class VenueControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private VenueService venueService;

    Venue venue1,venue2;
    @BeforeEach
    void setUp() {
        // 新建测试venue
        String venue_name = "venue";
        String description = "this is description";
        int price = 100;
        String picture = "";
        String address = "address";
        String open_time = "08:00";
        String close_time = "18:00";
        venue1 = new Venue(1, venue_name, description, price, picture, address, open_time, close_time);
        venue2 = new Venue(2, venue_name, description, price, picture, address, open_time, close_time);
    }


    @Test
    public void toGymPage() throws Exception {
        int venueID = 1;
        when(venueService.findByVenueID(venueID)).thenReturn(venue1);

        mockMvc.perform(get("/venue")
                        .param("venueID", String.valueOf(venueID)))
                .andExpect(status().isOk())
                .andExpect(view().name("venue"))
                .andExpect(model().attribute("venue", venue1));

        verify(venueService, times(1)).findByVenueID(venueID);
    }

    @Test
    public void testGetVenueList() throws Exception {
        int page = 1;
        List<Venue> venueList = Arrays.asList(venue1, venue2);
        Pageable pageable = PageRequest.of(page - 1, 5, Sort.by("venueID").ascending());

        when(venueService.findAll(any())).thenReturn(new PageImpl<>(venueList, pageable, 2));

        mockMvc.perform(get("/venuelist/getVenueList").param("page", String.valueOf(page)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].venueID", is(1)))
                .andExpect(jsonPath("$.content[0].venueName", is("venue")))
                .andExpect(jsonPath("$.content[1].venueID", is(2)))
                .andExpect(jsonPath("$.content[1].venueName", is("venue")));

        verify(venueService, times(1)).findAll(pageable);
    }

    @Test
    public void testVenueList() throws Exception {
        List<Venue> venueList = Arrays.asList(venue1, venue2);
        Pageable venuePageable = PageRequest.of(0, 5, Sort.by("venueID").ascending());
        Page<Venue> venuePage = new PageImpl<>(venueList, venuePageable, 2);

        when(venueService.findAll(venuePageable)).thenReturn(venuePage);

        mockMvc.perform(get("/venue_list"))
                .andExpect(status().isOk())
                .andExpect(view().name("venue_list"))
                .andExpect(model().attribute("venue_list", venueList))
                .andExpect(model().attribute("venue_list", hasSize(2)))
                .andExpect(model().attribute("total", venueService.findAll(venuePageable).getTotalPages()));

        verify(venueService, times(3)).findAll(venuePageable);
    }
}
