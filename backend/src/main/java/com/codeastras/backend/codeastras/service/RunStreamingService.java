//package com.codeastras.backend.codeastras.service;
//
//import com.codeastras.backend.codeastras.dto.RunStreamMessage;
//import com.codeastras.backend.codeastras.store.SessionRegistry;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.Executor;
//
//public class RunStreamingService {
//    private final Logger log  = LoggerFactory.getLogger(RunStreamingService.class);
//
//    private final SessionRegistry sessionRegistry;
//    private final SimpMessagingTemplate messagingTemplate;
//    private final Executor runExecutor;
//
//    @Value("${code.runner.max-output-bytes:131072}") // safety cap per run
//    private int maxOutputBytes;
//
//    @Value("${code.runner.line-chunk-bytes:2048}") // chunk size for each message if needed
//    private int lineChunkBytes;
//
//    public RunStreamingService(SessionRegistry sessionRegistry,
//                               SimpMessagingTemplate messagingTemplate,
//                               Executor runExecutor) {
//        this.sessionRegistry = sessionRegistry;
//        this.messagingTemplate = messagingTemplate;
//        this.runExecutor = runExecutor;
//    }
//
//    /**
//     * Stream the execution of `python <filename>` inside the container bound to sessionId.
//     * Sends incremental RunStreamMessage to topic: /topic/room/{roomId}/run-output-stream
//     *
//     * This method returns immediately (schedules the streaming task on runExecutor).
//     */
//     public void streamPythonInSession(String sessionId, String filename, int timeoutSeconds,
//                                       String roomId, String triggeredBy) {
//         runExecutor.execute(() -> {
//             var sessionInfo = sessionRegistry.get(sessionId);
//             if (sessionInfo == null) {
//                 messagingTemplate.convertAndSend(
//                         "/topic/room/" + roomId + "/run-output-stream",
//                         new RunStreamMessage("No active session", true, -1, triggeredBy)
//                 );
//                 return;
//             }
//
//             String containerName = sessionInfo.containerName;
//             List<String> cmd = new ArrayList<>();
//             cmd.add("docker");
//             cmd.add("exec");
//             cmd.add(containerName);
//             cmd.add("python");
//             cmd.add(filename == null || filename.isBlank() ? "main.py" : filename);
//
//             Process process = null;
//             StringBuilder cumulative = new StringBuilder();
//
//             try {
//                 ProcessBuilder pb = new ProcessBuilder(cmd);
//                 pb.redirectErrorStream(true);
//                 process = pb.start();
//
//                 try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//
//                     long startTs = System.currentTimeMillis();
//                     boolean finished = false;
//
//                     // read lines while process running — but we also respect timeout
//                     while (true) {
//                         // check if timed out
//                         long elapsedSec = (System.currentTimeMillis() - startTs) / 1000;
//                         if (elapsedSec > timeoutSeconds) {
//                             // kill and break
//                             process.destroyForcibly();
//                             appendAndSend(cumulative, "[Process killed after timeout " + timeoutSeconds + "s]\n", roomId, triggeredBy);
//                             break;
//                         }
//
//                         // if there's a line available, read and send
//                         if (reader.ready()) {
//                             String line = reader.readLine();
//                             if (line == null) break; // stream closed
//                             appendAndSend(cumulative, line + "\n", roomId, triggeredBy);
//
//                             // enforce cumulative cap
//                             if (cumulative.length() > maxOutputBytes) {
//                                 appendAndSend(cumulative, "[Output truncated]\n", roomId, triggeredBy);
//                                 // attempt to kill process to be safe
//                                 process.destroyForcibly();
//                                 break;
//                             }
//
//                             continue; // read next
//                         }
//
//                         // if process terminated and no more data
//                         try {
//                             finished = !process.isAlive();
//                             if (finished) {
//                                 // read remaining available lines
//                                 while (reader.ready()) {
//                                     String line2 = reader.readLine();
//                                     if (line2 == null) break;
//                                     appendAndSend(cumulative, line2 + "\n", roomId, triggeredBy);
//                                 }
//                                 break;
//                             }
//                         } catch (IllegalThreadStateException ignored) {}
//
//                         // small sleep to avoid busy spin
//                         Thread.sleep(20);
//                     } // end read loop
//
//                     int exitCode = process.waitFor();
//                     // final message with exit code
//                     messagingTemplate.convertAndSend(
//                             "/topic/room/" + roomId + "/run-output-stream",
//                             new RunStreamMessage("", true, exitCode, triggeredBy)
//                     );
//
//                 } // end try reader
//
//             } catch (Exception e) {
//                 log.error("Error streaming run in container {}", sessionId, e);
//                 messagingTemplate.convertAndSend(
//                         "/topic/room/" + roomId + "/run-output-stream",
//                         new RunStreamMessage("Error executing code: " + e.getMessage() + "\n", true, -1, triggeredBy)
//                 );
//                 if (process != null) {
//                     process.destroyForcibly();
//                 }
//             }
//         });
//     }
//
//    // helper: append to cumulative and publish chunk (break large lines into chunks)
//    private void appendAndSend(StringBuilder cumulative, String text, String roomId, String triggeredBy) {
//        cumulative.append(text);
//
//        // if chunk is larger than lineChunkBytes, split it into multiple messages
//        int start = 0;
//        String full = text;
//        while (start < full.length()) {
//            int end = Math.min(full.length(), start + lineChunkBytes);
//            String piece = full.substring(start, end);
//            messagingTemplate.convertAndSend(
//                    "/topic/room/" + roomId + "/run-output-stream",
//                    new RunStreamMessage(piece, false, null, triggeredBy)
//            );
//            start = end;
//        }
//    }
//}