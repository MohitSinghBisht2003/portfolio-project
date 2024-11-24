import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        System.out.println("Server is running on http://localhost:8000");

        while (true) {
            try (Socket socket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // Read HTTP request headers
                String line;
                StringBuilder requestBody = new StringBuilder();
                boolean isPost = false;
                int contentLength = 0;

                // Parse HTTP headers
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    if (line.startsWith("POST")) isPost = true;
                    if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.split(":")[1].trim());
                    }
                }

                // Read and process POST data
                if (isPost && contentLength > 0) {
                    char[] buffer = new char[contentLength];
                    int charsRead = in.read(buffer, 0, contentLength);
                    if (charsRead > 0) {
                        requestBody.append(buffer, 0, charsRead);

                        // Decode and parse the URL-encoded data
                        String decodedData = URLDecoder.decode(requestBody.toString(), StandardCharsets.UTF_8.name());
                        Map<String, String> formData = parseFormData(decodedData);

                        // Prepare data in a simplified format
                        String formattedData = String.format(
                                "Name: %s\nEmail: %s\nMessage: %s\n\n",
                                formData.getOrDefault("name", "N/A"),
                                formData.getOrDefault("email", "N/A"),
                                formData.getOrDefault("message", "N/A")
                        );

                        // Write simplified data to the file
                        synchronized (SimpleHttpServer.class) {
                            try (FileWriter writer = new FileWriter("submitted_data.txt", true)) {
                                writer.write(formattedData);
                            }
                        }

                        System.out.println("Saved Data:\n" + formattedData); // Debugging output
                    }
                }

                // Respond to client
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("Data received and saved successfully.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Helper function to parse URL-encoded form data
    private static Map<String, String> parseFormData(String data) {
        Map<String, String> formData = new HashMap<>();
        String[] pairs = data.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                formData.put(keyValue[0], keyValue[1]);
            }
        }
        return formData;
    }
}
