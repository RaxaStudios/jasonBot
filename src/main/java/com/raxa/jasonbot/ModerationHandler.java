/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.raxa.jasonbot;

import java.io.PrintStream;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author RaxaStudios
 */
public class ModerationHandler {

    private final PrintStream outstream;
    private static final Logger LOGGER = Logger.getLogger(ModerationHandler.class.getSimpleName());
    private final ConfigParser.Elements elements;
    private Pattern pattern;
    private Matcher matcher;
    private static final String BANNED_USERNAME =  "(\\d{7}([A-z]{1})\\d{7}|\\d{14})";
   

    public ModerationHandler(final ConfigParser.Elements elements,
            final PrintStream stream) {
        this.elements = elements;
        this.outstream = stream;
    }

    public Boolean filterCheck(String msg) {
        for (int i = 0; i < elements.filterNodes.getLength(); i++) {
            Element ca = (Element) elements.filterNodes.item(i);
            if (!Boolean.parseBoolean(ca.getAttribute("disabled"))) {
                String filter = ca.getAttribute("name");
                if (msg.contains(filter)) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public void handleTool(String username, String msg) {
        for (;;) {
            try {
                if (filterCheck(msg)) {
                    sendMessage(".timeout " + username + " 600 Caught by filter");
                    return;
                }
                else if(userCheck(username)){
                    sendMessage(".timeout " + username + " 600 Username caught by filter");
                    return;
                }
                return;

            } catch (Exception e) {
                LOGGER.severe(e.toString());
            }
            return;
        }

    }

    private boolean userCheck(String username){
        pattern = Pattern.compile(BANNED_USERNAME);
        matcher = pattern.matcher(username);
        System.out.println(matcher.matches());
        System.out.println(username);
        return matcher.matches();
    }

    private void sendMessage(final String msg) {
        final String message = msg;
        this.outstream.println("PRIVMSG #"
                + this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent()
                + " "
                + ":"
                + message);

    }
}
