package io.grpc.examples.helloworld;

/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class HelloWorldClient {
    private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /** Construct client for accessing HelloWorld server using the existing channel. */
    public HelloWorldClient(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    /** Say hello to server. */
    public void greet(String firstName, String lastName, String cin) {
        logger.info("Will try to greet " + firstName + " " + lastName + " (CIN: " + cin + ") ...");
        HelloRequest request = HelloRequest.newBuilder()
                                            .setFirstName(firstName)
                                            .setLastName(lastName)
                                            .setCin(cin)
                                            .build();
        HelloReply response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Greeting: " + response.getMessage());

        try {
            response = blockingStub.sayHelloAgain(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Greeting: " + response.getMessage());
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the first name, the second element is the last name, and the third element is the CIN (Client Identification Number).
     */
    public static void main(String[] args) throws Exception {
        String firstName = "Sabrine";
        String lastName = "Kammoun";
        String cin = "1234567"; // Example CIN
        // Access a service running on the local machine on port 50051
        String target = "localhost:50051";
        // Allow passing in the first name, last name, and CIN as command line arguments
        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                System.err.println("Usage: [firstName lastName cin [target]]");
                System.err.println("");
                System.err.println("  firstName   The first name of the person you wish to be greeted by. Defaults to " + firstName);
                System.err.println("  lastName    The last name of the person you wish to be greeted by. Defaults to " + lastName);
                System.err.println("  cin         The  Identification Number (CIN) of the person. Defaults to " + cin);
                System.err.println("  target      The server to connect to. Defaults to " + target);
                System.exit(1);
            }
            firstName = args[0];
        }
        if (args.length > 1) {
            lastName = args[1];
        }
        if (args.length > 2) {
            cin = args[2];
        }
        if (args.length > 3) {
            target = args[3];
        }

        // Create a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of your application and reuse
        // them until the application shuts down.
        //
        // For the example we use plaintext insecure credentials to avoid needing TLS certificates. To
        // use TLS, use TlsChannelCredentials instead.
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        try {
            HelloWorldClient client = new HelloWorldClient(channel);
            client.greet(firstName, lastName, cin);
        } finally {
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
