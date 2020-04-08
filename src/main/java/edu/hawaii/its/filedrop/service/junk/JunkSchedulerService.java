package edu.hawaii.its.filedrop.service.junk;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import edu.hawaii.its.filedrop.access.User;
import edu.hawaii.its.filedrop.access.UserContextService;
import edu.hawaii.its.filedrop.service.mail.EmailService;
import edu.hawaii.its.filedrop.service.mail.Mail;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.Recipient;

@Profile(value = { "test", "prod" })
@Service
public class JunkSchedulerService {

    private final Log logger = LogFactory.getLog(getClass());

    @Value("#{'${junk.fund.addresses}'.split(',')}")
    private List<String> fundAddresses;

    @Autowired
    @Qualifier("junkDataSource")
    private DataSource junkDataSource;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserContextService userContextService;

    @Scheduled(cron = "0 40 * * * ?")
    public void junkFileDropReporting() {
        logger.debug("!!!!!! FileDrop Report Job Start !!!!!!");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(junkDataSource);

        for (String fa : fundAddresses) {
            List<FileDrop> filedrops = findDownloadInfo(jdbcTemplate, fa);
            if (filedrops != null && !filedrops.isEmpty()) {
                sendEmailTemplate(fa, filedrops);
            }
        }

        logger.debug("!!!!!! FileDrop Report Job End !!!!!!");
    }

    private void sendEmailTemplate(String recipient, List<FileDrop> filedrops) {
        try {
            Mail mail = new Mail();
            mail.setFrom(emailService.getFrom());
            mail.setTo(recipient);
            mail.setBcc("duckart@hawaii.edu");

            Map<String, Object> contextMap = new HashMap<>();
            contextMap.put("recipient", recipient);
            contextMap.put("filedrops", filedrops);

            emailService.send(mail, "department", new Context(Locale.ENGLISH, contextMap));
        } catch (Exception e) {
            logger.error("Error:", e);
        }
    }

    public User currentUser() {
        return userContextService.getCurrentUser();
    }

    public List<FileDrop> findDownloadInfo(JdbcTemplate jt, String recipient) {
        List<FileDrop> list = null;
        try {
            String sql = "select id, "
                    + "     is_valid, "
                    + "     download_key, "
                    + "     trim(uploader) as uploader, "
                    + "     trim(recipient) as recipient, "
                    + "     created, "
                    + "     valid_until "
                    + "from filedrop "
                    + "where is_valid = 'Y' and recipient like '%" + escape(recipient) + "%' "
                    + "      and created >= NOW() - INTERVAL 61 MINUTE "
                    + "order by created";
            list = jt.query(sql,
                    new RowMapper<FileDrop>() {
                        @Override
                        public FileDrop mapRow(ResultSet rs, int row) throws SQLException {
                            FileDrop fd = new FileDrop();
                            fd.setId(rs.getInt(1));
                            fd.setValid(rs.getBoolean("is_valid"));
                            fd.setUploader(rs.getString("uploader"));
                            fd.setDownloadKey(rs.getString("download_key"));
                            fd.setCreated(toLocalDateTime(rs.getTimestamp("created")));

                            Recipient recipient = new Recipient();
                            recipient.setName(rs.getString("recipient"));
                            List<Recipient> recipients = Arrays.asList(recipient);
                            fd.setRecipients(recipients);

                            return fd;
                        }
                    });
        } catch (Exception e) {
            logger.error("Error:", e);
            list = new ArrayList<>();
        }

        return list;
    }

    public LocalDateTime toLocalDateTime(java.sql.Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        LocalDateTime result = null;
        try {
            Date date = new Date(timestamp.getTime());
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime zoneDateTime = instant.atZone(zoneId);
            result = zoneDateTime.toLocalDateTime();
        } catch (Exception e) {
            // Ignored.
        }

        return result;
    }

    private String escape(String s) {
        return s != null ? s.replace("'", "''") : "";
    }

}
