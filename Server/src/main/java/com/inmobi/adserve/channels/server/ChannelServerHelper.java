package com.inmobi.adserve.channels.server;

import com.inmobi.adserve.channels.server.api.ConnectionType;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;


public class ChannelServerHelper {
    private final Logger logger;
    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(ChannelServerHelper.class);
    public ChannelServerHelper(final Logger serverLogger) {
        logger = serverLogger;
    }

    public byte getDataCenterId(final String dataCenterIdKey) {
        byte dataCenterIdCode;
        try {
            dataCenterIdCode = Byte.parseByte(System.getProperty(dataCenterIdKey));
        } catch (NumberFormatException e) {
            logger.info("NumberFormatException in getDataCenterId");
            dataCenterIdCode = 0;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("dataCenterId is " + dataCenterIdCode);
        }
        return dataCenterIdCode;
    }

    public short getHostId(final String hostNameKey) {
        short hostId = 0;
        String hostName = System.getProperty(hostNameKey);
        if (hostName == null) {
            InetAddress addr = null;
            try {
                addr = InetAddress.getLocalHost();
                hostName = addr.getHostName();
            } catch (UnknownHostException e1) {
                LOG.info("UnknownHostException in getHostId, exception raised {}", e1);
            }
        }
        try {
            if (null != hostName) {
                hostId = Short.parseShort(hostName.substring(3, 7));
            } else {
                return hostId;
            }
        } catch (NumberFormatException e) {
            LOG.info("NumberFormatException in getHostId, exception raised {}", e);
        } catch (StringIndexOutOfBoundsException e) {
            LOG.info("StringIndexOutOfRangeException in getHostId, exception raised {}", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("hostid is " + hostId);
        }
        return hostId;
    }

    public String getDataCentreName(final String key) {
        return System.getProperty(key);
    }

    public Integer getMaxConnections(String connectionsKey, ConnectionType connectionType) {
        Integer maxConnections = null;
        try {
            maxConnections = Integer.parseInt(System.getProperty(connectionsKey));
        } catch (NumberFormatException e) {
            logger.info("NumberFormatException " + connectionType.toString() + "maxConnections");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Max limit for " +  connectionType.toString() + " connections is " + maxConnections);
        }
        return maxConnections;
    }
}
