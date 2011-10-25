# websocket-tap

This tool allows you to tap into a WebSocket stream to view (and soon modify) the messages as they happen.
This is handy for debugging, as current browsers don't provide a way to see the messages exchanged.

## Compatibility

I have only tested this so far with a server and client both using hybi-17 (Chrome 17.0.918.0).
I would expect things should work with implementations of any hybi draft that uses Sec-WebSocket-Version
values of 0, 6, 8, or 13, but no testing has been done.

## Requirements

[Maven](http://maven.apache.org/download.html) is needed to build and run the tool.

## Quick Start

To use the tool, just clone the project and provide a server port (unused port that the tap will expose which your existing client should connect to) and a client port (where a WebSocket server is already running).  Currently, it is assumed that all connections go to localhost.

    git clone git://github.com/jryans/websocket-tap.git
    cd websocket-tap
    ./websocket-tap SERVER_PORT CLIENT_PORT
