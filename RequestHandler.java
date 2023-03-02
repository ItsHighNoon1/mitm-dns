import java.io.IOException;
import java.net.DatagramPacket;
import java.sql.*;

public class RequestHandler implements Runnable {
    private void recordRequest(DatagramPacket requestPacket) {
        byte[] bytes = requestPacket.getData();

        // Bytes 0 - 3 are the ID and flags which don't contain user data
        // Bytes 4 and 5 are the number of questions which will help us later
        int nQuestions = (int)bytes[4] << 8 | (int)bytes[5];
        // Bytes 6 - 11 are counts for other resources I don't care about

        // Build the string based on the DNS question
        int byteIndex = 12;
        for (int q = 0; q < nQuestions && byteIndex < requestPacket.getLength(); q++) {
            StringBuilder nameBuilder = new StringBuilder();
            while (true) {
                int labelLength = bytes[byteIndex++];
                if (labelLength == 0) {
                    break;
                }

                // Add the label to the string builder
                for (int l = 0; l < labelLength; l++) {
                    nameBuilder.append((char)bytes[byteIndex++]);
                }
                nameBuilder.append('.');
            }
            System.out.println(requestPacket.getAddress() + "\t" + nameBuilder.substring(0, nameBuilder.length() - 1));


            // path to database
            // FILL THIS IN
            String url = "path/database.db";
            
            // SQL statement
            String sql = "INSERT INTO mitm(ip, query) VALUES(" + requestPacket.getAddress() + "," + nameBuilder.substring(0, nameBuilder.length() - 1) + ")";
            
            try (Connection conn = DriverManager.getConnection(url);

                PreparedStatement statement = conn.prepareStatement(sql)) {
            
                statement.executeUpdate();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        // Respond to user packets
        while(!Server.userSocket.isClosed()) {
            // Get the user request
            byte[] inBuffer = new byte[Server.MAX_PACKET_SIZE];
            DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);
            try {
                Server.userSocket.receive(inPacket);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            // Save user info
            recordRequest(inPacket);

            // Record the request ID and user address
            short dnsId = (short)(inBuffer[0] << 8 | inBuffer[1]);
            Server.activeRequests.put(dnsId, inPacket.getSocketAddress());

            // Send the request to the DNS
            inPacket.setSocketAddress(Server.dnsAddr);
            try {
                Server.dnsSocket.send(inPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
