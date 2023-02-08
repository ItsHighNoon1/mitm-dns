import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;

public class ResponseHandler implements Runnable {
    @Override
    public void run() {
        // Tunnel the response packets
        while (!Server.dnsSocket.isClosed()) {
            // Get the DNS response
            byte[] outBuffer = new byte[Server.MAX_PACKET_SIZE];
            DatagramPacket outPacket = new DatagramPacket(outBuffer, outBuffer.length);
            try {
                Server.dnsSocket.receive(outPacket);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            // Get the correct address for the response ID
            short dnsId = (short)(outBuffer[0] << 8 | outBuffer[1]);
            SocketAddress responseAddr = Server.activeRequests.get(dnsId);

            // Send the response to the user
            outPacket.setSocketAddress(responseAddr);
            try {
                Server.userSocket.send(outPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
