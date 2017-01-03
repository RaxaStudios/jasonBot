/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.raxa.jasonbot;

import com.raxa.jasonbot.ConfigParser.Configuration;
import com.raxa.jasonbot.ConfigParser.Elements;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author RaxaStudios
 */
public class JasonBot {

    private static final class jBot {

        private static Logger LOGGER = Logger.getLogger(JasonBot.class.getSimpleName());
        private PrintStream out;
        private BufferedReader in;
        private String dataIn;

        private final String botVersion = "v1.00";
        private final ConfigParser configParser = new ConfigParser();

        public Socket establishConnection(final String host, final int port) throws IOException {
            final Socket socket = new Socket(host, port);
            return socket;
        }

        public void beginReadingMessages(final ConfigParser.Elements elements) {

            final CommandParser parser = new CommandParser(elements, out);

            try {
                for (;;) {
                    dataIn = in.readLine();
                    parser.parse(dataIn);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "An error occurred with I/O, perhaps with the Twitch API: {0}", e.toString());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "A general error occurred parsing the message: {0}", e.toString());
            }
        }

        public void start() {
            try {
                LOGGER.info("JYVBot for Twitch " + botVersion + " by Raxa");

                LOGGER.info("Reading configuration XML file");
                final ConfigParser.Elements elements
                        = configParser.parseConfiguration("./jbot.xml");

                final ConfigParser.Configuration config
                        = configParser.getConfiguration(elements.configNode);

                LOGGER.info("Attempt to connect to Twitch servers.");
                final Socket socket = new Socket(config.host, config.port);

                out = new PrintStream(socket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Twitch uses IRC protocol to connect, this is how to connect
                // to the Twitch API
                out.println("PASS " + config.password);
                out.println("NICK " + config.account);
                out.println("JOIN #" + config.joinedChannel);
                out.println("CAP REQ :twitch.tv/tags");
                out.println("CAP REQ :twitch.tv/commands");

                final String ReadyMessage = "/me > " + botVersion + " has joined the channel.";
                out.println("PRIVMSG #"
                        + elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent()
                        + " :"
                        + ReadyMessage);

                LOGGER.info("Bot is now ready for service.");

                // start all periodic timers for broadcasting events
                // startTimers(elements);
                // start doing a blocking read on the socket
                beginReadingMessages(elements);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                LOGGER.log(Level.SEVERE, "Error caught at start up: {0}", e.toString());
            }
        }
    }

    public static void main(String[] args) {
        jBot app = new jBot();
        app.start();
    }
}
