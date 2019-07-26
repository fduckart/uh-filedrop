package edu.hawaii.its.filedrop.repository;

import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.service.HolidayService;
import edu.hawaii.its.filedrop.type.Holiday;
import edu.hawaii.its.filedrop.type.Type;
import edu.hawaii.its.filedrop.util.Dates;
import edu.hawaii.its.filedrop.util.Strings;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class HolidayRepositoryTest {

    private static final Log logger = LogFactory.getLog(HolidayRepositoryTest.class);

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private HolidayService holidayService;

    @Test
    public void findById() {
        Holiday h = holidayRepository.findById(115).get();
        assertThat(h.getDescription(), equalTo("Christmas"));
        assertThat(h.getHolidayTypes().size(), equalTo(3));
        LocalDate localDate = Dates.newLocalDate(2018, Month.DECEMBER, 25);
        Date date = Dates.toDate(localDate);
        assertThat(h.getObservedDate(), equalTo(date));
        assertThat(h.getOfficialDate(), equalTo(date));
        assertThat(h.getHolidayTypes().size(), equalTo(3));
    }

    // Not really a test, but used to learn some features.
    @Test
    public void findAllPaged() {
        int size = 14;
        Sort sort = Sort.by(new Sort.Order(Direction.ASC, "observedDate"));
        Pageable pageable = PageRequest.of(0, size, sort);
        Page<Holiday> page = holidayRepository.findAll(pageable);
        int pages = page.getTotalPages();

        logger.debug(">>> total-pages: " + pages);
        logger.debug(">>>        sort: " + page.getSort());

        logger.debug(Strings.fill('v', 98));
        logger.debug("++++++++++++++++++++++++++++++++++++++");

        for (int i = 0; i < pages; i++) {
            if (page.hasContent()) {
                List<Holiday> list = page.getContent();
                for (Holiday h : list) {
                    logger.debug("  " + h);
                }
                logger.debug("  ---------------------------------------");
            }
            page = holidayRepository.findAll(page.nextPageable());
        }

        logger.debug(Strings.fill('^', 98));
    }

    @Test
    public void save() {
        Holiday h = new Holiday();

        LocalDate localDate = Dates.newLocalDate(2030, Month.DECEMBER, 25);
        Date date = Dates.toDate(localDate);

        h.setOfficialDate(date);
        h.setObservedDate(date);
        h.setDescription("Christmas");
        assertNull(h.getId());

        h = holidayRepository.save(h);

        assertNotNull(h.getId());
        Holiday h0 = holidayRepository.findById(h.getId()).get();
        assertEquals(h0, h);
        h = null;
        h0 = null;

        localDate = Dates.firstOfNextMonth(localDate);
        date = Dates.toDate(localDate);
        List<Type> holidayTypes = holidayService.findTypes();

        Holiday h1 = new Holiday();
        h1.setDescription("New Year's Day, Woot!");
        h1.setObservedDate(date);
        h1.setOfficialDate(date);
        h1.setHolidayTypes(holidayTypes);

        h1 = holidayRepository.save(h1);

        Holiday h2 = holidayRepository.findById(h1.getId()).get();
        assertEquals(h1, h2);
        assertThat(h2.getDescription(), equalTo("New Year's Day, Woot!"));
    }
}
