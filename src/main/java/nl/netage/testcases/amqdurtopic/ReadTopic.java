package nl.netage.testcases.amqdurtopic;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Servlet implementation class ReadQueue
 */
@WebServlet("/ReadTopic")
public class ReadTopic extends HttpServlet {
	@Resource(name = "RI_JMS_Durrable_Connection_Factory")
	private TopicConnectionFactory connectionFactory;

	private static final long serialVersionUID = 1L;

	private final static Logger LOGGER = LogManager.getLogger(ReadTopic.class.getName());
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Headers", "*");
		String topicName = request.getParameter("id");
		
		PrintWriter out = response.getWriter();
		
	    String subId = request.getParameter("subid");
	    
	    if (connectionFactory == null) {
			out.println("ERROR");
			LOGGER.log(Level.INFO, "no connection factory");
			return;
		}

		TopicConnection connection = null;
		Session session = null;
		try {

			if (isTopicActive(topicName, subId)) {
				// Return error, the client needs to wait
				out.print("CONFLICT");
				response.setStatus(HttpServletResponse.SC_CONFLICT);
				LOGGER.log(Level.INFO, "Topic: "+topicName+ " is active!");
				return;
			}
			connection = connectionFactory.createTopicConnection();
			connection.setClientID(subId);

			
			session = connection.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Topic topic = session.createTopic(topicName);
			connection.start();
			MessageConsumer consumer = session.createDurableSubscriber(topic,
					subId);

			

			Message m = consumer.receive(30000);
			if (m instanceof TextMessage) {
				TextMessage message = (TextMessage) m;
				out.print(message.getText());
			} else
				out.print("OK");
			consumer.close();
			
		} catch (JMSException e) {
			out.print("ERROR");
			LOGGER.log(Level.INFO, "Issue with: "+topicName + " - "+subId);
			LOGGER.log(Level.INFO, e.getMessage(),e);
		} finally {
			if (connection != null) {
				try {
					connection.stop();
					session.close();
					connection.close();
				} catch (Exception e1) {
					System.out.println("Exception in finally: "+ e1.getMessage());
				}
			}
		}
	}

	private boolean isTopicActive(String topicName, String clientID) {
		MBeanServer thisServer = ManagementFactory.getPlatformMBeanServer();

		try {
			// we construct the query with topic and clientid
			ObjectName query = new ObjectName(
					"org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Topic,destinationName="
							+ topicName
							+ ",endpoint=Consumer,clientId="
							+ clientID
							+ ",consumerId=Durable("
							+ clientID
							+ "_" + clientID + ")");
			for (ObjectName name : thisServer.queryNames(query, null)) {
				// get the first "Active" attribute and return it.
				boolean isActive = (boolean) thisServer.getAttribute(name,
						"Active");
				return isActive;
			}
		} catch (MalformedObjectNameException e) {
			return false;
		} catch (InstanceNotFoundException e) {
			return false;
		} catch (ReflectionException e) {
			return false;
		} catch (AttributeNotFoundException e) {
			return false;
		} catch (MBeanException e) {
			return false;
		}
		// in case of an error return false
		return false;
	}
	//for Preflight
	  @Override
	  protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
	          throws ServletException, IOException {
	      setAccessControlHeaders(resp);
	      resp.setStatus(HttpServletResponse.SC_OK);
	  }

	  private void setAccessControlHeaders(HttpServletResponse resp) {
	      resp.setHeader("Access-Control-Allow-Origin", "*");
	      resp.setHeader("Access-Control-Allow-Headers", "*");
	      
	      resp.setHeader("Access-Control-Allow-Methods", "GET");
	  }
}
