package com.demo.service.impl;

import com.demo.dao.VenueDao;
import com.demo.entity.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {
    @Mock
    private VenueDao venueDao;

    private Venue venue1,venue2;

    @InjectMocks
    private VenueServiceImpl venueService;

    @BeforeEach
    void setUp(){
        venue1 = new Venue(1, "场馆1", "羽毛球馆", 200, "picture", "address1", "9:00", "20:00");
        venue2 = new Venue(2, "场馆2", "游泳馆", 200, "picture", "address1", "9:00", "20:00");
    }

    @Test
    void findByVenueID() {
        // Scenario 1: ID exists
        given(venueDao.getOne(1)).willReturn(venue1);
        Venue actualVenue1 = venueService.findByVenueID(1);
        assertEquals(venue1, actualVenue1);

        // Scenario 2: ID not exists
        given(venueDao.getOne(3)).willReturn(null);
        Venue actualVenue3 = venueService.findByVenueID(3);
        assertNull(actualVenue3);
    }

    @Test
    void findByVenueName() {
        // Scenario 1: Venue name exists
        String venueName1 = "场馆1";
        given(venueDao.findByVenueName(venueName1)).willReturn(venue1);
        Venue actualVenue1 = venueService.findByVenueName(venueName1);
        verify(venueDao, times(1)).findByVenueName(venueName1);
        assertEquals(venue1, actualVenue1);

        // Scenario 2: Venue name does not exist
        String venueName3 = "场馆3";
        given(venueDao.findByVenueName(venueName3)).willReturn(null);
        Venue actualVenue3 = venueService.findByVenueName(venueName3);
        verify(venueDao, times(1)).findByVenueName(venueName3);
        assertNull(actualVenue3);

        // Scenario 3: Venue name is null
        given(venueDao.findByVenueName(null)).willReturn(null);
        Venue actualVenueNull = venueService.findByVenueName(null);
        verify(venueDao, times(1)).findByVenueName(null);
        assertNull(actualVenueNull);
    }

    @Test
    void testFindAll() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        when(venueDao.findAll(pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(new Venue(3,"venue3","羽毛球馆", 200, "picture", "address1", "9:00", "20:00")), pageable, 1))
                .thenReturn(new PageImpl<>(Collections.singletonList(new Venue(4,"venue4","羽毛球馆", 200, "picture", "address1", "9:00", "20:00")), pageable, 1))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        Page<Venue> result1 = venueService.findAll(pageable);
        assertEquals(1, result1.getNumberOfElements());
        assertEquals("venue3", result1.getContent().get(0).getVenueName());

        Page<Venue> result2 = venueService.findAll(pageable);
        assertEquals(1, result2.getNumberOfElements());
        assertEquals("venue4", result2.getContent().get(0).getVenueName());

        Page<Venue> result3 = venueService.findAll(pageable);
        assertEquals(0, result3.getNumberOfElements());
        assertEquals(Collections.emptyList(), result3.getContent());

        verify(venueDao, times(3)).findAll(pageable);
    }

    @Test
    void findAllList() {
        // Scenario 1: List contains data
        List<Venue> venuesWithData = Arrays.asList(venue1, venue2);
        when(venueDao.findAll()).thenReturn(venuesWithData);

        // Call the method under test
        List<Venue> actualVenuesWithData = venueService.findAll();
        verify(venueDao, times(1)).findAll();

        // Assert the expected results
        assertEquals(2, actualVenuesWithData.size());
        assertEquals(venue1, actualVenuesWithData.get(0));
        assertEquals(venue2, actualVenuesWithData.get(1));

        // Scenario 2: List is empty
        List<Venue> venuesEmpty = Collections.emptyList();
        when(venueDao.findAll()).thenReturn(venuesEmpty);

        // Call the method under test
        List<Venue> actualVenuesEmpty = venueService.findAll();
        verify(venueDao, times(2)).findAll();

        // Assert the expected results
        assertEquals(0, actualVenuesEmpty.size());
    }

    @Test
    void create() {
        //given
        Venue create_venue = venue1;
        when(venueDao.save(create_venue)).thenReturn(create_venue);

        //when
        int create_venue_id = venueService.create(create_venue);

        //then
        verify(venueDao, times(1)).save(create_venue);
        assertEquals(venue1.getVenueID(), create_venue_id);
    }

    @Test
    void update() {
        // Prepare the test data
        Venue venueToUpdate = venue1;

        // Call the method under test
        venueService.update(venueToUpdate);

        // Verify the interaction with the mocked VenueDao
        verify(venueDao, times(1)).save(venueToUpdate);
    }

    @Test
    void delById() {
        // Prepare the test data
        int venueIdToDelete = venue1.getVenueID();

        // Call the method under test
        venueService.delById(venueIdToDelete);

        // Verify the interaction with the mocked VenueDao
        verify(venueDao, times(1)).deleteById(venueIdToDelete);
    }

    @Test
    void countVenueName() {
        String exist_venue_name = "Venue1";
        String not_exist_venue_name = "Venue3";

        // Given
        when(venueDao.countByVenueName(exist_venue_name)).thenReturn(1);
        when(venueDao.countByVenueName(not_exist_venue_name)).thenReturn(0);

        // When
        int result1 = venueService.countVenueName(exist_venue_name);
        int result2 = venueService.countVenueName(not_exist_venue_name);

        // Then
        assertEquals(1, result1);
        assertEquals(0, result2);
    }
}

