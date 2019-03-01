package com.cscglobal.sre.SonicMQ.SonicStatsMonitor;

import com.sonicsw.mf.jmx.client.JMSConnectorClient;
import com.sonicsw.mq.common.runtime.IDurableSubscriptionData;
import com.sonicsw.mq.common.runtime.IQueueData;
import com.sonicsw.mq.mgmtapi.runtime.IBrokerProxy;
import com.sonicsw.mq.mgmtapi.runtime.MQProxyFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.management.ObjectName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Referenced classes of package com.cscglobal.sre.SonicMQ.SonicStatsMonitor:
//            Common, BrokerConnectionDetails

public class CollectBrokerStats
{

    public CollectBrokerStats()
    {
    }

    public static void main(String args[])
    {
        BrokerConnectionDetails bcd;
        IBrokerProxy broker;
        DateFormat dfStart = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        logger.info((new StringBuilder("CollectBrokerStats Utility Starting at ")).append(dfStart.format(new Date())).toString());
        bcd = Common.getBrokerConnection();
        broker = null;
        try
        {
            ObjectName jmxName = new ObjectName(bcd.getJmxBeanName());
            broker = MQProxyFactory.createBrokerProxy(bcd.getJcc(), jmxName);
        }
        catch(Exception e1)
        {
            logger.fatal("COLLECTBROKERSTATS IS UNABLE TO OBTAIN A BROKER PROXY - EXITING!!");
            logger.fatal("Stack Trace:", e1);
            System.exit(1);
        }
        logger.debug("Connected and Broker Proxy obtained...");
        try
        {
            logger.debug(" Retrieving Users with Durable Subscriptions");
            ArrayList userSubscrList = broker.getUsersWithDurableSubscriptions(null);
            logger.debug((new StringBuilder(String.valueOf(userSubscrList.size()))).append(" Users with Durable Subscriptions found").toString());
            for(Iterator iterator = userSubscrList.iterator(); iterator.hasNext();)
            {
                String durableSubscriber = (String)iterator.next();
                ArrayList subscrList = broker.getDurableSubscriptions(durableSubscriber);
                logger.debug((new StringBuilder(String.valueOf(subscrList.size()))).append(" Subscriptions retrieved for User ").append(durableSubscriber).toString());
                logger.debug((new StringBuilder("\nThere are ")).append(subscrList.size()).append(" durable subscriptions for User: ").append(durableSubscriber).toString());
                for(Iterator iterator2 = subscrList.iterator(); iterator2.hasNext();)
                {
                    IDurableSubscriptionData subdata = (IDurableSubscriptionData)iterator2.next();
                    if(!subdata.getUser().equals("Administrator") || !subdata.getTopicName().contains("SonicMQ."))
                    {
                        int daysSinceLastConnect = 0;
                        String dateStr = null;
                        if(subdata.getLastConnectedTime() == -1L)
                        {
                            dateStr = "ACTIVE";
                            daysSinceLastConnect = 0;
                        } else
                        {
                            Date theDate = new Date(subdata.getLastConnectedTime());
                            DateFormat dfSub = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
                            dateStr = dfSub.format(theDate);
                            Date now = new Date();
                            long diff = now.getTime() - theDate.getTime();
                            daysSinceLastConnect = (int)TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                        }
                        logger.info((new StringBuilder("\tUserID = ")).append(subdata.getUser()).append(" Topic = ").append(subdata.getTopicName()).append(" SubscriptionName = ").append(subdata.getSubscriptionName()).append(" ClientID = ").append(subdata.getClientID()).append(" SubscriptionMessageCount = ").append(subdata.getMessageCount()).append(" TotalSubscriptionSize = ").append(subdata.getMessageSize()).append(" LastConnectedAt = ").append(dateStr).append(" DaysSinceLastConnect = ").append(daysSinceLastConnect).toString());
                    }
                }

            }

            logger.debug("Querying queues");
            ArrayList qlist = broker.getQueues(null);
            IQueueData qItem;
            for(Iterator iterator1 = qlist.iterator(); iterator1.hasNext(); logger.info((new StringBuilder("QueueName = ")).append(qItem.getQueueName()).append(" QueueMessageCount = ").append(qItem.getMessageCount()).append(" QueueSize = ").append(qItem.getTotalMessageSize()).toString()))
                qItem = (IQueueData)iterator1.next();

            break MISSING_BLOCK_LABEL_840;
        }
        catch(Throwable t)
        {
            logger.fatal("FATAL ERROR DURING EXECUTION - STACK TRACE FOLLOWS - EXITING!");
            logger.fatal("Stack Trace:", t);
        }
        if(bcd.getJcc() != null)
            bcd.getJcc().disconnect();
        DateFormat dfEnd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        logger.info((new StringBuilder("CollectBrokerStats Utility Ending at ")).append(dfEnd.format(new Date())).toString());
        break MISSING_BLOCK_LABEL_901;
        Exception exception;
        exception;
        if(bcd.getJcc() != null)
            bcd.getJcc().disconnect();
        DateFormat dfEnd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        logger.info((new StringBuilder("CollectBrokerStats Utility Ending at ")).append(dfEnd.format(new Date())).toString());
        throw exception;
        if(bcd.getJcc() != null)
            bcd.getJcc().disconnect();
        DateFormat dfEnd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        logger.info((new StringBuilder("CollectBrokerStats Utility Ending at ")).append(dfEnd.format(new Date())).toString());
    }

    private static Logger logger = LogManager.getLogger(com/cscglobal/sre/SonicMQ/SonicStatsMonitor/CollectBrokerStats);

}