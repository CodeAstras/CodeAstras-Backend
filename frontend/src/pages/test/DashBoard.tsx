import React from "react";
import { useNavigate } from "react-router-dom";

const Dashboard: React.FC = () => {
    const navigate = useNavigate();

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-transparent px-4">
            <h1 className="text-4xl font-bold mb-2 text-white">CodeAstra Dashboard</h1>
            <p className="mb-5 text-gray-300">Your collaborative coding space</p>
            <button
                onClick={() => navigate("/editor")}
                className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition duration-200"
            >
                Open Code Editor
            </button>
        </div>
    );
};

export default Dashboard;
