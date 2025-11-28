import { Client, Message } from "@stomp/stompjs";
import SockJS from "sockjs-client";

let stompClient: Client | null = null;

export const connectWebSocket = (
    projectId: string,
    onMessage: (msg: string) => void
) => {
    const socket = new SockJS("http://localhost:8080/ws");

    stompClient = new Client({
        webSocketFactory: () => socket as any,
        reconnectDelay: 3000,
    });

    stompClient.onConnect = () => {
        console.log("🟢 Connected to WebSocket:", projectId);

        stompClient?.subscribe(`/topic/code/${projectId}`, (message: Message) => {
            onMessage(message.body);
        });
    };

    stompClient.activate();
};

export const sendMessage = (projectId: string, code: string) => {
    if (stompClient && stompClient.connected) {
        stompClient.publish({
            destination: `/app/code/${projectId}`,
            body: code,
        });
    }
};
