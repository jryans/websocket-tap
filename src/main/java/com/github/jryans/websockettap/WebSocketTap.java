/*
 * Copyright 2011 J. Ryan Stinnett
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.jryans.websockettap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.eclipse.jetty.websocket.WebSocketHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class WebSocketTap {

    private static final int MAX_MESSAGE_SIZE = 1024 * 1024; // 2^20 chars
    private static final int MAX_IDLE_TIME = 0; // remain open infinitely

    private int _serverPort;
    private int _clientPort;

    private WebSocketClientFactory _webSocketClientFactory;

    public WebSocketTap(int serverPort, int clientPort) {
        _serverPort = serverPort;
        _clientPort = clientPort;
        _webSocketClientFactory = new WebSocketClientFactory();
    }

    public void startServer() throws Exception {
        Server server = new Server(_serverPort);

        server.setHandler(new WebSocketHandler() {
            @Override
            public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
                return new LoggingWebSocket(request.getPathInfo());
            }
        });

        server.start();
        _webSocketClientFactory.start();

        System.out.println("Ready for connections on port " + _serverPort);

        server.join();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: WebSocketTap serverPort clientPort");
            System.exit(1);
        }

        new WebSocketTap(Integer.parseInt(args[0]), Integer.parseInt(args[1])).startServer();
    }

    public class LoggingWebSocket implements OnTextMessage {

        private String _path;

        private Connection _serverConnection;
        private Connection _clientConnection;

        public LoggingWebSocket(String path) {
            _path = path;
        }

        @Override
        public void onOpen(Connection connection) {
            _serverConnection = connection;
            _serverConnection.setMaxTextMessageSize(MAX_MESSAGE_SIZE);
            _serverConnection.setMaxIdleTime(MAX_IDLE_TIME);
            System.out.println("Server connection opened on port " + _serverPort);

            System.out.println("Attempting client connection to port " + _clientPort + "...");

            WebSocketClient client = _webSocketClientFactory.newWebSocketClient();

            try {
                client.open(new URI("ws://localhost:" + _clientPort + _path), new OnTextMessage() {
                    @Override
                    public void onOpen(Connection connection) {
                        _clientConnection = connection;
                        _clientConnection.setMaxTextMessageSize(MAX_MESSAGE_SIZE);
                        _clientConnection.setMaxIdleTime(MAX_IDLE_TIME);
                        System.out.println("Client connection opened on port " + _clientPort);
                    }

                    @Override
                    public void onClose(int closeCode, String message) {
                        System.out.println("Client connection closed on port " + _clientPort + ": " + closeCode + ", " + message);

                        _serverConnection.disconnect();
                    }

                    @Override
                    public void onMessage(String data) {
                        // Received message as client
                        for (String line : StringUtils.split(data, '\n')) {
                            System.out.println(">>> " + line);
                        }

                        // Send through to server
                        try {
                            _serverConnection.sendMessage(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClose(int closeCode, String message) {
            System.out.println("Server connection closed on port " + _serverPort + ": " + closeCode + ", " + message);

            _clientConnection.disconnect();
        }

        @Override
        public void onMessage(String data) {
            // Received message as server
            for (String line : StringUtils.split(data, '\n')) {
                System.out.println("<<< " + line);
            }

            // Send through to client
            try {
                _clientConnection.sendMessage(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
