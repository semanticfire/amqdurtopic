package nl.netage.testcases.amqdurtopic;

import javax.annotation.Resource;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("/fw")

public class MessageForwarder {
	@Resource(name = "RI_JMS_Durrable_Connection_Factory")
	private TopicConnectionFactory connectionFactory;

	@Path("/topic/{topic}")
	@Produces("text/html")
	@GET
	public Response postTopic(@PathParam("topic") String topicName, @QueryParam("msg") String msg) {
		TopicConnection connection = null;
		Session session = null;
		try {
			try {
				connection = connectionFactory.createTopicConnection();
				
				session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
				TextMessage incidentURI = session.createTextMessage();
				Topic topic = session.createTopic(topicName);
				connection.start();
				MessageProducer producer = session.createProducer(topic);
				producer.setDeliveryMode(DeliveryMode.PERSISTENT);
				incidentURI.setText(msg);
				producer.send(incidentURI);

				producer.close();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				return Response.ok(e.getLocalizedMessage()).build();
			} finally {
				if (session != null)
					session.close();
				if (connection != null)
					connection.close();
			}
		} catch (Exception e) {
			return Response.ok(e.getLocalizedMessage()).build();
		}

		return Response.ok("OK\n").build();
	}

}
