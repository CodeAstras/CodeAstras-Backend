import React, { useEffect, useState } from "react";
import Editor from "@monaco-editor/react";
import { connectWebSocket, sendMessage } from "../../services/websocket";

const CodeEditor: React.FC = () => {
  const [code, setCode] = useState("// Start coding in CodeAstra 🚀");

  useEffect(() => {
    // Connect to WebSocket when the component mounts
    connectWebSocket((msg: string) => {
      console.log("Received message:", msg);
    });
  }, []);

  const handleEditorChange = (value: string | undefined) => {
    const updatedCode = value || ""; // fallback if value is undefined
    setCode(updatedCode);
    sendMessage(updatedCode);
  };

  return (
    <div className="min-h-screen bg-black text-white h-[100vh] w-full">
      <Editor
        height="100vh"
        defaultLanguage="javascript"
        value={code}
        theme="vs-dark"
        onChange={handleEditorChange}
        options={{
          fontSize: 14,
          minimap: { enabled: true },
          automaticLayout: true,
        }}
      />
    </div>
  );
};

export default CodeEditor;
