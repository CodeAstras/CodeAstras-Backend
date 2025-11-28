import { useEffect, useState } from "react";
import Editor from "@monaco-editor/react";
import { useParams } from "react-router-dom";
import { connectWebSocket, sendMessage } from "../services/websocket";

export default function CodeEditor() {
    const { projectId } = useParams();
    const [code, setCode] = useState("# Start coding in CodeAstras 🚀\n");

    useEffect(() => {
        if (!projectId) return;

        // Connect WebSocket room for this project
        connectWebSocket(projectId, (msg) => {
            console.log("Incoming code:", msg);
            setCode(msg);
        });
    }, [projectId]);

    const handleChange = (value: string | undefined) => {
        const updatedCode = value || "";
        setCode(updatedCode);
        sendMessage(projectId!, updatedCode);
    };

    return (
        <div className="w-full h-screen bg-[#0a0a0f] text-white overflow-hidden">
            <Editor
                height="100vh"
                width="100%"
                defaultLanguage="python"
                value={code}
                theme="vs-dark"
                onChange={handleChange}
                options={{
                    automaticLayout: true,
                    fontSize: 15,
                    minimap: { enabled: false },
                    scrollBeyondLastLine: false,
                }}
            />
        </div>
    );
}
