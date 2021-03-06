package com.inmobi.adserve.channels.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Created by ishanbhatnagar on 19/1/15.
 */
public class LoggerUtils {
    private static final Logger LOG = LoggerFactory.getLogger(LoggerUtils.class);

    public static void configureApplicationLoggers(Configuration loggerConfiguration) {
        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();

        try {
            configurator.doConfigure(loggerConfiguration.getString("loggerConf"));
        } catch (final JoranException e) {
            throw new RuntimeException(e);
        }
    }

    // Send eMail if channel server crashes
    @SuppressWarnings("unchecked")
    public static void sendMail(final String errorMessage, final String stackTrace,
                                 Configuration serverConfiguration) {
        final Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", serverConfiguration.getString("smtpServer"));
        final Session session = Session.getDefaultInstance(properties);
        try {
            final MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(serverConfiguration.getString("sender")));
            final List<String> recipients = serverConfiguration.getList("recipients");
            final javax.mail.internet.InternetAddress[] addressTo =
                    new javax.mail.internet.InternetAddress[recipients.size()];

            for (int index = 0; index < recipients.size(); index++) {
                addressTo[index] = new javax.mail.internet.InternetAddress(recipients.get(index));
            }

            message.setRecipients(Message.RecipientType.TO, addressTo);
            final InetAddress addr = InetAddress.getLocalHost();
            message.setSubject("Channel Ad Server Crashed on Host " + addr.getHostName());
            message.setText(errorMessage + stackTrace);
            Transport.send(message);
        } catch (final MessagingException mex) {
            if (null != LOG) {
                LOG.error("MessagingException raised while sending mail " + mex);
            } else {
                System.out.println("MessagingException raised while sending mail " + mex);
            }
        } catch (final UnknownHostException ex) {
            if (null != LOG) {
                LOG.error("UnknownException raised while sending mail " + ex);
            } else {
                System.out.println("UnknownException raised while sending mail " + ex);
            }
        }
    }

}
