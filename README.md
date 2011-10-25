# websocket-tap

This tool allows you to tap into a WebSocket stream to view (and soon modify) the messages as they happen.  This handy for debugging, as current browsers don't provide a way to see the messages exchanged.

## Requirements

[Maven](http://maven.apache.org/download.html) is needed to build and run the tool.

## Quick Start

To use the tool, just clone the project and provide a server port (unused port that the tap will expose which your existing client should connect to) and a client port (where a WebSocket server is already running).  Currently, it is assumed that all connections go to localhost.

    git clone git://github.com/jryans/websocket-tap.git
    cd websocket-tap
    ./websocket-tap SERVER_PORT CLIENT_PORT
