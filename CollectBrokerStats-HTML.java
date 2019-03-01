package com.sarav.sonicmq;

import com.cscglobal.sre.SonicMQ.SonicStatsMonitor.BrokerConnectionDetails;
import com.cscglobal.sre.SonicMQ.SonicStatsMonitor.Common;
import com.sonicsw.mq.common.runtime.IDurableSubscriptionData;
import com.sonicsw.mq.common.runtime.impl.ConnectionData;
import com.sonicsw.mq.common.runtime.IQueueData;
import com.sonicsw.mq.mgmtapi.runtime.IBrokerProxy;
import com.sonicsw.mq.mgmtapi.runtime.MQProxyFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import javax.management.ObjectName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;
import com.sonicsw.mq.common.runtime.IConnectionMemberDetails;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CollectBrokerStatsV2 {

   private static Logger logger = LogManager.getLogger(CollectBrokerStats.class);


   public static void main(String[] args) throws IOException {
      Hashtable <String, String> topichash = new Hashtable<String, String>();
      Hashtable <String, String> Queuehash = new Hashtable<String, String>();
      Hashtable <String, String> Conhash = new Hashtable<String, String>();
      SimpleDateFormat dfStart = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
      logger.info("CollectBrokerStats Utility Starting at " + dfStart.format(new Date()));
      BrokerConnectionDetails bcd = Common.getBrokerConnection();
      IBrokerProxy broker = null;

      String filename="SonicStats.html";
      BufferedWriter writer = new BufferedWriter(new FileWriter(filename));


      try {
         ObjectName t = new ObjectName(bcd.getJmxBeanName());
         broker = MQProxyFactory.createBrokerProxy(bcd.getJcc(), t);
      } catch (Exception var22) {
         logger.fatal("COLLECTBROKERSTATS IS UNABLE TO OBTAIN A BROKER PROXY - EXITING!!");
         logger.fatal("Stack Trace:", var22);
         System.exit(1);
      }

      logger.debug("Connected and Broker Proxy obtained...");

      try {
         logger.debug(" Retrieving Users with Durable Subscriptions");
         ArrayList t1 = broker.getUsersWithDurableSubscriptions((String)null);
         logger.debug(t1.size() + " Users with Durable Subscriptions found");
         Iterator qItem = t1.iterator();


         while(qItem.hasNext()) {
            String qlist = (String)qItem.next();
            ArrayList subscrList = broker.getDurableSubscriptions(qlist);
            logger.debug(subscrList.size() + " Subscriptions retrieved for User " + qlist);
            logger.debug("\nThere are " + subscrList.size() + " durable subscriptions for User: " + qlist);
            Iterator var9 = subscrList.iterator();
            int j=0;
            while(var9.hasNext()) {
               IDurableSubscriptionData subdata = (IDurableSubscriptionData)var9.next();
               if(!subdata.getUser().equals("Administrator") || !subdata.getTopicName().contains("SonicMQ.")) {
                  boolean daysSinceLastConnect = false;
                  String dateStr = null;
                  int daysSinceLastConnect1;
                  if(subdata.getLastConnectedTime() == -1L) {
                     dateStr = "ACTIVE";
                     daysSinceLastConnect1 = 0;
                  } else {
                     Date theDate = new Date(subdata.getLastConnectedTime());
                     SimpleDateFormat dfSub = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
                     dateStr = dfSub.format(theDate);
                     Date now = new Date();
                     long diff = now.getTime() - theDate.getTime();
                     daysSinceLastConnect1 = (int)TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                  }

                  logger.info("\tUserID = " + subdata.getUser() + " Topic = " + subdata.getTopicName() + " SubscriptionName = " + subdata.getSubscriptionName() + " ClientID = " + subdata.getClientID() + " SubscriptionMessageCount = " + subdata.getMessageCount() + " TotalSubscriptionSize = " + subdata.getMessageSize() + " LastConnectedAt = " + dateStr + " DaysSinceLastConnect = " + daysSinceLastConnect1);
                  topichash.put("UserID"+j,subdata.getUser());
                  topichash.put("Topic"+j,subdata.getTopicName());
                  topichash.put("SubscriptionName"+j,subdata.getSubscriptionName());
                  topichash.put("ClientID"+j,subdata.getClientID());
                  topichash.put("SubscriptionMessageCount"+j,Long.toString(subdata.getMessageCount()));
                  topichash.put("TotalSubscriptionSize"+j,Long.toString(subdata.getMessageSize()));
                  topichash.put("LastConnectedAt"+j,dateStr);
                  topichash.put("DaysSinceLastConnect"+j,Integer.toString(daysSinceLastConnect1));
                  /*("<tr><td>UserID</td><td>" + subdata.getUser() + "</td></tr> <tr><td>Topic</td><td>" + subdata.getTopicName() + "</td></tr> <tr><td>SubscriptionName</td><td>" + subdata.getSubscriptionName() + "</td></tr> <tr><td>ClientID</td><td>" + subdata.getClientID() + "</td></tr> <tr><td>TotalSubscriptionSize</td><td>" + subdata.getMessageSize() + "</td></tr> <tr><td>LastConnectedAt</td><td>" + dateStr + "</td></tr> <tr><td>DaysSinceLastConnect</td><td>" + daysSinceLastConnect1 + "</td></tr>");*/
          j++;
       }
    }
 }

 logger.debug("Querying queues");
 ArrayList qlist1 = broker.getQueues((String)null);
 Iterator subscrList1 = qlist1.iterator();
 int i=0;
 while(subscrList1.hasNext()) {
    IQueueData qItem1 = (IQueueData)subscrList1.next();
    Queuehash.put("QueueName"+i,qItem1.getQueueName());
    Queuehash.put("QueueMessageCount"+i,Long.toString(qItem1.getMessageCount()));
    Queuehash.put("QueueSize"+i,Long.toString(qItem1.getTotalMessageSize()));
    logger.info("QueueName = " + qItem1.getQueueName() + " QueueMessageCount = " + qItem1.getMessageCount() + " QueueSize = " + qItem1.getTotalMessageSize());
        i++;
 }





  try {
 String Prefix=null;
 ArrayList Connections=new ArrayList();
 Connections=broker.getConnections(Prefix);


 Iterator ConItem = Connections.iterator();
 i=0;
 while(ConItem.hasNext())
 {
        ConnectionData cdata=(ConnectionData) ConItem.next();
        /*writer.write("User"+cdata.getUser());
        writer.write("Identity"+cdata.getConnectID());
        writer.write("Host"+cdata.getHost());
        writer.write("ApplicationConnection"+String.valueOf(cdata.isApplicationConnection()));
        writer.write("Broker"+cdata.getBroker());
        writer.write("ConnectionMember"+cdata.getConnectionMemberRef());*/
        IConnectionMemberDetails Memberdetails = broker.getConnectionMemberDetails(cdata.getConnectionMemberRef());
        Map condata = Memberdetails.getProperties();
        Conhash.put("User"+i,String.valueOf(condata.get("User")));
        Conhash.put("Host"+i,cdata.getHost());
        Conhash.put("State"+i,String.valueOf(Memberdetails.getState()));
        Conhash.put("ConnectionType"+i,String.valueOf(condata.get("ConnectionType")));
        Conhash.put("AcceptorURL"+i,String.valueOf(condata.get("AcceptorUrl")));
        Conhash.put("RemoteSocket"+i,String.valueOf(condata.get("RemoteSocket")));
        Conhash.put("FaultTolerant"+i,String.valueOf(condata.get("FaultTolerant")));
        Conhash.put("SessionCount"+i,String.valueOf(condata.get("SessionCount")));
        Conhash.put("ConsumerCount"+i,String.valueOf(condata.get("ConsumerCount")));
        Conhash.put("Version"+i,String.valueOf(condata.get("Version")));
        Conhash.put("ConnectedTime"+i,String.valueOf(Memberdetails.getTimeConnected()));
        i++;
 }



      } catch (Exception var92) {
         System.out.println("FAILURE WHILE RETREIVING CONNECTIONS");
         System.out.println("Stack Trace:"+ var92);
         System.exit(1);
      }


 /*
 writer.write("GetConnectionsTree"+(String) broker.getConnectionTree()+"\n\n");
 writer.write("getConnectionMemberDetails"+(String) broker.getConnectionMemberDetails()+"\n\n");
 */


 System.out.println ("Size :"+topichash.size());
 System.out.println ("Size :"+Queuehash.size());
 System.out.println ("Size :"+Conhash.size());

 int topicmax=topichash.size()/8;
 int queuemax=Queuehash.size()/3;
 int Conmax=Conhash.size()/11;

 writer.write("<table class=\"myTable\"><thead><tr><th>User</th><th>Host</th><th>State</th><th>ConnectionType</th><th>AcceptorURL</th><th>RemoteSocket</th><th>FaultTolerant</th><th>SessionCount</th><th>ConsumerCount</th><th>Version</th><th>ConnectedTime</th></tr></thead>");
 for(int k=0;k<Conmax;k++)
 {
     writer.write("<tr>");
     writer.write("<td>"+Conhash.get("User"+k)+"</td>");
     writer.write("<td>"+Conhash.get("Host"+k)+"</td>");
     writer.write("<td>"+Conhash.get("State"+k)+"</td>");
     writer.write("<td>"+Conhash.get("ConnectionType"+k)+"</td>");
     writer.write("<td>"+Conhash.get("AcceptorURL"+k)+"</td>");
     writer.write("<td>"+Conhash.get("RemoteSocket"+k)+"</td>");
     writer.write("<td>"+Conhash.get("FaultTolerant"+k)+"</td>");
     writer.write("<td>"+Conhash.get("SessionCount"+k)+"</td>");
     writer.write("<td>"+Conhash.get("ConsumerCount"+k)+"</td>");
     writer.write("<td>"+Conhash.get("Version"+k)+"</td>");
     writer.write("<td>"+Conhash.get("ConnectedTime"+k)+"</td>");
     writer.write("</tr>");
 }
 writer.write("</table><br><br><table class=\"myTable\"><thead><tr><th>UserID</th><th>Topic</th><th>SubscriptionName</th><th>ClientID</th><th>MessageCount</th><th>TotalSubscriptionSize</th><th>LastConnectedAt</th><th>DaysSinceLastConnect</th></th></tr></thead>");
 for(int k=0;k<topicmax;k++)
 {
   writer.write("<tr>");
   writer.write("<td>"+topichash.get("UserID"+k)+"</td>");
   writer.write("<td>"+topichash.get("Topic"+k)+"</td>");
   writer.write("<td>"+topichash.get("SubscriptionName"+k)+"</td>");
   writer.write("<td>"+topichash.get("ClientID"+k)+"</td>");
   writer.write("<td>"+topichash.get("SubscriptionMessageCount"+k)+"</td>");
   writer.write("<td>"+topichash.get("TotalSubscriptionSize"+k)+"</td>");
   writer.write("<td>"+topichash.get("LastConnectedAt"+k)+"</td>");
   writer.write("<td>"+topichash.get("DaysSinceLastConnect"+k)+"</td>");
   writer.write("</tr>");
 }
 writer.write("</table><br><br><table class=\"myTable\"><thead><tr><td>QueueName</td><td>QueueMessageCount</td><td>QueueSize</td></tr></thead>");
 for(int l=0;l<queuemax;l++)
 {
    writer.write("<tr>");
    writer.write("<td>"+Queuehash.get("QueueName"+l)+"</td>");
    writer.write("<td>"+Queuehash.get("QueueMessageCount"+l)+"</td>");
    writer.write("<td>"+Queuehash.get("QueueSize"+l)+"</td>");
    writer.write("</tr>");
 }
 writer.write("</table>");
 writer.close();

} catch (Throwable var23) {
 logger.fatal("FATAL ERROR DURING EXECUTION - STACK TRACE FOLLOWS - EXITING!");
 logger.fatal("Stack Trace:", var23);
} finally {
 if(bcd.getJcc() != null) {
    bcd.getJcc().disconnect();
 }

 SimpleDateFormat dfEnd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
 logger.info("CollectBrokerStats Utility Ending at " + dfEnd.format(new Date()));
}

}
}
