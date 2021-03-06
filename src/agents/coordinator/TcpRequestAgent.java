package agents.coordinator;

import agents.utils.Helpers;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class TcpRequestAgent extends Agent {
    Socket server = null;
    private String targetHost = "Agent-classic-balancer-public-890787002.eu-west-3.elb.amazonaws.com";
    private BufferedReader in;
    private PrintWriter out;
    private int port = 1234;
    private int tickerPeriod = 1000; //1sec
    private int fbNr = 10;
    private Logger logger;

    protected void setup() {
        //Take host and port from argument
        Object[] args = getArguments();
        if (args != null && args.length >= 4) {
            targetHost = (String) args[0];
            port = Integer.parseInt((String) args[1]);
            tickerPeriod = Integer.parseInt((String) args[2]);
            fbNr = Integer.parseInt((String) args[3]);
        }

//        logger = Helpers.getLogger("TcpRequestAgent", "logs/tcpagent.log");
//        logger.info("Params: " + tickerPeriod + " - " + fbNr + "\n");

        //Define agent
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Smith");
        sd.setName(getName());
        sd.addOntologies("SmithAgent");
        dfd.setName(getAID());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            System.err.println(getLocalName()
                    + " registration with DF did not succeed. Reason: "
                    + e.getMessage());
            doDelete();
        }

        addBehaviour(new SendRequest(this, tickerPeriod));
    }

    public class SendRequest extends TickerBehaviour {

        public SendRequest(Agent a, int period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            try {
                //Create new socket
                Socket server = new Socket(targetHost, port);
                //write on the output stream (count of element in fibonacci series)
                out = new PrintWriter(server.getOutputStream(), true);
                out.println(fbNr);
                out.flush();
                
                //Close socket
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
