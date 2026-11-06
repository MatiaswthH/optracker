;(window as any).global = window;
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { API_ORIGIN } from "./config";

// stomp-client.ts
const stompClient = new Client({
    webSocketFactory: () => new SockJS(`${API_ORIGIN}/simulation/start`),
    debug: (msg) => console.log("[STOMP]", msg),
    reconnectDelay: 5000,
});
export default stompClient;
